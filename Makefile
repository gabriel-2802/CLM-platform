COMPOSE_TEST  := docker compose -p clm-test -f docker-compose.testing.yml --env-file .env.testing
STACK_TEST    := clm-test
STACK_PROD    := clm

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
        clean nuke-test \
        swarm-init \
        swarm-build-test swarm-build-prod \
        swarm-config-create swarm-config-update \
        swarm-secrets-test swarm-secrets-prod \
        swarm-deploy-test swarm-deploy-prod \
        swarm-down-test swarm-down-prod \
        swarm-ps swarm-logs \
        swarm-db swarm-db-users swarm-db-clients \
        prometheus-reload-swarm nginx-reload-swarm \
        swarm-rebuild swarm-restart

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
	@echo "$(BOLD)TESTING STACK (docker-compose)$(NC)  (all traffic via Nginx at https://localhost)"
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
	@echo "$(BOLD)SWARM — FIRST-TIME SETUP$(NC)"
	@echo "  $(YELLOW)make swarm-init$(NC)           Initialise Docker Swarm on this host (once)"
	@echo "  $(YELLOW)make certs$(NC)                Generate TLS certs (testing only — run once)"
	@echo "  $(YELLOW)make swarm-config-create$(NC)  Create prometheus Docker config"
	@echo "  $(YELLOW)make swarm-secrets-test$(NC)   Create Swarm secrets from .env.testing"
	@echo "  $(YELLOW)make swarm-secrets-prod$(NC)   Create Swarm secrets from .env.secrets"
	@echo "  $(YELLOW)make swarm-build-test$(NC)     Build all images locally (tag: local)"
	@echo ""
	@echo "$(BOLD)SWARM — DEPLOY$(NC)"
	@echo "  $(YELLOW)make swarm-deploy-test$(NC)    Deploy stack clm-test (.env.testing)"
	@echo "  $(YELLOW)make swarm-deploy-prod$(NC)    Deploy stack clm      (.env.production)"
	@echo "  $(YELLOW)make swarm-down-test$(NC)      Remove testing stack (data is preserved)"
	@echo "  $(YELLOW)make swarm-down-prod$(NC)      Remove production stack (data is preserved)"
	@echo "  $(YELLOW)make swarm-ps$(NC)             List all running stacks and services"
	@echo "  $(YELLOW)make swarm-logs$(NC)           Follow logs for the testing stack"
	@echo ""
	@echo "$(BOLD)SWARM — DATABASES (testing)$(NC)"
	@echo "  $(YELLOW)make swarm-db$(NC)             psql into clm_platform (main DB)"
	@echo "  $(YELLOW)make swarm-db-users$(NC)       psql into clm_users"
	@echo "  $(YELLOW)make swarm-db-clients$(NC)     psql into clm_clients"
	@echo ""
	@echo "$(BOLD)SWARM — DAY-TO-DAY$(NC)"
	@echo "  $(YELLOW)make swarm-rebuild name=<svc>$(NC)  Rebuild image + rolling update (e.g. name=contracts)"
	@echo "  $(YELLOW)make swarm-restart name=<svc>$(NC)  Rolling restart without rebuild"
	@echo "  $(YELLOW)make swarm-deploy-test$(NC)         Redeploy whole stack (idempotent)"
	@echo ""
	@echo "$(BOLD)SWARM — MAINTENANCE$(NC)"
	@echo "  $(YELLOW)make swarm-config-update$(NC)  Recreate prometheus config (after prometheus.yml changes)"
	@echo "  $(YELLOW)make prometheus-reload-swarm$(NC)  Hot-reload Prometheus config in swarm"
	@echo "  $(YELLOW)make nginx-reload-swarm$(NC)   Hot-reload Nginx config in swarm"
	@echo ""
	@echo "$(BOLD)FIRST-RUN SETUP (compose)$(NC)"
	@echo "  $(YELLOW)make test-init$(NC)            Push Prisma schema + seed after first make test"
	@echo "  $(YELLOW)                  $(NC)        Creates admin@example.com / Admin123!"
	@echo ""
	@echo "$(BOLD)MONITORING$(NC)"
	@echo "  $(YELLOW)make monitoring-logs$(NC)      Follow logs for Prometheus and Grafana only"
	@echo "  $(YELLOW)make prometheus-reload$(NC)    Hot-reload prometheus.yml (compose stack)"
	@echo ""
	@echo "$(BOLD)NGINX$(NC)"
	@echo "  $(YELLOW)make nginx-logs$(NC)           Follow Nginx access + error logs (compose)"
	@echo "  $(YELLOW)make nginx-reload$(NC)         Hot-reload Nginx config (compose)"
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
	@echo "  $(YELLOW)API Docs           =>$(NC)  https://localhost/docs/  $(RED)(testing only)$(NC)"
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


