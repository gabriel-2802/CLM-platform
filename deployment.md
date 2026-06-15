# CLM Platform — Deployment

## Overview

The platform is deployed as a single Docker Swarm stack defined in `docker-stack.yml`. One stack file covers both testing and production — the differences between environments are limited to secret values, image tags, and whether swagger-hub is enabled.

| Environment | Stack name | Deploy command |
|---|---|---|
| Testing | `clm-test` | `make swarm-deploy-test` |
| Production | `clm` | `make swarm-deploy-prod` |

---

## Architecture

```
                    ┌──────────────────────────────────────────────────────┐
  Browser / Client  │  HOST                                                │
  ───────────────── │                                                      │
  HTTPS :443  ──────┼──▶  nginx  (clm-edge + clm-backend)                 │
  HTTP  :80   ──────┼──▶  nginx  (redirects → HTTPS)                      │
                    │       │                                              │
                    │       │  clm-backend overlay network                 │
                    │       ├──▶  frontend            :3000  (Next.js)     │
                    │       ├──▶  user-service         :8083  (Spring Boot)│
                    │       ├──▶  contracts            :8081  (Spring Boot)│
                    │       ├──▶  client-service       :8084  (Spring Boot)│
                    │       ├──▶  notifications        :8082  (Spring Boot)│
                    │       ├──▶  negotiation-service  :8085  (Spring Boot)│
                    │       ├──▶  swagger-hub          :8090  (testing only│
                    │       ├──▶  prometheus           :9090               │
                    │       └──▶  grafana              :3000               │
                    │                                                      │
                    │  Dedicated Postgres 16 per service (clm-backend)    │
                    │    postgres           clm_platform  / schema: clm   │
                    │    postgres-users     clm_users     / schema: users  │
                    │    postgres-clients   clm_clients   / schema: clients│
                    │    postgres-negotiations clm_negotiations / schema: n│
                    └──────────────────────────────────────────────────────┘
```

---

## Service coordination

### Swarm as the orchestrator

Docker Swarm manages the full lifecycle of every container in the stack. Key properties:

- **Single `docker stack deploy` command** — Swarm reads `docker-stack.yml`, creates or updates all services, and reconciles desired vs actual state continuously.
- **Restart policy** — all services share the `on-failure / max 3 / delay 5 s` restart policy defined in the `x-restart` YAML anchor. Swarm restarts a crashed container automatically up to three times before giving up.
- **Rolling updates** — `docker service update --image <img> --force <service>` (called by `make swarm-rebuild`) replaces containers one replica at a time, so a service stays available during redeploys.
- **Idempotent deploys** — re-running `docker stack deploy` only touches services whose image digest or config actually changed. Services that are already up and unchanged are left alone.

### Health checks and service readiness

Every service declares a `healthcheck` block. Swarm marks a container unhealthy and restarts it if checks fail continuously. The checks used are:

| Service | Check |
|---|---|
| `postgres*` | `pg_isready` |
| Spring Boot services | `wget --spider http://127.0.0.1:<port>/actuator/health` |
| `frontend` | `wget --spider http://127.0.0.1:3000` |
| `nginx` | `wget -q -O /dev/null http://127.0.0.1:80/health` |
| `prometheus` | `wget --spider http://localhost:9090` |
| `grafana` | `curl -f http://localhost:3000/api/health` |

A `start_period` of 20–60 s is set per service so that slow-starting JVM processes are not restarted before they have had time to initialise.

### Network isolation

Two overlay networks partition the stack:

| Network | Members | Purpose |
|---|---|---|
| `clm-edge` | `nginx` only | Receives traffic from the host's published ports (80, 443) |
| `clm-backend` | Everything | Internal service-to-service communication |

`attachable: false` on both networks means no ad-hoc container can join them at runtime. Nginx sits on both networks and is the only service reachable from outside the host. Backend services have no published ports and are invisible to the outside world.

### Request routing through Nginx

