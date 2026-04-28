# CLM Platform — Testing Stack Reference

## Quick Start

```bash
# First time / clean slate
make test          # build images -> start all services

# After first start, initialize the database
make test-init     # push Prisma schema + seed admin user
                   # creates: admin@example.com / Admin123!
```

---

## Make Commands

| Command | What it does |
|---|---|
| `make test` | Build Docker images -> start all services |
| `make test-up` | Start stack (images must already exist) |
| `make test-down` | Stop and remove containers |
| `make test-restart` | Restart all running containers |
| `make test-rebuild` | Stop -> rebuild images -> start |
| `make test-logs` | Follow logs for all services |
| `make test-ps` | Show container status and health |
| `make test-init` | Push Prisma schema + seed (run once after first `make test`) |
| `make db-test` | Open `psql` shell in the test postgres container |
| `make nuke-test` | Stop stack and **delete database volume** (data loss — prompts confirmation) |

### Selective Rebuild (single service)

```bash
docker compose -p clm-test -f docker-compose.testing.yml --env-file .env.testing \
  up -d --build <service>

# e.g.
# ... --build contracts
# ... --build notifications
# ... --build client
```

Docker layer caching means only changed layers are rebuilt — unchanged services are left running.

---

## Service Endpoints (host-accessible)

| Service | URL | Notes |
|---|---|---|
| Client (Next.js) | http://localhost:3000 | Hot-reload dev server |
| Contracts API | http://localhost:8081 | Swagger UI at `/swagger-ui.html` |
| Notifications API | http://localhost:8082 | |
| PostgreSQL | `localhost:5433` | user: `clm_user` / db: `clm_platform` |

---

## Required `.env.testing` Variables

```bash
# Database
DB_USER=clm_user
DB_PASSWORD=<required>
DB_NAME=clm_platform
DB_EXPOSE_PORT=5433          # host port mapped to postgres (default: 5433)

# JWT
JWT_SECRET=<required>        # min 32 characters

# NextAuth
NEXTAUTH_SECRET=<required>

# Admin
ADMIN_REGISTER_CODE=devcode123   # default — override if needed

# Mail  (Mailtrap sandbox recommended for testing)
MAIL_HOST=sandbox.smtp.mailtrap.io
MAIL_PORT=2525
MAIL_USERNAME=<your-mailtrap-username>
MAIL_PASSWORD=<your-mailtrap-password>
MAIL_FROM=noreply@dev.example.com

# Service ports (all optional — defaults shown)
CONTRACTS_PORT=8081
NOTIFICATIONS_PORT=8082
CLIENT_PORT=3000

# URLs
FRONTEND_URL=http://localhost:3000
NEXT_PUBLIC_CONTRACTS_API_URL=http://localhost:8081
NEXT_PUBLIC_NOTIFICATIONS_API_URL=http://localhost:8082
CONTRACTS_SERVICE_URL=http://contracts:8081
NOTIFICATIONS_SERVICE_URL=http://notifications:8082

# Spring / misc (optional)
SPRING_PROFILES_ACTIVE=test
JWT_EXPIRATION=2592000000        # 30 days in ms
EMAIL_VERIFICATION_EXPIRATION=86400000
LIBREOFFICE_PATH=/usr/bin/libreoffice
```

---

## Service Configuration Reference

### postgres

| Setting | Value |
|---|---|
| Container | `clm-postgres-test` |
| Image | Built from `db.Dockerfile` |
| Internal port | `5432` |
| Host port | `${DB_EXPOSE_PORT:-5433}` |
| Networks | `data-net` only |
| Init script | `./scripts/init-db.sql` (schema + role grants) |
| Volume | `postgres_data` |
| Restart | `on-failure:3` |
| Healthcheck | `pg_isready` every 15s |

### contracts

| Setting | Value |
|---|---|
| Container | `clm-contracts-test` |
| Build context | `contracts/` |
| Port | `${CONTRACTS_PORT:-8081}` |
| Networks | `dmz-net` + `data-net` |
| Depends on | `postgres` (healthy) |
| Spring profile | `test` |
| DDL auto | `validate` — Flyway owns schema |
| Flyway schema | `clm` |
| Log level (app) | `DEBUG` on `clm.demo` |
| Restart | `on-failure:3` |
| Healthcheck | `nc -z localhost 8081` every 15s, 40s start period |

### notifications

| Setting | Value |
|---|---|
| Container | `clm-notifications-test` |
| Build context | `notifications/` |
| Port | `${NOTIFICATIONS_PORT:-8082}` |
| Networks | `dmz-net` only (no direct DB access) |
| Depends on | `postgres` (healthy) |
| Spring profile | `test` |
| Log level (app) | `DEBUG` on `clm.notifications` |
| Restart | `on-failure:3` |
| Healthcheck | `nc -z localhost 8082` every 15s, 40s start period |

### client

| Setting | Value |
|---|---|
| Container | `clm-client-test` |
| Build context | `general/` |
| Build target | `development` (hot-reload) |
| Port | `${CLIENT_PORT:-3000}` |
| Networks | `dmz-net` + `data-net` |
| Depends on | `contracts` + `notifications` (both healthy) |
| Database | Prisma -> `?schema=general` (isolated from Flyway history) |
| Restart | `on-failure:3` |
| Healthcheck | `wget --spider http://127.0.0.1:3000` every 15s, 60s start period |

---

## Network Layout

```
Browser / Host
      │
      ├── :3000  -> client      (dmz-net + data-net)
      ├── :8081  -> contracts   (dmz-net + data-net)
      ├── :8082  -> notifications (dmz-net)
      └── :5433  -> postgres    (data-net only)

dmz-net:   client ↔ contracts ↔ notifications
data-net:  contracts -> postgres
           client (Prisma) -> postgres

postgres is never on dmz-net.
notifications has no direct database access.
```

---
