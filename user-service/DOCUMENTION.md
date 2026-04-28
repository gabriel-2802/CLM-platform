# User Service Controller Documentation

This document describes the REST controllers exposed by the `user-service` module, including routes, request/response records, security requirements, and error handling behavior.

## Base API layout

- Authentication controller base path: `/api/auth`
- User controller base path: `/api/users`
- All payloads are JSON unless noted otherwise
- JWT authentication uses the `Authorization: Bearer <token>` header

## Security model

The application is configured as a stateless JWT API:

- `POST /api/auth/**` is public
- Swagger endpoints are public
- All other routes require authentication
- Admin-only routes are protected with `@PreAuthorize("hasRole('ADMIN')")`
- The security filter chain is stateless and disables CSRF
- CORS is enabled and configured from `app.cors.allowed-origins`

### Authorization matrix

| Route | Access |
| --- | --- |
| `POST /api/auth/register` | Public |
| `POST /api/auth/login` | Public |
| `GET /api/users/me` | Authenticated |
| `GET /api/users/{id}` | Admin only |
| `GET /api/users` | Admin only |
| `PUT /api/users/{id}/roles/admin` | Admin only |
| `DELETE /api/users/{id}/roles/admin` | Admin only |
| `PATCH /api/users/{id}/enabled` | Admin only |

## Common response conventions

### Success responses

- `201 Created` for successful registration
- `200 OK` for successful login and all user-management operations
- Response bodies are the record types documented below

### Error responses

The application uses `ProblemDetail` for controller-level errors.

Common status codes:

- `400 Bad Request` — validation errors
- `401 Unauthorized` — invalid or missing authentication
- `403 Forbidden` — authenticated but not allowed
- `404 Not Found` — user not found
- `409 Conflict` — duplicate email during registration
- `503 Service Unavailable` — database validation failure
- `500 Internal Server Error` — unexpected exception

#### `ProblemDetail` shape

For application-handled exceptions, the response includes the standard `ProblemDetail` fields plus the custom `timestamp` property set by the handler:

```json
{
  "type": "about:blank",
  "title": "...",
  "status": 400,
  "detail": "...",
  "timestamp": "2026-04-28T12:34:56.789Z"
}
```

Validation errors add an `errors` property:

```json
{
  "type": "about:blank",
  "status": 400,
  "detail": "Validation failed",
  "timestamp": "2026-04-28T12:34:56.789Z",
  "errors": [
    {
      "field": "email",
      "message": "must be a well-formed email address"
    }
  ]
}
```

#### Unauthorized response body

Authentication failures handled by the JWT entry point return JSON in this shape:

```json
{
  "timestamp": "2026-04-28T12:34:56.789Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/users/me"
}
```

## DTO reference

### `RegisterRequest`

Record: `clm.user.demo.dto.requests.RegisterRequest`

| Field | Type | Validation | Required | Notes |
| --- | --- | --- | --- | --- |
| `email` | `String` | `@NotBlank`, `@Email` | Yes | Must be a valid email address |
| `password` | `String` | `@NotBlank`, `@Size(min = 8, max = 128)` | Yes | Registration password |
| `name` | `String` | `@NotBlank`, `@Size(max = 255)` | Yes | Display name |
| `adminCode` | `String` | none | No | Optional admin registration code |

Example:

```json
{
  "email": "user@test.com",
  "password": "Password1!",
  "name": "Test User",
  "adminCode": "devcode123"
}
```

### `LoginRequest`

Record: `clm.user.demo.dto.requests.LoginRequest`

| Field | Type | Validation | Required | Notes |
| --- | --- | --- | --- | --- |
| `email` | `String` | `@NotBlank`, `@Email` | Yes | Login email |
| `password` | `String` | `@NotBlank` | Yes | Login password |

Example:

```json
{
  "email": "user@test.com",
  "password": "Password1!"
}
```

### `AuthResponse`

Record: `clm.user.demo.dto.responses.AuthResponse`

| Field | Type | Notes |
| --- | --- | --- |
| `token` | `String` | JWT string |
| `tokenType` | `String` | Always `Bearer` when created through `AuthResponse.of(...)` |
| `expiresIn` | `long` | Token lifetime in milliseconds |
| `user` | `UserResponse` | Registered/authenticated user snapshot |

Example:

```json
{
  "token": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "expiresIn": 2592000000,
  "user": {
    "id": 1,
    "email": "user@test.com",
    "name": "Test User",
    "enabled": true,
    "roles": ["ROLE_USER"],
    "createdAt": "2026-04-28T12:00:00Z"
  }
}
```

### `UserResponse`

