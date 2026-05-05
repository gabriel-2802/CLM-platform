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

### 3. **Grafana** (Visualization)
- **Port**: 3001
- **URL**: http://localhost:3001
- **Default Credentials**: 
  - Username: `admin`
  - Password: `Admin123!` (configurable via `GRAFANA_PASSWORD`)
