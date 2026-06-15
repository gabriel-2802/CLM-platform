# CLM Platform — File & Directory Architecture

## Root Layout

```
CLM-platform/
├── db/                        # PostgreSQL Docker image & init scripts
├── frontend/                  # Next.js 14 web application
├── nginx/                     # Reverse proxy (TLS termination, routing)
├── monitoring/                # Prometheus + Grafana stack
├── services/                  # Java Spring Boot microservices
│   ├── client-service/
│   ├── contract-service/
│   ├── negotiation-service/
│   ├── notification-service/
│   └── user-service/
├── swagger-hub/               # Aggregated API docs UI (Python + HTML)
├── scripts/                   # Shell utilities (secrets init, etc.)
├── template_examples/         # Sample .docx/.pdf contract templates
├── docker-stack.yml           # Docker Swarm production stack definition
├── docker-compose.testing.yml # Compose file for local/test environment
├── Makefile                   # All dev/deploy commands
├── .env.secrets.example       # Secret env var template
├── .env.production            # Production non-secret env vars
└── .env.testing               # Test environment env vars
```

---

## `db/`

```
db/
├── Dockerfile          # Extends postgres image
├── init-db.sql         # Creates schemas (one per service), roles, grants
├── pg_hba.conf         # Client authentication rules
└── postgresql.conf     # Postgres server config (shared_buffers, etc.)
```

Single PostgreSQL instance; each microservice operates in its own schema for isolation.

---

## `nginx/`

```
nginx/
├── Dockerfile
├── nginx.conf              # Worker config, upstream definitions
├── conf.d/
│   └── clm.conf            # HTTPS server block, location → upstream routing
└── certs/
    ├── clm.crt
    └── clm.key
```

Routes traffic: `/api/users/` → user-service, `/api/clients/` → client-service, `/api/contracts/` → contract-service, `/api/negotiations/` → negotiation-service. Bearer token is forwarded as-is.

---

## `monitoring/`

```
monitoring/
├── prometheus/
│   ├── Dockerfile
│   └── prometheus.yml          # Scrape targets for all services
└── grafana/
    ├── Dockerfile
    ├── datasources.yml          # Prometheus datasource config
    ├── dashboards.yml           # Dashboard provisioning config
    ├── DASHBOARDS.md
    └── dashboards/
        ├── clm-services.json    # Per-service latency/error dashboard
        └── contract-cache.json  # Cache hit rate dashboard
```

---

## `services/` — Microservices

All services are Spring Boot 3 / Java 21, use Flyway for DB migrations, and share a JWT-based auth model (stateless, token validated locally via shared secret).

### Common internal structure (per service)

```
<service>/
├── Dockerfile
├── docker-entrypoint.sh
├── pom.xml
└── src/
    ├── main/
    │   ├── java/clm/<domain>/
    │   │   ├── config/           # Spring beans, security, REST clients
    │   │   ├── controllers/      # @RestController classes
    │   │   ├── dto/
    │   │   │   ├── requests/
    │   │   │   └── responses/
    │   │   ├── exceptions/       # Domain exceptions + GlobalExceptionHandler
    │   │   ├── mappers/          # Entity ↔ DTO mappers
    │   │   ├── models/
    │   │   │   └── enums/
    │   │   ├── repositories/     # Spring Data JPA interfaces
    │   │   ├── security/         # JwtTokenProvider, filter, entry point
    │   │   └── services/         # Business logic
    │   └── resources/
    │       ├── application.yaml
    │       └── db/migration/     # Flyway V*__.sql scripts
    └── test/
        └── java/clm/<domain>/
            ├── controllers/
            ├── services/
            └── repositories/
```

### `user-service`

Manages authentication and user accounts.

| Package | Key classes |
|---|---|
| `controllers` | `AuthController`, `UserController` |
| `services` | `AuthService`, `UserService` |
| `models` | `User`, `Role`, `RoleName` |
| `config` | `SecurityConfig`, `AdminSeeder`, `FlywayConfig` |
| `dto/requests` | `LoginRequest`, `RegisterRequest`, `ResetPasswordRequest`, `UpdateUserRequest` |
| `dto/responses` | `AuthResponse`, `UserResponse` |

