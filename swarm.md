# CLM Platform — Docker Swarm

Single `docker-stack.yml` for both testing and production.  
Only secrets and replica counts differ between environments.  
Swagger-hub is disabled in production (`SWAGGER_HUB_REPLICAS=0`).

---

## Architecture

```
                        ┌─────────────────────────────────────────────────────┐
  Browser / Client      │  HOST                                               │
  ─────────────────     │                                                     │
  HTTPS :443  ──────────┼──▶  nginx  (clm-edge + clm-backend)                │
  HTTP  :80   ──────────┼──▶  nginx  (redirect → HTTPS)                      │
                        │       │                                             │
                        │       │  clm-backend overlay network               │
                        │       ├──▶  frontend       :3000  (Next.js)        │
                        │       ├──▶  user-service   :8083  (Spring Boot)    │
                        │       ├──▶  contracts      :8081  (Spring Boot)    │
                        │       ├──▶  client-service :8084  (Spring Boot)    │
                        │       ├──▶  notifications  :8082  (Spring Boot)    │
                        │       ├──▶  negotiation-service :8085 (Spring Boot)│
                        │       ├──▶  swagger-hub    :8090  (testing only)   │
                        │       └──▶  grafana        :3000                   │
                        │                                                     │
                        └─────────────────────────────────────────────────────┘
```

### Services

| Service | Port | Tech | Description |
|---|---|---|---|
| `nginx` | 80, 443 | nginx 1.27 | TLS termination, reverse proxy, rate limiting |
| `frontend` | 3000 | Next.js 20 (standalone) | UI + server-side API routes |
| `user-service` | 8083 | Spring Boot 21 | Auth, JWT issuance, user management |
| `contracts` | 8081 | Spring Boot 21 + LibreOffice | Contract lifecycle, DOCX→PDF rendering |
| `client-service` | 8084 | Spring Boot 21 | Client records, enums, task definitions |
| `notifications` | 8082 | Spring Boot 25 | Email dispatch |
| `negotiation-service` | 8085 | Spring Boot 21 | Negotiation workflows, scheduled reminders |
| `swagger-hub` | 8090 | Python 3.13 | Aggregated API docs (testing only) |
| `prometheus` | 9090 | prom/prometheus | Metrics scraping (internal only) |
| `grafana` | 3000 | Grafana 10.4 | Dashboards at `/grafana/` |

### Databases

Each service owns a dedicated Postgres 16 instance. Schema isolation is enforced by Flyway.

| Container | Database | Schema | Owned by |
|---|---|---|---|
| `postgres` | `clm_platform` | `clm` | contracts |
| `postgres-users` | `clm_users` | `users` | user-service |
| `postgres-clients` | `clm_clients` | `clients` | client-service |
| `postgres-negotiations` | `clm_negotiations` | `negotiations` | negotiation-service |

### Request routing (nginx)

| Path | Upstream | Notes |
|---|---|---|
| `/api/auth/login`, `/api/auth/register` | `user-service:8083` | Rate-limited, direct to service |
| `/api/users/*` | `frontend:3000` | Admin-only, session-cookie auth in Next.js |
| `/api/contracts/*`, `/api/templates/*`, `/api/appendices/*` | `contracts:8081` | Bearer token |
| `/api/clients/*`, `/api/enums/*`, `/api/tasks/*` | `client-service:8084` | Bearer token |
| `/api/negotiations/*` | `negotiation-service:8085` | Bearer token |
| `/api/tasks/generate*` | `frontend:3000` | Basic auth + multi-service orchestration |
| `/notifications/*` | `notifications:8082` | No auth (internal trigger) |
| `/docs/*` | `swagger-hub:8090` | Testing only |
| `/grafana/*` | `grafana:3000` | |
| `/*` | `frontend:3000` | Catch-all |

Download endpoints (`/api/(templates\|contracts\|appendices)/download`) bypass the frontend and go directly to the contracts service with a 24 h proxy timeout.

### Auth flow

```
Browser
  │  POST /api/auth/login  ──▶  user-service  (issues JWT)
  │  JWT stored as HttpOnly cookie by Next.js
  │
  │  Subsequent requests  ──▶  nginx  ──▶  frontend (Next.js middleware validates JWT)
  │                                    └──▶  backend services (Bearer header forwarded)
```

`JWT_SECRET` and `NEXTAUTH_SECRET` must be the same value — Next.js signs session tokens with `NEXTAUTH_SECRET` and Spring validates them with `JWT_SECRET`. Both are loaded from their respective Swarm secrets at container start via `docker-entrypoint.sh`.

### Secret injection

Postgres and Grafana read secrets natively via `*_FILE` env vars. All Spring Boot services and the Next.js frontend use a `docker-entrypoint.sh` wrapper that reads `/run/secrets/*` files into environment variables before the process starts, so no application code changes are needed.

---

## Networks & secrets model

| Network | Who lives here |
|---|---|
| `clm-edge` | Nginx only — the external-facing tier |
| `clm-backend` | All services + Nginx — internal only |

Secrets are stored in the Swarm raft keystore and injected as files under `/run/secrets/` inside containers. No secret values appear in `docker-stack.yml` or any env file.

