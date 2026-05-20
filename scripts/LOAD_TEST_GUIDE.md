# Load Testing Guide

This guide explains how to populate Prometheus metrics and Grafana dashboards with realistic traffic patterns using the load testing script.

## Quick Start

### 1. Start the monitoring stack

```bash
make test
```

This starts all services, Prometheus, and Grafana.

### 2. Run the load test

```bash
# Default: 300 seconds, 50 requests/sec
./scripts/load-test.sh

# Custom: 600 seconds, 100 requests/sec
./scripts/load-test.sh 600 100
```

### 3. Open Grafana

Navigate to http://localhost:3001 and log in with `admin` / `Admin123!`

Select the **"CLM Platform Services"** or **"Contract Service — Cache Metrics"** dashboard to view the populated metrics.

---

## Script Details

### What It Does

The script generates **realistic HTTP traffic** across all four monitored services:

| Service | Port | Endpoints Tested |
|---------|------|------------------|
| **user-service** | 8083 | Auth (login/register), user CRUD, admin roles |
| **contract-service** | 8081 | Contract generation, search, termination, renegotiation |
| **client-service** | 8084 | Client CRUD, assignments, histories, work points, details |
| **negotiation-service** | 8085 | Negotiation CRUD, accept/reject, state transitions |

### Metrics Generated

✅ **HTTP Request Rate** — all services, all methods  
✅ **2xx / 4xx / 5xx Error Rates** — realistic error distributions  
✅ **Latency Percentiles** — P50, P95, P99 with artificial slow paths  
✅ **Cache Hit/Miss Rates** — contract cache (template & contracts)  
✅ **JWT Validation Metrics** — auth failures, token generation  
✅ **Apdex Scores** — application performance index (T=0.5s)  
✅ **JVM Metrics** — heap memory, threads, GC pause  
✅ **Database Connection Pools** — HikariCP active/idle/pending  

### Traffic Patterns

1. **Successful requests (2xx)** — 80% of traffic
   - GET /api/contracts/1, /api/clients/1, /api/users/me
   - POST /api/contracts/generate (201)
   - PATCH state transitions (accept/reject/update)

2. **Client errors (4xx)** — ~15% of traffic
   - GET /api/contracts/999 (404)
   - Invalid JWT tokens (401/403)
   - Validation failures (400)

3. **Slow requests** — 5% of traffic
   - List endpoints with large page sizes (P95/P99)
   - Simulated database query delays (150–250 ms)

### Request Rate

- **Default**: 50 requests/sec (sustainable for test environment)
- **Custom**: Pass as second argument (e.g., `./scripts/load-test.sh 300 100` for 100 RPS)

The script respects the target RPS by inserting proportional delays between requests.

---

## Usage Examples

### Example 1: Quick 5-minute test

```bash
./scripts/load-test.sh 300 50
```

- Duration: 5 minutes
- Target rate: 50 req/sec
- Expected total requests: ~15,000

### Example 2: Long-running test for SLA tracking

```bash
./scripts/load-test.sh 1800 80
```

- Duration: 30 minutes
- Target rate: 80 req/sec
- Expected total requests: ~144,000
- Dashboards will show latency trends, GC pauses, connection pool exhaustion

### Example 3: Stress test the system

```bash
./scripts/load-test.sh 600 200
```

- Duration: 10 minutes
- Target rate: 200 req/sec (aggressive)
- Good for identifying bottlenecks and high-latency tail

---

## Viewing Results in Grafana

After running the script, open http://localhost:3001 and navigate to:

### CLM Platform Services (Main Dashboard)

**Overview Row:**
- Total Request Rate (RPS)
- 5xx Error Rate
- JWT Validation Failure Rate
- Global P99 Latency

**HTTP Traffic Row:**
- Request Rate by Service & Method
- Error Rate (4xx / 5xx) by Service

**Latency Row:**
- HTTP Latency P50 / P95 / P99 by Service
- P99 Latency by Endpoint (identifies slow paths)

