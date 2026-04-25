COMPOSE_TEST := docker compose -p clm-test -f docker-compose.testing.yml  --env-file .env.testing
COMPOSE_PROD := docker compose -p clm-prod -f docker-compose.production.yml --env-file .env.production

BLUE   := \033[0;34m
GREEN  := \033[0;32m
YELLOW := \033[0;33m
RED    := \033[0;31m
BOLD   := \033[1m
NC     := \033[0m

.PHONY: help \
        check-docker check-maven \
        build-contracts build-notifications build-jars \
        test test-up test-down test-restart test-logs test-ps test-rebuild db-test \
        prod prod-preflight prod-up prod-down prod-restart prod-logs prod-ps prod-rebuild db-prod \
        install migrate seed dev \
        clean-jars clean nuke-test nuke-prod

# ─── Help ─────────────────────────────────────────────────────────────────────

help:
	@echo ""
	@echo "$(BLUE)$(BOLD)╔══════════════════════════════════════════════════════════════════╗$(NC)"
	@echo "$(BLUE)$(BOLD)║            CLM Platform — Docker & Development Commands           ║$(NC)"
	@echo "$(BLUE)$(BOLD)╚══════════════════════════════════════════════════════════════════╝$(NC)"
	@echo ""
	@echo "$(BOLD)PRE-REQUISITES$(NC)"
	@echo "  $(YELLOW)make check-docker$(NC)       Verify Docker daemon is running"
	@echo "  $(YELLOW)make check-maven$(NC)        Verify Maven (mvn) is on PATH"
	@echo ""
	@echo "$(BOLD)BUILD$(NC)"
	@echo "  $(YELLOW)make build-jars$(NC)         Compile contracts + notifications JARs locally (optional)"
	@echo "  $(YELLOW)make build-contracts$(NC)    Compile contracts JAR locally (optional)"
	@echo "  $(YELLOW)make build-notifications$(NC)Compile notifications JAR locally (optional)"
	@echo "  $(YELLOW)                $(NC)        (Docker builds JARs automatically — these are for local use only)"
	@echo ""
	@echo "$(BOLD)TESTING STACK$(NC)  (all services exposed on host, verbose logging)"
	@echo "  $(YELLOW)make test$(NC)               Build JARs → build images → start all services"
	@echo "  $(YELLOW)make test-up$(NC)            Start testing stack (JARs must already exist)"
	@echo "  $(YELLOW)make test-down$(NC)          Stop and remove testing containers"
	@echo "  $(YELLOW)make test-restart$(NC)       Restart all testing services"
	@echo "  $(YELLOW)make test-rebuild$(NC)       Stop → rebuild images → start"
	@echo "  $(YELLOW)make test-logs$(NC)          Follow logs for all testing services"
	@echo "  $(YELLOW)make test-ps$(NC)            Show testing service status + health"
	@echo "  $(YELLOW)make db-test$(NC)            Open psql shell in test postgres"
	@echo ""
	@echo "$(BOLD)PRODUCTION STACK$(NC)  (only client port 3000 exposed)"
	@echo "  $(YELLOW)make prod$(NC)               Preflight → build JARs → build images → start"
	@echo "  $(YELLOW)make prod-preflight$(NC)     Validate all secrets in .env.production are set"
	@echo "  $(YELLOW)make prod-up$(NC)            Start production stack (JARs must already exist)"
	@echo "  $(YELLOW)make prod-down$(NC)          Stop and remove production containers"
	@echo "  $(YELLOW)make prod-restart$(NC)       Restart all production services"
	@echo "  $(YELLOW)make prod-rebuild$(NC)       Stop → rebuild images → start"
	@echo "  $(YELLOW)make prod-logs$(NC)          Follow logs for all production services"
	@echo "  $(YELLOW)make prod-ps$(NC)            Show production service status + health"
	@echo "  $(YELLOW)make db-prod$(NC)            Open psql shell in prod postgres"
	@echo ""
	@echo "$(BOLD)FIRST-RUN SETUP$(NC)"
	@echo "  $(YELLOW)make test-init$(NC)          Push Prisma schema + seed after first `make test`"
	@echo "  $(YELLOW)                $(NC)        Creates admin@example.com / Admin123!"
	@echo ""
	@echo "$(BOLD)LOCAL DEV$(NC)  (Next.js without Docker)"
	@echo "  $(YELLOW)make install$(NC)            npm install in general/"
	@echo "  $(YELLOW)make migrate$(NC)            Run Prisma migrations (requires DB running)"
	@echo "  $(YELLOW)make seed$(NC)               Seed the database via Prisma"
	@echo "  $(YELLOW)make dev$(NC)                Start Next.js dev server locally"
	@echo ""
	@echo "$(BOLD)CLEANUP$(NC)"
	@echo "  $(YELLOW)make clean-jars$(NC)         Remove compiled JARs from target/ directories"
	@echo "  $(YELLOW)make clean$(NC)              Remove node_modules, .next, and build artifacts"
	@echo "  $(YELLOW)make nuke-test$(NC)          Stop testing stack and delete its volumes (data loss!)"
	@echo "  $(YELLOW)make nuke-prod$(NC)          Stop production stack and delete its volumes (DATA LOSS!)"
	@echo ""

# ─── Guards ───────────────────────────────────────────────────────────────────

check-docker:
	@echo "$(BLUE)Checking Docker...$(NC)"
	@command -v docker >/dev/null 2>&1 || { \
		echo "$(RED)✗ Docker not found. Install Docker Desktop: https://docs.docker.com/get-docker/$(NC)"; \
		exit 1; }
	@docker info >/dev/null 2>&1 || { \
		echo "$(RED)✗ Docker daemon is not running. Start Docker Desktop and try again.$(NC)"; \
		exit 1; }
	@echo "$(GREEN)✓ Docker is running$(NC)"

check-maven:
	@echo "$(BLUE)Checking Maven...$(NC)"
	@command -v mvn >/dev/null 2>&1 || { \
		echo "$(RED)✗ mvn not found. Install Maven: https://maven.apache.org/install.html$(NC)"; \
		echo "$(YELLOW)  macOS:  brew install maven$(NC)"; \
		echo "$(YELLOW)  Linux:  sudo apt install maven / sudo dnf install maven$(NC)"; \
		exit 1; }
	@echo "$(GREEN)✓ Maven $(shell mvn -q --version 2>&1 | head -1) available$(NC)"

# ─── Build JARs ───────────────────────────────────────────────────────────────

build-contracts: check-maven
	@echo "$(BLUE)Building contracts JAR...$(NC)"
	cd contracts && mvn clean package -DskipTests -q
	@echo "$(GREEN)✓ contracts/target/*.jar ready$(NC)"

build-notifications: check-maven
	@echo "$(BLUE)Building notifications JAR...$(NC)"
	cd notifications && mvn package -DskipTests -q
	@echo "$(GREEN)✓ notifications/target/*.jar ready$(NC)"

build-jars: build-contracts build-notifications
	@echo "$(GREEN)✓ All JARs compiled$(NC)"

# ─── Testing Stack ────────────────────────────────────────────────────────────

# One-shot: build Docker images (JARs compiled inside Docker) and start everything
test: check-docker
	@echo "$(BLUE)Building and starting testing stack...$(NC)"
	$(COMPOSE_TEST) up -d --build
	@echo ""
	@echo "$(GREEN)$(BOLD)╔══════════════════════════════════════════════════╗$(NC)"
	@echo "$(GREEN)$(BOLD)║       Testing stack is up — service endpoints     ║$(NC)"
	@echo "$(GREEN)$(BOLD)╚══════════════════════════════════════════════════╝$(NC)"
	@echo ""
	@echo "  $(YELLOW)Client        →$(NC)  http://localhost:3000"
	@echo "  $(YELLOW)Contracts API →$(NC)  http://localhost:8081"
	@echo "  $(YELLOW)Notifications →$(NC)  http://localhost:8082"
	@echo "  $(YELLOW)PostgreSQL    →$(NC)  localhost:5433  (user: clm_user / db: clm_platform)"
	@echo ""
	@echo "  $(BLUE)Logs:    make test-logs$(NC)"
	@echo "  $(BLUE)Status:  make test-ps$(NC)"
	@echo "  $(BLUE)Stop:    make test-down$(NC)"
	@echo ""

# Start containers without rebuilding images or recompiling JARs
test-up: check-docker
	@echo "$(BLUE)Starting testing stack...$(NC)"
	$(COMPOSE_TEST) up -d
	@echo "$(GREEN)✓ Testing stack started$(NC)"

test-down:
	@echo "$(BLUE)Stopping testing stack...$(NC)"
	$(COMPOSE_TEST) down
	@echo "$(GREEN)✓ Testing stack stopped$(NC)"

test-restart:
	@echo "$(BLUE)Restarting testing services...$(NC)"
	$(COMPOSE_TEST) restart
	@echo "$(GREEN)✓ Done$(NC)"

# Stop, rebuild images (no JAR recompile), start again
test-rebuild: check-docker
	@echo "$(BLUE)Rebuilding testing images...$(NC)"
	$(COMPOSE_TEST) down
	$(COMPOSE_TEST) up -d --build
	@echo "$(GREEN)✓ Testing stack rebuilt and started$(NC)"

test-logs:
	$(COMPOSE_TEST) logs -f

test-ps:
	$(COMPOSE_TEST) ps

# psql shell directly into the test database
db-test:
	@docker exec -it clm-postgres-test psql -U clm_user -d clm_platform

# ─── Production Stack ─────────────────────────────────────────────────────────

# Validate that no required secrets are empty in .env.production
prod-preflight:
	@echo "$(BLUE)Running production pre-flight checks...$(NC)"
	@[ -f .env.production ] || { \
		echo "$(RED)✗ .env.production not found$(NC)"; exit 1; }
	@missing=0; \
	for var in DB_PASSWORD JWT_SECRET ADMIN_REGISTER_CODE \
	            MAIL_HOST MAIL_USERNAME MAIL_PASSWORD MAIL_FROM \
	            FRONTEND_URL NEXTAUTH_SECRET NEXTAUTH_URL \
	            NEXT_PUBLIC_CONTRACTS_API_URL NEXT_PUBLIC_NOTIFICATIONS_API_URL; do \
		line=$$(grep -E "^$$var=" .env.production 2>/dev/null | head -1); \
		val=$$(echo "$$line" | cut -d= -f2- | sed 's/[[:space:]]*#.*//' | tr -d '[:space:]'); \
		if [ -z "$$val" ]; then \
			echo "  $(RED)✗ $$var is not set$(NC)"; \
			missing=$$((missing + 1)); \
		else \
			echo "  $(GREEN)✓ $$var$(NC)"; \
		fi; \
	done; \
	if [ "$$missing" -gt "0" ]; then \
		echo ""; \
		echo "$(RED)Pre-flight failed: $$missing variable(s) missing in .env.production$(NC)"; \
		echo "$(YELLOW)Edit .env.production and fill in all required values, then retry.$(NC)"; \
		exit 1; \
	else \
		echo ""; \
		echo "$(GREEN)✓ All required secrets are set — ready to deploy$(NC)"; \
	fi

# One-shot: validate secrets, build images (JARs compiled inside Docker), start
prod: check-docker prod-preflight
	@echo "$(BLUE)Building and starting production stack...$(NC)"
	$(COMPOSE_PROD) up -d --build
	@echo ""
	@echo "$(GREEN)$(BOLD)╔══════════════════════════════════════════════════╗$(NC)"
	@echo "$(GREEN)$(BOLD)║          Production stack is running              ║$(NC)"
	@echo "$(GREEN)$(BOLD)╚══════════════════════════════════════════════════╝$(NC)"
	@echo ""
	@echo "  $(YELLOW)Client (public) →$(NC)  http://localhost:3000"
	@echo "  All other services are internal-only."
	@echo ""
	@echo "  $(BLUE)Logs:    make prod-logs$(NC)"
	@echo "  $(BLUE)Status:  make prod-ps$(NC)"
	@echo "  $(BLUE)Stop:    make prod-down$(NC)"
	@echo ""

prod-up: check-docker
	@echo "$(BLUE)Starting production stack...$(NC)"
	$(COMPOSE_PROD) up -d
	@echo "$(GREEN)✓ Production stack started$(NC)"

prod-down:
	@echo "$(BLUE)Stopping production stack...$(NC)"
	$(COMPOSE_PROD) down
	@echo "$(GREEN)✓ Production stack stopped$(NC)"

prod-restart:
	@echo "$(BLUE)Restarting production services...$(NC)"
	$(COMPOSE_PROD) restart
	@echo "$(GREEN)✓ Done$(NC)"

prod-rebuild: check-docker
	@echo "$(BLUE)Rebuilding production images...$(NC)"
	$(COMPOSE_PROD) down
	$(COMPOSE_PROD) up -d --build
	@echo "$(GREEN)✓ Production stack rebuilt and started$(NC)"

prod-logs:
	$(COMPOSE_PROD) logs -f

prod-ps:
	$(COMPOSE_PROD) ps

# psql shell directly into the production database — use with caution
db-prod:
	@echo "$(YELLOW)⚠ Connecting to PRODUCTION database$(NC)"
	@docker exec -it clm-postgres-prod psql \
		-U "$$(grep -E '^DB_USER=' .env.production | cut -d= -f2 | tr -d '[:space:]')" \
		-d "$$(grep -E '^DB_NAME=' .env.production | cut -d= -f2 | tr -d '[:space:]')"

# ─── Local Dev (Next.js without Docker) ───────────────────────────────────────

install:
	@echo "$(BLUE)Installing Next.js dependencies...$(NC)"
	cd general && npm install
	@echo "$(GREEN)✓ Dependencies installed$(NC)"

# Push the Prisma schema and seed — run once after `make test` on a fresh volume
test-init:
	@echo "$(BLUE)Pushing Prisma schema to general schema...$(NC)"
	docker exec clm-client-test npx prisma db push --force-reset --skip-generate
	@echo "$(BLUE)Seeding database...$(NC)"
	docker exec clm-client-test npx prisma db seed
	@echo ""
	@echo "$(GREEN)✓ Schema ready — login: admin@example.com / Admin123!$(NC)"

migrate:
	@echo "$(BLUE)Running Prisma migrations...$(NC)"
	cd general && npx prisma migrate deploy
	@echo "$(GREEN)✓ Migrations applied$(NC)"

seed:
	@echo "$(BLUE)Seeding database...$(NC)"
	cd general && npx prisma db seed
	@echo "$(GREEN)✓ Database seeded$(NC)"

dev:
	@echo "$(BLUE)Starting Next.js dev server...$(NC)"
	cd general && npm run dev

# ─── Cleanup ──────────────────────────────────────────────────────────────────

clean-jars:
	@echo "$(BLUE)Removing compiled JARs...$(NC)"
	rm -rf contracts/target notifications/target
	@echo "$(GREEN)✓ target/ directories removed$(NC)"

clean:
	@echo "$(BLUE)Cleaning build artifacts and dependencies...$(NC)"
	rm -rf general/.next general/node_modules general/out general/dist
	@echo "$(GREEN)✓ Clean complete$(NC)"

# Stop testing stack and delete its database volume — ALL TEST DATA LOST
nuke-test:
	@echo "$(RED)⚠  This will stop the testing stack and DELETE its database volume.$(NC)"
	@printf "$(RED)Continue? [y/N]: $(NC)"; read r; \
	[ "$$r" = "y" ] || [ "$$r" = "Y" ] || { echo "$(YELLOW)Cancelled$(NC)"; exit 0; }; \
	$(COMPOSE_TEST) down -v --remove-orphans; \
	docker rm -f clm-postgres-test clm-contracts-test clm-notifications-test clm-client-test 2>/dev/null || true; \
	echo "$(GREEN)✓ Testing stack and volumes removed$(NC)"

# Stop production stack and delete its database volume — PRODUCTION DATA LOSS
nuke-prod:
	@echo "$(RED)$(BOLD)⚠  WARNING: This will DELETE the PRODUCTION database volume.$(NC)"
	@echo "$(RED)$(BOLD)   This action is IRREVERSIBLE. Ensure you have a backup.$(NC)"
	@printf "$(RED)Type YES to confirm: $(NC)"; read r; \
	[ "$$r" = "YES" ] || { echo "$(YELLOW)Cancelled$(NC)"; exit 0; }; \
	$(COMPOSE_PROD) down -v; \
	echo "$(GREEN)✓ Production stack and volumes removed$(NC)"
