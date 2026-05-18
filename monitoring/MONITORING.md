# CLM Platform Monitoring Setup

This document describes the monitoring stack for the CLM Platform using Prometheus and Grafana.

## Architecture

```
Services (with Micrometer/Prometheus)
├── user-service:8083 (/actuator/prometheus)
├── contract-service:8081 (/actuator/prometheus)
└── client-service:8084 (/actuator/prometheus)
        ↓
    Prometheus:9090 (scrapes metrics)
        ↓
    Grafana:3001 (visualizes metrics)
```

## Components

### 1. **Services** (Spring Boot with Actuator)
Each service exposes metrics via Spring Boot Actuator:
- **Endpoint**: `/actuator/prometheus`
- **Port**: Service-specific (8081, 8083, 8084)
- **Metrics Exposed**:
  - HTTP request metrics
  - JVM memory/thread metrics
  - Database connection pools
  - Custom application metrics

### 2. **Prometheus** (Metrics Scraper)
- **Port**: 9090
- **URL**: http://localhost:9090
- **Config**: `prometheus.yml`
- **Function**: Scrapes metrics from all services every 15 seconds

### 4. **Contract Cache Metrics**
Contract-service exposes cache metrics on the same `/actuator/prometheus` endpoint.
Grafana dashboard: `monitoring/grafana/dashboards/contract-cache.json`.

Key queries:
- Hit rate (contracts):
  - `rate(cache_gets_total{name="contracts", result="hit"}[5m]) / rate(cache_gets_total{name="contracts"}[5m]) * 100`
- Hit rate (templates):
  - `rate(cache_gets_total{name="templates", result="hit"}[5m]) / rate(cache_gets_total{name="templates"}[5m]) * 100`
- RPS (hits vs misses):
  - `rate(cache_gets_total{name=~"contracts|templates"}[1m])`
- Evictions per second:
  - `rate(cache_evictions_total{name=~"contracts|templates"}[5m])`
- Cache size:
  - `cache_size{name=~"contracts|templates"}`

### 3. **Grafana** (Visualization)
- **Port**: 3001
- **URL**: http://localhost:3001
- **Default Credentials**: 
  - Username: `admin`
  - Password: `Admin123!` (configurable via `GRAFANA_PASSWORD`)
