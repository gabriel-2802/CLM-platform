# Client Service — Technical Documentation

**Project:** CLM Platform (Client Lifecycle Management)
**Service:** `client-service`
**Version:** Spring Boot 4.0.5 / Java 21
**Author:** Gabriel Cărăuleanu
**Date:** 2026-05-17

---

## Table of Contents

1. [Architectural Overview & Design Patterns](#1-architectural-overview--design-patterns)
2. [Deep Dive: Request Processing Pipeline & Implementation](#2-deep-dive-request-processing-pipeline--implementation)
3. [Database Schema & Data Strategy](#3-database-schema--data-strategy)
4. [Security & Token Handling](#4-security--token-handling)

---

## 1. Architectural Overview & Design Patterns

### 1.1 System Context

The `client-service` is a self-contained REST microservice within the CLM platform. It is the **sole system of record** for all client-related data: company profiles, compliance details, work points, financial history, task assignments, and user-to-client relationships. No other service writes to its PostgreSQL schema.

The service runs on port `8084` internally and is accessed externally through an Nginx reverse proxy at the path `/api/clients/`. This decoupled ingress means the service itself has no awareness of TLS termination, load balancing, or rate limiting — concerns handled entirely at the gateway layer.

```
[ Browser / Next.js Frontend ]
           │
           ▼  HTTPS
    [ Nginx Reverse Proxy ]  ← TLS termination, /api/clients/** routing
           │
           ▼  HTTP :8084
    [ client-service ]       ← Spring Boot 4.0.5, Java 21
           │
           ▼  JDBC
    [ PostgreSQL :5445 ]     ← Schema: clients (schema-isolated)
```

> **Architectural decision:** The service does not embed an API gateway or act as a BFF (Backend for Frontend). It is a pure domain service. Cross-cutting concerns (auth token issuance, user management) belong to the `user-service`. The `client-service` only _validates_ tokens issued by the shared JWT secret — it never issues them.

### 1.2 Service Responsibilities

| Responsibility | Implementation | Notes |
|---|---|---|
| Client CRUD | `ClientService` + `ClientController` | Full + partial update with separate MapStruct strategies |
| Compliance details | `DetailsService` + `DetailsController` | Upsert pattern: insert-or-update in one endpoint |
| Work points | `WorkPointService` + `WorkPointController` | N:1 with clients, cascaded deletes |
| Financial history | `HistoryService` + `HistoryController` | Keyed by (client_id, year), unique constraint enforced at DB level |
| Task management | `TaskService` + `TaskController` | Role-sensitive list visibility |
| User-client assignments | `ClientAssignmentService` + `ClientAssignmentController` | Atomic replace via delete-then-insert |
| JWT validation | `JwtTokenProvider` + `JwtAuthenticationFilter` | Stateless, no session, no DB lookup |
| Schema migrations | Flyway + `FlywayMigrationConfig` | Versioned SQL, schema-isolated |

### 1.3 Design Patterns Applied

**Repository Pattern with Specification Executor**

`ClientRepository` extends both `JpaRepository<Client, Long>` and `JpaSpecificationExecutor<Client>`. This combination allows the `ClientService.listClients()` method to compose arbitrarily complex predicates at runtime without writing custom JPQL per filter combination.

```java
// Specification composition in ClientService
Specification<Client> spec = (root, query, builder) -> builder.conjunction();

if (Objects.nonNull(request.active())) {
    spec = spec.and((root, query, builder) ->
            builder.equal(root.get("active"), request.active()));
}
if (Objects.nonNull(resolvedUserIdFilter)) {
    spec = spec.and((root, query, builder) -> {
        var join = root.join("userClients", JoinType.INNER);
        query.distinct(true);
        return builder.equal(join.get("userId"), resolvedUserIdFilter);
    });
}
```

The INNER JOIN against `user_clients` in the specification is the mechanism by which `ROLE_USER` principals are restricted to only their assigned clients. The filter is injected by the controller layer after inspecting the JWT principal — the service method itself is agnostic to how `resolvedUserIdFilter` is populated.

**DTO / Mapper Separation**

All external contracts are expressed as immutable Java records (request/response DTOs). The `Client` JPA entity never leaks outside the service layer. MapStruct generates the mapping bytecode at compile time, eliminating reflection overhead at runtime. Three distinct mapping strategies exist on `ClientMapper`:

| Method | MapStruct Strategy | Effect |
|---|---|---|
| `toEntity(request)` | Default | DTO → new Entity (nulls written as null) |
| `updateEntity(client, request)` | `SET_TO_NULL` | Full replace (nulls explicitly clear fields) |
| `partialUpdateEntity(client, request)` | `IGNORE` | PATCH semantics (nulls in DTO leave entity field untouched) |

This is a non-trivial distinction that prevents PATCH requests from accidentally nullifying fields the caller did not intend to modify.

**Upsert Pattern for 1:1 Sub-Resources**

`DetailsService.upsertDetails()` demonstrates the upsert pattern for the `client_details` sub-resource:

```java
@Transactional
public DetailsResponse upsertDetails(Long clientId, DetailsRequest request) {
    Client client = findClient(clientId);
    ClientDetails details = detailsRepository.findByClientId(clientId)
            .map(existing -> detailsMapper.updateEntity(existing, request))
            .orElseGet(() -> detailsMapper.toEntity(request));
    details.setClient(client);
    return detailsMapper.toResponse(detailsRepository.save(details));
}
```

A single `PUT` endpoint handles both create and update semantics. The caller is responsible for sending the full desired state; the service resolves whether this is an insert or update internally. This eliminates a class of race conditions where a `POST` and `PUT` could conflict if the frontend does not reliably track creation state.

**Atomic Assignment Replace**

`ClientAssignmentService.replaceAssignments()` uses a delete-then-insert transaction to atomically swap the full set of user assignments for a client:

```java
@Transactional
public AssignmentResponse replaceAssignments(Long clientId, AssignmentRequest request) {
    userClientRepository.deleteByClientId(clientId);
    Set<Long> distinctUserIds = request.userIds().stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    userClientRepository.saveAll(
            distinctUserIds.stream()
                    .map(userId -> buildAssignment(client, userId))
                    .toList()
    );
    return new AssignmentResponse(clientId, List.copyOf(distinctUserIds));
}
```

The `@Transactional` boundary guarantees that no observer ever sees a partially replaced assignment list. The deduplication via `Collectors.toSet()` before persistence prevents duplicate key violations when the caller sends repeated user IDs.

**Idempotent Single Assignment**

`assignUser()` explicitly swallows `DataIntegrityViolationException` from the unique constraint on `(user_id, client_id)`:

```java
try {
    userClientRepository.save(buildAssignment(client, userId));
} catch (DataIntegrityViolationException ignored) {
    log.debug("user {} already assigned to client {} — skipping duplicate", userId, clientId);
}
```

This makes the `POST /{clientId}/users/{userId}` endpoint idempotent by design — calling it twice produces the same state as calling it once, without surfacing an error to the caller.


### 1.5 Download Logic Decoupling

The `client-service` deliberately contains **no file download or document generation logic**. This is an intentional architectural boundary. All data is served as structured JSON through the REST API. If a consumer needs a downloadable document (e.g., a PDF export of client data), that concern is pushed to a dedicated export service or handled client-side by the frontend. This keeps the `client-service` domain-pure and avoids coupling I/O latency (file streaming, rendering) to business-critical read/write paths. The `Content-Disposition` header is exposed in the CORS configuration as a precaution for other services that may stream files through the same gateway, not for this service itself.

---

## 2. Deep Dive: Request Processing Pipeline & Implementation

### 2.1 Full Request Lifecycle

Every authenticated request follows a deterministic pipeline. The steps below trace a representative `GET /api/clients?active=true` from Nginx to the database and back.

```
1. Nginx receives HTTPS request
       └─ strips TLS, proxies to http://client-service:8084/api/clients?active=true

2. Servlet container (Tomcat embedded in Spring Boot)
       └─ creates HttpServletRequest, routes to Spring DispatcherServlet

3. Spring Security FilterChain executes in order:
   a. CorsFilter          — validates Origin, sets CORS response headers
   b. JwtAuthenticationFilter (OncePerRequestFilter)
          ├─ extractBearer(): reads Authorization header, strips "Bearer " prefix
          ├─ JwtTokenProvider.getClaims(): parses & validates JWT signature (HS512)
          ├─ extractAuthorities(): reads "roles" claim → List<SimpleGrantedAuthority>
          └─ SecurityContextHolder.setAuthentication(UsernamePasswordAuthenticationToken)

4. Spring Security authorization check
       └─ @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')") → passes

5. DispatcherServlet routes to ClientController.listClients()
       └─ @ModelAttribute binds query params to ClientListRequest record

6. ClientService.listClients(request, resolvedUserIdFilter)
       ├─ builds Specification<Client> by composing predicates
       ├─ calls clientRepository.findAll(spec, PageRequest.of(page, size))
       └─ Hibernate generates SQL: SELECT ... FROM clients.clients c
             INNER JOIN clients.user_clients uc ON uc.client_id = c.id
             WHERE c.active = $1 AND uc.user_id = $2
             LIMIT $3 OFFSET $4

7. ClientMapper.toResponse() maps each Client entity → ClientResponse DTO
       └─ MapStruct-generated code, zero reflection

8. ResponseEntity<Page<ClientResponse>> serialised to JSON by Jackson
       └─ includes pagination metadata: totalElements, totalPages, size, number

9. Response traverses filter chain in reverse (no-ops)
10. Nginx adds upstream headers, returns to browser
```

### 2.2 JWT Parsing Pipeline — Step-by-Step Breakdown

The token parsing pipeline is entirely contained within `JwtTokenProvider` and `JwtAuthenticationFilter`. It is designed to be **fail-safe**: a malformed, expired, or tampered token causes the request to proceed as unauthenticated rather than return an error, allowing Spring Security's authorization layer to produce a structured 401 response via `JwtAuthenticationEntryPoint`.

**Step 1 — Key Initialization (`@PostConstruct`)**

```java
@PostConstruct
void init() {
    byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
    if (keyBytes.length < 64) {
        log.warn("jwt.secret should be at least 64 characters (512 bits) for HS512...");
    }
    this.signingKey = Keys.hmacShaKeyFor(keyBytes);
}
```

The signing key is built once at bean initialization from the raw UTF-8 bytes of the configured secret. JJWT's `Keys.hmacShaKeyFor()` wraps this into a `SecretKey` suitable for HMAC-SHA512. The minimum-length warning is defensive: HMAC-SHA512 requires a 512-bit (64-byte) key to achieve its full security guarantee; shorter keys are technically accepted by the spec but reduce the effective security margin.

**Step 2 — Token Extraction**

```java
private Optional<String> extractBearer(HttpServletRequest request) {
    return Optional.ofNullable(request.getHeader(AUTHORIZATION_HEADER))
            .filter(h -> h.startsWith(BEARER_PREFIX))
            .map(h -> h.substring(BEARER_PREFIX.length()));
}
```

The extraction uses `Optional` chaining to express the three possible states (header absent, header present but not Bearer, header present and Bearer) without branching. Only the raw token string (after the 7-character `"Bearer "` prefix) is passed downstream.

**Step 3 — Claims Parsing**

```java
private Optional<Claims> parseClaims(String token) {
    if (!StringUtils.hasText(token)) return Optional.empty();
    try {
        Claims payload = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Optional.of(payload);
    } catch (JwtException | IllegalArgumentException e) {
        log.warn("JWT parsing failed for token: {}", e.getMessage());
        return Optional.empty();
    }
}
```

`parseSignedClaims()` performs three checks atomically: (1) structural validity of the JWT format, (2) signature verification against `signingKey`, and (3) expiry claim (`exp`) validation. Any failure collapses to `Optional.empty()`, which the filter interprets as "no authenticated principal for this request."

**Step 4 — Authority Extraction**

```java
private List<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
    var rolesClaim = claims.get("roles");
    List<String> roles = switch (rolesClaim) {
        case String rolesString     -> List.of(rolesString.split(","));
        case Collection<?> collection -> collection.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .toList();
        default -> List.of();
    };
    return roles.stream()
            .map(String::trim)
            .filter(StringUtils::hasText)
            .map(SimpleGrantedAuthority::new)
            .toList();
}
```

The sealed `switch` on the runtime type of `rolesClaim` handles both JWT producers that embed roles as a comma-separated string (`"ROLE_USER,ROLE_MANAGER"`) and those that embed roles as a JSON array (which JJWT deserialises to a `Collection`). This makes the service compatible with different token issuers without configuration.

### 2.3 Data Filtering Pipeline — Specification Pattern

The `ClientService.buildSpecification()` method implements a **composable predicate builder** using the JPA Criteria API. Each optional filter is a lambda that closes over the filter value and is composed with `Specification.and()`:

```java
private Specification<Client> buildSpecification(ClientListRequest request, Long resolvedUserIdFilter) {
    // Start with a tautology (always-true predicate)
    Specification<Client> spec = (root, query, builder) -> builder.conjunction();

    if (Objects.nonNull(request.active())) {
        spec = spec.and((root, query, builder) ->
                builder.equal(root.get("active"), request.active()));
    }
    if (Objects.nonNull(request.type())) {
        spec = spec.and((root, query, builder) ->
                builder.equal(root.get("type"), request.type()));
    }
    if (Objects.nonNull(resolvedUserIdFilter)) {
        spec = spec.and((root, query, builder) -> {
            var join = root.join("userClients", JoinType.INNER);
            query.distinct(true);
            return builder.equal(join.get("userId"), resolvedUserIdFilter);
        });
    }
    return spec;
}
```

**Correctness argument:** The initial `conjunction()` is the identity element for `AND`-composition — appending zero filters produces `WHERE TRUE`, returning all rows. Each subsequent `and()` narrows the result set monotonically. The user-ID join uses `INNER JOIN`, meaning clients with no assignments in `user_clients` are excluded when the filter is active. `query.distinct(true)` prevents row duplication when a client has multiple assignment rows that all satisfy the predicate.

### 2.4 Upsert & History Year Resolution

`HistoryService` must resolve a potential mismatch between the year in the URL path and any year field in the request body:

```java
@Transactional
public HistoryResponse upsertHistory(Long clientId, Integer pathYear, HistoryRequest request) {
    Client client = findClient(clientId);
    Integer year = Objects.nonNull(pathYear) ? pathYear : request.year();
    if (Objects.isNull(year)) {
        throw new IllegalArgumentException("Year must be specified");
    }
    ClientHistory history = historyRepository.findByClientIdAndYear(clientId, year)
            .map(existing -> historyMapper.updateEntity(existing, request))
            .orElseGet(() -> {
                ClientHistory h = historyMapper.toEntity(request);
                h.setClient(client);
                h.setYear(year);
                return h;
            });
    return historyMapper.toResponse(historyRepository.save(history));
}
```

The URL path year takes precedence. If the body also contains a year, it is ignored in favour of the authoritative path parameter. This prevents clients from inadvertently overwriting a different year's record by submitting a mismatched body.

### 2.5 Implementation Difficulties & Solutions

**Challenge 1: N+1 Query Problem with Lazy Associations**

**Problem:** The `Client` entity has four `@OneToMany` collections (`workPoints`, `histories`, `userClients`, tasks implied via `TaskRepository`). With default lazy loading and Spring's `open-in-view` pattern, accessing any of these collections outside a transaction would trigger additional SELECT queries per entity — catastrophic for paginated list endpoints returning 50 clients.

**Solution:** Two complementary strategies are applied:

1. `spring.jpa.open-in-view: false` is explicitly set in `application.yaml`. This disables the anti-pattern where the session remains open through the view rendering phase, forcing all data access to happen within explicit `@Transactional` boundaries.

2. `@BatchSize(size = 50)` is applied to the `workPoints`, `histories`, and `userClients` collections on the `Client` entity:

```java
@OneToMany(mappedBy = "client", cascade = CascadeType.ALL,
           fetch = FetchType.LAZY, orphanRemoval = true)
@BatchSize(size = 50)
private Set<WorkPoint> workPoints = new HashSet<>();
```

With batch-size 50, if a page of 50 clients is loaded and their work points are accessed, Hibernate issues one `SELECT ... WHERE client_id IN (id1, id2, ..., id50)` query rather than 50 individual queries. Since the list endpoint returns `ClientResponse` DTOs that do not include collection fields, the collections are never accessed during listing — they are only relevant for single-entity detail endpoints where the N+1 problem does not apply.

**Challenge 2: Enum Constraint Drift Between JPA and Database**

**Problem:** Java enums (`CompanyType`, `TaxType`, `TaxFrequency`, `Administration`) define the valid values in the application layer. Without corresponding database-level constraints, a raw SQL insert or a future code change that removes an enum member could leave orphaned string values in the database that break deserialization.

**Solution:** Migration `V4__constrains_for_fields.sql` adds explicit `CHECK` constraints to the database for every string-persisted enum column. The `Administration` enum alone enumerates over 120 valid Romanian ANAF tax office codes (DGRF, AJFP, UFM, UFO, UFC, SFC prefixes representing General Regional Directorates, County Finance Administrations, and local fiscal units respectively). These constraints are enforced at the PostgreSQL level independently of Hibernate, creating a **dual-validation fence**: the JPA layer rejects invalid enum conversions before they reach the database, and the database rejects any bypass attempt.

**Challenge 3: PATCH Semantics With Struct-Like Request DTOs**

**Problem:** Standard Java request objects do not distinguish between "the caller sent `null` for field X" and "the caller did not send field X at all." Both cases result in a `null` Java field, making it impossible to implement true PATCH semantics where absent fields are preserved.

**Solution:** MapStruct's `NullValuePropertyMappingStrategy.IGNORE` on the `partialUpdateEntity()` method handles this at the mapping level. The tradeoff accepted is that a PATCH request **cannot explicitly nullify a nullable field** — setting a field to `null` via PATCH is indistinguishable from omitting it. Full nullification requires `PUT`. This limitation is documented in the API contract (OpenAPI annotations) and is an acceptable tradeoff for the use case, where partial updates are used for single-field edits (e.g., toggling `active`, updating `verificationDate`) rather than wholesale state replacement.

**Challenge 4: Race Condition in User Assignment**

**Problem:** Concurrent `POST /{clientId}/users/{userId}` calls (e.g., from double-click on a frontend button) could both pass the pre-save existence check and attempt to insert the same `(user_id, client_id)` row, causing one to fail with a `DataIntegrityViolationException` from the `uk_user_clients_user_client` unique constraint.

**Solution:** Rather than adding an explicit `EXISTS` check (which would not eliminate the TOCTOU window), the service wraps the save in a try-catch that swallows the constraint violation:

```java
try {
    userClientRepository.save(buildAssignment(client, userId));
} catch (DataIntegrityViolationException ignored) {
    log.debug("user {} already assigned to client {} — skipping duplicate", userId, clientId);
}
```

This makes the endpoint idempotent by leveraging the database's uniqueness guarantee as the conflict detector. The first concurrent call wins; the second silently succeeds from the caller's perspective. This is the optimistic approach: attempt the insert, handle the conflict — more efficient than pessimistic locking for low-contention operations.

### 2.6 Correctness Argument for the Specification Builder

**Claim:** The `buildSpecification()` method is deterministic, safe, and produces correct SQL for any combination of nullable filter inputs.

**Proof sketch:**

Let F = {active, type, userIdFilter} be the set of filter values, where each element is either `null` (absent) or a concrete value.

1. **Base case (∅ filters):** The initial `conjunction()` maps to `SQL WHERE TRUE`, which is equivalent to no WHERE clause. All clients are returned. This is correct — an empty filter means "return all."

2. **Inductive step:** For each non-null filter f ∈ F, `spec.and(predicate_f)` is applied. By the semantics of SQL `AND`, adding a non-contradictory predicate can only narrow or maintain the result set, never expand it. Each predicate (`equal`, `join + equal`) maps bijectively to a SQL expression with a bound parameter — there is no string interpolation, ruling out SQL injection.

3. **Completeness:** Every possible input combination of {null, non-null} across three filter fields produces a unique, unambiguous Specification. There is no branch that is unreachable or produces an undefined state.

4. **Ordering safety:** Predicate composition order does not affect correctness because SQL `AND` is commutative and associative.

5. **Pagination safety:** `PageRequest.of(page, size)` with Spring Data's `findAll(Specification, Pageable)` issues `SELECT COUNT(*)` with the same WHERE clause to populate `totalElements`, then the data query with `LIMIT/OFFSET`. The `query.distinct(true)` flag in the user-ID join specification ensures the COUNT is also deduplicated, preventing `totalElements` from overcounting.

---

## 3. Database Schema & Data Strategy

### 3.1 Schema Isolation

All tables reside in the dedicated PostgreSQL schema `clients`, created by the first Flyway migration. The application user (`client_user`) has access only to this schema. This provides:

- **Namespace isolation:** No risk of table name collisions with other services sharing the same PostgreSQL cluster.
- **Permission boundary:** `client_user` cannot read or write to `users` schema or any other service's schema.
- **Migration safety:** Flyway's `flyway.schemas: clients` setting scopes migration history to this schema's `flyway_schema_history` table.

The JPA configuration enforces this at the ORM level:

```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_schema: clients
```

### 3.2 Entity-Relationship Model

```
clients.clients (1) ──────────────────── (0..1) clients.client_details
     │                                            (FK: client_id UNIQUE)
     │
     │ (1) ──────────────────────────── (0..N) clients.work_points
     │                                            (FK: client_id)
     │
     │ (1) ──────────────────────────── (0..N) clients.client_histories
     │                                            (FK: client_id, UNIQUE: client_id+year)
     │
     │ (1) ──────────────────────────── (0..N) clients.tasks
     │                                            (FK: client_id)
     │
     │ (1) ──────────────────────────── (0..N) clients.user_clients
                                                 (FK: client_id, UNIQUE: user_id+client_id)
```

All foreign keys use `ON DELETE CASCADE`, so deleting a client atomically removes all its sub-resources in a single database transaction without requiring application-level orchestration.

### 3.3 Table Definitions

#### `clients.clients` — Core Client Record

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGSERIAL` | PK | Auto-incrementing surrogate key |
| `name` | `VARCHAR(255)` | NOT NULL | Legal company name |
| `company_type` | `VARCHAR(50)` | NOT NULL, CHECK | SRL, PFA, II, ASOC, SA, SPARL |
| `tax_id` | `VARCHAR(50)` | NOT NULL, UNIQUE | Romanian CUI/CIF |
| `active` | `BOOLEAN` | NOT NULL | Client status |
| `administration` | `VARCHAR(50)` | NOT NULL, CHECK | Responsible ANAF office code |
| `tax_type` | `VARCHAR(50)` | CHECK | MICRO_1, MICRO_3, PROFIT |
| `vat_payer` | `VARCHAR(50)` | NOT NULL, CHECK | DA_LUNAR, DA_TRIM, NU |
| `vat_on_collection` | `BOOLEAN` | — | TVA la încasare flag |
| `has_eu_vat_code` | `BOOLEAN` | — | Has intra-EU VAT code |
| `eu_vat_code` | `VARCHAR(255)` | — | RO-prefixed EU VAT code |
| `eu_operation` | `BOOLEAN` | — | Intra-EU transactions flag |
| `dividends` | `BOOLEAN` | — | Dividend distribution flag |
| `employees` | `VARCHAR(255)` | — | Employee count description |
| `cash_register` | `BOOLEAN` | — | Has fiscal cash register |
| `verification_date` | `DATE` | — | Last ANAF verification date |
| `hq_expiration_date` | `DATE` | — | Headquarters contract expiry |
| `admin_mandate_expiration` | `DATE` | — | Administrator mandate expiry |
| `fiscal_certificate_date` | `DATE` | — | Fiscal certificate issue date |
| `payer_sheet_date` | `DATE` | — | Payer registration date |
| `fiscal_vector_date` | `DATE` | — | Fiscal vector update date |
| `address` | `VARCHAR(255)` | — | Registered address |
| `created_at` | `TIMESTAMP` | NOT NULL, DEFAULT NOW() | Immutable creation timestamp |
| `updated_at` | `TIMESTAMP` | NOT NULL, DEFAULT NOW() | Last modification timestamp |

> **Note on date columns:** Migration V5 converted six date-related columns from `TIMESTAMP` to `DATE`. The original V1 schema used `TIMESTAMP` as a safe default, but the domain model requires only calendar dates (no time component). V5 performs an `ALTER TABLE ... ALTER COLUMN ... TYPE DATE USING column::DATE` cast, which is lossless for values that had no time component and truncates the time for any that did.

#### `clients.client_details` — Compliance Sub-Record (1:1)

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGSERIAL` | PK | Surrogate key |
| `client_id` | `BIGINT` | NOT NULL, UNIQUE, FK→clients | Owner client (UNIQUE enforces 1:1) |
| `uc_registry` | `BOOLEAN` | NOT NULL | Registered with UC (Uniunea Contabililor) |
| `fiscal_evidence_registry` | `VARCHAR(50)` | NOT NULL, CHECK(DA\|NU\|NU_E_CAZUL) | Fiscal evidence register status |
| `money_laundering_office` | `BOOLEAN` | NOT NULL | Anti-money-laundering office registration |
| `internal_rules` | `BOOLEAN` | NOT NULL | Internal rules document present |
| `accounting_policies_manual` | `BOOLEAN` | NOT NULL | Accounting policies manual present |
| `revisal_address` | `BOOLEAN` | NOT NULL | ReviSal reporting address registered |
| `itm_password` | `VARCHAR(255)` | nullable | ITM (Labor Inspectorate) portal password |
| `online_declarations` | `BOOLEAN` | NOT NULL | Online declaration submission enabled |
| `fiscal_file_access` | `VARCHAR(50)` | NOT NULL, CHECK(DA\|NU\|NU_E_CAZUL) | Accountant has access to fiscal file |

The `UNIQUE` constraint on `client_id` enforces the 1:1 cardinality at the database level. The JPA entity uses `@OneToOne(mappedBy = "client")` with `orphanRemoval = true`, ensuring that the details record is deleted if the client is deleted.

#### `clients.client_histories` — Annual Financial History

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGSERIAL` | PK | Surrogate key |
| `client_id` | `BIGINT` | NOT NULL, FK→clients | Owner client |
| `year` | `INTEGER` | NOT NULL | Fiscal year (e.g., 2024) |
| `turnover` | `NUMERIC(19,2)` | NOT NULL | Annual turnover in RON |
| `inventory` | `BOOLEAN` | NOT NULL | Inventory conducted |
| `june_semester_balance` | `VARCHAR(50)` | NOT NULL, CHECK | H1 balance submitted (DA\|NU\|NU_E_CAZUL) |
| `annual_balance` | `VARCHAR(50)` | NOT NULL, CHECK | Annual balance submitted |
| — | — | UNIQUE(client_id, year) | One record per client per year |

> **Design decision on `turnover` type:** Migration V2 altered this column from `DOUBLE PRECISION` to `NUMERIC(19,2)`. Floating-point types are unsuitable for monetary values because they cannot represent decimal fractions exactly in binary. `NUMERIC(19,2)` provides exact fixed-point arithmetic up to 17 digits before the decimal point — sufficient for any realistic company turnover in RON.

#### `clients.work_points` — Additional Operational Locations

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGSERIAL` | PK | Surrogate key |
| `client_id` | `BIGINT` | NOT NULL, FK→clients | Owner client |
| `name` | `VARCHAR(255)` | NOT NULL | Work point name |
| `valid_from` | `DATE` | NOT NULL | Operational start date |
| `valid_to` | `DATE` | nullable | Operational end date (null = ongoing) |
| `administration` | `VARCHAR(50)` | NOT NULL, CHECK | ANAF office for this location |
| `uc_registry` | `BOOLEAN` | NOT NULL | UC registration for this location |
| `employee_count` | `INTEGER` | NOT NULL | Employees at this location |
| `tax_id` | `VARCHAR(50)` | nullable | Tax ID specific to this work point |
| `cash_register` | `BOOLEAN` | NOT NULL | Has fiscal cash register |

#### `clients.tasks` — User Task Records (Added in V3)

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGSERIAL` | PK | Surrogate key |
| `client_id` | `BIGINT` | NOT NULL, FK→clients | Associated client |
| `user_id` | `BIGINT` | NOT NULL | Owning user (cross-service reference, no FK) |
| `done` | `BOOLEAN` | DEFAULT FALSE | Completion status |
| `title` | `VARCHAR(500)` | — | Task title |
| `notes` | `TEXT` | — | Free-form notes |
| `blocked` | `TEXT` | — | Blocking reason |
| `objective` | `TEXT` | — | Task objective |
| `date` | `DATE` | NOT NULL | Due date |

`user_id` is a **cross-service reference** — there is no foreign key to a `users` table because that table lives in a different schema managed by a different service. This is an intentional distributed systems decision: enforcing referential integrity across service boundaries at the database level would couple the schemas and make independent deployments and migrations impossible.

#### `clients.user_clients` — Assignment Join Table

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGSERIAL` | PK | Surrogate key |
| `user_id` | `BIGINT` | NOT NULL | User (cross-service reference) |
| `client_id` | `BIGINT` | NOT NULL, FK→clients | Assigned client |
| — | — | UNIQUE(user_id, client_id) | No duplicate assignments |

### 3.4 Index Strategy

| Index | Table | Column(s) | Purpose |
|---|---|---|---|
| `idx_clients_tax_id` | clients | `tax_id` | Fast lookup by CUI, enforces uniqueness |
| `idx_clients_active` | clients | `active` | Efficient filtering of active/inactive clients |
| `idx_client_details_client_id` | client_details | `client_id` | Fast 1:1 join |
| `idx_client_histories_client_id` | client_histories | `client_id` | Fast history list by client |
| `idx_client_histories_year` | client_histories | `year` | Fast history lookup by year |
| `idx_work_points_client_id` | work_points | `client_id` | Fast work point list by client |
| `idx_user_clients_client_id` | user_clients | `client_id` | Fast assignment lookup by client |
| `idx_user_clients_user_id` | user_clients | `user_id` | Fast client list by user (role filter) |
| `idx_tasks_client_id` | tasks | `client_id` | Fast task list by client |
| `idx_tasks_user_id` | tasks | `user_id` | Fast task list by user |
| `idx_tasks_date` | tasks | `date` | Ordering and range queries by due date |

The `idx_user_clients_user_id` index is performance-critical: it is hit on every request from a `ROLE_USER` principal, where the Specification builder joins `user_clients` on `user_id` to restrict the visible client set.

### 3.5 Flyway Migration Strategy

Migrations are linear, versioned, and irreversible by convention. The service uses `ddl-auto: validate` (not `update`), meaning Hibernate will refuse to start if the entity model diverges from the database schema. This makes schema drift a startup failure rather than a silent data corruption.

| Migration | Description | Key Change |
|---|---|---|
| V1 | Initial schema | 5 tables, all indexes, FK constraints |
| V2 | Turnover column type fix | `DOUBLE PRECISION` → `NUMERIC(19,2)` |
| V3 | Tasks table | Added `tasks` with date, user_id, client_id FK |
| V4 | Enum constraints | CHECK constraints on all enum-valued columns |
| V5 | Date column alignment | 8 columns converted from `TIMESTAMP` → `DATE` |

---

## 4. Security & Token Handling

### 4.1 Security Architecture

The `client-service` implements a **stateless, token-forward security model**. It never issues tokens, never stores session state, and never queries a user store. Its sole security responsibility is validating the cryptographic signature on incoming JWTs and populating the Spring Security context with the authenticated principal and their roles.

```
┌──────────────────────────────────────────────────────────┐
│                  Spring Security FilterChain              │
│                                                           │
│  CorsFilter → JwtAuthenticationFilter → AuthorizationFilter │
│       │               │                        │          │
│  CORS headers    validate JWT          @PreAuthorize      │
│  preflight        parse claims         role checks        │
│  handling         set SecurityCtx                         │
└──────────────────────────────────────────────────────────┘
```

### 4.2 SecurityConfig — Filter Chain Definition

```java
return http
    .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
    .csrf(AbstractHttpConfigurer::disable)
    .httpBasic(AbstractHttpConfigurer::disable)
    .formLogin(AbstractHttpConfigurer::disable)
    .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
    .authorizeHttpRequests(authz -> authz
            .requestMatchers(
                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                "/swagger-resources/**", "/webjars/**",
                "/api/clients/v3/api-docs/**", "/api/clients/swagger-ui/**",
                "/api/enums", "/actuator/**"
            ).permitAll()
            .anyRequest().authenticated())
    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
    .build();
```

**Decisions:**

- **CSRF disabled:** Stateless REST APIs do not use cookies for authentication, making CSRF attacks impossible. CSRF protection applies only to session-cookie-based authentication.
- **HTTP Basic and Form Login disabled:** Explicitly disabled to prevent Spring Boot's auto-configuration from enabling these mechanisms, which would create unexpected authentication endpoints.
- **STATELESS session:** No `HttpSession` is created. Each request is independently authenticated. This enables horizontal scaling without session affinity.
- **Swagger paths permitted:** OpenAPI documentation endpoints are publicly accessible, allowing API discovery without credentials. The duplicated `/api/clients/swagger-ui/**` paths handle Nginx-proxied access where the prefix is preserved.
- **`addFilterBefore`:** The JWT filter is inserted before `UsernamePasswordAuthenticationFilter` to ensure the security context is populated before Spring's built-in authentication processing runs.

### 4.3 Role-Based Access Control Matrix

| Operation | USER | MANAGER | ADMIN |
|---|:---:|:---:|:---:|
| List clients (own) | ✓ | ✓ | ✓ |
| List clients (all) | — | ✓ | ✓ |
| Get client by ID | ✓ | ✓ | ✓ |
| Create client | — | ✓ | ✓ |
| Update client (PUT/PATCH) | — | ✓ | ✓ |
| Delete client | — | — | ✓ |
| View/upsert client details | ✓ | ✓ | ✓ |
| Manage work points (write) | — | ✓ | ✓ |
| Delete work point | — | — | ✓ |
| View/upsert history | ✓ | ✓ | ✓ |
| Delete history | — | — | ✓ |
| Create/view own tasks | ✓ | ✓ | ✓ |
| View all tasks | — | ✓ | ✓ |
| Delete tasks | — | ✓ | ✓ |
| Manage user assignments | — | — | ✓ |
| View user assignments | — | — | ✓ |

Role enforcement occurs at two levels:

1. **Controller level (`@PreAuthorize`):** Coarse-grained per-endpoint role requirement. Example: `@PreAuthorize("hasRole('ADMIN')")` on `deleteClient()`.
2. **Service level (programmatic):** Fine-grained data filtering. Example: `SecurityUtils.isUserOnly()` determines whether `resolvedUserIdFilter` is injected, restricting the client list to only assigned clients.

### 4.4 SecurityUtils — Thread-Safe Principal Extraction

```java
public static Optional<Long> getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (Objects.isNull(authentication) || !authentication.isAuthenticated()) {
        return Optional.empty();
    }
    try {
        return Optional.of(Long.parseLong(authentication.getName()));
    } catch (NumberFormatException ex) {
        return Optional.empty();
    }
}
```

`SecurityContextHolder` uses a `ThreadLocal`-backed `SecurityContext` by default, making `getCurrentUserId()` thread-safe within a single request's thread. The JWT subject (`sub` claim) is expected to be the user's numeric ID as a string — this is the contract between the `user-service` (token issuer) and `client-service` (token validator). `Long.parseLong()` enforces this contract: a non-numeric subject yields `Optional.empty()` rather than an exception propagating to the response.

### 4.5 Structured 401 Response

`JwtAuthenticationEntryPoint` implements `AuthenticationEntryPoint` to ensure unauthenticated requests receive a structured JSON error rather than Spring's default HTML error page:

```java
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        var body = new ErrorResponse(
                Instant.now(), 401, "Unauthorized",
                "Authentication required", request.getRequestURI()
        );
        new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .writeValue(response.getWriter(), body);
    }
}
```

This is critical for API consumers: a 401 with `Content-Type: text/html` would break JSON-parsing frontend clients. The `JavaTimeModule` registration is required because `Instant` serialization depends on it, and this `ObjectMapper` instance is not the Spring-managed one (which already has the module configured).

### 4.6 CORS Configuration

```java
configuration.setAllowedOrigins(List.of("*"));
configuration.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
configuration.setAllowedHeaders(List.of("*"));
configuration.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
configuration.setAllowCredentials(false);
configuration.setMaxAge(3600L);
```

`AllowCredentials: false` is a deliberate security choice. Setting `allowedOrigins("*")` and `allowCredentials(true)` simultaneously is prohibited by the CORS specification — browsers will reject such responses. Since the API uses Bearer token authentication (not cookies), credentials in the CORS sense (cookies, HTTP auth) are irrelevant, and `false` is the correct setting. `MaxAge: 3600` caches the preflight response for one hour, reducing OPTIONS request overhead for authenticated operations.

### 4.7 Romanian Tax Administration (ANAF) Domain Encoding

The `Administration` enum encodes the **full hierarchy of Romanian ANAF (National Agency for Fiscal Administration) offices** as a controlled vocabulary. The hierarchy follows the official ANAF organisational structure:

| Prefix | Type | Example | Description |
|---|---|---|---|
| `DGRF_` | General Regional Directorate of Public Finances | `DGRF_BUCURESTI` | Top-level regional authority |
| `AJFP_` | County Administration of Public Finances | `AJFP_CLUJ` | County-level office |
| `UFM_` | Municipal Fiscal Unit | `UFM_TURDA` | Municipality-level office |
| `UFO_` | Fiscal Office | `UFO_HUEDIN` | Sub-municipal fiscal office |
| `UFC_` | Communal Fiscal Unit | `UFC_BAIA` | Commune-level fiscal unit |
| `SFC_` | Fiscal Service | `SFC_SAVARSIN` | Smallest administrative fiscal unit |
| `AFCM_` | Fiscal Administration for Medium Companies | `AFCM_DGRF_BUCURESTI` | Specialised medium-company division |
| `AFCN_` | National Fiscal Administration for Large Taxpayers | `AFCN` | National large-taxpayer division |
| `SECTOR_` | Bucharest Sector | `SECTOR_1`…`SECTOR_6` | Bucharest municipal sectors |

Each client record must be associated with exactly one administration code. This field is NOT NULL and subject to a database CHECK constraint. When a client's registered address falls under the jurisdiction of a specific ANAF office, that office's code is stored here — enabling the accounting firm using the CLM platform to group and filter clients by their responsible tax authority, which determines which ANAF portal and procedures apply.

---

*End of documentation.*
