# client-management-service

Spring Boot microservice owning all client data. Replaces direct Prisma access to
`Client`, `Detalii`, `PunctDeLucru`, `Istoric`, and `UserClient` in main-service.
Tasks, Rules, and ContractTemplates stay in main-service (or their own services later).

---

## Database

- **Database:** `clm_platform` (shared postgres instance, same as current main-service DB)
- **Schema:** `clients`
- **Migrations:** Flyway (`classpath:db/migration`)
- **Auth:** JWT validated from shared `JWT_SECRET` (same as user-service and contracts)

---

## Domain Model

### Enums

```
Tip               → SRL | PFA | II | ASOC | SA | SPARL
DaNuNuECazul      → DA | NU | NU_E_CAZUL
Impozit           → MICRO_1 | MICRO_3 | PROFIT
DaLunarTrim       → DA_LUNAR | DA_TRIM | NU
Administratie     → (all existing values — DGRF_BUCURESTI … UFO_SANNICOLAU_MARE)
```

### clients.clients

| Column                  | Type        | Constraints          |
|-------------------------|-------------|----------------------|
| id                      | BIGSERIAL   | PK                   |
| denumire                | VARCHAR     | NOT NULL             |
| tip                     | VARCHAR     | NOT NULL (enum)      |
| cui                     | VARCHAR     | NOT NULL, UNIQUE     |
| activa                  | BOOLEAN     | NOT NULL             |
| data_verificarii        | TIMESTAMP   |                      |
| adresa                  | VARCHAR     |                      |
| administratie           | VARCHAR     | NOT NULL (enum)      |
| impozit                 | VARCHAR     |                      |
| platitor_tva            | VARCHAR     | NOT NULL (enum)      |
| tva_la_incasare         | BOOLEAN     |                      |
| are_cod_tva_ue          | BOOLEAN     |                      |
| cod_tva_ue              | VARCHAR     |                      |
| operatiune_ue           | BOOLEAN     |                      |
| dividende               | BOOLEAN     |                      |
| salariati               | VARCHAR     |                      |
| casa_de_marcat          | BOOLEAN     |                      |
| data_exp_sediu_social   | TIMESTAMP   |                      |
| data_exp_mandat_admin   | TIMESTAMP   |                      |
| data_certificat_fiscal  | TIMESTAMP   |                      |
| data_fisa_platitor      | TIMESTAMP   |                      |
| data_vect_fiscal        | TIMESTAMP   |                      |
| created_at              | TIMESTAMP   | NOT NULL DEFAULT NOW |
| updated_at              | TIMESTAMP   | NOT NULL             |

### clients.detalii

| Column                          | Type      | Constraints             |
|---------------------------------|-----------|-------------------------|
| id                              | BIGSERIAL | PK                      |
| client_id                       | BIGINT    | NOT NULL, UNIQUE, FK    |
| registru_uc                     | BOOLEAN   | NOT NULL                |
| registru_ev_fiscala             | VARCHAR   | NOT NULL (enum)         |
| of_spalare_bani                 | BOOLEAN   | NOT NULL                |
| regulament_ordine_interioara    | BOOLEAN   | NOT NULL                |
| manual_politici_contabile       | BOOLEAN   | NOT NULL                |
| adresa_revisal                  | BOOLEAN   | NOT NULL                |
| parola_itm                      | VARCHAR   |                         |
| depunere_declaratii_online      | BOOLEAN   | NOT NULL                |
| acces_dosar_fiscal              | VARCHAR   | NOT NULL (enum)         |

### clients.puncte_de_lucru

| Column        | Type      | Constraints          |
|---------------|-----------|----------------------|
| id            | BIGSERIAL | PK                   |
| client_id     | BIGINT    | NOT NULL, FK         |
| denumire      | VARCHAR   | NOT NULL             |
| de_la         | TIMESTAMP | NOT NULL             |
| pana_la       | TIMESTAMP |                      |
| administratie | VARCHAR   | NOT NULL (enum)      |
| registru_uc   | BOOLEAN   | NOT NULL             |
| salariati     | INT       | NOT NULL             |
| cui           | VARCHAR   |                      |
| casa_de_marcat| BOOLEAN   | NOT NULL             |

### clients.istorice

| Column         | Type      | Constraints                  |
|----------------|-----------|------------------------------|
| id             | BIGSERIAL | PK                           |
| client_id      | BIGINT    | NOT NULL, FK                 |
| anul           | INT       | NOT NULL                     |
| cifra_afaceri  | DOUBLE    | NOT NULL                     |
| inventar       | BOOLEAN   | NOT NULL                     |
| bilant_sem_iun | VARCHAR   | NOT NULL (enum)              |
| bilant_anual   | VARCHAR   | NOT NULL (enum)              |
|                |           | UNIQUE (client_id, anul)     |