Flyway migrations: V1 roles → V2 users → V3 user_roles → V4 seed roles → V5 seed manager role.

### `client-service`

Manages clients, work points, client history, tasks, and user–client assignments.

| Package | Key classes |
|---|---|
| `controllers` | `ClientController`, `WorkPointController`, `DetailsController`, `HistoryController`, `TaskController`, `ClientAssignmentController`, `EnumsController` |
| `services` | `ClientService`, `WorkPointService`, `DetailsService`, `HistoryService`, `TaskService`, `ClientAssignmentService` |
| `models` | `Client`, `WorkPoint`, `ClientDetails`, `ClientHistory`, `Task`, `UserClient` |
| `models/enums` | `CompanyType`, `Administration`, `TaxType`, `TaxFrequency`, `YesNoNa` |

### `contract-service`

Core domain — template parsing, contract generation, appendices, lifecycle events, and document downloads.

| Package | Key classes |
|---|---|
| `controllers` | `ContractController`, `TemplateController`, `AppendixController`, `ReportController` |
| `services` | `ContractService`, `TemplateService`, `AppendixService`, `ReportService`, `NegotiationLifecycleClient` |
| `services/download` | `DocumentDownloadService`, `DocumentProviderRegistry`, provider implementations per document type |
| `listeners` | `ContractActivationListener`, `ContractDeactivationListener` |
| `jobs` | `ContractArchiveJob`, `ContractTerminationJob` |
| `utils/docx` | `DocxFiller`, `DocxNormalizer`, `DocxUtils`, `PlaceholderProcessor` |
| `utils/file` | `FileParser`, `FileUtils` |
| `models` | `Contract`, `ContractDetails`, `DocumentTemplate`, `TemplateField`, `Document`, `DocumentFieldValue`, `Appendix` |
| `models/enums` | `ContractStatus`, `AppendixStatus`, `ContractGenerationStatus`, `DocumentType`, `DocumentFormat`, `DataType`, `DataTransformation`, `MappingStatus` |
| `events` | `ContractActivatedEvent`, `ContractDeactivatedEvent` |

Flyway migrations: V1 initial schema → … → V10+.

### `negotiation-service`

Manages negotiation lifecycle and triggers contract state changes via HTTP.

| Package | Key classes |
|---|---|
| `controllers` | `NegotiationController`, `ContractLifecycleController` |
| `services` | `NegotiationService`, `ContractLifecycleService`, `ContractApiClient`, `NegotiationEmailService`, `NegotiationNotificationService` |
| `models` | `Negotiation`, `TerminatedContract` |
| `models/enums` | `NegotiationStatus` |

Flyway migrations: V1 init → V2 status varchar → V3 created_by → V4 draft→sent → V5 terminated contracts.

### `notification-service`

Scheduled email notifications for contracts nearing expiry; no persistent DB.

| Package | Key classes |
|---|---|
| `controllers` | `NotificationController` |
| `services` | `NotificationService`, `ContractApiService`, `EmailService`, `ICalendarService` |
| `jobs` | `ContractNotificationJob` |
| `dto` | `ContractSummaryDTO`, `ContractFieldValueDTO` |

---

## `frontend/`

Next.js 14 App Router application. Auth via NextAuth.js with JWT sessions.

