# CLM Platform - Setup & Make Commands Guide

Complete guide for setting up and managing the CLM (Contabilitate Ledger Management) Platform using Make commands.

## Quick Start

For a new developer setup, use these two commands:

```bash
make docker-up     # Start PostgreSQL in Docker
make setup        # Install dependencies, migrate, and seed database
make dev          # Start development server
```

Then open [http://localhost:3000](http://localhost:3000) in your browser.

---

## Table of Contents

1. [Initial Setup](#initial-setup)
2. [Docker Targets](#docker-targets)
3. [Database Commands](#database-commands)
4. [Development Workflow](#development-workflow)
5. [Production Deployment](#production-deployment)
6. [Cleanup & Reset](#cleanup--reset)
7. [Troubleshooting](#troubleshooting)
8. [Environment Variables](#environment-variables)

---

## Initial Setup

### `make setup`
**Full initial setup in one command**

Runs: `install` → `setup-db` → `migrate` → `seed`

```bash
make setup
```

**What it does:**
- Installs npm dependencies from `general/package.json`
- Creates PostgreSQL database and schemas
- Runs all Prisma migrations (16 migrations)
- Seeds database with initial data (users, roles, rules, test clients)

**Prerequisites:**
- Docker must be running: `make docker-up`
- PostgreSQL container must be healthy
- `docker-compose.yml` configured properly

**Output:**
```
Installing dependencies for main app...
✓ Dependencies installed
Setting up PostgreSQL database with two schemas...
✓ Database and schemas created successfully
Running Prisma migrations...
✓ All migrations completed
Seeding database with initial data...
✓ Database seeded

╔════════════════════════════════════════════════════════════════╗
║               ✓ Setup completed successfully!                   ║
╚════════════════════════════════════════════════════════════════╝

Next steps:
  1. Check your environment variables in: general/.env.local
  2. Start development: make dev
  3. Open browser: http://localhost:3000
```

### `make install`
**Install npm dependencies only**

```bash
make install
```

**What it does:**
- Runs `npm install` in the `general/` directory
- Installs all packages from `package.json` and `package-lock.json`
- Generates Prisma client

**When to use:**
- First-time setup
- After cloning the repository
- After adding new npm packages to `package.json`

---

## Docker Targets

### `make docker-up`
**Start PostgreSQL 16 in Docker container**

```bash
make docker-up
```

**What it does:**
- Runs `docker-compose up -d`
- Starts PostgreSQL 16 Alpine container
- Creates named volume for data persistence
- Initializes database schemas (public and contracts)
- Waits for database to be ready (~5 seconds)

**Prerequisites:**
- Docker installed and running
- Docker Compose installed
- Port 5432 not already in use

**Connection Details:**
```
Host:     localhost
Port:     5432
Database: clm_platform
Username: clm_user
Password: clm_password
```

**Output:**
```
Starting PostgreSQL in Docker...
✓ PostgreSQL started
Connection details:
  Host: localhost
  Port: 5432
  Database: clm_platform
  Username: clm_user
  Password: clm_password

Waiting for database to be ready...
```

### `make docker-down`
**Stop PostgreSQL container (preserves data)**

```bash
make docker-down
```

**What it does:**
- Runs `docker-compose down`
- Stops the PostgreSQL container
- **Keeps** the named volume (`clm_platform_postgres_data`) with all data
- Container can be restarted with `make docker-up`

**When to use:**
- End of work session
- Freeing up system resources
- Quick pause before restart

**Important:** Data is preserved! Use `make docker-reset` if you want to delete data.

### `make docker-logs`
**View PostgreSQL container logs**

```bash
make docker-logs
```

**What it does:**
- Shows real-time logs from the PostgreSQL container
- Useful for troubleshooting database issues
- Shows initialization scripts output

**Example output:**
```
postgres-1  | LOG:  database system is ready to accept connections
postgres-1  | LOG:  autovacuum launcher started
```

### `make docker-reset`
**Stop container and DELETE all database data**

```bash
make docker-reset
```

**What it does:**
- Runs `docker-compose down -v` (the `-v` flag removes volumes)
- Stops PostgreSQL container
- **Permanently deletes** the data volume
- Container and image remain (can be reused)

**⚠️ WARNING: This action is destructive - all data will be lost!**

**When to use:**
- Starting completely fresh
- Testing migrations from scratch
- Cleaning up corrupted data
- Development/testing environment cleanup

**After running:**
```bash
make docker-up    # Recreate container with fresh database
make setup        # Reinitialize everything
```

---

## Database Commands

### `make setup-db`
**Create PostgreSQL database and schemas**

```bash
make setup-db
```

**What it does:**
- Creates the `clm_platform` database
- Creates `public` schema (default, for accounting data)
- Creates `contracts` schema (reserved for Spring Boot service)
- Sets up proper permissions for `clm_user`

**Prerequisites:**
- Local PostgreSQL must be running (or Docker container)
- `psql` CLI tool must be installed
- Valid credentials provided

**Interactive prompts:**
```
Enter PostgreSQL username [postgres]: <your-username>
Enter PostgreSQL password: <your-password>
```

**Note:** When using Docker, this is automatically handled by `docker-entrypoint.sql`

### `make migrate`
**Run Prisma migrations for both schemas**

```bash
make migrate
```

**What it does:**
- Runs `prisma migrate deploy` for general app (public schema)
- Runs `prisma migrate deploy` for contracts app (contracts schema)
- Creates all tables based on Prisma schema definitions
- Currently applies 16 migrations for the general schema

**Migration files location:**
- General: `general/prisma/migrations/`
- Contracts: `contracts/prisma/migrations/`

**Output:**
```
Running Prisma migrations for general schema (public)...
Applying m1_initial_migration
Applying m2_add_tasks
...
✓ All migrations completed
```

**Check migration status:**
```bash
cd general && npx prisma migrate status
```

### `make seed`
**Populate database with initial data**

```bash
make seed
```

**What it does:**
- Runs Prisma seed script (`general/prisma/seed.ts`)
- Creates default users:
  - Admin user (admin@example.com)
  - Manager user (manager@example.com)
  - Regular user (user@example.com)
- Seeds compliance rules
- Creates test client with details and work locations
- Establishes user-client relationships

**Seed script location:**
- `general/prisma/seed.ts`

**When to use:**
- After fresh database setup
- To reset to initial data state
- During development to restore test data

**Output:**
```
Seeding database with initial data...
✓ 3 users created
✓ 5 rules created
✓ Test client created
✓ Database seeded
```

---

## Development Workflow

### `make dev`
**Start development server**

```bash
make dev
```

**What it does:**
- Runs `npm run dev` in the `general/` directory
- Starts Next.js development server
- Enables hot reload for code changes
- Starts on default port 3000

**Prerequisites:**
- `make docker-up` must be running
- `make setup` must be completed successfully
- Port 3000 must be available
- `DATABASE_URL` environment variable set in `.env.local`

**Access points after starting:**
- Web app: [http://localhost:3000](http://localhost:3000)
- API: [http://localhost:3000/api](http://localhost:3000/api)

**Keyboard shortcuts in dev mode:**
- `h` - Show help (in terminal)
- `o` - Open in browser
- `q` - Quit development server

**Output:**
```
> dev
> next dev

 ▲ Next.js 15.x.x (Production - compiled client and server code)
 - Local:        http://localhost:3000

✓ Ready in 1.23s
```

---

## Production Deployment

### `make build`
**Build application for production**

```bash
make build
```

**What it does:**
- Runs `npm run build` in the `general/` directory
- Compiles TypeScript to JavaScript
- Optimizes React components
- Creates `.next/` directory with production build
- Generates standalone deployment files

**Prerequisites:**
- All dependencies installed (`make install`)
- Valid `.env.local` with `DATABASE_URL`

**Output:**
```
Building for production...
> build
> next build

 ▲ Next.js 15.x.x Production
 - Compiled client and server successfully

✓ Build completed
```

**Build artifacts:**
- `.next/` directory - Production build files
- `.next/standalone/` - Standalone executable version

### `make start`
**Start production server (requires build)**

```bash
make start
```

**Equivalent to:**
```bash
make build && cd general && npm start
```

**What it does:**
- Builds production version (if not already built)
- Starts production Next.js server
- Runs on port 3000 (or `PORT` environment variable)

**Prerequisites:**
- Build completed successfully
- Database running and accessible
- Proper environment variables configured

**Environment variables for production:**
```bash
NODE_ENV=production
DATABASE_URL=postgresql://...
NEXTAUTH_SECRET=<secure-secret>
NEXTAUTH_URL=<production-url>
```

---

## Cleanup & Reset

### `make logs`
**Show configuration and database status**

```bash
make logs
```

**What it does:**
- Displays environment configuration from `.env.local`
- Shows `DATABASE_URL` configuration
- Checks PostgreSQL connection status
- Tests database connectivity with `pg_isready`

**Output example:**
```
Environment Configuration:
Database URLs from .env.local:
DATABASE_URL=postgresql://clm_user:clm_password@localhost:5432/clm_platform

PostgreSQL Status:
✓ Accepting connections
```

**Troubleshooting use:**
- Verify correct database URL
- Check if PostgreSQL is running
- Diagnose connection issues

### `make reset-db`
**Drop and recreate database (with confirmation)**

```bash
make reset-db
```

**What it does:**
- Prompts for confirmation (prevents accidental data loss)
- Runs `prisma migrate reset --force` for general schema
- Runs `prisma migrate reset --force` for contracts schema
- Re-applies all migrations
- Clears all data

**Confirmation prompt:**
```
⚠ WARNING: This will drop the database and all schemas, losing all data!
Are you sure? [y/N]: 
```

**Type `y` or `Y` to confirm, anything else to cancel.**

**After completion:**
```bash
make setup  # Reinitialize with fresh data
```

### `make clean`
**Remove build files and dependencies (keep data)**

```bash
make clean
```

**What it does:**
- Deletes `.next/` directory (Next.js build)
- Deletes `node_modules/` directory
- Deletes `out/` directory (static export)
- Deletes `dist/` directory (if exists)
- Deletes `.env.local` file

**When to use:**
- Freeing disk space
- Resolving corrupted dependencies
- Preparing for fresh install
- Cleaning up node_modules conflicts

**⚠️ WARNING:** Removes `.env.local` - keep a backup!

**After cleaning:**
```bash
make install   # Reinstall dependencies
```

### `make reset`
**Full reset: clean + reset-db + reinstall (with confirmation)**

```bash
make reset
```

**Runs in sequence:**
1. `make clean` (removes build, dependencies, .env.local)
2. `make reset-db` (drops database and data)
3. `make install` (reinstalls dependencies)

**Confirmation prompts (2 total):**
```
⚠ Full reset will delete all data and dependencies
Continue? [y/N]: y

⚠ WARNING: This will drop the database and all schemas, losing all data!
Are you sure? [y/N]: y
```

**Complete state after:**
- Clean `node_modules/` directory
- Empty build directories
- Blank database with fresh schemas
- No `.env.local` file

**To get back to running state:**
```bash
make docker-up  # Start database
make setup      # Full initialization
make dev        # Start development
```

---

## Complete Workflows

### First-Time Setup (Recommended)

```bash
# 1. Start database
make docker-up

# 2. Full setup (installs, migrates, seeds)
make setup

# 3. Start development
make dev

# Open http://localhost:3000
```

### Daily Development Workflow

```bash
# 1. Start (if not already running)
make docker-up

# 2. Code and test
make dev

# 3. End of session
make docker-down  # Keeps data
```

### Restart After System Reboot

```bash
# Database still has data (volume still exists)
make docker-up
make dev
```

### Completely Fresh Start

```bash
# Delete everything
make docker-reset

# Rebuild from scratch
make docker-up
make setup
make dev
```

### Test Database Migrations

```bash
# Try new migration
cd general && npx prisma migrate dev --name feature_name

# Reset and replay all migrations
make reset-db
make seed
```

### Production-like Testing

```bash
# Build production version
make build

# Start production server (local)
make start

# Test at http://localhost:3000
```

---

## Environment Variables

### Auto-Configured Variables

Located in `general/.env.local` (created during setup):

```bash
NEXTAUTH_SECRET=83bcccbbe4d7a36aec47753a3a253075722f261952e3a45f5a0b90563f633e2a
NEXTAUTH_URL=http://localhost:3000
DATABASE_URL=postgresql://clm_user:clm_password@localhost:5432/clm_platform
```

### Custom Configuration

Edit `general/.env.local`:

```bash
# Authentication
NEXTAUTH_SECRET=<change-to-secure-random-value>
NEXTAUTH_URL=http://localhost:3000

# Database (for Docker setup)
DATABASE_URL=postgresql://clm_user:clm_password@localhost:5432/clm_platform

# Optional: for production
NODE_ENV=production
NEXT_PUBLIC_API_URL=https://your-domain.com
```

### Database-Only Variables

```bash
POSTGRES_USER=clm_user
POSTGRES_PASSWORD=clm_password
POSTGRES_DB=clm_platform
POSTGRES_PORT=5432
```

---

## Troubleshooting

### "Error: docker is not installed"

```bash
# Install Docker Desktop from docker.com
# After installation, verify:
docker --version
docker-compose --version
```

### "Error: psql is not installed"

Occurs when using local PostgreSQL (not Docker recommended). Install:

**macOS:**
```bash
brew install postgresql
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt install postgresql-client
```

**Windows:**
Download from [postgresql.org](https://www.postgresql.org/download/windows/)

### "Environment variable not found: DATABASE_URL"

```bash
# Set in your shell session:
export DATABASE_URL='postgresql://clm_user:clm_password@localhost:5432/clm_platform'

# Or add to ~/.zshrc or ~/.bash_profile:
echo "export DATABASE_URL='postgresql://clm_user:clm_password@localhost:5432/clm_platform'" >> ~/.zshrc
```

### "Cannot connect to database"

**Check if Docker container is running:**
```bash
docker ps | grep postgres
```

**If not running:**
```bash
make docker-up
```

**If running but not responding:**
```bash
make docker-logs
docker-compose exec postgres pg_isready -U clm_user
```

### "Port 5432 already in use"

```bash
# Find process using port:
lsof -i :5432

# Option 1: Stop existing PostgreSQL
# Option 2: Use different port in docker-compose.yml and DATABASE_URL
```

### "Port 3000 already in use"

```bash
# Find process using port:
lsof -i :3000

# Kill the process:
kill -9 <PID>

# Or use different port:
PORT=3001 make dev
```

### "Build failed during `make setup`"

```bash
# Full clean and rebuild:
make clean
make docker-up
make setup
```

### "Migrations failed"

```bash
# Check migration status:
cd general && npx prisma migrate status

# Rollback last migration (if possible):
cd general && npx prisma migrate resolve --rolled-back migration_name

# Or reset and retry:
make reset-db
make setup
```

### "Database seed failed"

```bash
# Check migrations applied:
cd general && npx prisma migrate status

# Reseed:
make seed
```

---

## File Structure Reference

```
CLM-platform/
├── Makefile                    # All make targets
├── docker-compose.yml          # Docker configuration
├── SETUP_GUIDE.md             # This file
├── README.md                   # Project overview
│
├── general/                    # Next.js app
│   ├── package.json           # npm dependencies
│   ├── .env.local             # Environment variables (auto-created)
│   ├── prisma/
│   │   ├── schema.prisma      # Data models
│   │   ├── seed.ts            # Seed script
│   │   └── migrations/        # Migration files (16 total)
│   ├── src/
│   ├── app/                   # Next.js app directory
│   ├── components/            # React components
│   ├── actions/               # Server actions
│   └── lib/                   # Utilities
│
├── contracts/                  # Placeholder for Spring Boot
│   └── prisma/
│       ├── schema.prisma      # Empty (for Spring Boot)
│       └── migrations/        # (currently empty)
│
├── scripts/
│   └── init-db.sql            # Database initialization (Docker)
│
└── agent/                      # Python agent (monitoring)
    ├── monitor_folder.py
    ├── prompt.py
    └── requirements.txt
```

---

## Quick Reference

| Command | Purpose | Time | Data Impact |
|---------|---------|------|------------|
| `make docker-up` | Start database | 5-10s | None |
| `make install` | Install deps | 30-60s | None |
| `make migrate` | Run migrations | 5-10s | Creates tables |
| `make seed` | Initial data | 2-5s | Adds seed data |
| `make dev` | Start server | 5-10s | None |
| `make build` | Production build | 30-60s | None |
| `make logs` | Check config | <1s | None |
| `make clean` | Delete builds | <5s | Deletes .next, node_modules, .env.local |
| `make reset-db` | Reset data | 10s | **Deletes all data** |
| `make docker-down` | Stop database | <2s | **Keeps data** |
| `make docker-reset` | Remove container | <5s | **Deletes all data** |
| `make reset` | Full reset | 1-2min | **Deletes everything** |

---

## Support & Documentation

- **Project README:** [README.md](README.md)
- **Docker Guide:** [DOCKER_SETUP.md](DOCKER_SETUP.md)
- **API Documentation:** [http://localhost:3000/api](http://localhost:3000/api)
- **Next.js Docs:** [https://nextjs.org/docs](https://nextjs.org/docs)
- **Prisma Docs:** [https://www.prisma.io/docs](https://www.prisma.io/docs)
