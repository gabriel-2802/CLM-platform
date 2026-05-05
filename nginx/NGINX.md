# Nginx — CLM Platform Reverse Proxy

Nginx is the single entry point for all external traffic. It terminates TLS, enforces security headers, rate-limits API calls, and routes requests to the appropriate upstream service.

## Topology

```
Internet
  │
  ▼
Nginx :443 (TLS termination)
  ├─ /api/auth/login          → user-service    (Spring — exact match)
  ├─ /api/auth/register       → user-service    (Spring — exact match)
  ├─ /api/auth/**             → frontend        (NextAuth routes)
  ├─ /api/users   (no slash)  → frontend        (Next.js adds Bearer token, then calls user-service)
  ├─ /api/users/  (slash+)    → user-service    (Swagger UI / direct calls with Bearer token)
  ├─ /api/contracts/**        → contract-service
  ├─ /api/appendices/**       → contract-service
  ├─ /api/templates/**        → contract-service
  ├─ /api/clients/**          → client-service
  ├─ /api/tasks/**            → client-service
  ├─ /api/enums/**            → client-service
  ├─ /api/notifications/**    → notification-service  (testing only)
  ├─ /actuator/**             → 403 (blocked)
  ├─ /docs/                   → swagger-hub
  ├─ /grafana/                → grafana
  └─ /                        → frontend (Next.js catch-all)

Prometheus: data-net only, never exposed externally.
```

## TLS

Certificates are mounted at runtime from `nginx/certs/` and never baked into the image. Only TLSv1.2 and TLSv1.3 are accepted. Session tickets are disabled to preserve forward secrecy.

## Security headers

| Header | Value |
|---|---|
| Strict-Transport-Security | `max-age=63072000; includeSubDomains` |
| X-Frame-Options | `SAMEORIGIN` |
| X-Content-Type-Options | `nosniff` |
| X-XSS-Protection | `1; mode=block` |
| Referrer-Policy | `strict-origin-when-cross-origin` |

## Rate limiting

All API locations use `limit_req zone=api`. Burst allowances per location:

| Path | Burst |
|---|---|
| `/api/auth/login` | 20 |
| `/api/auth/register` | 10 |
| Everything else | 50 |

## `/api/users` split routing

`/api/users` (no trailing slash) and `/api/users/` (with slash) are handled differently because Spring Boot 6+ dropped trailing-slash route matching.

- **Browser → `GET /api/users`** — routed to Next.js via `location = /api/users` (exact match). Next.js reads the session cookie, extracts the Bearer token, and calls user-service internally. The exact match is required to prevent nginx's built-in behaviour of 301-redirecting no-slash paths to their slash equivalent, which would bypass the auth layer.

- **`GET /api/users/` and sub-paths** — routed directly to user-service. The caller must supply an `Authorization: Bearer` header (Swagger UI, service-to-service calls). The rewrite rule strips the exact trailing slash on the collection endpoint so Spring's `@GetMapping` matches.

## Trailing-slash rewrites

Several `location /api/<resource>/` blocks include:

```nginx
rewrite ^/api/<resource>/$ /api/<resource> break;
```

This strips the trailing slash only on the collection root, preserving sub-paths like `/api/contracts/123`. The `break` flag stops further rewrite processing and proxies in the same location block.

## Notifications (testing only)

`/api/notifications/` rewrites the prefix to `/notifications/` before forwarding because the notification service controller is mounted at `/notifications`, not `/api/notifications`.

## Updating the config

The nginx config is baked into the image at build time (`nginx/Dockerfile`). Only the TLS certificates are mounted as a volume at runtime.

To apply a config change in a running environment without rebuilding:

```bash
docker cp nginx/conf.d/clm.conf clm-nginx-test:/etc/nginx/conf.d/clm.conf
docker exec clm-nginx-test nginx -t
docker exec clm-nginx-test nginx -s reload
```

Always run `nginx -t` before reloading. A broken config leaves the running workers serving the old config until the next hard restart.