Record: `clm.user.demo.dto.responses.UserResponse`

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `Long` | User ID |
| `email` | `String` | User email |
| `name` | `String` | Display name |
| `enabled` | `boolean` | Whether the account is enabled |
| `roles` | `Set<String>` | Role names such as `ROLE_USER` and `ROLE_ADMIN` |
| `createdAt` | `Instant` | Account creation timestamp |

Notes:

- `roles` is serialized as a JSON array, but order is not guaranteed because it comes from a set
- `createdAt` is serialized in ISO-8601 format

## Controller documentation

## `AuthController`

Base path: `/api/auth`

### `POST /api/auth/register`

Registers a new user and returns a JWT response.

- **Access**: Public
- **Validation**: `@Valid` request body
- **Success status**: `201 Created`

#### Request body

`RegisterRequest`

#### Behavior

- Fails with `409 Conflict` if the email already exists
- Persists the user with the `ROLE_USER` role
- Grants `ROLE_ADMIN` only when `adminCode` is present and matches the configured admin registration code
- Returns a generated JWT and the created user snapshot

#### Example request

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "email": "user@test.com",
  "password": "Password1!",
  "name": "Test User",
  "adminCode": "devcode123"
}
```

#### Example response

```http
201 Created
```

```json
{
  "token": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "expiresIn": 2592000000,
  "user": {
    "id": 1,
    "email": "user@test.com",
    "name": "Test User",
    "enabled": true,
    "roles": ["ROLE_USER", "ROLE_ADMIN"],
    "createdAt": "2026-04-28T12:00:00Z"
  }
}
```

#### Common errors

- `400 Bad Request` — invalid email, short password, blank fields, or malformed JSON
- `409 Conflict` — duplicate email
- `500 Internal Server Error` — unexpected server failure

### `POST /api/auth/login`

Authenticates a user and returns a JWT response.

- **Access**: Public
- **Validation**: `@Valid` request body
- **Success status**: `200 OK`

#### Request body

`LoginRequest`

#### Behavior

- Uses the Spring Security `AuthenticationManager` to verify credentials
- Returns `401 Unauthorized` when credentials are invalid
- Looks up the user by email after successful authentication and includes the user snapshot in the response
- Returns a JWT token with the `Bearer` token type

#### Example request

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "user@test.com",
  "password": "Password1!"
}
```

#### Example response

```http
200 OK
```

```json
{
  "token": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "expiresIn": 2592000000,
  "user": {
    "id": 1,
    "email": "user@test.com",
    "name": "Test User",
    "enabled": true,
    "roles": ["ROLE_USER"],
    "createdAt": "2026-04-28T12:00:00Z"
  }
}
```

#### Common errors

- `400 Bad Request` — invalid email, blank password, or malformed JSON
- `401 Unauthorized` — invalid credentials
- `404 Not Found` — if the authenticated principal is resolved but the backing user record cannot be found
- `500 Internal Server Error` — unexpected server failure

## `UserController`

Base path: `/api/users`

All endpoints in this controller require a valid JWT unless otherwise noted, and all routes except `/me` are admin-only.

### `GET /api/users/me`

Returns the currently authenticated user.

- **Access**: Authenticated
- **Success status**: `200 OK`
- **Authentication source**: `@AuthenticationPrincipal UserDetails`

#### Behavior

- Reads the username from the authenticated principal
- Resolves the corresponding user via `UserService.getByEmail(...)`
- Returns `404 Not Found` if the user record is missing

#### Example request

```http
GET /api/users/me
Authorization: Bearer <token>
```

#### Example response

```http
200 OK
```

```json
{
  "id": 1,
  "email": "user@test.com",
  "name": "Test User",
  "enabled": true,
  "roles": ["ROLE_USER"],
  "createdAt": "2026-04-28T12:00:00Z"
}
```

### `GET /api/users/{id}`

Returns a user by ID.

- **Access**: Admin only
- **Success status**: `200 OK`

#### Path parameters

| Name | Type | Required |
| --- | --- | --- |
| `id` | `Long` | Yes |

#### Example request

```http
GET /api/users/1
Authorization: Bearer <admin-token>
```

#### Example response

```http
200 OK
```

```json
{
  "id": 1,
  "email": "user@test.com",
  "name": "Test User",
  "enabled": true,
  "roles": ["ROLE_USER"],
  "createdAt": "2026-04-28T12:00:00Z"
}
```

#### Common errors

- `401 Unauthorized` — missing or invalid JWT
- `403 Forbidden` — authenticated user is not an admin
- `404 Not Found` — user ID does not exist

### `GET /api/users`

Returns all users.

- **Access**: Admin only
- **Success status**: `200 OK`

#### Example request

```http
GET /api/users
Authorization: Bearer <admin-token>
```

#### Example response

```http
200 OK
```