# ─── soft wipe (data only, keeps flyway schema history) ──────────────────────

# ==============================================================================
# SWARM TARGETS
# ==============================================================================

# ─── swarm init ───────────────────────────────────────────────────────────────

swarm-init: check-docker
	@docker info --format '{{.Swarm.LocalNodeState}}' | grep -q active && \
		echo "$(YELLOW)Swarm already active$(NC)" || \
		{ docker swarm init && echo "$(GREEN)✓ Swarm initialised$(NC)"; }

# ─── build ────────────────────────────────────────────────────────────────────

swarm-build-test: check-docker
	@echo "$(BLUE)Building all CLM images (tag: local)...$(NC)"
	docker build -t clm-db:local             -f db/Dockerfile .
	docker build -t clm-user-service:local   services/user-service
	docker build -t clm-contracts:local      services/contract-service
	docker build -t clm-client-service:local services/client-service
	docker build -t clm-notifications:local  services/notification-service
	docker build -t clm-negotiation-service:local services/negotiation-service
	docker build -t clm-swagger-hub:local    swagger-hub
	docker build -t clm-nginx:local          nginx
	docker build -t clm-grafana:local        monitoring/grafana
	@set -a && . .env.testing && set +a && \
	  docker build \
	    --build-arg NEXT_PUBLIC_CONTRACTS_API_URL="$$NEXT_PUBLIC_CONTRACTS_API_URL" \
	    --build-arg NEXT_PUBLIC_NOTIFICATIONS_API_URL="$$NEXT_PUBLIC_NOTIFICATIONS_API_URL" \
	    --build-arg NEXT_PUBLIC_USER_SERVICE_URL="$$NEXT_PUBLIC_USER_SERVICE_URL" \
	    --build-arg NEXT_PUBLIC_CLIENT_SERVICE_URL="$$NEXT_PUBLIC_CLIENT_SERVICE_URL" \
	    -t clm-frontend:local \
	    frontend
	@echo "$(GREEN)✓ All images built (tag: local)$(NC)"

swarm-build-prod: check-docker
	@echo "$(BLUE)Building and pushing production images...$(NC)"
	@set -a && . .env.production && set +a && \
	  PREFIX="$${IMAGE_PREFIX}" TAG="$${IMAGE_TAG:-latest}" && \
	  docker build -t "$${PREFIX}clm-db:$$TAG"             -f db/Dockerfile . && \
	  docker build -t "$${PREFIX}clm-user-service:$$TAG"   services/user-service && \
	  docker build -t "$${PREFIX}clm-contracts:$$TAG"      services/contract-service && \
	  docker build -t "$${PREFIX}clm-client-service:$$TAG" services/client-service && \
	  docker build -t "$${PREFIX}clm-notifications:$$TAG"  services/notification-service && \
	  docker build -t "$${PREFIX}clm-negotiation-service:$$TAG" services/negotiation-service && \
	  docker build -t "$${PREFIX}clm-swagger-hub:$$TAG"    swagger-hub && \
	  docker build -t "$${PREFIX}clm-nginx:$$TAG"          nginx && \
	  docker build -t "$${PREFIX}clm-grafana:$$TAG"        monitoring/grafana && \
	  docker build \
	    --build-arg NEXT_PUBLIC_CONTRACTS_API_URL="$$NEXT_PUBLIC_CONTRACTS_API_URL" \
	    --build-arg NEXT_PUBLIC_NOTIFICATIONS_API_URL="$$NEXT_PUBLIC_NOTIFICATIONS_API_URL" \
	    --build-arg NEXT_PUBLIC_USER_SERVICE_URL="$$NEXT_PUBLIC_USER_SERVICE_URL" \
	    --build-arg NEXT_PUBLIC_CLIENT_SERVICE_URL="$$NEXT_PUBLIC_CLIENT_SERVICE_URL" \
	    -t "$${PREFIX}clm-frontend:$$TAG" \
	    frontend
	@echo "$(BLUE)Pushing images...$(NC)"
	@set -a && . .env.production && set +a && \
	  PREFIX="$${IMAGE_PREFIX}" TAG="$${IMAGE_TAG:-latest}" && \
	  for svc in clm-db clm-user-service clm-contracts clm-client-service \
	             clm-notifications clm-negotiation-service clm-swagger-hub \
	             clm-nginx clm-grafana clm-frontend; do \
	    docker push "$${PREFIX}$$svc:$$TAG"; \
	  done
	@echo "$(GREEN)✓ All images built and pushed$(NC)"

