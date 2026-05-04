# CLM Platform — Grafana Dashboards

> **URL:** http://localhost:3001  
> **Login:** `admin` / value of `GRAFANA_PASSWORD` in `.env.testing`  
> **Dashboard path:** Dashboards → CLM Platform → CLM Platform Services

---

## Service variable

Every panel is filtered by the **Service** dropdown at the top of the dashboard.  
Select one, several, or **All** to scope all panels simultaneously.

---

## Sections

### Overview
Four stat cards that give an instant health read at a glance.

| Card | Metric | Turns yellow / red at |
|------|--------|-----------------------|
| Total Request Rate | Sum of all req/s across selected services | 50 / 200 req/s |
| 5xx Error Rate | Share of server errors in total traffic | 1 % / 5 % |
| JWT Validation Failure Rate | Share of 401s from `user-service` | 1 % / 5 % |
| Global P99 Latency | 99th-percentile response time | 500 ms / 1 s |

---

### HTTP Traffic
| Panel | What it shows |
|-------|---------------|
| HTTP Request Rate by Service & Method | Per-service req/s split by HTTP verb — reveals which service is bearing load and whether it is reads or writes |
| HTTP Error Rate (4xx / 5xx) by Service | Error share over time — separates client mistakes (4xx) from server faults (5xx) |

---

### Latency
| Panel | What it shows |
|-------|---------------|
| HTTP Latency P50 / P95 / P99 by Service | Three percentile curves per service — P50 is typical, P99 is worst-case felt by users |
| P99 Latency by Endpoint | P99 for every individual endpoint — use this to find the one slow route hiding inside an otherwise healthy service average |

---

### JWT Security — user-service
| Panel | What it shows |
|-------|---------------|
| JWT Validation Failures per Minute | Bar chart of 401 and 403 counts per minute — spikes indicate brute-force attempts, token leaks, or a broken client sending expired tokens |
| Token Generation Latency P50 / P95 / P99 | Response-time percentiles for auth endpoints (`/login`, `/token`, `/auth/**`) — sustained P99 growth here usually means BCrypt work-factor or DB lookup pressure |

---

### JVM & Infrastructure
| Panel | What it shows |
|-------|---------------|
| JVM Heap — Used vs Committed | Used and committed heap bytes — a used curve that continuously approaches committed without GC relief signals a memory leak |
| JVM Thread States | Live, daemon, and blocked thread counts — a rising blocked count indicates lock contention or thread-pool exhaustion |
| GC Pause Time Rate | Seconds spent in GC per second — sustained values above ~50 ms/s cause observable latency spikes |
| HikariCP Connection Pool | Active, idle, and pending connection counts — pending > 0 for more than a few seconds means the pool is undersized for current load |

---

### Top Endpoints
| Panel | What it shows |
|-------|---------------|
| Top 10 Most Called Endpoints | Highest-traffic routes ranked by req/s — primary candidates for caching, pagination, or rate-limiting |
| Top 10 Slowest Endpoints (P99) | Routes with the worst tail latency — start DB query analysis and downstream call tracing here |
| Top 10 Most Error-Prone Endpoints | Routes generating the most 4xx + 5xx responses — pinpoints broken clients or recent regressions to a specific path |
| HTTP Status Code Distribution | Bar chart of 2xx / 3xx / 4xx / 5xx shares — a healthy stack is overwhelmingly green; orange or red bars require immediate triage |

---

### Apdex / SLI
Apdex is an industry-standard single-number satisfaction score.  
Formula: `(satisfied + tolerating / 2) / total` where **T = 0.5 s**.

| Score range | Rating |
|-------------|--------|
| ≥ 0.94 | Excellent |
| ≥ 0.85 | Good |
| ≥ 0.70 | Fair |
| < 0.70 | Poor — investigate immediately |

| Panel | What it shows |
|-------|---------------|
| Apdex — user-service | Current score with colour-coded rating |
| Apdex — contract-service | Current score with colour-coded rating |
| Apdex — client-service | Current score with colour-coded rating |
| Apdex Score over Time | All three services on one chart with a 0.94 threshold line — dips below the line that don't recover within minutes warrant an incident |

---

### Endpoint Performance Table
A sortable, filterable table of **every endpoint** currently receiving traffic.

| Column | Colour logic |
|--------|-------------|
| RPS | — |
| Error % | Yellow ≥ 1 %, Red ≥ 5 % |
| P50 | — |
| P95 | Yellow ≥ 500 ms, Red ≥ 1 s |
| P99 | Yellow ≥ 1 s, Red ≥ 2 s |

Click any column header to re-sort. Use the search filter to narrow by service, method, or URI fragment.
