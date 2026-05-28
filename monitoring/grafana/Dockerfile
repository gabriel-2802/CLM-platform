# FIX: pinned to a specific minor version — 'latest' caused the provisioning
#      format to silently break when Grafana 11 shipped new datasource schema.
FROM grafana/grafana:10.4.3

# Provisioning directories already exist in the official image.
COPY datasources.yml /etc/grafana/provisioning/datasources/
COPY dashboards.yml  /etc/grafana/provisioning/dashboards/
COPY dashboards      /var/lib/grafana/dashboards

EXPOSE 3000

HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
  CMD wget -q --spider http://localhost:3000/api/health || exit 1