COMPOSE_TEST := docker compose -p clm-test -f docker-compose.testing.yml --env-file .env.testing

BLUE   := \033[0;34m
GREEN  := \033[0;32m
YELLOW := \033[0;33m
RED    := \033[0;31m
BOLD   := \033[1m
NC     := \033[0m

.PHONY: help \
        check-docker \
        certs trust-cert \
        test test-up test-down test-restart test-logs test-ps test-rebuild test-rebuild-service \
        db-test db-users-test db-clients-test \
        test-init \
        monitoring-logs prometheus-reload \
        nginx-logs nginx-reload \
        clean nuke-test

# ─── help ─────────────────────────────────────────────────────────────────────

help:
	@echo ""
	@echo "$(BLUE)$(BOLD)╔══════════════════════════════════════════════════════════════════╗$(NC)"
	@echo "$(BLUE)$(BOLD)║            CLM Platform — Docker & Development Commands           ║$(NC)"
	@echo "$(BLUE)$(BOLD)╚══════════════════════════════════════════════════════════════════╝$(NC)"
	@echo ""
	@echo "$(BOLD)PRE-REQUISITES$(NC)"
	@echo "  $(YELLOW)make check-docker$(NC)         Verify Docker daemon is running"
	@echo "  $(YELLOW)make certs$(NC)                Generate self-signed TLS certs for Nginx (run once)"
	@echo "  $(YELLOW)make trust-cert$(NC)           Trust the cert in macOS Keychain (run once, needs sudo)"
	@echo ""
	@echo "$(BOLD)TESTING STACK$(NC)  (all traffic via Nginx at https://localhost)"
	@echo "  $(YELLOW)make test$(NC)                 Build images and start all services"
	@echo "  $(YELLOW)make test-up$(NC)              Start testing stack (images must already exist)"
	@echo "  $(YELLOW)make test-down$(NC)            Stop and remove testing containers"
	@echo "  $(YELLOW)make test-restart$(NC)         Restart all testing services"
	@echo "  $(YELLOW)make test-rebuild$(NC)         Stop => rebuild images => start"
	@echo "  $(YELLOW)make test-logs$(NC)            Follow logs for all testing services"
	@echo "  $(YELLOW)make test-ps$(NC)              Show testing service status and health"
	@echo "  $(YELLOW)make db-test$(NC)              Open psql shell in test postgres (clm_platform)"
	@echo "  $(YELLOW)make db-users-test$(NC)        Open psql shell in test users postgres (clm_users)"
	@echo "  $(YELLOW)make db-clients-test$(NC)      Open psql shell in test clients postgres (clm_clients)"
	@echo ""
	@echo "$(BOLD)FIRST-RUN SETUP$(NC)"
	@echo "  $(YELLOW)make test-init$(NC)            Push Prisma schema + seed after first make test"
	@echo "  $(YELLOW)                  $(NC)        Creates admin@example.com / Admin123!"
	@echo ""
	@echo "$(BOLD)MONITORING$(NC)"
	@echo "  $(YELLOW)make monitoring-logs$(NC)      Follow logs for Prometheus and Grafana only"
	@echo "  $(YELLOW)make prometheus-reload$(NC)    Hot-reload prometheus.yml without restarting"
	@echo ""
	@echo "$(BOLD)NGINX$(NC)"
	@echo "  $(YELLOW)make nginx-logs$(NC)           Follow Nginx access + error logs"
	@echo "  $(YELLOW)make nginx-reload$(NC)         Hot-reload Nginx config without downtime"
	@echo ""
	@echo "$(BOLD)CLEANUP$(NC)"
	@echo "  $(YELLOW)make clean$(NC)                Remove node_modules, .next, and build artifacts"
	@echo "  $(YELLOW)make nuke-test$(NC)            Stop testing stack and delete its volumes (data loss!)"
	@echo ""

# ─── certs ────────────────────────────────────────────────────────────────────

# Generates a self-signed certificate valid for 825 days (macOS/Linux).
# The output files are git-ignored — never commit the private key.
certs:
	@mkdir -p nginx/certs
	@echo "$(BLUE)Generating self-signed TLS certificate for localhost...$(NC)"
	@openssl req -x509 -nodes -days 825 \
		-newkey rsa:2048 \
		-keyout nginx/certs/clm.key \
		-out    nginx/certs/clm.crt \
		-subj   "/CN=localhost/O=CLM-Platform/C=US" \
		-addext "subjectAltName=DNS:localhost,IP:127.0.0.1"
	@chmod 600 nginx/certs/clm.key
	@echo "$(GREEN)✓ Certificates written to nginx/certs/$(NC)"
	@echo "  $(YELLOW)clm.crt$(NC) — certificate (add to your browser/OS trust store to silence warnings)"
	@echo "  $(YELLOW)clm.key$(NC) — private key  (git-ignored, never commit)"

# Adds the cert to the macOS System Keychain and marks it Always Trust for SSL.
# Requires sudo — you will be prompted for your password.
# After running, restart your browser for the change to take effect.
trust-cert:
	@[ -f nginx/certs/clm.crt ] || { \
		echo "$(RED)✗ nginx/certs/clm.crt not found — run make certs first$(NC)"; exit 1; }
	@echo "$(BLUE)Trusting nginx/certs/clm.crt in macOS System Keychain (sudo required)...$(NC)"
	@sudo security add-trusted-cert -d -r trustRoot \
		-k /Library/Keychains/System.keychain \
		nginx/certs/clm.crt
	@echo "$(GREEN)✓ Certificate trusted$(NC)"
	@echo "  Restart Chrome / Safari / Firefox for the change to take effect."

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
	@echo "$(GREEN)$(BOLD)╔═══════════════════════════════════════════════════╗$(NC)"
	@echo "$(GREEN)$(BOLD)║      Testing stack is up — all traffic via Nginx  ║$(NC)"
	@echo "$(GREEN)$(BOLD)╚═══════════════════════════════════════════════════╝$(NC)"
	@echo ""
	@echo "  $(YELLOW)Frontend           =>$(NC)  https://localhost"
	@echo "  $(YELLOW)Auth               =>$(NC)  https://localhost/api/auth/"
	@echo "  $(YELLOW)User Service       =>$(NC)  https://localhost/api/users/"
	@echo "  $(YELLOW)Contracts API      =>$(NC)  https://localhost/api/contracts/"
	@echo "  $(YELLOW)Client Service     =>$(NC)  https://localhost/api/clients/"
	@echo "  $(YELLOW)Notifications API  =>$(NC)  https://localhost/api/notifications/  $(RED)(testing only)$(NC)"
	@echo "  $(YELLOW)API Docs           =>$(NC)  https://localhost/docs/"
	@echo "  $(YELLOW)Grafana            =>$(NC)  https://localhost/grafana/"
	@echo "  $(YELLOW)PostgreSQL (main)  =>$(NC)  localhost:5433  (clm_user / clm_platform)"
	@echo "  $(YELLOW)PostgreSQL (users) =>$(NC)  localhost:5434  (clm_user / clm_users)"
	@echo "  $(YELLOW)PostgreSQL (clnts) =>$(NC)  localhost:5435  (clm_user / clm_clients)"
	@echo ""
	@echo "  $(BLUE)Logs:             make test-logs$(NC)"
	@echo "  $(BLUE)Status:           make test-ps$(NC)"
	@echo "  $(BLUE)Nginx logs:       make nginx-logs$(NC)"
	@echo "  $(BLUE)Monitoring logs:  make monitoring-logs$(NC)"
	@echo "  $(BLUE)Stop:             make test-down$(NC)"
	@echo ""
	@echo "  $(RED)TLS:$(NC) browser will warn about self-signed cert — run $(YELLOW)make certs$(NC) first"
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

db-clients-test:
	@docker exec -it clm-postgres-clients-test psql -U clm_user -d clm_clients

# ─── first-run ────────────────────────────────────────────────────────────────

test-init:
	@echo "$(BLUE)Running Prisma migrations and seeding admin user...$(NC)"
	$(COMPOSE_TEST) exec client npx prisma migrate deploy
	$(COMPOSE_TEST) exec client npx prisma db seed
	@echo "$(GREEN)✓ Schema pushed and admin seeded$(NC)"

# ─── monitoring ───────────────────────────────────────────────────────────────

monitoring-logs:
	@echo "$(BLUE)Following Prometheus and Grafana logs (Ctrl+C to stop)...$(NC)"
	$(COMPOSE_TEST) logs -f prometheus grafana

# Sends a POST to Prometheus's /-/reload endpoint (requires --web.enable-lifecycle,
# which is set in docker-compose.testing.yml).  Reloads prometheus.yml and all
# rule files without restarting the container or losing TSDB data.
prometheus-reload:
	@echo "$(BLUE)Reloading Prometheus configuration...$(NC)"
	@docker exec clm-prometheus-test wget -q --post-data='' \
		http://localhost:9090/-/reload -O - >/dev/null && \
		echo "$(GREEN)✓ Prometheus configuration reloaded$(NC)" || \
		echo "$(RED)✗ Reload failed — is the container running?$(NC)"

# ─── nginx ────────────────────────────────────────────────────────────────────

nginx-logs:
	@echo "$(BLUE)Following Nginx logs (Ctrl+C to stop)...$(NC)"
	$(COMPOSE_TEST) logs -f nginx

# Sends SIGHUP to Nginx inside the container — reloads config with zero downtime.
nginx-reload:
	@echo "$(BLUE)Reloading Nginx configuration...$(NC)"
	@docker exec clm-nginx-test nginx -s reload && \
		echo "$(GREEN)✓ Nginx configuration reloaded$(NC)" || \
		echo "$(RED)✗ Reload failed — is the container running?$(NC)"

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
	             clm-client-service-test clm-client-test \
	             clm-prometheus-test clm-grafana-test clm-nginx-test 2>/dev/null || true; \
	echo "$(GREEN)✓ Testing stack and volumes removed$(NC)"