```
frontend/
├── app/                        # App Router pages and API routes
│   ├── (auth)/
│   │   ├── signin/page.tsx
│   │   └── register/page.tsx
│   ├── api/                    # Next.js Route Handlers (BFF layer)
│   │   ├── auth/[...nextauth]/
│   │   ├── register/
│   │   ├── users/[id]/
│   │   ├── clients-list/
│   │   ├── contracts/download/[id]/
│   │   ├── appendices/download/[id]/
│   │   ├── templates/download/[id]/{pdf,}/
│   │   └── tasks/generate{,Conditional,WithRules}/
│   ├── dashboard/page.tsx
│   ├── clients/
│   │   ├── page.tsx            # Client list
│   │   ├── new/page.tsx
│   │   ├── edit/[id]/page.tsx
│   │   ├── activi/page.tsx
│   │   ├── fosti/page.tsx
│   │   ├── documente/page.tsx
│   │   └── rapoarte/page.tsx
│   ├── contracts/page.tsx
│   ├── contract-templates/
│   │   ├── page.tsx
│   │   └── template-mapping-modal.tsx
│   ├── situatie/page.tsx
│   ├── taskuri/
│   │   ├── page.tsx
│   │   └── edit/[id]/page.tsx
│   ├── users/page.tsx
│   ├── layout.tsx              # Root layout (sidebar, auth wrapper)
│   ├── globals.css
│   └── page.tsx                # Root redirect
│
├── actions/                    # Server Actions (data fetching / mutations)
│   ├── clients.ts
│   ├── client-detalii.ts
│   ├── client-istoric.ts
│   ├── client-punct.ts
│   ├── client-users.ts
│   ├── contracts.ts
│   ├── contract-rows.ts
│   ├── contract-templates.ts
│   ├── appendices.ts
│   ├── negotiations.ts
│   ├── situatie.ts
│   └── tasks.ts
│
├── components/
│   ├── ui/                     # shadcn/ui primitives (button, dialog, table, etc.)
│   ├── auth/
│   │   └── auth-provider.tsx
│   ├── clients/                # Client-domain components
│   │   ├── clients-table.tsx
│   │   ├── client-form.tsx
│   │   ├── client-detalii-form.tsx
│   │   ├── client-punct-form.tsx / panels / table
│   │   ├── client-istoric-form.tsx / panels
│   │   ├── client-users-panel.tsx
│   │   ├── generate-contract-modal.tsx
│   │   ├── negocieri-dialog.tsx
│   │   ├── contract-audit-dialog.tsx
│   │   └── acte-aditionale-dialog.tsx
│   ├── contracts/
│   │   └── new-contract-modal.tsx
│   ├── situatie/
│   │   ├── situatie-table.tsx
│   │   ├── situatie-row.tsx
│   │   └── firma-autocomplete.tsx
│   ├── tasks/
│   │   ├── tasks-table.tsx
│   │   ├── task-form.tsx
│   │   └── task-row.tsx
│   ├── dashboard/
│   │   └── full-data-table.tsx
│   ├── app-sidebar.tsx
│   ├── site-header.tsx
│   ├── nav-main.tsx / nav-user.tsx / nav-documents.tsx / nav-projects.tsx / nav-secondary.tsx
│   ├── data-table.tsx          # Generic TanStack Table wrapper
│   ├── month-picker.tsx
│   ├── conditional-shell.tsx
│   ├── authenticated-download-link.tsx
│   └── chart-area-interactive.tsx
│
├── Dockerfile
├── docker-entrypoint.sh
├── components.json             # shadcn/ui config
├── next.config.ts
├── tailwind.config.ts
├── tsconfig.json
└── package.json
```

---

## `swagger-hub/`

```
swagger-hub/
├── Dockerfile
├── server.py       # Python HTTP server that aggregates OpenAPI specs from all services
└── index.html      # Swagger UI wrapper
```

---

## `scripts/`

```
scripts/
└── secrets-init.sh     # Creates Docker Swarm secrets from .env.secrets
```

---

## Infrastructure Files

| File | Purpose |
|---|---|
| `docker-stack.yml` | Docker Swarm production stack (all services, replicas, secrets, networks) |
| `docker-compose.testing.yml` | Local multi-container environment for integration tests |
| `Makefile` | Dev commands: `test-up`, `swarm-deploy-prod`, `nginx-reload`, `db-*`, etc. |
| `.env.secrets.example` | Template for credentials (DB passwords, JWT secret, SMTP, etc.) |
| `.env.production` | Non-secret production env vars (image tags, ports) |
| `.env.testing` | Non-secret test env vars |
