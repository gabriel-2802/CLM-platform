.PHONY: help setup migrate seed dev build start clean reset install logs docker-up docker-down docker-logs docker-reset

# Colors for output
BLUE := \033[0;34m
GREEN := \033[0;32m
YELLOW := \033[0;33m
RED := \033[0;31m
NC := \033[0m # No Color

# Default target
help:
	@echo "$(BLUE)╔════════════════════════════════════════════════════════════════╗$(NC)"
	@echo "$(BLUE)║         CLM Platform - Development Setup & Management           ║$(NC)"
	@echo "$(BLUE)╚════════════════════════════════════════════════════════════════╝$(NC)"
	@echo ""
	@echo "$(GREEN)Targets:$(NC)"
	@echo "  $(YELLOW)make setup$(NC)              - Full initial setup (install, migrate, seed)"
	@echo "  $(YELLOW)make install$(NC)            - Install dependencies"
	@echo "  $(YELLOW)make migrate$(NC)            - Run Prisma migrations for both schemas"
	@echo "  $(YELLOW)make seed$(NC)               - Seed the database with initial data"
	@echo "  $(YELLOW)make dev$(NC)                - Start development server"
	@echo "  $(YELLOW)make build$(NC)              - Build for production"
	@echo "  $(YELLOW)make start$(NC)              - Start production server"
	@echo "  $(YELLOW)make logs$(NC)               - Show database setup logs"
	@echo "  $(YELLOW)make reset-db$(NC)           - Drop and recreate database and schemas"
	@echo "  $(YELLOW)make clean$(NC)              - Clean up node_modules, .next, and build files"
	@echo "  $(YELLOW)make reset$(NC)              - Full reset (clean + reset-db + reinstall)"
	@echo ""
	@echo "$(GREEN)Docker Targets (Recommended):$(NC)"
	@echo "  $(YELLOW)make docker-up$(NC)          - Start PostgreSQL in Docker"
	@echo "  $(YELLOW)make docker-down$(NC)        - Stop PostgreSQL Docker container"
	@echo "  $(YELLOW)make docker-logs$(NC)        - View PostgreSQL logs"
	@echo "  $(YELLOW)make docker-reset$(NC)       - Stop and remove PostgreSQL container + data"
	@echo ""

# Install dependencies for both services
install:
	@echo "$(GREEN)Installing dependencies for main app...$(NC)"
	cd general && npm install
	@echo "$(GREEN)✓ Dependencies installed$(NC)"



# Run Prisma migrations for both schemas
migrate:
	@echo "$(GREEN)Running Prisma migrations for general schema (public)...$(NC)"
	cd general && set -a && source .env.local && set +a && npx prisma migrate deploy
	@echo "$(GREEN)✓ General schema migrations completed$(NC)"
	@echo "$(YELLOW)Note: Contracts schema is managed by Spring Boot service (not migrated here)$(NC)"

# Seed the database
seed:
	@echo "$(GREEN)Seeding databases with initial data...$(NC)"
	cd general && set -a && source .env.local && set +a && npx prisma db seed
	@echo "$(GREEN)✓ Database seeded$(NC)"

# Full initial setup (Docker must be running: make docker-up)
setup: install migrate seed
	@echo ""
	@echo "$(GREEN)╔════════════════════════════════════════════════════════════════╗$(NC)"
	@echo "$(GREEN)║               ✓ Setup completed successfully!                   ║$(NC)"
	@echo "$(GREEN)╚════════════════════════════════════════════════════════════════╝$(NC)"
	@echo ""
	@echo "$(YELLOW)Prerequisites:$(NC)"
	@echo "  • Docker container must be running: $(BLUE)make docker-up$(NC)"
	@echo "  • Environment variables in: general/.env.local"
	@echo ""
	@echo "$(YELLOW)Next steps:$(NC)"
	@echo "  1. Start development: $(BLUE)make dev$(NC)"
	@echo "  2. Open browser: $(BLUE)http://localhost:3000$(NC)"
	@echo ""

# Start development server
dev:
	@echo "$(GREEN)Starting development server...$(NC)"
	cd general && npm run dev

# Build for production
build:
	@echo "$(GREEN)Building for production...$(NC)"
	cd general && npm run build
	@echo "$(GREEN)✓ Build completed$(NC)"

# Start production server
start: build
	@echo "$(GREEN)Starting production server...$(NC)"
	cd general && npm start

# Reset database and schemas (drop and recreate)
reset-db:
	@echo "$(RED)⚠ WARNING: This will drop the database and all schemas, losing all data!$(NC)"
	@echo "$(RED)Are you sure? [y/N]:$(NC)"
	@read -r response; \
	if [ "$$response" = "y" ] || [ "$$response" = "Y" ]; then \
		echo "$(RED)Resetting database...$(NC)"; \
		cd general && npx prisma migrate reset --force; \
		echo "$(RED)Resetting contracts schema...$(NC)"; \
		cd contracts && npx prisma migrate reset --force; \
		echo "$(GREEN)✓ Database and schemas reset$(NC)"; \
	else \
		echo "$(YELLOW)Cancelled$(NC)"; \
	fi

# Clean up build files and dependencies
clean:
	@echo "$(GREEN)Cleaning up...$(NC)"
	rm -rf general/.next
	rm -rf general/node_modules
	rm -rf general/out
	rm -rf general/dist
	rm -f general/.env.local
	@echo "$(GREEN)✓ Cleanup completed$(NC)"

# Full reset
reset: clean
	@echo "$(RED)⚠ Full reset will delete all data and dependencies$(NC)"
	@echo "$(RED)Continue? [y/N]:$(NC)"
	@read -r response; \
	if [ "$$response" = "y" ] || [ "$$response" = "Y" ]; then \
		make reset-db; \
		make install; \
		echo "$(GREEN)✓ Full reset completed$(NC)"; \
	else \
		echo "$(YELLOW)Cancelled$(NC)"; \
	fi

# Show setup logs/information
logs:
	@echo "$(GREEN)Environment Configuration:$(NC)"
	@if [ -f general/.env.local ]; then \
		echo "$(YELLOW)Database URLs from .env.local:$(NC)"; \
		grep -i database general/.env.local || echo "No database configuration found"; \
	else \
		echo "$(YELLOW)⚠ .env.local not found$(NC)"; \
	fi
	@echo ""
	@echo "$(GREEN)PostgreSQL Status:$(NC)"
	@if command -v pg_isready >/dev/null 2>&1; then \
		pg_isready -h localhost -p 5432 || echo "$(RED)PostgreSQL not responding on localhost:5432$(NC)"; \
	else \
		echo "$(YELLOW)pg_isready not available$(NC)"; \
	fi

# ====================== DOCKER TARGETS ======================

# Start PostgreSQL in Docker
docker-up:
	@echo "$(GREEN)Starting PostgreSQL in Docker...$(NC)"
	@command -v docker >/dev/null 2>&1 || { echo "$(RED)Error: docker is not installed$(NC)"; exit 1; }
	@command -v docker-compose >/dev/null 2>&1 || { echo "$(RED)Error: docker-compose is not installed$(NC)"; exit 1; }
	docker-compose up -d
	@echo ""
	@echo "$(GREEN)✓ PostgreSQL started$(NC)"
	@echo "$(YELLOW)Connection details:$(NC)"
	@echo "  Host: localhost"
	@echo "  Port: 5432"
	@echo "  Database: clm_platform"
	@echo "  Username: clm_user"
	@echo "  Password: clm_password"
	@echo ""
	@sleep 5
	@echo "$(GREEN)Waiting for database to be ready...$(NC)"
	@docker-compose exec -T postgres pg_isready -U clm_user -d clm_platform || true

# Stop PostgreSQL Docker container
docker-down:
	@echo "$(GREEN)Stopping PostgreSQL container...$(NC)"
	docker-compose down
	@echo "$(GREEN)✓ PostgreSQL stopped$(NC)"

# View PostgreSQL logs
docker-logs:
	@echo "$(GREEN)PostgreSQL logs:$(NC)"
	docker-compose logs postgres

# Reset PostgreSQL (stop container and remove data)
docker-reset:
	@echo "$(RED)⚠ This will stop the container and DELETE all database data!$(NC)"
	@echo "$(RED)Are you sure? [y/N]:$(NC)"
	@read -r response; \
	if [ "$$response" = "y" ] || [ "$$response" = "Y" ]; then \
		echo "$(RED)Removing container and data...$(NC)"; \
		docker-compose down -v; \
		echo "$(GREEN)✓ PostgreSQL container and data removed$(NC)"; \
	else \
		echo "$(YELLOW)Cancelled$(NC)"; \
	fi
