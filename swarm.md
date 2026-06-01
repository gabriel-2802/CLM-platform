# CLM Platform — Docker Swarm Deployment Guide

This document covers every architectural decision required to promote the CLM
platform from the local `docker-compose.testing.yml` workflow to a
production-grade Docker Swarm stack (`docker-stack.yml`).

---

## 1. Service Topology

| Service | Image | Port | Role |
|---|---|---|---|
| `postgres` | custom (`db/Dockerfile`) | 5432 | Contracts DB |
| `postgres-users` | `postgres:16-alpine` | 5432 | Users DB |
| `postgres-clients` | `postgres:16-alpine` | 5432 | Clients DB |
| `postgres-negotiations` | `postgres:16-alpine` | 5432 | Negotiations DB |
| `user-service` | custom (JRE 21) | 8083 | Auth / JWT |
| `contracts` | custom (JRE 21 + LibreOffice) | 8081 | Contract CRUD + PDF |
| `client-service` | custom (JRE 21) | 8084 | Client management |
| `notifications` | custom (JRE 25) | 8082 | Email dispatch |
| `negotiation-service` | custom (JRE 21) | 8085 | Negotiation + cron |
| `swagger-hub` | custom (Python 3.13) | 8090 | API docs aggregator |
| `frontend` | custom (Node 20, Next.js) | 3000 | SSR + Next.js API routes |
| `prometheus` | `prom/prometheus:v2.51.2` | 9090 | Metrics collection |
| `grafana` | custom (`monitoring/grafana/Dockerfile`) | 3000 | Metrics dashboards |
| `nginx` | custom (`nginx/Dockerfile`) | 80, 443 | TLS termination + reverse proxy |

Traffic flow: all external requests enter on nginx ports 80/443. Nginx
terminates TLS and proxies to the appropriate backend over the `data-net`
overlay network. Databases are never reachable from outside the cluster.

---

## 2. Image Registry Requirements

`docker stack deploy` does **not** support the `build:` key. Every service
that has a custom `Dockerfile` must be built locally and pushed to a container
registry before deployment.

### Build and push script

```bash
REGISTRY=registry.example.com/clm
TAG=1.0.0

# Custom-build images
docker build -t ${REGISTRY}/clm-postgres:${TAG}           -f db/Dockerfile .
docker build -t ${REGISTRY}/clm-user-service:${TAG}       services/user-service/
docker build -t ${REGISTRY}/clm-contracts:${TAG}          services/contract-service/
docker build -t ${REGISTRY}/clm-client-service:${TAG}     services/client-service/
docker build -t ${REGISTRY}/clm-notifications:${TAG}      services/notification-service/
docker build -t ${REGISTRY}/clm-negotiation-service:${TAG} services/negotiation-service/
docker build -t ${REGISTRY}/clm-swagger-hub:${TAG}        swagger-hub/

# Frontend — public NEXT_PUBLIC_* vars must be baked in at build time
docker build \
  --build-arg NEXT_PUBLIC_CONTRACTS_API_URL=https://clm.example.com/api/contracts \
  --build-arg NEXT_PUBLIC_NOTIFICATIONS_API_URL=https://clm.example.com/api/notifications \
  --build-arg NEXT_PUBLIC_USER_SERVICE_URL=https://clm.example.com/api/users \
  --build-arg NEXT_PUBLIC_CLIENT_SERVICE_URL=https://clm.example.com/api/clients \
  -t ${REGISTRY}/clm-frontend:${TAG} frontend/

docker build -t ${REGISTRY}/clm-grafana:${TAG}            monitoring/grafana/
docker build -t ${REGISTRY}/clm-nginx:${TAG}              nginx/

# Push all images
for svc in clm-postgres clm-user-service clm-contracts clm-client-service \
           clm-notifications clm-negotiation-service clm-swagger-hub \
           clm-frontend clm-grafana clm-nginx; do
  docker push ${REGISTRY}/${svc}:${TAG}
done
```

Set `REGISTRY` and `IMAGE_TAG` in `.env.production` before deploying.

---

## 3. Node Labels

Apply labels to Swarm nodes to enable placement constraints for stateful
services. Databases are pinned to a dedicated node to guarantee that named
volumes remain co-located with the containers that own them.

```bash
# Designate one node as the DB host (replace <node-id> with actual ID)
docker node update --label-add role=db         <db-node-id>

# Designate one node for monitoring (can be the manager node)
docker node update --label-add role=monitoring  <monitoring-node-id>
```

Verify with:

```bash
docker node inspect --pretty <node-id> | grep Labels -A5
```

---

