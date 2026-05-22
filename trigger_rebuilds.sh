#!/bin/bash
# Modify file contents by appending a blank line
echo "" >> frontend/middleware.ts
start=$(date +%s.%N)
docker build -t frontend-multi -f frontend/Dockerfile frontend > /dev/null
end=$(date +%s.%N)
echo "Frontend Multi-Stage Rebuild Time: $(echo "$end - $start" | bc) s"

echo "" >> frontend/middleware.ts
start=$(date +%s.%N)
docker build -t frontend-single -f frontend/Dockerfile.normal frontend > /dev/null
end=$(date +%s.%N)
echo "Frontend Single-Stage Rebuild Time: $(echo "$end - $start" | bc) s"

echo "" >> services/user-service/src/main/resources/application-test.yml
start=$(date +%s.%N)
docker build -t user-multi -f services/user-service/Dockerfile services/user-service > /dev/null
end=$(date +%s.%N)
echo "User Multi-Stage Rebuild Time: $(echo "$end - $start" | bc) s"

echo "" >> services/user-service/src/main/resources/application-test.yml
start=$(date +%s.%N)
docker build -t user-single -f services/user-service/Dockerfile.normal services/user-service > /dev/null
end=$(date +%s.%N)
echo "User Single-Stage Rebuild Time: $(echo "$end - $start" | bc) s"