# ─── config (prometheus.yml) ──────────────────────────────────────────────────

swarm-config-create:
	@docker config inspect clm_prometheus_config > /dev/null 2>&1 && \
	  echo "$(YELLOW)clm_prometheus_config already exists — use make swarm-config-update to refresh$(NC)" || \
	  { docker config create clm_prometheus_config monitoring/prometheus/prometheus.yml && \
	    echo "$(GREEN)✓ clm_prometheus_config created$(NC)"; }

swarm-config-update:
	@echo "$(BLUE)Updating prometheus config...$(NC)"
	@docker config rm clm_prometheus_config 2>/dev/null || true
	@docker config create clm_prometheus_config monitoring/prometheus/prometheus.yml
	@echo "$(GREEN)✓ clm_prometheus_config updated$(NC)"
	@echo "$(YELLOW)Note: redeploy the stack to apply the new config$(NC)"

# ─── secrets ──────────────────────────────────────────────────────────────────

swarm-secrets-test: check-docker
	@bash scripts/secrets-init.sh testing

swarm-secrets-prod: check-docker
	@bash scripts/secrets-init.sh production

# ─── deploy ───────────────────────────────────────────────────────────────────

swarm-deploy-test: check-docker
	@echo "$(BLUE)Deploying Swarm stack [$(STACK_TEST)]...$(NC)"
	@set -a && . .env.testing && set +a && \
	  docker stack deploy -c docker-stack.yml $(STACK_TEST)
	@echo ""
	@echo "$(GREEN)$(BOLD)╔═══════════════════════════════════════════════════╗$(NC)"
	@echo "$(GREEN)$(BOLD)║      Testing swarm stack is deploying             ║$(NC)"
	@echo "$(GREEN)$(BOLD)╚═══════════════════════════════════════════════════╝$(NC)"
	@echo ""
	@echo "  $(YELLOW)Frontend    =>$(NC)  https://localhost"
	@echo "  $(YELLOW)API Docs    =>$(NC)  https://localhost/docs/  $(RED)(testing only)$(NC)"
	@echo "  $(YELLOW)Grafana     =>$(NC)  https://localhost/grafana/"
	@echo ""
	@echo "  $(BLUE)Status:  make swarm-ps$(NC)"
	@echo "  $(BLUE)Logs:    make swarm-logs$(NC)"
	@echo "  $(BLUE)Stop:    make swarm-down-test$(NC)"
	@echo ""

swarm-deploy-prod: check-docker
	@echo "$(BLUE)Deploying Swarm stack [$(STACK_PROD)]...$(NC)"
	@set -a && . .env.production && set +a && \
	  docker stack deploy -c docker-stack.yml --with-registry-auth $(STACK_PROD)
	@echo "$(GREEN)✓ Production stack deploying — use make swarm-ps to monitor$(NC)"

# ─── down ─────────────────────────────────────────────────────────────────────

swarm-down-test:
	@echo "$(BLUE)Removing swarm stack [$(STACK_TEST)] (volumes preserved)...$(NC)"
	@docker stack rm $(STACK_TEST)
	@echo "$(GREEN)✓ Stack removed$(NC)"

swarm-down-prod:
	@echo "$(RED)Removing PRODUCTION swarm stack [$(STACK_PROD)] (volumes preserved).$(NC)"
	@printf "$(RED)Continue? [y/N]: $(NC)"; read r; \
	[ "$$r" = "y" ] || [ "$$r" = "Y" ] || { echo "$(YELLOW)Cancelled$(NC)"; exit 0; }; \
	docker stack rm $(STACK_PROD); \
	echo "$(GREEN)✓ Production stack removed$(NC)"

# ─── observe ──────────────────────────────────────────────────────────────────

swarm-ps:
	@echo "$(BLUE)Stacks:$(NC)"
	@docker stack ls
	@echo ""
	@echo "$(BLUE)Services [$(STACK_TEST)]:$(NC)"
	@docker stack services $(STACK_TEST) 2>/dev/null || true
	@echo ""
	@echo "$(BLUE)Services [$(STACK_PROD)]:$(NC)"
	@docker stack services $(STACK_PROD) 2>/dev/null || true

swarm-logs:
	@echo "$(BLUE)Following logs for stack [$(STACK_TEST)] (Ctrl+C to stop)...$(NC)"
	@docker service logs -f $(STACK_TEST)_nginx 2>/dev/null & \
	  docker service logs -f $(STACK_TEST)_frontend 2>/dev/null & \
	  docker service logs -f $(STACK_TEST)_contracts 2>/dev/null & \
	  docker service logs -f $(STACK_TEST)_user-service 2>/dev/null & \
	  wait