```json
[
  {
    "id": 1,
    "email": "user@test.com",
    "name": "Test User",
    "enabled": true,
    "roles": ["ROLE_USER"],
    "createdAt": "2026-04-28T12:00:00Z"
  }
]
```

#### Common errors

- `401 Unauthorized` — missing or invalid JWT
- `403 Forbidden` — authenticated user is not an admin

### `PUT /api/users/{id}/roles/admin`

Grants `ROLE_ADMIN` to the specified user.

- **Access**: Admin only
- **Success status**: `200 OK`

#### Path parameters

| Name | Type | Required |
| --- | --- | --- |
| `id` | `Long` | Yes |

#### Behavior

- Loads the user by ID
- Looks up `ROLE_ADMIN`
- Adds the role to the user's role set
- Saving is idempotent at the collection level because roles are stored as a set

#### Example request

```http
PUT /api/users/1/roles/admin
Authorization: Bearer <admin-token>
```

#### Example response

```http
200 OK
```

```json
{
  "id": 1,
  "email": "user@test.com",
  "name": "Test User",
  "enabled": true,
  "roles": ["ROLE_USER", "ROLE_ADMIN"],
  "createdAt": "2026-04-28T12:00:00Z"
}
```

#### Common errors

- `401 Unauthorized` — missing or invalid JWT
- `403 Forbidden` — not an admin
- `404 Not Found` — user not found
- `500 Internal Server Error` — `ROLE_ADMIN` missing from the database

### `DELETE /api/users/{id}/roles/admin`

Revokes `ROLE_ADMIN` from the specified user.

- **Access**: Admin only
- **Success status**: `200 OK`

#### Path parameters

| Name | Type | Required |
| --- | --- | --- |
| `id` | `Long` | Yes |

#### Behavior

- Loads the user by ID
- Removes any role whose name is `ROLE_ADMIN`
- Saves and returns the updated user

#### Example request

```http
DELETE /api/users/1/roles/admin
Authorization: Bearer <admin-token>
```

#### Example response

```http
200 OK
```

```json
{
  "id": 1,
  "email": "user@test.com",
  "name": "Test User",
  "enabled": true,
  "roles": ["ROLE_USER"],
  "createdAt": "2026-04-28T12:00:00Z"
}
```

#### Common errors

- `401 Unauthorized` — missing or invalid JWT
- `403 Forbidden` — not an admin
- `404 Not Found` — user not found

### `PATCH /api/users/{id}/enabled`

Enables or disables the specified user.

- **Access**: Admin only
- **Success status**: `200 OK`

#### Path parameters

| Name | Type | Required |
| --- | --- | --- |
| `id` | `Long` | Yes |

#### Query parameters

| Name | Type | Required | Notes |
| --- | --- | --- | --- |
| `enabled` | `boolean` | Yes | `true` enables the user, `false` disables the user |

#### Example request

```http
PATCH /api/users/1/enabled?enabled=false
Authorization: Bearer <admin-token>
```

#### Example response

```http
200 OK
```

```json
{
  "id": 1,
  "email": "user@test.com",
  "name": "Test User",
  "enabled": false,
  "roles": ["ROLE_USER"],
  "createdAt": "2026-04-28T12:00:00Z"
}
```

#### Common errors

- `401 Unauthorized` — missing or invalid JWT
- `403 Forbidden` — not an admin
- `404 Not Found` — user not found

## Controller-level validation rules

### Registration

`POST /api/auth/register` validates the request body with Jakarta Validation:

- `email` must be present and valid
- `password` must be present and between 8 and 128 characters
- `name` must be present and at most 255 characters
- malformed JSON also returns `400 Bad Request`

### Login

`POST /api/auth/login` validates:

- `email` must be present and valid
- `password` must be present

## Notes for API consumers

- Use the `token` value from `AuthResponse` exactly as returned in the `Authorization` header for subsequent calls
- The token type is always `Bearer`, so the header format is:

```http
Authorization: Bearer eyJhbGciOi...
```

- `UserResponse.roles` may appear in any order
- Admin-only endpoints require both a valid JWT and a user with `ROLE_ADMIN`
- Public endpoints under `/api/auth/**` can be called without authentication

## Quick endpoint index

| Method | Route | Summary |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Authenticate and receive a JWT |
| `GET` | `/api/users/me` | Get the current authenticated user |
| `GET` | `/api/users/{id}` | Get a user by ID |
| `GET` | `/api/users` | List all users |
| `PUT` | `/api/users/{id}/roles/admin` | Grant admin role |
| `DELETE` | `/api/users/{id}/roles/admin` | Revoke admin role |
| `PATCH` | `/api/users/{id}/enabled` | Enable or disable a user |


