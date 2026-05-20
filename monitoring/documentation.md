# Monitoring Infrastructure — Technical Documentation

---

## Table of Contents

1. [Architectural Overview](#1-architectural-overview)
2. [Prometheus — Metrics Collection Layer](#2-prometheus--metrics-collection-layer)
3. [Grafana — Visualization & Observability Layer](#3-grafana--visualization--observability-layer)
4. [Dashboard Design & Rationale](#4-dashboard-design--rationale)
5. [Security Architecture](#5-security-architecture)
6. [Infrastructure-as-Code & Operational Design](#6-infrastructure-as-code--operational-design)

---

## 1. Architectural Overview

### 1.1 System Architecture

The CLM Platform monitoring stack is built on the **Prometheus + Grafana** open-source observability duo, which has become the de facto standard for containerized microservice environments. The architecture follows a **pull-based metrics collection model**, where Prometheus periodically scrapes HTTP endpoints exposed by each service, rather than services pushing data to a central collector. This inversion of responsibility is a deliberate design decision: it decouples application code from the monitoring backend, allows Prometheus to detect service downtime (a scrape failure is itself a signal), and eliminates the need for any agent-side buffering logic.

The full data flow is illustrated below:

```
┌───────────────────────────────────────────────────────────────────┐
│                        Docker Network: data-net                   │
│                                                                   │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────────────┐  │
│  │ user-service │   │contract-svc  │   │  client-service      │  │
│  │  :8083       │   │  :8081       │   │  :8084               │  │
│  │/actuator/    │   │/actuator/    │   │ /actuator/prometheus │  │
│  │ prometheus   │   │ prometheus   │   └──────────────────────┘  │
│  └──────┬───────┘   └──────┬───────┘                             │
│         │                  │           ┌──────────────────────┐  │
│         │                  │           │ negotiation-service   │  │
│         │                  │           │  :8085               │  │
│         │   PULL (scrape)  │           │ /actuator/prometheus │  │
│         ▼                  ▼           └──────────┬───────────┘  │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │                 Prometheus  :9090                            │ │
│  │  • scrape interval: 15s                                      │ │
│  │  • retention: 15 days                                        │ │
│  │  • TSDB (time-series database) on prometheus_data volume     │ │
│  └──────────────────────────┬───────────────────────────────────┘ │
│                             │  PromQL queries                     │
│                             ▼                                     │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │                   Grafana  :3000                             │ │
│  │  • datasource: Prometheus (proxy mode, POST method)          │ │
│  │  • 2 provisioned dashboards (file-based IaC)                 │ │
│  └──────────────────────────┬───────────────────────────────────┘ │
│                             │                                     │
└─────────────────────────────┼─────────────────────────────────────┘
                              │  Nginx reverse proxy (TLS termination)
                              ▼
                    https://localhost/grafana
```

> **Architectural note:** All monitoring components reside exclusively on the internal `data-net` Docker network. They are not directly reachable from the host or external networks. The only ingress point is through the Nginx reverse proxy, which terminates TLS and forwards authenticated requests. This ensures that raw metric data and the Grafana UI are never exposed on plaintext ports.

### 1.2 Component Responsibility Matrix

| Component | Role | Version Pinned | Port (Internal) |
|---|---|---|---|
| Spring Boot Actuator (per service) | Exposes `/actuator/prometheus` endpoint | Per service | 8081–8085 |
| Micrometer (per service) | Instruments JVM, HTTP, HikariCP, Caffeine | Per service | — |
| Prometheus | Time-series database + scrape engine | `v2.51.2` | 9090 |
| Grafana | Dashboard UI + PromQL query runner | `10.4.3` | 3000 |
| Nginx | TLS termination + reverse proxy for `/grafana` | Per nginx config | 443 |

### 1.3 Why Prometheus + Grafana

The choice of Prometheus and Grafana over alternatives (e.g., Datadog, InfluxDB + Chronograf, ELK stack) was driven by several constraints specific to this project:

- **Zero external dependencies:** The entire stack runs inside Docker Compose with no SaaS accounts, API keys, or network egress required. This is critical for a development/testing environment.
- **Spring Boot native support:** Micrometer, included as a Spring Boot dependency, has a first-class `prometheus` registry that exposes all standard JVM, HTTP, and HikariCP metrics with zero additional configuration beyond adding the actuator and micrometer-prometheus dependencies.
- **PromQL expressiveness:** The Prometheus Query Language allows arbitrary mathematical transformations on time-series data — rate calculations, histogram quantiles, ratio expressions — which are essential for computing SLI metrics like Apdex scores and error rates without preprocessing data on the application side.
- **Grafana's provisioning model:** Grafana supports declaring dashboards and datasources as YAML and JSON files, which can be version-controlled alongside application code. This aligns with the Infrastructure-as-Code principle applied throughout the CLM platform.

---

## 2. Prometheus — Metrics Collection Layer

### 2.1 Configuration Deep Dive

The Prometheus configuration (`prometheus/prometheus.yml`) governs three concerns: global timing parameters, relabeling rules for target metadata, and the list of scrape jobs.

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  scrape_timeout: 10s
  external_labels:
    monitor: 'clm-monitor'
    environment: 'testing'

rule_files: []

scrape_configs:
  - job_name: 'user-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['user-service:8083']
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance

  - job_name: 'contract-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['contract-service:8081']
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance

  - job_name: 'client-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['client-service:8084']
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance

  - job_name: 'negotiation-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['negotiation-service:8085']
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance
```

### 2.2 Timing Parameters — Rationale

**`scrape_interval: 15s`** — The scrape interval defines the resolution of all time-series data. At 15 seconds, the platform achieves sufficient granularity to detect response latency spikes, sudden error rate increases, and cache eviction bursts without excessive storage overhead. A lower interval (e.g., 5 seconds) would triple the write load on the TSDB and is unnecessary for HTTP-level metrics where individual requests are aggregated across the window. A higher interval (e.g., 60 seconds) would cause the PromQL `rate()` function to produce inaccurate results for short-lived spikes.

**`scrape_timeout: 10s`** — The timeout is set below the scrape interval (10s < 15s), which is a hard requirement: if a scrape takes longer than the interval, Prometheus would begin a new scrape before the previous one completed, causing concurrent requests and potential metric duplication. Setting timeout to 10s ensures a 5-second margin between scrape completion and the next cycle.

**`evaluation_interval: 15s`** — Although no alerting rules are currently defined (`rule_files: []`), the evaluation interval is pre-configured to match the scrape interval. This is the correct configuration for when alerting rules are added: mismatching these intervals can cause alerts to fire on stale data.

### 2.3 External Labels

```yaml
external_labels:
  monitor: 'clm-monitor'
  environment: 'testing'
```

External labels are appended to every time-series written to this Prometheus instance. They serve two purposes:
1. **Federation disambiguation:** If metrics from this instance are ever federated into a parent Prometheus (e.g., a production aggregation layer), the `monitor` label prevents label collisions.
2. **Dashboard filtering:** The `environment: testing` label enables environment-specific PromQL filtering in multi-environment setups where the same dashboard is shared across staging and production.

### 2.4 Relabeling Strategy

The `relabel_configs` block on each job copies the `__address__` label (the scrape target's `host:port`) into the `instance` label. Without this relabeling, Prometheus would auto-generate `instance` as the raw target address, which is verbose for the `static_configs` case. The relabeling step ensures `instance` contains a predictable, human-readable identifier (e.g., `user-service:8083`) that is used as a dimension in all PromQL queries and dashboard legends.

### 2.5 Metric Families Exposed

All four Spring Boot services expose metrics through the Micrometer Prometheus registry at `/actuator/prometheus`. Micrometer maps each metric to a Prometheus **metric type** with a defined aggregation semantics:

| Metric Family | Prometheus Type | Key Labels | Description |
|---|---|---|---|
| `http_server_requests_seconds` | Histogram | `job`, `method`, `uri`, `status` | HTTP request duration; provides `_count`, `_sum`, `_bucket` |
| `jvm_memory_used_bytes` | Gauge | `job`, `area`, `id` | Heap and non-heap memory used |
| `jvm_memory_committed_bytes` | Gauge | `job`, `area`, `id` | Memory committed to JVM from OS |
| `jvm_threads_live_threads` | Gauge | `job` | Current live thread count |
| `jvm_threads_states_threads` | Gauge | `job`, `state` | Thread count per state (BLOCKED, RUNNABLE, etc.) |
| `jvm_gc_pause_seconds` | Histogram | `job`, `action`, `cause` | GC pause duration |
| `hikaricp_connections_active` | Gauge | `job`, `pool` | Active DB connections |
| `hikaricp_connections_idle` | Gauge | `job`, `pool` | Idle DB connections |
| `hikaricp_connections_pending` | Gauge | `job`, `pool` | Threads awaiting a connection |
| `cache_gets_total` | Counter | `job`, `cache`, `result` | Cache get operations; `result=hit\|miss` |
| `cache_evictions_total` | Counter | `job`, `cache` | Cache entries evicted (contract-service only) |
| `cache_size` | Gauge | `job`, `cache` | Current entry count in cache |

> **Note on Histogram metrics:** `http_server_requests_seconds` is stored as a histogram, not a summary. This is the critical distinction for computing accurate percentiles in PromQL: histograms allow server-side aggregation across multiple instances using `histogram_quantile()`, whereas summaries compute percentiles client-side per-instance and cannot be correctly aggregated. Even though each service currently has one instance, the histogram approach is correct by design for future horizontal scaling.

### 2.6 Data Retention & Storage

Prometheus persists time-series data to the `prometheus_data` Docker volume, configured with a **15-day retention window** (`--storage.tsdb.retention.time=15d`). The TSDB (Time Series Database) built into Prometheus uses a block-based storage format: each 2-hour block is a directory containing chunk files (compressed raw samples), an index, tombstones, and metadata. After 15 days, blocks are deleted automatically. The **`--web.enable-lifecycle`** flag is enabled, allowing configuration reloads via `POST /-/reload` without restarting the container — essential for adding new scrape targets without downtime.

---

## 3. Grafana — Visualization & Observability Layer

### 3.1 Datasource Configuration

The Prometheus datasource is provisioned via `grafana/datasources.yml`:

```yaml
apiVersion: 1
datasources:
  - name: Prometheus
    type: prometheus
    uid: prometheus-clm
    access: proxy
    url: http://prometheus:9090
    httpMethod: POST
    isDefault: true
    editable: false
    jsonData:
      timeInterval: "15s"
```

Several decisions embedded in this configuration warrant explanation:

**`uid: prometheus-clm` (stable UID):** Grafana resolves datasource references in dashboard JSON by UID, not by display name. By fixing the UID to `prometheus-clm`, dashboard JSON files can be moved between environments or renamed without breaking the datasource binding. If the UID were auto-generated, every dashboard re-export would produce a different UID, making diff-based version control noisy and error-prone.

**`access: proxy`:** In proxy mode, the Grafana backend (running inside Docker) makes the Prometheus HTTP requests, not the user's browser. This is mandatory in this architecture because Prometheus is on the internal `data-net` network, not reachable from the user's machine. Browser-direct (`access: direct`) would fail because the browser cannot resolve `prometheus:9090`.

**`httpMethod: POST`:** Grafana uses POST requests for PromQL queries rather than GET. The practical reason is URL length: complex multi-series PromQL expressions (especially those with long `job=~"service1|service2|..."` label matchers) can exceed HTTP GET URL length limits (~8 KB in most proxies and browsers). POST encodes the query body, eliminating this constraint.

**`editable: false`:** The datasource cannot be modified through the Grafana UI. Any change requires editing `datasources.yml` in version control and redeploying. This enforces the IaC principle and prevents configuration drift between environments.

### 3.2 Dashboard Provisioning

```yaml
apiVersion: 1
providers:
  - name: 'CLM Dashboards'
    type: file
    folder: 'CLM Platform'
    options:
      path: /var/lib/grafana/dashboards
    updateIntervalSeconds: 30
    disableDeletion: true
    allowUiUpdates: false
```

**`disableDeletion: true`:** Prevents a provisioned dashboard from being deleted through the UI. Without this, an operator could accidentally delete a dashboard that exists only in Grafana's internal SQLite database (not in the provisioned file), and the only recovery would be to restart the Grafana container.

**`allowUiUpdates: false`:** Changes made to a dashboard through the Grafana UI are not persisted. This forces all changes to go through the JSON file in version control. It eliminates the class of bugs where a developer tweaks a dashboard during an incident, the change is never committed, and the tweak is lost on the next container restart.

**`updateIntervalSeconds: 30`:** Grafana polls the dashboard directory every 30 seconds for changes. If a dashboard JSON file is updated on disk (e.g., after a `git pull`), Grafana will automatically reload it within 30 seconds without requiring a container restart.

---

## 4. Dashboard Design & Rationale

### 4.1 CLM Platform Services Dashboard

This is the primary operational dashboard, designed for **cross-service comparison and incident diagnosis**. It is organized into seven functional sections, each targeting a specific observability concern.

#### 4.1.1 Service Variable (Dynamic Filtering)

```
Variable: service
Type: Custom
Query: user-service,contract-service,client-service,negotiation-service
Multi-value: true
Include All: true
Default: All
```

The dashboard exposes a multi-select `$service` variable that propagates into every PromQL query as a label matcher (`job=~"$service"`). This allows an operator to narrow the view to a single service (e.g., during an incident on `contract-service`) or view all services simultaneously. The `All` option selects all services by generating a regex that matches all job labels.

#### 4.1.2 Overview Stats (4 KPI Cards)

The top row presents four single-number KPIs with color-coded thresholds, designed to deliver an immediate health signal within one second of opening the dashboard.

| Panel | PromQL | Threshold Logic |
|---|---|---|
| Total Request Rate | `sum(rate(http_server_requests_seconds_count{job=~"$service"}[5m]))` | Green <50 rps, Yellow 50–200, Red >200 |
| 5xx Error Rate | `sum(rate(...{status=~"5.."}[5m])) / sum(rate(...[5m])) * 100` | Green <1%, Yellow 1–5%, Red >5% |
| JWT Failure Rate (401/403) | `sum(rate(...{job="user-service",status=~"401\|403"}[5m])) / sum(rate(...[5m])) * 100` | Green <1%, Yellow 1–5%, Red >5% |
| Global P99 Latency | `histogram_quantile(0.99, sum by (le) (rate(http_server_requests_seconds_bucket[5m])))` | Green <500ms, Yellow 500ms–1s, Red >1s |

The error rate threshold of 5% for Red is derived from common SLO definitions: a service that returns errors on more than 1 in 20 requests is degraded, not merely noisy.

#### 4.1.3 HTTP Traffic Panels

**Request Rate by Service & Method:**
```promql
sum by (job, method) (
  rate(http_server_requests_seconds_count{job=~"$service"}[5m])
)
```
Breaking down request rate by HTTP verb reveals traffic patterns: a POST rate spike on `contract-service` likely indicates new contract creation, while a GET spike indicates reads (potentially a caching opportunity). Splitting by method is more informative than a per-service total.

**Error Rate by Service (4xx / 5xx separation):**
```promql
sum by (job, status) (
  rate(http_server_requests_seconds_count{job=~"$service", status=~"[45].."}[5m])
) / sum by (job) (
  rate(http_server_requests_seconds_count{job=~"$service"}[5m])
)
```
4xx and 5xx errors have fundamentally different root causes: 4xx errors are client-side (bad requests, authentication failures, not-found), while 5xx errors indicate server-side failures (exceptions, database outages, timeouts). Keeping them separate avoids the situation where a spike in 404s (low severity) masks the absence of 5xx errors, or vice versa.

#### 4.1.4 Latency Percentiles — Why P50/P95/P99

```promql
histogram_quantile(0.99,
  sum by (job, le) (
    rate(http_server_requests_seconds_bucket{job=~"$service"}[5m])
  )
)
```

The dashboard displays three latency percentiles per service:

- **P50 (median):** The experience of the typical user. If P50 is acceptable but P99 is high, the slow requests are outliers — possibly long-running DB queries or GC pauses.
- **P95:** The 95th percentile — used by many SLO definitions as the boundary for "acceptable" latency. 5% of requests exceed this value.
- **P99:** The 99th percentile — the worst-case experience for 1 in 100 requests. High P99 latency is a leading indicator of underlying resource contention (thread pool exhaustion, lock contention, GC pressure).

Averages are not shown because they are statistically misleading: a high volume of fast requests can average out a small number of extremely slow ones, hiding tail latency problems entirely. The histogram_quantile approach is mathematically sound: Prometheus buckets track the cumulative distribution of request durations, and `histogram_quantile()` interpolates the Φ-quantile from the bucket boundaries.

#### 4.1.5 JWT Security Panels (user-service specific)

The dashboard dedicates a section specifically to the `user-service` authentication layer, reflecting its role as the platform's security perimeter.

**JWT Validation Failures (401/403 rate):**
```promql
sum by (status) (
  rate(
    http_server_requests_seconds_count{
      job="user-service",
      uri=~".*(login|token|auth).*",
      status=~"401|403"
    }[1m]
  )
)
```

The 1-minute rate window (rather than the standard 5-minute used elsewhere) is intentional: JWT attacks (credential stuffing, brute-force token probing) manifest as rapid, short-duration spikes in 401 responses. A 5-minute window would smooth these spikes below the visual noise floor, making the panel useless for attack detection. The 1-minute window preserves spike amplitude at the cost of noisier baselines — an acceptable trade-off for a security panel.

**Token Generation Latency:**
```promql
histogram_quantile(0.99,
  sum by (le) (
    rate(
      http_server_requests_seconds_bucket{
        job="user-service",
        uri=~".*(login|token|auth).*"
      }[5m]
    )
  )
)
```
Token generation involves cryptographic operations (JWT signing with HMAC-SHA256 or RSA) that are CPU-intensive. A sudden increase in token generation latency, especially correlated with a CPU spike, may indicate key material issues or an unexpected increase in signing workload.

#### 4.1.6 JVM & Infrastructure Panels

**Heap Memory — Used vs Committed:**
```promql
sum by (job) (jvm_memory_used_bytes{area="heap", job=~"$service"})
sum by (job) (jvm_memory_committed_bytes{area="heap", job=~"$service"})
```
The critical observation here is the *gap* between used and committed memory. If `used` continuously approaches `committed`, the JVM is under heap pressure and is likely running GC frequently. If `used` suddenly plateaus near `committed` without recovering, this is a memory leak indicator — an object retention bug that prevents GC from reclaiming heap space.

**GC Pause Rate:**
```promql
sum by (job) (rate(jvm_gc_pause_seconds_sum{job=~"$service"}[5m]))
```
This metric represents the fraction of real time spent in garbage collection. A value of 0.1 means 10% of clock time is spent pausing all threads for GC — a severe performance problem. Modern JVMs with G1GC or ZGC target sub-1ms pause times; consistent GC pause rates above 0.05 (5%) indicate a tuning problem.

**HikariCP Connection Pool:**
```promql
hikaricp_connections_active{job=~"$service"}
hikaricp_connections_idle{job=~"$service"}
hikaricp_connections_pending{job=~"$service"}
```
The connection pool panels expose database connection lifecycle. **Pending connections** are the critical metric: a nonzero `pending` count means application threads are blocked waiting for a database connection, directly adding latency to every affected request. A rising pending count with a flat active count indicates the pool size (`maximumPoolSize`) is too small for the current workload.

#### 4.1.7 Apdex Score — Application Performance Index

The Apdex (Application Performance Index) is an industry-standard formula that converts a latency distribution into a single satisfaction score in the range [0, 1]. The formula used is:

```
Apdex(T) = (Satisfied + Tolerating / 2) / Total

where:
  Satisfied  = requests with latency ≤ T (threshold)
  Tolerating = requests with latency > T and ≤ 4T
  T = 0.5s (chosen threshold)
  4T = 2.0s
```

In PromQL (for `user-service`, T=0.5s):

```promql
(
  sum(rate(http_server_requests_seconds_bucket{job="user-service", le="0.5"}[5m]))
  + sum(rate(http_server_requests_seconds_bucket{job="user-service", le="2.0"}[5m]))
) / 2
/
sum(rate(http_server_requests_seconds_count{job="user-service"}[5m]))
```

The threshold T=0.5 seconds was chosen based on user experience research: response times below 500ms are perceived as immediate by users interacting with web interfaces. The Apdex rating scale used:

| Apdex Score | Rating | Meaning |
|---|---|---|
| ≥ 0.94 | Excellent | Users are satisfied |
| 0.85 – 0.93 | Good | Minor dissatisfaction |
| 0.70 – 0.84 | Fair | Noticeable degradation |
| < 0.70 | Poor | User experience is unacceptable |

The Apdex is displayed both as a current stat card (instantaneous score) and as a time-series panel (score over time), enabling operators to see whether service quality is improving or degrading over a shift.

#### 4.1.8 Endpoint Performance Table

The most actionable panel in the dashboard is the **Endpoint Performance Table**, which correlates four dimensions per endpoint simultaneously: request rate, error rate, P50 latency, P95 latency, and P99 latency.

```promql
# RPS column
sum by (job, method, uri) (rate(http_server_requests_seconds_count{job=~"$service"}[5m]))

# Error % column
sum by (job, method, uri) (rate(http_server_requests_seconds_count{job=~"$service", status=~"[45].."}[5m]))
/ sum by (job, method, uri) (rate(http_server_requests_seconds_count{job=~"$service"}[5m])) * 100

# P95 column
histogram_quantile(0.95, sum by (job, method, uri, le) (
  rate(http_server_requests_seconds_bucket{job=~"$service"}[5m])
))
```

Conditional cell coloring is applied:
- **Error %:** Yellow ≥ 1%, Red ≥ 5%
- **P95 latency:** Yellow ≥ 500ms, Red ≥ 1s
- **P99 latency:** Yellow ≥ 1s, Red ≥ 2s

This table directly answers: *"Which endpoint do I investigate first?"* — the combination of high RPS and high P99 latency identifies the endpoints causing the most user-visible performance degradation.

---

### 4.2 Contract Service — Cache Metrics Dashboard

The cache dashboard is separated from the main services dashboard for a specific reason: **different operational cadence**. Cache metrics change on a per-request basis (every cache miss or hit immediately updates the counter), whereas JVM and HTTP metrics are meaningful over longer windows. The cache dashboard refreshes every **10 seconds** (vs. 30 seconds for the main dashboard) to capture eviction events and cache warm-up curves with high fidelity.

#### 4.2.1 Hit Rate Panels

The hit rate is computed as a ratio of rates, not a ratio of cumulative totals. This is the correct approach for a counter metric:

```promql
rate(cache_gets_total{cache="contracts", result="hit"}[5m])
/
rate(cache_gets_total{cache="contracts"}[5m])
* 100
```

Using cumulative totals (`cache_gets_total{result="hit"} / cache_gets_total`) would produce a historically-smoothed hit rate that barely changes minute-to-minute, masking a sudden degradation. The `rate()` function computes the per-second average over the 5-minute window, giving a current hit rate rather than a lifetime average.

**Why two separate caches (`contracts` and `templates`):**  
The contract-service uses Caffeine in-memory caching for two distinct entity types with different access patterns. Contract objects are fetched by ID frequently (read-heavy) and have a longer TTL; template objects are shared across contracts and thus have an extremely high natural hit rate after warm-up. Separating the hit rate panels prevents a high template cache hit rate from masking a low contract cache hit rate.

#### 4.2.2 Cache Size and Eviction Rate

```promql
# Evictions per second
rate(cache_evictions_total{cache=~"contracts|templates"}[5m])

# Current size
cache_size{cache=~"contracts|templates"}
```

**Eviction rate** is a leading indicator of cache thrashing: if the cache is evicting entries faster than they are being requested, the cache is effectively not functioning — every eviction will likely be followed by a cache miss and a database read. High eviction rates combined with low hit rates confirm cache configuration issues (cache size too small for the working set).

**Cache size thresholds** are set at:
- Green: < 200 entries (low memory pressure, plenty of headroom)
- Yellow: 200–1000 entries (approaching configuration limits)
- Red: > 1000 entries (approaching or exceeding Caffeine maximum size configuration)

---

## 5. Security Architecture

### 5.1 Network Isolation

The monitoring stack (Prometheus and Grafana) is deployed on the internal `data-net` Docker bridge network. This network is not accessible from the host machine directly; all external access is routed through the Nginx reverse proxy.

```
External network (host) ──► Nginx (443) ──► data-net ──► Grafana (3000)
                                                       ──► Prometheus (9090) [no external route]
```

Prometheus has **no external access path** — it is only reachable by containers on `data-net`. This prevents unauthorized metric scraping, configuration inspection via the Prometheus HTTP API, and query execution against the TSDB.

### 5.2 Grafana Authentication

Grafana is configured with:

```
GF_SECURITY_ADMIN_USER=admin
GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_PASSWORD}  # from .env.testing
GF_USERS_ALLOW_SIGN_UP=false
```

**`GF_USERS_ALLOW_SIGN_UP: false`** disables self-registration. Without this flag, any user who can reach the Grafana login page could create an account and access dashboards. Since all dashboards expose application internals (error rates, latency, cache state), unauthorized read access constitutes an information disclosure vulnerability.

The `GRAFANA_PASSWORD` is injected at container startup from an environment file (`.env.testing`), never hardcoded in the Docker Compose file or version-controlled configuration. This follows the twelve-factor app principle of separating config from code.

### 5.3 Nginx Reverse Proxy Integration

Grafana is served under the `/grafana` subpath:

```
GF_SERVER_ROOT_URL=https://localhost/grafana
GF_SERVER_SERVE_FROM_SUB_PATH=true
```

Nginx terminates TLS at port 443 and proxies requests matching `location /grafana` to `http://grafana:3000`. The TLS certificate is generated locally via `make certs`. All Grafana traffic is encrypted in transit; unencrypted HTTP access to port 3000 is not exposed.

### 5.4 Immutable Dashboard Provenance

The `allowUiUpdates: false` and `disableDeletion: true` provisioning flags enforce an audit trail for all dashboard changes. Because dashboards can only be modified through version-controlled JSON files, every change has an associated git commit with author, timestamp, and diff. This is essential for compliance contexts where observability configuration must be auditable.

---

## 6. Infrastructure-as-Code & Operational Design

### 6.1 Version Pinning Strategy

Both monitoring components are pinned to specific versions:

```dockerfile
# prometheus/Dockerfile
FROM prom/prometheus:v2.51.2

# grafana/Dockerfile
FROM grafana/grafana:10.4.3
```

**Why pin versions?** Grafana, in particular, has a history of breaking changes to dashboard JSON schema and provisioning file formats between major versions. Using `latest` would cause the monitoring stack to silently break after an upstream release: dashboard panels could fail to render, datasource references could break, or the provisioning format could be rejected entirely. Pinned versions ensure that the monitoring stack behaves identically across every deployment of the CLM platform.

### 6.2 Lifecycle Management

Prometheus is started with:
```
--web.enable-lifecycle
```

This enables the `POST /-/reload` endpoint, which re-reads the `prometheus.yml` configuration and restarts the scrape manager without restarting the container. This is critical for production-like operations: restarting the Prometheus container causes a gap in time-series data (no scrapes occur during the restart) and evicts the query engine's label index from memory (requiring re-warming).

### 6.3 Health Checks and Dependency Ordering

Docker Compose `depends_on` with `condition: service_healthy` ensures Prometheus does not attempt to scrape services before they are ready:

```yaml
prometheus:
  depends_on:
    user-service:
      condition: service_healthy
    contract-service:
      condition: service_healthy
    client-service:
      condition: service_healthy

grafana:
  depends_on:
    prometheus:
      condition: service_healthy
```

Without health-check conditions, Docker Compose would start services in dependency order but not wait for readiness. Prometheus would begin scraping immediately and record `scrape_duration_seconds` failures during service startup, creating misleading early data in the TSDB. The health-check approach ensures the first recorded scrape reflects a fully initialized service state.

### 6.4 Dashboard Organization

Both dashboards are provisioned into the **CLM Platform** folder in Grafana:

| Dashboard | UID | Refresh Rate | Time Range | Primary Audience |
|---|---|---|---|---|
| CLM Platform Services | `clm-services` | 30 seconds | Last 1 hour | On-call engineers, developers |
| Contract Service — Cache | `contract-cache` | 10 seconds | Last 6 hours | Backend developers, performance engineers |

The different time ranges reflect the different diagnostic question each dashboard answers:
- **1 hour** for the services dashboard: incident response operates in the window of the last hour. What changed in the last hour? When did the error rate spike?
- **6 hours** for the cache dashboard: cache behavior is observed over longer warm-up periods. Did the hit rate degrade gradually over a shift? When did the first evictions appear?

---

*Documentation generated for CLM Platform — Monitoring Infrastructure*  
*Stack: Prometheus v2.51.2 · Grafana 10.4.3 · Spring Boot Actuator · Micrometer*