# ─── database access (swarm — exec into running container) ────────────────────

swarm-db:
	@docker exec -it \
	  $$(docker ps -qf "name=$(STACK_TEST)_postgres" | head -1) \
	  psql -U clm_user -d clm_platform

swarm-db-users:
	@docker exec -it \
	  $$(docker ps -qf "name=$(STACK_TEST)_postgres-users" | head -1) \
	  psql -U clm_user -d clm_users

swarm-db-clients:
	@docker exec -it \
	  $$(docker ps -qf "name=$(STACK_TEST)_postgres-clients" | head -1) \
	  psql -U clm_user -d clm_clients

# ─── maintenance ──────────────────────────────────────────────────────────────

prometheus-reload-swarm:
	@echo "$(BLUE)Reloading Prometheus in swarm...$(NC)"
	@docker exec \
	  $$(docker ps -qf "name=$(STACK_TEST)_prometheus" | head -1) \
	  wget -q --post-data='' http://localhost:9090/-/reload -O - > /dev/null && \
	  echo "$(GREEN)✓ Prometheus reloaded$(NC)" || \
	  echo "$(RED)✗ Reload failed$(NC)"

nginx-reload-swarm:
	@echo "$(BLUE)Reloading Nginx in swarm...$(NC)"
	@docker exec \
	  $$(docker ps -qf "name=$(STACK_TEST)_nginx" | head -1) \
	  nginx -s reload && \
	  echo "$(GREEN)✓ Nginx reloaded$(NC)" || \
	  echo "$(RED)✗ Reload failed$(NC)"

# ─── rebuild / restart individual services ────────────────────────────────────
# Usage:
#   make swarm-rebuild name=contracts       rebuild image + rolling update
#   make swarm-restart name=contracts       rolling restart without rebuild
#
# Service names (stack service name = what you pass as name=):
#   postgres  user-service  contracts  client-service  notifications
#   negotiation-service  swagger-hub  frontend  nginx  grafana

swarm-rebuild: check-docker
	@[ -n "$(name)" ] || { echo "$(RED)Usage: make swarm-rebuild name=<service>$(NC)"; exit 1; }
	@set -a && . .env.testing && set +a; \
	case "$(name)" in \
	  postgres)            img=clm-db:local;                 ctx="-f db/Dockerfile ." ;; \
	  user-service)        img=clm-user-service:local;       ctx="services/user-service" ;; \
	  contracts)           img=clm-contracts:local;          ctx="services/contract-service" ;; \
	  client-service)      img=clm-client-service:local;     ctx="services/client-service" ;; \
	  notifications)       img=clm-notifications:local;      ctx="services/notification-service" ;; \
	  negotiation-service) img=clm-negotiation-service:local; ctx="services/negotiation-service" ;; \
	  swagger-hub)         img=clm-swagger-hub:local;        ctx="swagger-hub" ;; \
	  nginx)               img=clm-nginx:local;              ctx="nginx" ;; \
	  grafana)             img=clm-grafana:local;            ctx="monitoring/grafana" ;; \
	  frontend) \
	    img=clm-frontend:local; \
	    docker build \
	      --build-arg NEXT_PUBLIC_CONTRACTS_API_URL="$$NEXT_PUBLIC_CONTRACTS_API_URL" \
	      --build-arg NEXT_PUBLIC_NOTIFICATIONS_API_URL="$$NEXT_PUBLIC_NOTIFICATIONS_API_URL" \
	      --build-arg NEXT_PUBLIC_USER_SERVICE_URL="$$NEXT_PUBLIC_USER_SERVICE_URL" \
	      --build-arg NEXT_PUBLIC_CLIENT_SERVICE_URL="$$NEXT_PUBLIC_CLIENT_SERVICE_URL" \
	      -t $$img frontend; \
	    docker service update --image $$img --force $(STACK_TEST)_$(name); \
	    exit 0 ;; \
	  *) echo "$(RED)Unknown service: $(name)$(NC)"; exit 1 ;; \
	esac; \
	echo "$(BLUE)Building $$img...$(NC)"; \
	docker build -t $$img $$ctx; \
	echo "$(BLUE)Updating $(STACK_TEST)_$(name)...$(NC)"; \
	docker service update --image $$img --force $(STACK_TEST)_$(name); \
	echo "$(GREEN)✓ $(name) rebuilt and updating$(NC)"

swarm-restart: check-docker
	@[ -n "$(name)" ] || { echo "$(RED)Usage: make swarm-restart name=<service>$(NC)"; exit 1; }
	@docker service update --force $(STACK_TEST)_$(name)
	@echo "$(GREEN)✓ $(name) restarting$(NC)"