## 4. Secrets Management

Sensitive values from `.env.production` are migrated to Docker Swarm Secrets.
Secrets are encrypted at rest (in the Raft log) and in transit, and are only
ever decrypted inside the container at `/run/secrets/<name>`.

### Secret inventory

| Secret name | Maps to (env var) | Consumer services |
|---|---|---|
| `db_password` | `DB_PASSWORD` / `POSTGRES_PASSWORD` | all postgres instances, all Spring Boot services |
| `jwt_secret` | `JWT_SECRET` | user-service, contracts, client-service, notifications, negotiation-service |
| `nextauth_secret` | `NEXTAUTH_SECRET` | frontend |
| `admin_register_code` | `ADMIN_REGISTER_CODE` | user-service, contracts |
| `admin_password` | `APP_ADMIN_PASSWORD` | user-service |
| `mail_username` | `SPRING_MAIL_USERNAME` | contracts, notifications, negotiation-service |
| `mail_password` | `SPRING_MAIL_PASSWORD` | contracts, notifications, negotiation-service |
| `grafana_admin_password` | `GF_SECURITY_ADMIN_PASSWORD` | grafana |
| `nginx_tls_cert` | TLS certificate file | nginx |
| `nginx_tls_key` | TLS private key file | nginx |

> **JWT / NextAuth alignment:** `jwt_secret` and `nextauth_secret` must hold
> the same value. Spring Boot validates JWT tokens signed by NextAuth. Create
> them separately on the Swarm so services can reference the name that matches
> their env var, but populate both with the same `openssl rand -hex 32` output.

### Secret initialisation commands

```bash
# Generate strong values for secrets that need them
JWT_VAL=$(openssl rand -hex 32)
echo -n "$JWT_VAL" | docker secret create jwt_secret -
echo -n "$JWT_VAL" | docker secret create nextauth_secret -

echo -n "$(openssl rand -base64 24)" | docker secret create db_password -
echo -n "$(openssl rand -base64 16)" | docker secret create admin_register_code -
echo -n "ChangeMe_StrongAdminPassword1!" | docker secret create admin_password -

echo -n "your-smtp-username"  | docker secret create mail_username -
echo -n "your-smtp-password"  | docker secret create mail_password -
echo -n "$(openssl rand -base64 16)" | docker secret create grafana_admin_password -

# TLS certificate and key (replace with your cert, e.g. from Let's Encrypt)
docker secret create nginx_tls_cert /path/to/fullchain.pem
docker secret create nginx_tls_key  /path/to/privkey.pem
```

---

## 5. Spring Boot Secret Injection

The official PostgreSQL image natively supports `POSTGRES_PASSWORD_FILE`.
Spring Boot does **not** natively read environment variables from files using a
`_FILE` suffix. To bridge this gap, add a thin wrapper entrypoint script to
each Spring Boot image.

### Wrapper script (`docker-entrypoint-wrapper.sh`)

Place this in each Spring Boot service directory and reference it in the
Dockerfile:

```bash
#!/bin/sh
# Exports Docker Swarm secrets as environment variables before JVM starts.
_read_secret() {
  local name="$1"
  local file="/run/secrets/${name}"
  [ -r "$file" ] && cat "$file"
}

export SPRING_DATASOURCE_PASSWORD="$(_read_secret db_password)"
export JWT_SECRET="$(_read_secret jwt_secret)"
export APP_JWT_SECRET="$(_read_secret jwt_secret)"
export APP_ADMIN_REGISTER_CODE="$(_read_secret admin_register_code)"
export APP_ADMIN_PASSWORD="$(_read_secret admin_password)"
export SPRING_MAIL_USERNAME="$(_read_secret mail_username)"
export SPRING_MAIL_PASSWORD="$(_read_secret mail_password)"

exec "$@"
```

Update each Spring Boot Dockerfile runtime stage:

```dockerfile
COPY docker-entrypoint-wrapper.sh /usr/local/bin/entrypoint-wrapper.sh
RUN chmod +x /usr/local/bin/entrypoint-wrapper.sh

ENTRYPOINT ["/usr/local/bin/entrypoint-wrapper.sh", "java", \
  "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", "-jar", "<service>.jar"]
```

For the frontend (`nextauth_secret`), Next.js reads `NEXTAUTH_SECRET` from the
environment. Use the same wrapper pattern in the Node image, reading
`/run/secrets/nextauth_secret` and exporting it as `NEXTAUTH_SECRET` before
`node server.js` is called.

---

## 6. High Availability & Replicas

