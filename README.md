# CLM Platform Setup & Commands Guide

This guide details how to manage the CLM (Contabilitate Ledger Management) Platform using Make commands.

## Quick Start

Execute these commands in order to set up a new environment:

```bash
make docker-up     # Start PostgreSQL in Docker
make setup         # Install dependencies, migrate, and seed
make dev           # Start the development server
```

Access the application at: **http://localhost:3000**

---

## Core Commands

### Initial Setup
* **`make setup`**: Runs the full initialization sequence (`install` -> `setup-db` -> `migrate` -> `seed`).
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

---`

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