### clients.user_clients

| Column    | Type      | Constraints              |
|-----------|-----------|--------------------------|
| id        | BIGSERIAL | PK                       |
| user_id   | BIGINT    | NOT NULL                 |
| client_id | BIGINT    | NOT NULL, FK             |
|           |           | UNIQUE (user_id, client_id) |

> `user_id` is a plain integer reference to user-service — no FK constraint.

---

## Flyway Migrations

```
V1__create_enums.sql          → all enum types
V2__create_clients.sql        → clients table
V3__create_detalii.sql        → detalii table
V4__create_puncte_de_lucru.sql
V5__create_istorice.sql
V6__create_user_clients.sql
```

---

## Security

JWTs are **issued exclusively by user-service**. This service only parses and verifies them — it never generates tokens.

The `JwtAuthenticationFilter` must:
1. Extract the `Bearer` token from `Authorization` header
2. Verify the signature using the shared `JWT_SECRET`
3. Build `UsernamePasswordAuthenticationToken` directly from the `roles` claim in the payload — **no database lookup**

All endpoints require a valid JWT except health/actuator.
Role checks via `@PreAuthorize`:

- `ROLE_ADMIN` or `ROLE_MANAGER` — write operations
- `ROLE_USER` — read-only (own assigned clients only, enforced via user_clients)
- `ROLE_ADMIN` — delete, bulk operations, user-client assignment

---

## Endpoints

### Clients — `/api/clients`

| Method | Path               | Role                    | Description                          |
|--------|--------------------|-------------------------|--------------------------------------|
| GET    | `/api/clients`     | USER / MANAGER / ADMIN  | List clients. USER sees only assigned ones. Supports `?activa=true\|false` filter. |
| GET    | `/api/clients/{id}`| USER / MANAGER / ADMIN  | Get client by id. USER must be assigned. |
| POST   | `/api/clients`     | MANAGER / ADMIN         | Create client                        |
| PUT    | `/api/clients/{id}`| MANAGER / ADMIN         | Full update                          |
| PATCH  | `/api/clients/{id}`| MANAGER / ADMIN         | Partial update                       |
| DELETE | `/api/clients/{id}`| ADMIN                   | Delete client (cascades all sub-entities) |

#### GET `/api/clients` query params

| Param   | Type    | Description                         |
|---------|---------|-------------------------------------|
| activa  | boolean | Filter by active/inactive           |
| tip     | string  | Filter by Tip enum                  |
| userId  | long    | Filter by assigned user (ADMIN only)|
| page    | int     | Page index (default 0)              |
| size    | int     | Page size (default 50)              |

#### ClientResponse

```json
{
  "id": 1,
  "denumire": "Firma SRL",
  "tip": "SRL",
  "cui": "RO12345678",
  "activa": true,
  "dataVerificarii": "2025-01-15T00:00:00Z",
  "adresa": "Str. Exemplu nr. 1",
  "administratie": "DGRF_BUCURESTI",
  "impozit": "MICRO_1",
  "platitorTVA": "DA_LUNAR",
  "tvaLaIncasare": false,
  "areCodTVAUE": false,
  "codTVAUE": null,
  "operatiuneUE": false,
  "dividende": false,
  "salariati": "NU",
  "casaDeMarcat": false,
  "dataExpSediuSocial": null,
  "dataExpMandatAdmin": null,
  "dataCertificatFiscal": null,
  "dataFisaPlatitor": null,
  "dataVectFiscal": null,
  "createdAt": "2025-01-01T00:00:00Z",
  "updatedAt": "2025-01-01T00:00:00Z"
}
```

---

### Detalii — `/api/clients/{clientId}/detalii`

| Method | Path                            | Role                   | Description                   |
|--------|---------------------------------|------------------------|-------------------------------|
| GET    | `/api/clients/{clientId}/detalii` | USER / MANAGER / ADMIN | Get detalii for client       |
| PUT    | `/api/clients/{clientId}/detalii` | MANAGER / ADMIN        | Create or replace detalii    |
| PATCH  | `/api/clients/{clientId}/detalii` | MANAGER / ADMIN        | Partial update               |

#### DetaliiResponse

```json
{
  "id": 1,
  "clientId": 1,
  "registruUC": true,
  "registruEvFiscala": "DA",
  "ofSpalareBani": false,
  "regulamentOrdineInterioara": true,
  "manualPoliticiContabile": true,
  "adresaRevisal": false,
  "parolaITM": null,
  "depunereDeclaratiiOnline": true,
  "accesDosarFiscal": "DA"
}
```

---

### Puncte de Lucru — `/api/clients/{clientId}/puncte-de-lucru`