Nginx is the single TLS termination point and reverse proxy. All inter-service calls go through DNS service discovery on `clm-backend` (Docker's built-in overlay DNS resolves service names like `contracts`, `user-service`, etc.).

| Path pattern | Upstream | Auth |
|---|---|---|
| `/api/auth/login`, `/api/auth/register` | `user-service:8083` | Rate-limited, no token required |
| `/api/users/*` | `frontend:3000` | Session cookie validated in Next.js middleware |
| `/api/contracts/*`, `/api/templates/*`, `/api/appendices/*` | `contracts:8081` | Bearer JWT |
| `/api/clients/*`, `/api/enums/*`, `/api/tasks/*` | `client-service:8084` | Bearer JWT |
| `/api/negotiations/*` | `negotiation-service:8085` | Bearer JWT |
| `/api/tasks/generate*` | `frontend:3000` | Basic auth + orchestration across services |
| `/notifications/*` | `notifications:8082` | No auth (internal trigger only) |
| `/docs/*` | `swagger-hub:8090` | Testing only (`SWAGGER_HUB_REPLICAS=0` in prod) |
| `/grafana/*` | `grafana:3000` | — |
| `/*` | `frontend:3000` | Catch-all |

Download endpoints (`/api/(templates|contracts|appendices)/download`) bypass the frontend and proxy directly to the contracts service with a 24 h timeout.

### Authentication flow

```
Browser
  │  POST /api/auth/login ──▶ user-service (validates credentials, issues JWT)
  │  Next.js stores JWT as an HttpOnly session cookie
  │
  │  Subsequent requests ──▶ nginx ──▶ frontend (Next.js middleware validates JWT)
  │                                └──▶ backend services (Bearer header forwarded)
```

`JWT_SECRET` (Spring Boot) and `NEXTAUTH_SECRET` (Next.js) **must be the same value**. Spring validates tokens that Next.js signed. Both are derived from the same Swarm secret (`clm_jwt_secret` / `clm_nextauth_secret`) at container start.

### Database isolation

Each service owns a dedicated Postgres 16 instance. Flyway enforces schema-level isolation on first start:

| Container | Database | Schema | Owner |
|---|---|---|---|
| `postgres` | `clm_platform` | `clm` | `contracts` |
| `postgres-users` | `clm_users` | `users` | `user-service` |
| `postgres-clients` | `clm_clients` | `clients` | `client-service` |
| `postgres-negotiations` | `clm_negotiations` | `negotiations` | `negotiation-service` |

No service can reach another service's database over the network — each Spring Boot service only has the URL of its own instance in `SPRING_DATASOURCE_URL`.

---

## Secrets

### How Swarm stores secrets

Docker Swarm secrets are stored encrypted in the Raft keystore (the distributed consensus database that Swarm manager nodes use internally). They are decrypted in-memory and mounted as read-only files under `/run/secrets/<name>` inside each container that is granted access. The secret value is **never written to disk on the host** and **never appears** in `docker-stack.yml`, environment files, or container inspect output.

### The ten secrets

| Secret name | Contains | Consumed by |
|---|---|---|
| `clm_db_password` | Postgres password shared by all DB instances | All `postgres*` services, all Spring Boot services |
| `clm_jwt_secret` | JWT signing key | `user-service`, `contracts`, `client-service`, `notifications`, `negotiation-service` |
| `clm_nextauth_secret` | NextAuth session key — must equal `clm_jwt_secret` | `frontend` |
| `clm_admin_password` | Initial admin account password | `user-service` |
| `clm_admin_register_code` | Registration code required for admin self-registration | `user-service`, `contracts`, `frontend` |
| `clm_mail_username` | SMTP username | `contracts`, `notifications`, `negotiation-service` |
| `clm_mail_password` | SMTP password | `contracts`, `notifications`, `negotiation-service` |
| `clm_grafana_password` | Grafana admin password | `grafana` |
| `clm_tls_cert` | TLS certificate (PEM) | `nginx` (mounted at `/etc/nginx/certs/clm.crt`) |
| `clm_tls_key` | TLS private key (PEM) | `nginx` (mounted at `/etc/nginx/certs/clm.key`, mode 0400) |

### How secrets are created — `scripts/secrets-init.sh`

The init script is idempotent: it inspects each secret name with `docker secret inspect` and skips any that already exist.

**Testing** — values sourced from `.env.testing` (committed, non-sensitive test values):

```sh
make swarm-secrets-test
# internally: bash scripts/secrets-init.sh testing
```

**Production** — values sourced from `.env.secrets` (never committed, derived from `.env.secrets.example`):

```sh
cp .env.secrets.example .env.secrets
# fill in all values
make swarm-secrets-prod
# internally: bash scripts/secrets-init.sh production
```

TLS certificates are handled differently from string secrets:

- **Testing** — `make certs` generates a self-signed cert with `openssl` and writes it to `nginx/certs/`. The init script creates the `clm_tls_cert` and `clm_tls_key` Swarm secrets directly from those files.
- **Production** — `TLS_CERT_FILE` and `TLS_KEY_FILE` in `.env.secrets` point to your real certificate files. The init script creates the secrets from those paths.

### How secrets reach application processes

**Postgres and Grafana** consume secrets natively. Postgres reads `POSTGRES_PASSWORD_FILE=/run/secrets/clm_db_password`; Grafana reads `GF_SECURITY_ADMIN_PASSWORD__FILE=/run/secrets/clm_grafana_password`. No wrapper script needed.

**Spring Boot services and the Next.js frontend** use a thin `docker-entrypoint.sh` wrapper that runs before the main process:

```sh
# Pattern used in every service entrypoint
_secret() {
  local file="/run/secrets/$2"
  [ -f "$file" ] || return 0
  export "$1=$(cat "$file")"
}

_secret SPRING_DATASOURCE_PASSWORD  clm_db_password
_secret JWT_SECRET                  clm_jwt_secret
# ... etc.

exec "$@"   # hand off to the real process
```

The secret value is exported as an environment variable, then the entrypoint execs the real command (e.g. `java -jar app.jar`). The variable exists only in the process's memory — it is not written to disk and is not visible in `docker inspect`.

### Secret rotation

Swarm secrets are immutable once created. To rotate a secret:

```sh
docker secret rm clm_jwt_secret
make swarm-secrets-test          # or swarm-secrets-prod
make swarm-deploy-test           # redeploy picks up the new secret
# Then restart affected services to reload the new value:
make swarm-restart name=user-service
make swarm-restart name=contracts
# ... repeat for each service that uses the rotated secret
```

---

## Environment variables

Non-secret configuration comes from the env file sourced at deploy time (`.env.testing` or `.env.production`). Key ones:

| Variable | Testing default | Production |
|---|---|---|
| `IMAGE_PREFIX` | _(empty — local images)_ | `registry.example.com/clm/` |
| `IMAGE_TAG` | `local` | `latest` |
| `SWAGGER_HUB_REPLICAS` | `1` | `0` |
| `FRONTEND_URL` | `https://localhost` | `https://your-domain.com` |
| `SPRING_PROFILES_ACTIVE` | `test` | `prod` |
| `DB_USER` | `clm_user` | `clm_user` |

`NEXT_PUBLIC_*` variables are baked into the frontend image at build time via `--build-arg`. Changing them requires a rebuild:

```sh
make swarm-rebuild name=frontend
```

---

## First-time setup

```sh
# 1. Initialise Swarm (once per host)
make swarm-init

# 2. Generate TLS certs (testing only)
make certs
make trust-cert   # optional — silences browser warnings on macOS

# 3. Register Prometheus config as a Docker config object
make swarm-config-create

# 4. Push secrets into the Swarm keystore
make swarm-secrets-test     # testing
# make swarm-secrets-prod   # production (requires .env.secrets)

# 5. Build images
make swarm-build-test       # testing (tag: local)
# Production: set IMAGE_PREFIX/IMAGE_TAG in .env.production, then:
# make swarm-build-prod     # builds + pushes to registry

# 6. Deploy the stack
make swarm-deploy-test
# make swarm-deploy-prod
```

---

## Day-to-day operations

### Rebuild one service after a code change

```sh
make swarm-rebuild name=contracts
```

Builds a new local image and triggers a rolling update in the stack.

### Restart a service without rebuilding

```sh
make swarm-restart name=user-service
```

Forces Swarm to recreate the container — useful after rotating a secret or updating a non-secret env var via redeploy.

### Reload configs without a full restart

```sh
# Nginx — sends SIGHUP, zero downtime
make nginx-reload-swarm

# Prometheus — POST to /-/reload, no data loss
make prometheus-reload-swarm
```

### Observe the stack

```sh
make swarm-ps          # list stacks and replica counts
make swarm-logs        # tail nginx + frontend + contracts + user-service

# Tail a single service
docker service logs -f clm-test_contracts
```

### Database access (testing)

```sh
make swarm-db           # psql → clm_platform
make swarm-db-users     # psql → clm_users
make swarm-db-clients   # psql → clm_clients
```

### Tear down

```sh
make swarm-down-test    # removes stack; volumes are preserved
make swarm-down-prod    # same — prompts for confirmation

# To also delete all data volumes:
docker volume rm clm-test_postgres_data clm-test_postgres_users_data \
  clm-test_postgres_clients_data clm-test_postgres_negotiations_data \
  clm-test_prometheus_data clm-test_grafana_data
```

---

## Monitoring

Prometheus scrapes the `/actuator/prometheus` endpoint on each Spring Boot service (exposed via Actuator, tagged with `application=<service-name>`). Metrics are retained for 15 days in the `prometheus_data` volume. Grafana reads from Prometheus and is available at `https://<host>/grafana/`.

Prometheus configuration is stored as a Docker config object (`clm_prometheus_config`) created from `monitoring/prometheus/prometheus.yml`. To update it after changing the file:

```sh
make swarm-config-update
make swarm-deploy-test   # picks up the new config version
```