| Secret | Used by |
|---|---|
| `clm_db_password` | All Postgres instances + Spring services |
| `clm_jwt_secret` | user-service, contracts, client-service, notifications, negotiation-service |
| `clm_nextauth_secret` | frontend (must equal `clm_jwt_secret`) |
| `clm_admin_password` | user-service |
| `clm_admin_register_code` | user-service, contracts, frontend |
| `clm_mail_username` | contracts, notifications, negotiation-service |
| `clm_mail_password` | contracts, notifications, negotiation-service |
| `clm_grafana_password` | grafana |
| `clm_tls_cert` | nginx (mounted at `/etc/nginx/certs/clm.crt`) |
| `clm_tls_key` | nginx (mounted at `/etc/nginx/certs/clm.key`) |

---

## First-time setup

### 1 — Initialise Swarm

```sh
make swarm-init
```

### 2 — TLS certificates (testing only)

```sh
make certs
# Optional: trust the cert in your browser
make trust-cert
```

For production, place your real cert and key paths in `.env.secrets` (`TLS_CERT_FILE` / `TLS_KEY_FILE`).

### 3 — Create the Prometheus config

```sh
make swarm-config-create
```

This registers `monitoring/prometheus/prometheus.yml` as a Docker config named `clm_prometheus_config`.

### 4 — Create secrets

**Testing** — reads values from `.env.testing`:

```sh
make swarm-secrets-test
```

**Production** — reads values from `.env.secrets` (never committed):

```sh
cp .env.secrets.example .env.secrets
# fill in all values
make swarm-secrets-prod
```

Secrets are idempotent — existing ones are skipped. To rotate a secret:

```sh
docker secret rm clm_jwt_secret
make swarm-secrets-test   # or prod
make swarm-deploy-test    # pick up the new secret
```

### 5 — Build images

**Testing** (builds locally, tag: `local`):

```sh
make swarm-build-test
```

**Production** — build and push to your registry. Set `IMAGE_PREFIX` and `IMAGE_TAG` in `.env.production` first:

```sh
make swarm-build-prod
```

### 6 — Deploy

```sh
make swarm-deploy-test    # stack name: clm-test
make swarm-deploy-prod    # stack name: clm
```

`docker stack deploy` is idempotent — re-running it only updates services whose definition changed.

---

## Day-to-day

### Rebuild one service after a code change

```sh
make swarm-rebuild name=contracts
```

Rebuilds the image locally and does a rolling update in the stack. Available service names:

```
postgres  user-service  contracts  client-service  notifications
negotiation-service  swagger-hub  frontend  nginx  grafana
```

### Restart a service without rebuilding

```sh
make swarm-restart name=contracts
```

Useful after rotating a secret or changing an env var — redeploy the stack and then restart the affected service.

### Redeploy the whole stack

```sh
make swarm-deploy-test
```

Only services whose image or config actually changed will be restarted.

### Nginx config changed

```sh
make swarm-rebuild name=nginx
# or hot-reload without a full rebuild:
make nginx-reload-swarm
```

### Prometheus config changed

```sh
make swarm-config-update    # recreates clm_prometheus_config
make swarm-deploy-test      # picks up the new config
# or hot-reload:
make prometheus-reload-swarm
```

---

## Observe

```sh
make swarm-ps          # list stacks and services with replica counts
make swarm-logs        # follow logs (nginx + frontend + contracts + user-service)
```

Tail a specific service:

```sh
docker service logs -f clm-test_contracts
```

---

## Database access (testing)

```sh
make swarm-db           # psql → clm_platform  (main DB)
make swarm-db-users     # psql → clm_users
make swarm-db-clients   # psql → clm_clients
```

---

## Tear down

```sh
make swarm-down-test    # removes stack, volumes are preserved
make swarm-down-prod    # same — prompts for confirmation
```

To also delete volumes (destroys all data):

```sh
docker volume rm clm-test_postgres_data clm-test_postgres_users_data \
  clm-test_postgres_clients_data clm-test_postgres_negotiations_data \
  clm-test_prometheus_data clm-test_grafana_data
```

---

## Environment variables

Non-secret values come from the env file passed at deploy time. Key ones:

| Variable | Testing | Production |
|---|---|---|
| `IMAGE_PREFIX` | _(empty — local images)_ | `registry.example.com/clm/` |
| `IMAGE_TAG` | `local` | `latest` |
| `SWAGGER_HUB_REPLICAS` | `1` | `0` |
| `FRONTEND_URL` | `https://localhost` | `https://your-domain.com` |
| `SPRING_PROFILES_ACTIVE` | `test` | `prod` |

`NEXT_PUBLIC_*` variables are baked into the frontend image at build time — changing them requires a rebuild (`make swarm-rebuild name=frontend`).

---

## Cheatsheet

```sh
# First time
make swarm-init && make certs && make swarm-config-create
make swarm-secrets-test && make swarm-build-test && make swarm-deploy-test

# Changed contracts service
make swarm-rebuild name=contracts

# Restart any service
make swarm-restart name=user-service

# Check status
make swarm-ps

# Tear down
make swarm-down-test
```