| Service | Replicas | Rationale |
|---|---|---|
| `postgres` | 1 | Stateful; pinned to db node |
| `postgres-users` | 1 | Stateful; pinned to db node |
| `postgres-clients` | 1 | Stateful; pinned to db node |
| `postgres-negotiations` | 1 | Stateful; pinned to db node |
| `user-service` | 2 | Stateless; rolling update safe |
| `contracts` | 2 | Stateless per-request; LibreOffice conversions are independent |
| `client-service` | 2 | Stateless; rolling update safe |
| `notifications` | 1 | Event-driven emailer; multiple replicas risk duplicate sends |
| `negotiation-service` | 1 | Contains a monthly cron job; multiple replicas would fire it N times |
| `swagger-hub` | 1 | Read-only; low traffic |
| `frontend` | 2 | Stateless SSR; rolling update safe |
| `prometheus` | 1 | Stateful TSDB; must be pinned to its data volume |
| `grafana` | 1 | Stateful (SQLite); must be pinned to its data volume |
| `nginx` | 2 | Entry point; Swarm routing mesh distributes incoming connections |

### Common update_config for stateless services

```yaml
update_config:
  parallelism: 1        # replace one replica at a time
  delay: 10s            # wait between each replacement
  failure_action: rollback
  monitor: 60s          # time to watch each new task before declaring success
  max_failure_ratio: 0.1
  order: start-first    # new replica healthy before old is removed (zero downtime)
```

### Stateful / singleton services use `order: stop-first`

Databases, Prometheus, and Grafana use `stop-first` to prevent two instances
accessing the same volume simultaneously.

---

## 7. State & Volumes

All stateful services use **named Docker volumes** scoped to the node they run
on. Combined with the `node.labels.role == db` and `node.labels.role ==
monitoring` placement constraints, the container always lands on the same node
as its data directory.

| Volume | Service | Contents |
|---|---|---|
| `postgres_data` | postgres | Contracts + schema `clm` |
| `postgres_users_data` | postgres-users | Users + schema `users` |
| `postgres_clients_data` | postgres-clients | Clients + schema `clients` |
| `postgres_negotiations_data` | postgres-negotiations | Negotiations + schema `negotiations` |
| `prometheus_data` | prometheus | TSDB blocks (15-day retention) |
| `grafana_data` | grafana | Dashboards state, users, SQLite |

> For larger production clusters, replace local volumes with an NFS share or a
> cloud block-storage volume driver (e.g., `rexray/ebs` on AWS) so that the
> placement constraint becomes unnecessary and the database can survive node
> failure.

---

## 8. Networking

| Network | Driver | Purpose |
|---|---|---|
| `dmz-net` | overlay | nginx ↔ Swarm ingress mesh |
| `data-net` | overlay | all internal service-to-service communication |

`nginx` is attached to **both** networks so it can accept external traffic
(`dmz-net`) and proxy to backends (`data-net`). Every other service is
attached to `data-net` only. No backend service publishes a host port, so they
are unreachable from outside the overlay.

Swarm's built-in overlay DNS resolves service names (e.g. `frontend`,
`contracts`) to their respective Virtual IPs, which load-balance across all
healthy replicas of that service. The existing nginx upstream blocks
(`server frontend:3000`, `server contracts:8081`, etc.) work without
modification.

---

## 9. Deployment

### Prerequisites checklist

- [ ] Docker Swarm initialised (`docker swarm init`)
- [ ] Node labels applied (§ 3)
- [ ] All images built and pushed to registry (§ 2)
- [ ] All Docker secrets created (§ 4)
- [ ] `.env.production` filled in with all non-secret values
- [ ] Prometheus config Docker config created or available locally

### Deploy command

```bash
docker stack deploy \
  --compose-file docker-stack.yml \
  --env-file .env.production \
  --with-registry-auth \
  clm
```

### Useful operational commands

```bash
# List all running tasks
docker stack ps clm

# Watch service replicas converge
docker service ls

# Tail logs for a service
docker service logs -f clm_contracts

# Rolling update after pushing a new image
docker service update --image registry.example.com/clm/clm-contracts:1.1.0 clm_contracts

# Scale a stateless service
docker service scale clm_user-service=3

# Remove the entire stack
docker stack rm clm
```

---

## 10. Post-deployment Verification

```bash
# All services should show replicas satisfied (e.g. 2/2)
docker service ls

# Nginx endpoints
curl -k https://<swarm-node-ip>/api/users/actuator/health   # should 403 (blocked by nginx)
curl -k https://<swarm-node-ip>/                            # Next.js frontend

# Grafana
open https://<swarm-node-ip>/grafana/
```
