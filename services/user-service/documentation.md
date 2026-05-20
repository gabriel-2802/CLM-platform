# User Service — Technical Documentation

**CLM Platform · Contract Lifecycle Management**
**Service:** `user-service` · **Runtime:** Java 21 / Spring Boot 4.0.5 · **Port:** 8083

---

## Table of Contents

1. [Architectural Overview & Design Patterns](#1-architectural-overview--design-patterns)
2. [Deep Dive: Data Parsing & Transformation Pipeline](#2-deep-dive-data-parsing--transformation-pipeline)
3. [Database Schema & Data Strategy](#3-database-schema--data-strategy)
4. [Security, Token Handling & Cross-Service Token Propagation](#4-security-token-handling--cross-service-token-propagation)

---

## 1. Architectural Overview & Design Patterns

### 1.1 Service Responsibilities

The `user-service` is the **sole identity authority** of the CLM platform. It owns the lifecycle of every user account — from registration through credential management — and is the only service that writes to the `users`, `roles`, and `user_roles` tables. Every other downstream service (e.g., `contract-service`) consumes the JWT tokens this service issues but never contacts it at runtime to validate them; token verification is performed locally by sharing the same HMAC-SHA256 secret.

The service is deliberately narrow in scope:

| Responsibility | Owned by user-service | Delegated elsewhere |
|---|---|---|
| User registration & login | ✅ | — |
| BCrypt password hashing | ✅ | — |
| JWT issuance | ✅ | — |
| JWT *validation* in downstream requests | — | Each consuming service (local key copy) |
| Role assignment / revocation | ✅ | — |
| Contract management | — | `contract-service` |
| File storage / download | — | `contract-service` |
| Audit logging | — | `contract-service` |

This boundary means the user-service never becomes a synchronous bottleneck. Downstream services validate tokens offline using the shared secret and only forward the token's `sub` (email) claim into their own security context.

---

### 1.2 Layered Architecture

The service follows a strict **Layered (Onion) Architecture** with four concentric rings:

```
┌─────────────────────────────────────────────────────────────────┐
│  HTTP Layer  (Controllers + DTOs + GlobalExceptionHandler)      │
├─────────────────────────────────────────────────────────────────┤
│  Security Layer  (JwtAuthenticationFilter, SecurityConfig,      │
│                   UserDetailsServiceImpl, EntryPoint)           │
├─────────────────────────────────────────────────────────────────┤
│  Business Layer  (AuthService, UserService)                     │
├─────────────────────────────────────────────────────────────────┤
│  Persistence Layer  (UserRepository, RoleRepository, Flyway)    │
└─────────────────────────────────────────────────────────────────┘
```

No layer skips its immediate neighbour. Controllers never call repositories directly; services never parse HTTP headers. This separation makes each ring independently testable with Mockito mocks.

---

### 1.3 Design Patterns in Use

#### Stateless Authentication (JWT Bearer Token)

HTTP sessions are entirely absent. Every request that requires an authenticated identity must carry a signed JWT in the `Authorization: Bearer <token>` header. Spring Security is configured with `SessionCreationPolicy.STATELESS`, which prevents any `HttpSession` from being created or consulted.

**Consequence:** Horizontal scaling requires zero session-affinity configuration. Any replica can serve any request.

#### Role-Based Access Control (RBAC) — RATB-Compliant Model

The role model was designed to satisfy a **three-tier RBAC hierarchy** common in public-sector procurement platforms such as RATB (Regia Autonomă de Transport București):

| Role | Scope | Granted by |
|---|---|---|
| `ROLE_USER` | Read own profile | Automatic on registration |
| `ROLE_MANAGER` | Manages contract workflows | Admin grants via PUT /users/{id} |
| `ROLE_ADMIN` | Full user management | Admin register-code at signup, or via role endpoint |

> **Architectural note:** Every user always holds `ROLE_USER` as a baseline. A manager therefore holds `{ROLE_USER, ROLE_MANAGER}`. An admin holds `{ROLE_USER, ROLE_ADMIN}`. This additive model ensures that stripping an elevated role never accidentally strips basic platform access.

The registration endpoint supports an **out-of-band admin bootstrapping code** (`app.admin.register-code`). A RATB procurement officer can self-register as an admin during platform onboarding by providing this code — which is distributed through a secure off-channel — without requiring an existing admin to perform the grant. This code is compared using `MessageDigest.isEqual` (constant-time comparison) to eliminate timing-oracle attacks.

#### Repository Pattern with Entity Graph Pre-fetching

Both `UserRepository` and `RoleRepository` extend `JpaRepository`. The only custom query in `UserRepository` is:

```java
@EntityGraph(attributePaths = {"roles"})
Optional<User> findByEmail(String email);
```

The `@EntityGraph` annotation forces a single `JOIN FETCH` SQL query instead of the default Hibernate lazy-load N+1 pattern. This is critical because `UserDetailsServiceImpl` calls `findByEmail` on every request, and role data is always needed to build the `GrantedAuthority` list. Without the entity graph, every authenticated request would trigger a second query to load roles.

#### DTO Projection

All API responses are mapped to immutable DTO records (`UserResponse`, `AuthResponse`) before serialization. The `User` JPA entity is never exposed directly. This pattern:
- Prevents accidental password hash leakage
- Allows the domain model to evolve without breaking the API contract
- Enables `@JsonProperty(access = WRITE_ONLY)` on the password field without coupling serialization concerns to persistence concerns

---

### 1.5 Interaction Flow: Authentication Sequence

```
Client                user-service                         PostgreSQL
  │                        │                                    │
  │  POST /api/auth/login  │                                    │
  │  {email, password}     │                                    │
  ├───────────────────────>│                                    │
  │                        │  findByEmail(email)                │
  │                        │  @EntityGraph JOIN FETCH roles     │
  │                        ├───────────────────────────────────>│
  │                        │<─── Optional<User> ───────────────-│
  │                        │                                    │
  │                        │  BCrypt.matches(raw, hash)         │
  │                        │  [local CPU — no DB round-trip]    │
  │                        │                                    │
  │                        │  JwtTokenProvider.generateToken()  │
  │                        │  sub=email, roles=[...], exp=+30d  │
  │                        │  sign(HMAC-SHA256, secret)         │
  │                        │                                    │
  │  200 AuthResponse      │                                    │
  │  {token, expiresIn}    │                                    │
  │<───────────────────────│                                    │
  │                        │                                    │
  │  GET /api/users/me     │                                    │
  │  Authorization: Bearer │                                    │
  ├───────────────────────>│                                    │
  │                        │  JwtAuthenticationFilter           │
  │                        │  parse + verify signature          │
  │                        │  SecurityContextHolder.set(auth)   │
  │                        │                                    │
  │                        │  UserController.getMe()            │
  │                        │  extract email from principal      │
  │                        ├───────────────────────────────────>│
  │                        │<─── User entity (with roles) ──────│
  │  200 UserResponse      │                                    │
  │<───────────────────────│                                    │
```

---

## 2. Deep Dive: Data Parsing & Transformation Pipeline

### 2.1 The Parsing Pipeline

The user-service does not parse binary documents or streaming data. Its parsing concerns are three distinct transformations: **inbound request deserialization**, **credential transformation**, and **outbound DTO projection**. Each stage is deterministic and side-effect-free (except the credential hashing stage, which is intentionally one-way).

#### Stage 1 — JSON Deserialization & Bean Validation

Spring's `HttpMessageConverter` chain deserializes the request body from JSON into a request DTO. Immediately after deserialization, JSR-380 Bean Validation runs:

```java
// RegisterRequest.java
public record RegisterRequest(
    @NotBlank @Email
    String email,

    @NotBlank @Size(min = 8, max = 128)
    String password,

    @Size(max = 255)
    String name,

    String adminCode          // optional; no validation annotation
) {}
```

Validation failures throw `MethodArgumentNotValidException`, caught by `GlobalExceptionHandler`, which assembles a structured `ProblemDetail` response listing every violated field:

```json
{
  "type": "https://api.clm-user.demo/errors/validation-failed",
  "status": 400,
  "detail": "Validation failed",
  "errors": [
    { "field": "email",    "message": "must be a well-formed email address" },
    { "field": "password", "message": "size must be between 8 and 128" }
  ]
}
```

This fail-fast strategy ensures no partially-validated object reaches the service layer.

#### Stage 2 — Duplicate Email Check

Before any write, `AuthService.register` queries:

```java
if (userRepository.findByEmail(request.email()).isPresent()) {
    throw new DuplicateEmailException(request.email());
}
```

The `findByEmail` query hits the `idx_users_email` B-tree index (O(log n) lookup). No full-table scan occurs regardless of user count.

#### Stage 3 — Password Hashing

```java
String hashedPassword = passwordEncoder.encode(request.password());
```

`BCryptPasswordEncoder` uses bcrypt with a work factor of 10 (Spring Boot default). The output is a 60-character string in `$2a$10$<salt><hash>` format. The raw password never touches the database or any log.

#### Stage 4 — Admin Code Evaluation

```java
boolean isAdminRequest = adminRegisterCode != null
    && MessageDigest.isEqual(
         adminRegisterCode.getBytes(StandardCharsets.UTF_8),
         request.adminCode().getBytes(StandardCharsets.UTF_8)
       );
```

`MessageDigest.isEqual` runs in constant time proportional to the length of the longer array. This eliminates the short-circuit timing difference that would otherwise allow an attacker to determine correct prefix characters via response-time analysis (timing oracle). The admin code is injected from the environment variable `app.admin.register-code`.

#### Stage 5 — Entity Assembly & Persistence

```java
User user = User.builder()
    .email(request.email())
    .password(hashedPassword)
    .name(request.name())
    .enabled(true)
    .build();

Set<Role> roles = new HashSet<>();
roles.add(roleRepository.findByName(RoleName.ROLE_USER).orElseThrow());
if (isAdminRequest) {
    roles.add(roleRepository.findByName(RoleName.ROLE_ADMIN).orElseThrow());
}
user.setRoles(roles);
userRepository.save(user);
```

Hibernate translates this to:
1. `INSERT INTO users (email, password, name, enabled, created_at, updated_at)`
2. `INSERT INTO user_roles (user_id, role_id)` — one row per assigned role

Both writes happen within a single transaction boundary (propagation `REQUIRED` by default on `@Transactional` service methods).

#### Stage 6 — JWT Generation (Claim Encoding)

```java
String token = jwtTokenProvider.generateToken(userDetails);
```

Internally, `JwtTokenProvider.generateToken` encodes the following claims:

```java
Jwts.builder()
    .subject(userDetails.getUsername())        // email
    .claim("roles", userDetails.getAuthorities()
                               .stream()
                               .map(GrantedAuthority::getAuthority)
                               .toList())      // ["ROLE_USER", "ROLE_ADMIN"]
    .issuedAt(new Date())
    .expiration(new Date(now + jwtExpirationMs))
    .signWith(signingKey)                      // HMAC-SHA256
    .compact();
```

The resulting token is a three-part Base64URL string: `header.payload.signature`.

#### Stage 7 — DTO Projection

```java
public UserResponse toResponse(User user) {
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getName(),
        user.isEnabled(),
        user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toUnmodifiableSet()),
        user.getCreatedAt()
    );
}
```

The password hash is never included. The `roles` set is projected to a `Set<String>` of role names, removing the internal `Role` entity from the public contract.

---

### 2.2 Implementation Difficulties & Solutions

#### Challenge 1 — N+1 Queries on Role Loading

**Problem:** Spring Security calls `UserDetailsServiceImpl.loadUserByUsername` on every authenticated request. The `User` entity has a `@ManyToMany` relationship with `Role`. Hibernate's default fetch type for collections is `LAZY`, meaning each call to `user.getRoles()` would issue a second `SELECT` against `user_roles JOIN roles`.

**Solution:** `@EntityGraph(attributePaths = {"roles"})` on `findByEmail` forces Hibernate to emit a single `JOIN FETCH` query:

```sql
SELECT u.*, r.*
FROM users u
LEFT JOIN user_roles ur ON ur.user_id = u.id
LEFT JOIN roles r       ON r.id = ur.role_id
WHERE u.email = ?
```

This eliminates the N+1 pattern entirely. Role data arrives in the same network round-trip as user data.

#### Challenge 2 — Flyway Schema Isolation Under Docker Compose

**Problem:** Multiple services share the same PostgreSQL instance. Running `flyway migrate` without schema isolation would cause migration version conflicts between services.

**Solution:** Each service connects to a dedicated PostgreSQL schema (e.g., `user_service_schema`). Flyway's `schemas` property is set per service, and the `flyway_schema_history` table is per-schema. This achieves complete migration isolation without requiring separate database instances.

#### Challenge 3 — Trailing Slash Redirect Compatibility

**Problem:** Nginx reverse proxy appended trailing slashes to some paths before forwarding. Spring Boot 3+ deprecated trailing-slash matching by default, causing 404 errors on paths like `/api/auth/login/`.

**Solution:** `TrailingSlashFilter` runs as the first servlet filter in the chain. It intercepts any request whose path ends with `/` (excluding the root path) and issues a `302 Found` redirect to the normalized path while preserving query string parameters:

```java
String redirectPath = StringUtils.hasText(queryString)
    ? stripped + "?" + queryString
    : stripped;
response.sendRedirect(redirectPath);
```

#### Challenge 4 — JWT Secret Minimum Length Enforcement

**Problem:** HMAC-SHA256 requires a key of at least 256 bits (32 bytes). If the application starts with a short secret, `jjwt` throws a `WeakKeyException` at token generation time — a runtime failure rather than a startup failure.

**Solution:** `JwtTokenProvider` validates key length in its `@PostConstruct` initializer:

```java
@PostConstruct
private void init() {
    if (secret.length() < 32) {
        throw new IllegalStateException(
            "JWT secret must be at least 32 characters (256 bits). " +
            "Current length: " + secret.length()
        );
    }
    signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
}
```

This converts a latent runtime bug into an **eager startup crash**, ensuring misconfiguration is caught at deployment time rather than during the first login attempt.

---

### 2.3 Algorithm Correctness: JWT Validation Determinism

The `validateToken` method must be:
1. **Deterministic** — the same valid token always returns `Optional<Claims>` present
2. **Safe** — an expired, tampered, or malformed token always returns `Optional.empty()`
3. **Non-throwing** — all exceptions are caught internally

**Proof sketch:**

Given token `T` and signing key `K`:

- `Jwts.parser().verifyWith(K).build().parseSignedClaims(T)` succeeds **if and only if**:
  - The token is well-formed Base64URL (otherwise `MalformedJwtException`)
  - The signature matches `HMAC-SHA256(header + "." + payload, K)` (otherwise `SignatureException`)
  - `exp` claim ≥ current time (otherwise `ExpiredJwtException`)
  - `T` is not null/empty (otherwise `IllegalArgumentException`)

All four exception types are subclasses of `JwtException` or `IllegalArgumentException`, both of which are caught:

```java
public Optional<Claims> validateToken(String token) {
    try {
        return Optional.of(
            Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
        );
    } catch (JwtException | IllegalArgumentException ex) {
        log.debug("JWT validation failed: {}", ex.getMessage());
        return Optional.empty();
    }
}
```

There is no code path that returns `Optional.of(null)` because `getPayload()` on a successfully parsed `Jws<Claims>` is contractually non-null by the JJWT specification.

**Tamper resistance:** Modifying any byte of the payload changes the HMAC digest. An attacker without `K` cannot produce a valid signature for a modified payload (collision resistance of SHA-256: 2^128 work to find a collision). Therefore a token claiming elevated roles that was not issued by this service will always fail validation.

---

## 3. Database Schema & Data Strategy

### 3.1 Schema Design

The schema is managed exclusively by **Flyway**, ensuring reproducible, ordered migrations across all environments. The migration history is stored in `flyway_schema_history` within the service's dedicated schema.

#### Table: `roles`

```sql
CREATE TABLE roles (
    id   SERIAL      PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);
```

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `SERIAL` (int4) | PK, auto-increment | Small domain; 32-bit is sufficient |
| `name` | `VARCHAR(50)` | NOT NULL, UNIQUE | Role identifier string (e.g., `ROLE_ADMIN`) |

**Index strategy:** The `UNIQUE` constraint on `name` implicitly creates a B-tree index. `RoleRepository.findByName` uses this index.

#### Table: `users`

```sql
CREATE TABLE users (
    id         BIGSERIAL    PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    name       VARCHAR(255),
    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users (email);
```

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `BIGSERIAL` | PK | 64-bit to accommodate large user base |
| `email` | `VARCHAR(255)` | NOT NULL, UNIQUE | RFC 5321 max length; case-sensitive comparison |
| `password` | `VARCHAR(255)` | NOT NULL | Stores BCrypt `$2a$10$...` (always 60 chars) |
| `name` | `VARCHAR(255)` | nullable | Display name; optional |
| `enabled` | `BOOLEAN` | NOT NULL, DEFAULT TRUE | Account lock flag |
| `created_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT NOW() | UTC-aware; set on insert |
| `updated_at` | `TIMESTAMPTZ` | NOT NULL, DEFAULT NOW() | UTC-aware; updated by Hibernate trigger |

**Index strategy:** `idx_users_email` is a redundant explicit B-tree index alongside the implicit `UNIQUE` constraint index. Both resolve to the same physical index in PostgreSQL, but declaring it explicitly makes the intent visible in query plans and allows the DBA to add a `CONCURRENTLY` rebuild without touching the constraint.

**Timezone choice (`TIMESTAMPTZ`):** `TIMESTAMPTZ` stores timestamps as UTC internally and adjusts on read to the client's session timezone. This prevents the class of bugs that arise when comparing `TIMESTAMP WITHOUT TIME ZONE` values across application servers in different time zones.

#### Table: `user_roles` (Junction)

```sql
CREATE TABLE user_roles (
    user_id BIGINT  NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);
```

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `user_id` | `BIGINT` | NOT NULL, FK → users(id) | Cascade delete removes memberships on user deletion |
| `role_id` | `INTEGER` | NOT NULL, FK → roles(id) | Cascade delete removes memberships if role is deleted |
| (user_id, role_id) | — | PRIMARY KEY (composite) | Enforces uniqueness; prevents duplicate role assignments |

**Cascade strategy:** `ON DELETE CASCADE` on both foreign keys means:
- Deleting a user automatically removes all their role memberships. No orphaned `user_roles` rows.
- Deleting a role (operationally rare) automatically removes all user-role memberships for that role.

This is enforced at the database level, not just at the application level, guaranteeing referential integrity even if maintenance scripts bypass the service.

---

### 3.2 Migration Sequence

| Version | File | Purpose |
|---|---|---|
| V1 | `V1__create_roles.sql` | Creates `roles` table |
| V2 | `V2__create_users.sql` | Creates `users` table + email index |
| V3 | `V3__create_user_roles.sql` | Creates junction table with FK constraints |
| V4 | `V4__seed_roles.sql` | Inserts `ROLE_USER`, `ROLE_ADMIN` |
| V5 | `V5__seed_manager_role.sql` | Inserts `ROLE_MANAGER` |

> **Design rationale:** Role seeds are separate migrations (V4, V5) rather than application-startup logic. This makes them idempotent under re-runs (`INSERT ... ON CONFLICT DO NOTHING`) and visible in the migration history. The `AdminSeeder` bean handles the *user* seed at startup because it needs the hashed password, which cannot be pre-computed in a static SQL file.

---

### 3.3 Entity-Relationship Diagram

```
┌──────────────────────────────────┐         ┌──────────────────┐
│              users               │         │      roles       │
├──────────────────────────────────┤         ├──────────────────┤
│ PK  id          BIGSERIAL        │         │ PK  id  SERIAL   │
│     email       VARCHAR(255) UQ  │         │     name VARCHAR │
│     password    VARCHAR(255)     │         │          UQ      │
│     name        VARCHAR(255)     │         └────────┬─────────┘
│     enabled     BOOLEAN          │                  │
│     created_at  TIMESTAMPTZ      │         ┌────────┴──────────────┐
│     updated_at  TIMESTAMPTZ      │         │      user_roles       │
└──────────────┬───────────────────┘         ├───────────────────────┤
               │  1                          │ FK  user_id  BIGINT   │
               └────────────────────── N ────│ FK  role_id  INTEGER  │
                                             │     PK (user_id,      │
                                             │         role_id)      │
                                             └───────────────────────┘
```

---

### 3.4 Query Performance Profile

| Operation | Query Pattern | Index Used | Estimated Cost |
|---|---|---|---|
| Login lookup | `SELECT * FROM users WHERE email = ?` JOIN roles | `idx_users_email` B-tree | O(log n) |
| Admin: list users | `SELECT * FROM users ORDER BY created_at DESC LIMIT 15 OFFSET ?` | None (sequential scan acceptable at typical scale) | O(n/page) |
| Role lookup by name | `SELECT * FROM roles WHERE name = ?` | Implicit unique index | O(log r) where r ≤ 5 |
| User deletion | `DELETE FROM users WHERE id = ?` (cascades to user_roles) | PK index | O(1) + O(k) cascade |

---

## 4. Security, Token Handling & Cross-Service Token Propagation

### 4.1 Security Architecture Overview

The user-service implements a **defence-in-depth** strategy across five layers:

```
Layer 1: TLS (handled by Nginx reverse proxy)
Layer 2: CORS origin validation (SecurityConfig)
Layer 3: JWT signature & expiry verification (JwtAuthenticationFilter)
Layer 4: Role-based endpoint authorization (@PreAuthorize)
Layer 5: Input validation & exception isolation (Bean Validation + GlobalExceptionHandler)
```

No single layer is sufficient alone; each layer assumes the layers above it may have been bypassed.

---

### 4.2 Password Security

**Algorithm:** BCrypt with cost factor 10 (Spring Boot default).

BCrypt deliberately introduces computational work (`2^10 = 1024` iterations of the Blowfish key setup). On a modern server, a single `encode` call takes ~100ms. This rate-limits brute-force attacks: an attacker who obtains the database dump can attempt approximately 10 guesses/second per CPU core, making dictionary attacks on long passwords computationally infeasible.

**Storage:** The 60-character BCrypt string (`$2a$10$<22-char salt><31-char hash>`) stores the algorithm identifier, cost factor, and per-password salt in a single field. No separate salt column is needed.

**Plain-text exposure surface:** The raw password string exists only:
1. In the HTTP request body (TLS-encrypted in transit)
2. Inside the `@Valid RegisterRequest` DTO in the JVM heap during a single request lifecycle
3. As an argument to `passwordEncoder.encode()`

It is never written to a log (Spring's `@Slf4j` fields log DTO types, not field values), never stored to the database, and garbage collected after the request completes.

---

### 4.3 JWT Token Structure & Issuance

#### Header

```json
{ "alg": "HS256", "typ": "JWT" }
```

#### Payload (Claims)

```json
{
  "sub": "user@ratb.ro",
  "roles": ["ROLE_USER", "ROLE_MANAGER"],
  "iat": 1747432800,
  "exp": 1750024800
}
```

| Claim | Source | Purpose |
|---|---|---|
| `sub` | `UserDetails.getUsername()` (email) | Principal identity |
| `roles` | `getAuthorities()` mapped to strings | Offline role enforcement in downstream services |
| `iat` | `System.currentTimeMillis()` | Issued-at timestamp |
| `exp` | `iat + jwt.expiration` | Expiry (default 30 days) |

#### Signature

```
HMAC-SHA256(
  Base64URL(header) + "." + Base64URL(payload),
  SecretKey derived from JWT_SECRET env var
)
```

The signing key is created once at startup in `@PostConstruct`:

```java
signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
```

`Keys.hmacShaKeyFor` validates that the byte array is at least 32 bytes and returns a `javax.crypto.SecretKey` backed by the `HmacSHA256` JCA provider.

---

### 4.4 Token Validation Request Lifecycle

Every protected request passes through `JwtAuthenticationFilter` before it reaches any controller:

```
Request → TrailingSlashFilter → JwtAuthenticationFilter → SecurityConfig rules → Controller

JwtAuthenticationFilter logic:
─────────────────────────────
1. Read "Authorization" header
2. If null or doesn't start with "Bearer " → skip (SecurityConfig
   will reject protected endpoints later)
3. Extract raw token string (after "Bearer ")
4. Call jwtTokenProvider.validateToken(token)
5. On Optional.empty() → skip (no authentication set)
6. On Optional<Claims> present:
   a. claims.getSubject()                 → principal (email string)
   b. claims.get("roles", List.class)     → ["ROLE_USER", "ROLE_ADMIN"]
   c. Build List<GrantedAuthority>
   d. Create UsernamePasswordAuthenticationToken(principal, null, authorities)
   e. Set WebAuthenticationDetails on token
   f. SecurityContextHolder.getContext().setAuthentication(auth)
7. Continue filter chain
```

**Key design decision:** The filter does **not** hit the database. All information needed to establish the security context (email + roles) is embedded in the JWT. This means authentication is O(1) in terms of database queries for every authenticated request. The database is only consulted when the user explicitly calls `/api/users/me` to retrieve full profile data.

---

### 4.5 RATB Admin Bootstrapping — Secure Registration Code

The platform supports a **privileged registration path** for RATB administrators who need to self-onboard during initial deployment or organizational expansion:

```
POST /api/auth/register
{
  "email":     "director@ratb.ro",
  "password":  "SecureP@ss123",
  "name":      "Ion Popescu",
  "adminCode": "<distributed out-of-band>"
}
```

If `adminCode` matches `app.admin.register-code` (env var), the new account is created with both `ROLE_USER` and `ROLE_ADMIN`. The code comparison uses constant-time byte comparison:

```java
MessageDigest.isEqual(
    storedCode.getBytes(StandardCharsets.UTF_8),
    providedCode.getBytes(StandardCharsets.UTF_8)
)
```

> **Security property:** A timing oracle attack against this comparison would require the attacker to make a statistically significant number of requests and observe sub-millisecond response-time differences — infeasible given network jitter and the BCrypt operation that follows within the same code path. The `MessageDigest.isEqual` call is a defence-in-depth measure.

After bootstrap, the admin code should be rotated (change the environment variable and redeploy) so that it cannot be brute-forced in subsequent attacks.

---

### 4.6 Role-Based Endpoint Authorization

Authorization is enforced declaratively via Spring Security's `@PreAuthorize` annotation on controller methods:

```java
@GetMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) { ... }
```

Spring Security evaluates `hasRole('ADMIN')` against the `GrantedAuthority` list in the current `SecurityContextHolder`. These authorities were set by `JwtAuthenticationFilter` from the `roles` claim — meaning **no database query is needed for role checking**.

| Endpoint | Authorization Rule |
|---|---|
| `POST /api/auth/register` | Public |
| `POST /api/auth/login` | Public |
| `GET /api/users/me` | Any authenticated user |
| `GET /api/users/{id}` | `ROLE_ADMIN` |
| `GET /api/users` | `ROLE_ADMIN` |
| `PUT /api/users/{id}` | `ROLE_ADMIN` |
| `PATCH /api/users/{id}/password` | `ROLE_ADMIN` |
| `DELETE /api/users/{id}` | `ROLE_ADMIN` |
| `PUT /api/users/{id}/roles/admin` | `ROLE_ADMIN` |
| `DELETE /api/users/{id}/roles/admin` | `ROLE_ADMIN` |
| `PATCH /api/users/{id}/enabled` | `ROLE_ADMIN` |

`AccessDeniedException` (thrown when an authenticated user lacks the required role) is caught by `GlobalExceptionHandler` and returned as HTTP 403 with a structured ProblemDetail body.

---

### 4.7 Token Propagation to Downstream Services

The `user-service` is the **only token issuer** in the CLM platform. Downstream services (`contract-service`, and any future services) act as **token consumers** — they verify tokens but never issue them.

#### Shared Secret Model

Both services are configured with the same `JWT_SECRET` environment variable. When the `contract-service` receives a request with `Authorization: Bearer <token>`, its own `JwtTokenProvider` (identical implementation) verifies the signature locally:

```
client ──[ Bearer token ]──► contract-service
                                │
                                │  local HMAC-SHA256 verify
                                │  no HTTP call to user-service
                                │
                                ▼
                          SecurityContextHolder
                          principal = email (sub claim)
```

This **offline verification** pattern eliminates a synchronous dependency between services. The `contract-service` does not go down if the `user-service` is temporarily unavailable.

#### What the contract-service extracts from the token

The `contract-service`'s `JwtAuthenticationFilter` sets the JWT `sub` claim (email) as the `Authentication` principal. Controllers receive user identity via Spring Security's `@AuthenticationPrincipal` or by reading `SecurityContextHolder.getContext().getAuthentication().getName()`.

#### Token Lifecycle in a Cross-Service Request

```
1. User logs in via user-service → receives JWT (30d expiry)
2. User calls contract-service with the same JWT
3. contract-service JwtAuthenticationFilter:
   a. Extracts token from Authorization header
   b. Verifies HMAC-SHA256 signature with shared secret
   c. Checks expiry claim
   d. Sets SecurityContext principal = email
4. contract-service business logic executes with user identity
5. No user-service involvement in steps 2–4
```

#### No Refresh Token (Architectural Decision)

The current implementation does not implement refresh tokens. This is an intentional trade-off:

| Consideration | Single Access Token (current) | Access + Refresh Token |
|---|---|---|
| Implementation complexity | Low | High (token store, revocation list) |
| Token revocation | Not possible until expiry | Possible |
| Logout support | Client discards token | Server-side invalidation |
| Suitable for | Admin tooling, server-to-server | User-facing SPAs, mobile clients |

For the CLM platform's current use case (web-based admin panel with long sessions), a 30-day token with no refresh is acceptable. If mobile clients or stricter session control is introduced, a refresh token mechanism with server-side revocation (Redis blacklist) should be added.

---

### 4.8 Account Disabling & Its Effect on Tokens

When an admin calls `PATCH /api/users/{id}/enabled?enabled=false`, the `enabled` column in `users` is set to `false`. However, **existing JWTs for that user remain cryptographically valid** until their `exp` claim expires.

The `enabled` flag is enforced at **login time only** via `UserDetailsServiceImpl`:

```java
return new org.springframework.security.core.userdetails.User(
    user.getEmail(),
    user.getPassword(),
    user.isEnabled(),      // accountNonLocked
    true,                  // accountNonExpired
    true,                  // credentialsNonExpired
    true,                  // enabled
    authorities
);
```

Spring Security's `DaoAuthenticationProvider` checks `accountNonLocked` before issuing a new token. A disabled user cannot log in and obtain a new token, but their existing token (if any) continues to work until expiry.

> **Operational implication for RATB:** If an employee's access must be terminated immediately (e.g., termination for cause), the operator should both disable the account **and** rotate the `JWT_SECRET` environment variable and redeploy all services. Secret rotation invalidates all existing tokens platform-wide. This is the intended emergency revocation mechanism.

---

### 4.9 CORS Configuration

CORS is configured in `SecurityConfig` to allow the Angular/Next.js frontend to communicate with the API:

```java
CorsConfiguration config = new CorsConfiguration();
config.setAllowedOrigins(
    Arrays.stream(allowedOrigins.split(","))
          .map(String::trim)
          .toList()
);
config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
config.setAllowedHeaders(List.of("*"));
config.setAllowCredentials(true);
```

The `app.cors.allowed-origins` property accepts a comma-separated list of origins, enabling multi-environment configuration (e.g., `http://localhost:3000,https://clm.ratb.ro`) without code changes.

`setAllowCredentials(true)` is required for the browser to include the `Authorization` header in cross-origin requests.

---

### 4.10 Security Configuration Summary

```java
http
    .csrf(AbstractHttpConfigurer::disable)          // stateless API — no CSRF token needed
    .cors(cors -> cors.configurationSource(...))    // origin whitelist
    .sessionManagement(sm ->
        sm.sessionCreationPolicy(STATELESS))        // no HttpSession created or used
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/v3/api-docs/**",
                         "/swagger-ui/**",
                         "/actuator/**").permitAll()
        .anyRequest().authenticated()
    )
    .exceptionHandling(ex -> ex
        .authenticationEntryPoint(entryPoint))      // JSON 401 response
    .addFilterBefore(
        jwtFilter,
        UsernamePasswordAuthenticationFilter.class  // JWT filter runs first
    );
```

| Security Property | Value | Rationale |
|---|---|---|
| CSRF | Disabled | Stateless bearer-token API; no cookie-based auth |
| Sessions | `STATELESS` | No `JSESSIONID` cookies; horizontal scaling friendly |
| Password encoder | BCrypt (cost=10) | Industry standard; ~100ms per hash; resists brute force |
| JWT algorithm | HMAC-SHA256 | Symmetric; performant; secret shared only between trusted services |
| Token expiry | 30 days | Long-lived for admin tooling; rotate on compromise |
| Admin code comparison | Constant-time (`MessageDigest.isEqual`) | Eliminates timing oracle |
| Non-root container user | `appuser` (uid 1001) | Limits blast radius of container escape |
| JVM entropy | `/dev/./urandom` | Prevents SecureRandom blocking on key generation |

---

*End of documentation — user-service, CLM Platform*
