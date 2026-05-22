#!/bin/bash
# nginx
echo "" >> nginx/nginx.conf
start=$(date +%s.%N)
docker build -q -t nginx-test -f nginx/Dockerfile nginx > /dev/null
end=$(date +%s.%N)
time_nginx=$(echo "$end - $start" | bc)

# grafana
echo "" >> monitoring/grafana/datasources.yml
start=$(date +%s.%N)
docker build -q -t grafana-test -f monitoring/grafana/Dockerfile monitoring/grafana > /dev/null
end=$(date +%s.%N)
time_grafana=$(echo "$end - $start" | bc)

# prometheus
echo "" >> monitoring/prometheus/Dockerfile
start=$(date +%s.%N)
docker build -q -t prom-test -f monitoring/prometheus/Dockerfile monitoring/prometheus > /dev/null
end=$(date +%s.%N)
time_prom=$(echo "$end - $start" | bc)

printf "Nginx: %.2f s\n" $time_nginx
printf "Grafana: %.2f s\n" $time_grafana
printf "Prometheus: %.2f s\n" $time_prom
