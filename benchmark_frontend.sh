#!/bin/bash
touch frontend/middleware.ts
start=$(date +%s.%N)
docker build -q -t frontend-multi -f frontend/Dockerfile frontend > /dev/null
end=$(date +%s.%N)
time_multi=$(echo "$end - $start" | bc)
printf "Frontend Multi-Stage Rebuild Time: %.2f s\n" $time_multi

touch frontend/middleware.ts
start=$(date +%s.%N)
docker build -q -t frontend-single -f frontend/Dockerfile.normal frontend > /dev/null
end=$(date +%s.%N)
time_single=$(echo "$end - $start" | bc)
printf "Frontend Single-Stage Rebuild Time: %.2f s\n" $time_single