| Method | Path                                          | Role            | Description          |
|--------|-----------------------------------------------|-----------------|----------------------|
| GET    | `/api/clients/{clientId}/puncte-de-lucru`     | USER+           | List all             |
| GET    | `/api/clients/{clientId}/puncte-de-lucru/{id}`| USER+           | Get one              |
| POST   | `/api/clients/{clientId}/puncte-de-lucru`     | MANAGER / ADMIN | Create               |
| PUT    | `/api/clients/{clientId}/puncte-de-lucru/{id}`| MANAGER / ADMIN | Update               |
| DELETE | `/api/clients/{clientId}/puncte-de-lucru/{id}`| ADMIN           | Delete               |

#### PunctDeLucruResponse

```json
{
  "id": 1,
  "clientId": 1,
  "denumire": "Punct Cluj",
  "deLa": "2023-01-01T00:00:00Z",
  "panaLa": null,
  "administratie": "AJFP_CLUJ",
  "registruUC": false,
  "salariati": 3,
  "cui": null,
  "casaDeMarcat": true
}
```

---

### Istorice — `/api/clients/{clientId}/istorice`

| Method | Path                                        | Role            | Description          |
|--------|---------------------------------------------|-----------------|----------------------|
| GET    | `/api/clients/{clientId}/istorice`          | USER+           | List all years       |
| GET    | `/api/clients/{clientId}/istorice/{anul}`   | USER+           | Get one year         |
| PUT    | `/api/clients/{clientId}/istorice/{anul}`   | MANAGER / ADMIN | Create or replace    |
| DELETE | `/api/clients/{clientId}/istorice/{anul}`   | ADMIN           | Delete one year      |

#### IstoricResponse

```json
{
  "id": 1,
  "clientId": 1,
  "anul": 2024,
  "cifraAfaceri": 150000.00,
  "inventar": false,
  "bilantSemIun": "NU_E_CAZUL",
  "bilantAnual": "DA"
}
```

---

### User–Client Assignments — `/api/clients/{clientId}/users`

| Method | Path                                   | Role  | Description                      |
|--------|----------------------------------------|-------|----------------------------------|
| GET    | `/api/clients/{clientId}/users`        | ADMIN | List user IDs assigned to client |
| PUT    | `/api/clients/{clientId}/users`        | ADMIN | Replace full assignment list     |
| POST   | `/api/clients/{clientId}/users/{userId}` | ADMIN | Assign user to client          |
| DELETE | `/api/clients/{clientId}/users/{userId}` | ADMIN | Remove user from client        |

Also supports the reverse lookup:

| Method | Path                          | Role  | Description                        |
|--------|-------------------------------|-------|------------------------------------|
| GET    | `/api/users/{userId}/clients` | ADMIN | List client IDs assigned to a user |

#### AssignmentResponse

```json
{
  "clientId": 1,
  "userIds": [2, 5, 7]
}
```

---

## Environment Variables

```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/clm_platform
SPRING_DATASOURCE_USERNAME=clm_user
SPRING_DATASOURCE_PASSWORD=...
SPRING_JPA_PROPERTIES_HIBERNATE_DEFAULT_SCHEMA=clients
SPRING_FLYWAY_SCHEMAS=clients
JWT_SECRET=...            # shared with user-service and contracts
SERVER_PORT=8084
```

---

## Package Structure

```
clm.client.demo
├── config/
│   └── SecurityConfig.java
├── controllers/
│   ├── ClientController.java
│   ├── DetaliiController.java
│   ├── PunctDeLucruController.java
│   ├── IstoricController.java
│   └── UserClientController.java
├── dto/
│   ├── requests/
│   └── responses/
├── models/
│   ├── Client.java
│   ├── Detalii.java
│   ├── PunctDeLucru.java
│   ├── Istoric.java
│   └── UserClient.java
├── repositories/
├── services/
├── security/
│   ├── JwtTokenProvider.java          ← verify + parse only, no generateToken
│   └── JwtAuthenticationFilter.java   ← builds auth from claims, no DB lookup
└── exceptions/
```

---

## Notes

- JWTs are generated solely by user-service. `JwtTokenProvider` here only exposes `getClaims(token)` / `validateToken(token)` — no `generateToken` method.
- `JwtAuthenticationFilter` builds authorities from the `roles` claim directly — no `UserDetailsService`, no DB lookup per request.
- `UserClient.userId` is a plain `BIGINT` with no FK — user-service owns user identity.
- On client delete, cascade removes detalii, puncte-de-lucru, istorice, and user_clients.
- main-service will call this service for all client reads/writes, replacing direct Prisma queries in `actions/clients.ts`, `actions/client-detalii.ts`, `actions/client-punct.ts`, `actions/client-istoric.ts`, and `actions/client-users.ts`.
