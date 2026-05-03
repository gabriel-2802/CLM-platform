COMPOSE_TEST := docker compose -p clm-test -f docker-compose.testing.yml --env-file .env.testing

BLUE   := \033[0;34m
GREEN  := \033[0;32m
YELLOW := \033[0;33m
RED    := \033[0;31m
BOLD   := \033[1m
NC     := \033[0m

.PHONY: help \
        check-docker \
        test test-up test-down test-restart test-logs test-ps test-rebuild \
        db-test db-users-test \
        test-init \
        clean nuke-test

# ─── help ─────────────────────────────────────────────────────────────────────

help:
	@echo ""
	@echo "$(BLUE)$(BOLD)╔══════════════════════════════════════════════════════════════════╗$(NC)"
	@echo "$(BLUE)$(BOLD)║            CLM Platform — Docker & Development Commands           ║$(NC)"
	@echo "$(BLUE)$(BOLD)╚══════════════════════════════════════════════════════════════════╝$(NC)"
	@echo ""
	@echo "$(BOLD)PRE-REQUISITES$(NC)"
	@echo "  $(YELLOW)make check-docker$(NC)       Verify Docker daemon is running"
	@echo ""
	@echo "$(BOLD)TESTING STACK$(NC)  (all services exposed on host, verbose logging)"
	@echo "  $(YELLOW)make test$(NC)               Build images and start all services"
	@echo "  $(YELLOW)make test-up$(NC)            Start testing stack (images must already exist)"
	@echo "  $(YELLOW)make test-down$(NC)          Stop and remove testing containers"
	@echo "  $(YELLOW)make test-restart$(NC)       Restart all testing services"
	@echo "  $(YELLOW)make test-rebuild$(NC)       Stop => rebuild images => start"
	@echo "  $(YELLOW)make test-logs$(NC)          Follow logs for all testing services"
	@echo "  $(YELLOW)make test-ps$(NC)            Show testing service status and health"
	@echo "  $(YELLOW)make db-test$(NC)            Open psql shell in test postgres"
	@echo "  $(YELLOW)make db-users-test$(NC)      Open psql shell in test users postgres"
	@echo ""
	@echo "$(BOLD)FIRST-RUN SETUP$(NC)"
	@echo "  $(YELLOW)make test-init$(NC)          Push Prisma schema + seed after first make test"
	@echo "  $(YELLOW)                $(NC)        Creates admin@example.com / Admin123!"
	@echo ""
	@echo "$(BOLD)CLEANUP$(NC)"
	@echo "  $(YELLOW)make clean$(NC)              Remove node_modules, .next, and build artifacts"
	@echo "  $(YELLOW)make nuke-test$(NC)          Stop testing stack and delete its volumes (data loss!)"
	@echo ""

# ─── guards ───────────────────────────────────────────────────────────────────

check-docker:
	@echo "$(BLUE)Checking Docker...$(NC)"
	@command -v docker >/dev/null 2>&1 || { \
		echo "$(RED)✗ Docker not found. Install Docker Desktop: https://docs.docker.com/get-docker/$(NC)"; \
		exit 1; }
	@docker info >/dev/null 2>&1 || { \
		echo "$(RED)✗ Docker daemon is not running. Start Docker Desktop and try again.$(NC)"; \
		exit 1; }
	@echo "$(GREEN)✓ Docker is running$(NC)"

# ─── testing stack ────────────────────────────────────────────────────────────

test: check-docker
	@echo "$(BLUE)Building and starting testing stack...$(NC)"
	$(COMPOSE_TEST) up -d --build
	@echo ""
	@echo "$(GREEN)$(BOLD)╔══════════════════════════════════════════════════╗$(NC)"
	@echo "$(GREEN)$(BOLD)║       Testing stack is up — service endpoints     ║$(NC)"
	@echo "$(GREEN)$(BOLD)╚══════════════════════════════════════════════════╝$(NC)"
	@echo ""
	@echo "  $(YELLOW)Client        =>$(NC)  http://localhost:3000"
	@echo "  $(YELLOW)Contracts API =>$(NC)  http://localhost:8081"
	@echo "  $(YELLOW)Notifications =>$(NC)  http://localhost:8082"
	@echo "  $(YELLOW)User Service  =>$(NC)  http://localhost:8083"
	@echo "  $(YELLOW)Client Svc    =>$(NC)  http://localhost:8084"
	@echo "  $(YELLOW)PostgreSQL    =>$(NC)  localhost:5433  (user: clm_user / db: clm_platform)"
	@echo "  $(YELLOW)PostgreSQL    =>$(NC)  localhost:5434  (user: clm_user / db: clm_users)"
	@echo "  $(YELLOW)PostgreSQL    =>$(NC)  localhost:5435  (user: clm_user / db: clm_clients)"
	@echo ""
	@echo "  $(GREEN) All endpoints docs can be accessed at =>$(NC)  http://localhost:8090"
	@echo "  
	@echo ""
	@echo ""
	@echo "  $(BLUE)Logs:    make test-logs$(NC)"
	@echo "  $(BLUE)Status:  make test-ps$(NC)"
	@echo "  $(BLUE)Stop:    make test-down$(NC)"
	@echo ""

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

test-rebuild: check-docker
	@echo "$(BLUE)Rebuilding testing images...$(NC)"
	$(COMPOSE_TEST) down
	$(COMPOSE_TEST) up -d --build
	@echo "$(GREEN)✓ Testing stack rebuilt and started$(NC)"

test-rebuild-service: check-docker
	@echo "$(BLUE)Rebuilding service $(YELLOW)$(name)$(BLUE)...$(NC)"
	$(COMPOSE_TEST) up -d --build $(name)
	@echo "$(GREEN)✓ $(name) rebuilt and started$(NC)"

test-logs:
	$(COMPOSE_TEST) logs -f

test-ps:
	$(COMPOSE_TEST) ps

db-test:
	@docker exec -it clm-postgres-test psql -U clm_user -d clm_platform

db-users-test:
	@docker exec -it clm-postgres-users-test psql -U clm_user -d clm_users

# ─── cleanup ──────────────────────────────────────────────────────────────────

clean:
	@echo "$(BLUE)Cleaning build artifacts and dependencies...$(NC)"
	rm -rf main-service/.next main-service/node_modules
	@echo "$(GREEN)✓ Clean complete$(NC)"

nuke-test:
	@echo "$(RED) This will stop the testing stack and DELETE its database volumes.$(NC)"
	@printf "$(RED)Continue? [y/N]: $(NC)"; read r; \
	[ "$$r" = "y" ] || [ "$$r" = "Y" ] || { echo "$(YELLOW)Cancelled$(NC)"; exit 0; }; \
	$(COMPOSE_TEST) down -v --remove-orphans; \
	docker rm -f clm-postgres-test clm-postgres-users-test clm-postgres-clients-test \
	             clm-user-service-test clm-contracts-test clm-notifications-test \
	             clm-client-service-test clm-client-test 2>/dev/null || true; \
	echo "$(GREEN)✓ Testing stack and volumes removed$(NC)"