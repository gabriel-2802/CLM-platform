# CLM Platform Monitoring Setup

This document explains how Prometheus and Grafana are wired into the CLM Platform, how to add new metrics, and how to validate dashboards.

## Architecture

```
Services (Micrometer/Prometheus)
├── user-service:8083 (/actuator/prometheus)
├── contract-service:8081 (/actuator/prometheus)
├── client-service:8084 (/actuator/prometheus)
└── negotiation-service:8085 (/actuator/prometheus)
        ↓
    Prometheus:9090 (scrapes metrics)
        ↓
    Grafana:3001 (visualizes metrics)
```

## Quick Start

- Start the testing stack with `make test`.
- Open Grafana at http://localhost:3001.
- Default credentials are `admin` / `Admin123!`.
- Dashboards are provisioned from [monitoring/grafana/dashboards](monitoring/grafana/dashboards).

## Components

### 1) Services (Spring Boot + Actuator)

All services expose Prometheus metrics at `/actuator/prometheus`.

- Ports: 8081 (contract-service), 8083 (user-service), 8084 (client-service), 8085 (negotiation-service)
- Common metrics: HTTP requests, JVM memory/threads, database pools
- Custom metrics: service-specific counters, timers, gauges

### 2) Prometheus (Metrics Scraper)

- URL: http://localhost:9090
- Config: [monitoring/prometheus/prometheus.yml](monitoring/prometheus/prometheus.yml)
- Scrape interval: 15 seconds
- Scrape model: pull via `/actuator/prometheus` on each service

Add new scrape targets by extending `scrape_configs`. Reload without restart:

```
make prometheus-reload
```

Prometheus target labels used by dashboards:

- `job`: service name defined in `scrape_configs` (e.g., `contract-service`)
- `service`: static label added in the scrape config
- `instance`: relabeled from the target address

### 3) Grafana (Visualization)

- URL: http://localhost:3001
- Datasource: [monitoring/grafana/datasources.yml](monitoring/grafana/datasources.yml)
- Dashboards: [monitoring/grafana/dashboards.yml](monitoring/grafana/dashboards.yml)

Grafana loads all JSON dashboards from [monitoring/grafana/dashboards](monitoring/grafana/dashboards). Do not edit dashboards in the UI. Changes will be overwritten on reload.

#### Provisioned Dashboards

- CLM Services — [monitoring/grafana/dashboards/clm-services.json](monitoring/grafana/dashboards/clm-services.json)
- Contract Service — Cache Metrics — [monitoring/grafana/dashboards/contract-cache.json](monitoring/grafana/dashboards/contract-cache.json)

## Contract Cache Metrics

The contract-service uses Caffeine caches for `contracts` and `templates` and publishes Micrometer cache metrics. Prometheus scrapes these from the same `/actuator/prometheus` endpoint.

### Metric Families

- `cache_gets_total` counter with tags `name`, `result=hit|miss`
- `cache_puts_total` counter with tag `name`
- `cache_evictions_total` counter with tag `name`
- `cache_size` gauge with tag `name`

The `name` tag is either `contracts` or `templates`.

### Dashboard

Provisioned dashboard file: [monitoring/grafana/dashboards/contract-cache.json](monitoring/grafana/dashboards/contract-cache.json)

Panels included:

- Hit rate % per cache
- Requests per second (hits vs misses)
- Evictions per second
- Current cache size

### PromQL Queries

Hit rate (contracts):

```
rate(cache_gets_total{name="contracts", result="hit"}[5m])
/
rate(cache_gets_total{name="contracts"}[5m])
* 100
```

Hit rate (templates):

```
rate(cache_gets_total{name="templates", result="hit"}[5m])
/
rate(cache_gets_total{name="templates"}[5m])
* 100
```

Requests per second (hits vs misses):

```
rate(cache_gets_total{name=~"contracts|templates"}[1m])
```

Evictions per second:

```
rate(cache_evictions_total{name=~"contracts|templates"}[5m])
```

Current cache size:

```
cache_size{name=~"contracts|templates"}
```

## Explore the Data

### Prometheus (Raw Metrics)

Open http://localhost:9090 and use the expression browser:

- Verify scrape health: http://localhost:9090/targets
- Inspect cache series names:

```
cache_gets_total
cache_size
```

- Filter by cache name:

```
cache_size{name="contracts"}
```

### Grafana Explore

Use Explore to iterate on queries before saving a panel:

- Data source: Prometheus
- Example: cache hit rate

```
rate(cache_gets_total{name="contracts", result="hit"}[5m])
/
rate(cache_gets_total{name="contracts"}[5m])
* 100
```

## Troubleshooting

- No data in Grafana: check Prometheus target health at http://localhost:9090/targets.
- Missing cache metrics: confirm `CaffeineCacheMetrics.monitor()` is called and scrape endpoint returns `cache_*` metrics.
- Dashboard not appearing: ensure JSON is placed under [monitoring/grafana/dashboards](monitoring/grafana/dashboards) and Grafana has reloaded.
- Prometheus config changes not applied: run `make prometheus-reload`.
