#!/bin/bash

################################################################################
# Fast Load Testing Script for CLM Platform Monitoring
#
# High-throughput version: minimizes artificial delays to maximize RPS
#
# Usage:
#   ./scripts/load-test-fast.sh [DURATION_SECONDS] [REQUESTS_PER_SECOND]
#
# Defaults:
#   DURATION: 600 seconds (10 minutes)
#   RPS: 200 requests/sec
################################################################################

set -euo pipefail

# Configuration
DURATION="${1:-600}"
RPS="${2:-200}"
DELAY=$(echo "scale=4; 1/$RPS" | bc)

# Service base URLs
USER_SVC="http://localhost:8083"
CONTRACT_SVC="http://localhost:8081"
CLIENT_SVC="http://localhost:8084"
NEGOTIATION_SVC="http://localhost:8085"

# JWT Token
TOKEN="eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsInJvbGVzIjpbIlJPTEVfQURNSU4iLCJST0xFX1VTRVIiXSwiaWF0IjoxNzc5MTE5MTE4LCJleHAiOjE3ODE3MTExMTh9.ZphsIRLjeJQk4O5TCp1jGDw9IlqyLIRQJV-DTKgg-6ZB4bFbrENIaYAhiEhY9FKvfjR7V6LKDd2avjFnDVkcnw"

# Counters
TOTAL_REQUESTS=0
START_TIME=$(date +%s)

log_info() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1"
}

make_request() {
    local method=$1
    local url=$2
    local data=${3:-}

    TOTAL_REQUESTS=$((TOTAL_REQUESTS + 1))

    if [[ -n "$data" ]]; then
        curl -s -X "$method" \
            -H "Authorization: Bearer $TOKEN" \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$url" >/dev/null 2>&1 || true
    else
        curl -s -X "$method" \
            -H "Authorization: Bearer $TOKEN" \
            "$url" >/dev/null 2>&1 || true
    fi

    sleep "$DELAY"
}

################################################################################
# Compact test functions (high-speed, minimal endpoints)
################################################################################

test_user_service() {
    # GET requests (cache hits)
    make_request GET "$USER_SVC/api/users/me"
    make_request GET "$USER_SVC/api/users/1"
    make_request GET "$USER_SVC/api/users/2"
    
    # Updates
    make_request PUT "$USER_SVC/api/users/2" '{"email":"u2@example.com","name":"User Two"}'
    
    # Failures
    curl -s -X GET "$USER_SVC/api/users/1" >/dev/null 2>&1 || true  # 401 no token
    make_request GET "$USER_SVC/api/users/999"  # 404
}

test_contract_service() {
    # Generate (201)
    make_request POST "$CONTRACT_SVC/api/contracts/generate" \
        '{"templateId":1,"clientId":1,"createdByUserId":1,"fieldValues":{"f1":"v1"}}'
    
    # GETs (cache hits)
    for i in {1..5}; do
        make_request GET "$CONTRACT_SVC/api/contracts/$i"
    done
    
    # List
    make_request GET "$CONTRACT_SVC/api/contracts/all?page=0&size=20"
    make_request GET "$CONTRACT_SVC/api/contracts/all?page=0&size=20"  # Hit
    
    # Updates
    make_request PATCH "$CONTRACT_SVC/api/contracts/1/update-terms" '{"contractValue":50000.00}'
    
    # Failures
    make_request GET "$CONTRACT_SVC/api/contracts/999"  # 404
    make_request POST "$CONTRACT_SVC/api/contracts/search" '{"page":-1,"size":0}'  # 400
}

test_client_service() {
    # GETs (cache)
    make_request GET "$CLIENT_SVC/api/clients?page=0&size=20"
    make_request GET "$CLIENT_SVC/api/clients?page=0&size=20"  # Hit
    
    for i in {1..5}; do
        make_request GET "$CLIENT_SVC/api/clients/$i"
    done
    
    # Create (201)
    make_request POST "$CLIENT_SVC/api/clients" \
        '{"name":"Client X","registrationNumber":"RON123","active":true}'
    
    # Updates
    make_request PUT "$CLIENT_SVC/api/clients/1" \
        '{"name":"Updated","registrationNumber":"RON123","active":true}'
    
    # Failures
    make_request GET "$CLIENT_SVC/api/clients/999"  # 404
}

test_negotiation_service() {
    # Create (201)
    make_request POST "$NEGOTIATION_SVC/api/negotiations" \
        '{"contractId":1,"proposedContractValue":48000.00,"proposedContractEndDate":"2026-12-31","notes":"Terms"}'
    
    # GETs (cache)
    for i in {1..5}; do
        make_request GET "$NEGOTIATION_SVC/api/negotiations/$i"
    done
    
    # By contract/client
    make_request GET "$NEGOTIATION_SVC/api/negotiations/contract/1"
    make_request GET "$NEGOTIATION_SVC/api/negotiations/client/1"
    
    # State transitions
    make_request PATCH "$NEGOTIATION_SVC/api/negotiations/1/accept"
    make_request PATCH "$NEGOTIATION_SVC/api/negotiations/2/reject" '{"notes":"Rejected"}'
    
    # Failures
    make_request GET "$NEGOTIATION_SVC/api/negotiations/999"  # 404
}

################################################################################
# Main Loop
################################################################################

main() {
    log_info "Starting FAST load test (duration: ${DURATION}s, target RPS: ${RPS})"
    echo ""

    END_TIME=$((START_TIME + DURATION))
    ITERATION=0

    while [[ $(date +%s) -lt $END_TIME ]]; do
        ITERATION=$((ITERATION + 1))
        ELAPSED=$(($(date +%s) - START_TIME))

        test_user_service
        test_contract_service
        test_client_service
        test_negotiation_service

        if [[ $((ITERATION % 10)) -eq 0 ]]; then
            log_info "Progress: ${ELAPSED}s / ${DURATION}s | Requests: ${TOTAL_REQUESTS}"
        fi
    done

    FINAL_TIME=$(($(date +%s) - START_TIME))
    ACTUAL_RPS=$(echo "scale=2; $TOTAL_REQUESTS / $FINAL_TIME" | bc)

    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    log_info "Load test completed!"
    log_info "Duration: ${FINAL_TIME}s"
    log_info "Total requests: ${TOTAL_REQUESTS}"
    log_info "Actual RPS: ${ACTUAL_RPS}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "Waiting 30s for Prometheus to scrape..."
    sleep 30
    echo "Now refresh Grafana at http://localhost:3001"
}

# Check dependencies
if ! command -v curl &>/dev/null; then
    echo "ERROR: curl not found"
    exit 1
fi

if ! command -v bc &>/dev/null; then
    echo "ERROR: bc not found"
    exit 1
fi

main