**JWT Security Row (user-service only):**
- JWT Validation Failures per Minute (401 / 403)
- Token Generation Latency P50 / P95 / P99

**JVM & Infrastructure Row:**
- JVM Heap Used vs Committed
- JVM Thread States (live, daemon, blocked)
- GC Pause Time Rate
- HikariCP Connection Pool (active, idle, pending)

**Top Endpoints Row:**
- Top 10 Most Called Endpoints
- Top 10 Slowest Endpoints (P99)
- Top 10 Most Error-Prone Endpoints

**Apdex / SLI Row:**
- Apdex Score per Service (T=0.5s)
- Apdex Score Trend (shows when dips below 0.94)

**Endpoint Performance Table:**
- Full endpoint inventory with RPS, Error %, P50/P95/P99
- Color-coded thresholds (green/yellow/red)
- Sortable by any column

### Contract Service — Cache Metrics

**Cache Hit Rate %:**
- Contracts cache
- Templates cache
- Thresholds: green ≥90%, yellow 80–90%, red <50%

**Requests per Second (Hits vs Misses):**
- Time series split by result tag
- Shows cache effectiveness

**Evictions per Second:**
- Spike indicates entries being evicted before TTL
- Consider increasing `maximumSize` if spikes are frequent

**Current Cache Size:**
- Entries in memory (max 1,000 for contracts, 200 for templates)
- Helps detect memory pressure

---

## Troubleshooting

### Script exits early or no metrics appear

1. **Check services are running:**
   ```bash
   docker compose -f docker-compose.testing.yml ps
   ```

2. **Verify ports are open:**
   ```bash
   curl -s http://localhost:8083/actuator/prometheus | head -20
   curl -s http://localhost:8081/actuator/prometheus | head -20
   ```

3. **Check Prometheus scrapes services:**
   - Open http://localhost:9090/targets
   - All services should show green "UP" status

### Grafana shows "No data"

1. **Wait 30–60 seconds** for Prometheus to scrape and ingest metrics
2. **Refresh the dashboard** (Ctrl+R or Cmd+R)
3. **Check the time range** — ensure it covers when you ran the load test (default is 1 hour)

### Metrics appear but are flat/boring

- **Increase RPS**: Run `./scripts/load-test.sh 300 200` for more traffic
- **Increase duration**: Run longer tests to see latency trends and GC cycles
- **Run multiple times**: Successive runs will show metrics building over time

---

## Performance Tips

### For realistic production-like metrics:

1. **Run for at least 5 minutes** to see GC cycles and connection pool behavior
2. **Use RPS 50–100** — above 200 may stress the test environment
3. **Run multiple times daily** — metrics reset when Prometheus restarts
4. **Combine with manual testing** — the script generates happy-path traffic; add error scenarios manually

### To generate more diverse latency data:

Edit the script and increase artificial delays in `make_request` calls:

```bash
# Add 250 ms delay for slow path simulation
make_request GET "$CONTRACT_SVC/api/contracts/all?page=0&size=100" "" 200 250
```

---

## PromQL Queries for Exploration

Copy these into Prometheus expression browser (http://localhost:9090) to experiment:

**Request rate by service:**
```promql
sum by (job) (rate(http_server_requests_seconds_count[5m]))
```

**Error rate (4xx + 5xx):**
```promql
sum by (job) (rate(http_server_requests_seconds_count{status=~"[45].."}[5m]))
```

**Cache hit rate (contract-service):**
```promql
rate(cache_gets_total{name="contracts", result="hit"}[5m])
/
rate(cache_gets_total{name="contracts"}[5m])
* 100
```

**P99 latency by service:**
```promql
histogram_quantile(0.99, sum by (job, le) (rate(http_server_requests_seconds_bucket[5m])))
```

**JVM heap usage:**
```promql
jvm_memory_used_bytes{area="heap"}
```

---

## Clean Up

To reset all metrics:

```bash
make nuke-test  # Stops stack and deletes volumes (data loss!)
make test       # Restart fresh
```

Then run the load test again.
