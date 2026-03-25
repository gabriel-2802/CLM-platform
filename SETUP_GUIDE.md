# CLM Platform Setup & Commands Guide

**Database Setup: Docker Only** - No local PostgreSQL required. All database initialization happens via Docker.

## Prerequisites

- **Node.js 18+**: [nodejs.org](https://nodejs.org/)
- **Docker Desktop**: [docker.com](https://www.docker.com/products/docker-desktop) (includes Docker Compose)
- **Make**:
  - **macOS**: Usually pre-installed, or `brew install make`
  - **Linux**: `sudo apt install make` (Ubuntu/Debian)
  - **Windows**: `choco install make` (Chocolatey) or `scoop install make` (Scoop)

## Quick Start

```bash
# 1. Start PostgreSQL in Docker (schemas created automatically)
make docker-up

# 2. Install dependencies, run migrations & seed
make setup

# 3. Start development server
make dev
```

Access the application at: **http://localhost:3000**

---

## How It Works

1. **`make docker-up`**: 
   - Starts PostgreSQL 16 container
   - Runs `scripts/init-db.sql` which creates `public` and `contracts` schemas
   - All data persisted in Docker volume

2. **`make setup`**: 
   - Installs npm dependencies
   - Runs Prisma migrations (creates tables in `public` schema)
   - Seeds database with initial data

3. **`make dev`**: 
   - Starts Next.js development server
   - Database already ready from steps 1-2

No `psql` or local PostgreSQL installation needed!

---

## All Make Commands

### Initial Setup

### Initial Setup
* **`make setup`**: Runs the full initialization sequence (`install` -> `migrate` -> `seed`). Docker must be running first.
* **`make install`**: Installs npm packages in the `general/` directory and generates the Prisma client.

### Docker Management
* **`make docker-up`**: Starts the PostgreSQL 16 container. Creates the database and schemas automatically.
* **`make docker-down`**: Stops the container but **preserves** all data in the Docker volume.
* **`make docker-logs`**: Displays real-time database logs for debugging.
* **`make docker-reset`**: Stops the container and **deletes** the data volume.

### Database Operations
* **`make migrate`**: Applies Prisma migrations to both the `public` and `contracts` schemas.
* **`make seed`**: Populates the database with default admin users, compliance rules, and test clients.
* **`make reset-db`**: Drops and recreates the database schemas. This clears all existing data.

### Development & Production
* **`make dev`**: Launches the Next.js development server with hot reloading.
* **`make build`**: Compiles the application for production.
* **`make start`**: Runs the compiled production server.

---

## Connection Details

| Property | Value |
| :--- | :--- |
| **Host** | localhost |
| **Port** | 5432 |
| **Database** | clm_platform |
| **Username** | clm_user |
| **Password** | clm_password |

---

## Cleanup & Maintenance

* **`make logs`**: Verifies the connection status and displays current environment variables.
* **`make clean`**: Deletes `node_modules`, `.next` build files, and the `.env.local` file.
* **`make reset`**: A destructive command that runs `clean`, `reset-db`, and `install` in sequence.

---

## Windows Setup

### Install Make

**Using Chocolatey** (Recommended)
```powershell
choco install make
```

**Using Scoop**
```powershell
scoop install make
```

**Manual Installation**
- Download from [GNU Make for Windows](https://gnuwin32.sourceforge.io/packages/make.htm)
- Add to PATH

### Verify Installation

```powershell
make --version
```

Then use the same `make` commands as macOS/Linux users.

---

## Windows-Specific Troubleshooting

### Make command not found after installation

If `make` still doesn't work after installing:

1. **Restart PowerShell/Command Prompt** (environment variables need reload)
2. **Check PATH**: `echo $env:Path` (PowerShell) or `echo %PATH%` (Command Prompt)
3. **Reinstall Make**: `choco uninstall make && choco install make`

### Can't connect to Docker from PowerShell

- Ensure Docker Desktop is running (check system tray)
- Use PowerShell 5.1+ (or Windows Terminal)
- Rebuild VSCode terminal or restart editor

#### "Port 5432 already in use"
```powershell
# Find and stop existing container
docker ps
docker stop <container_id>

# Or use different port in docker-compose.yml
```

#### "Port 3000 already in use" on Windows
```powershell
# Find process using port
netstat -ano | findstr :3000

# Kill process (replace PID with actual process ID)
taskkill /PID <PID> /F

# Or start on different port
$env:PORT=3001; npm run dev
```

---

## Troubleshooting Common Issues

### Database Connection Failures
1. Ensure Docker is running: `docker ps`.
2. Check if port 5432 is occupied by a local PostgreSQL instance: `lsof -i :5432`.
3. Verify the `DATABASE_URL` in `general/.env.local` matches the connection details above.

### Port 3000 Occupied
If the web server fails to start, kill the existing process or start on a different port:
```bash
PORT=3001 make dev
```

### Dependency Conflicts
If you encounter build errors after a dependency update, run:
```bash
make clean
make install
```

---

## Command Summary

| Command | Action | Data Impact |
| :--- | :--- | :--- |
| `make docker-up` | Start Postgres | None |
| `make setup` | Full Install | Creates tables/data |
| `make dev` | Start Server | None |
| `make docker-down` | Stop Postgres | Data preserved |
| `make docker-reset` | Wipe Docker | **Data deleted** |
| `make reset-db` | Wipe Tables | **Data deleted** |
| `make clean` | Delete Builds | Deletes config/deps |