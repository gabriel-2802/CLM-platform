#!/bin/bash

################################################################################
# AGGRESSIVE Load Testing Script for CLM Platform Monitoring
#
# Fixes empty dashboards by maximizing:
# - Cache hits/misses (repeated GETs)
# - Histogram latency samples (P50/P95/P99)
# - JWT failures (401/403 errors)
# - 5xx errors
#
# Usage:
#   ./scripts/load-test-aggressive.sh [DURATION_SECONDS] [RPS]
#
# Defaults:
#   DURATION: 900 seconds (15 minutes)
#   RPS: 300 requests/sec
################################################################################

set -euo pipefail

# Configuration
DURATION="${1:-900}"
RPS="${2:-300}"
DELAY=$(echo "scale=4; 1/$RPS" | bc)

# Service base URLs
USER_SVC="http://localhost:8083"
CONTRACT_SVC="http://localhost:8081"
CLIENT_SVC="http://localhost:8084"
NEGOTIATION_SVC="http://localhost:8085"

# JWT Token (valid)
TOKEN="eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsInJvbGVzIjpbIlJPTEVfQURNSU4iLCJST0xFX1VTRVIiXSwiaWF0IjoxNzc5MTE5MTE4LCJleHAiOjE3ODE3MTExMTh9.ZphsIRLjeJQk4O5TCp1jGDw9IlqyLIRQJV-DTKgg-6ZB4bFbrENIaYAhiEhY9FKvfjR7V6LKDd2avjFnDVkcnw"

# Counters
TOTAL_REQUESTS=0
START_TIME=$(date +%s)

make_request() {
    local method=$1
    local url=$2
    local data=${3:-}
    local token=${4:-$TOKEN}  # Allow override for JWT failures

    TOTAL_REQUESTS=$((TOTAL_REQUESTS + 1))

    if [[ -n "$data" ]]; then
        curl -s -X "$method" \
            -H "Authorization: Bearer $token" \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$url" >/dev/null 2>&1 || true
    else
        curl -s -X "$method" \
            -H "Authorization: Bearer $token" \
            "$url" >/dev/null 2>&1 || true
    fi

    sleep "$DELAY"
}

################################################################################
# Service test functions - AGGRESSIVE for histogram/cache data
################################################################################

test_user_service() {
    # Repeated GETs for latency histogram and cache hits
    for i in {1..8}; do
        make_request GET "$USER_SVC/api/users/1"
        make_request GET "$USER_SVC/api/users/2"
    done

    # Some updates for variance
    make_request PUT "$USER_SVC/api/users/2" '{"email":"u2@example.com","name":"User Two"}'
    make_request PUT "$USER_SVC/api/users/3" '{"email":"u3@example.com","name":"User Three"}'

    # JWT FAILURES - Generate 401/403 for JWT security metrics
    make_request GET "$USER_SVC/api/users/1" "" "invalid.token"
    make_request GET "$USER_SVC/api/users/2" "" "invalid.token"
    curl -s -X GET "$USER_SVC/api/users/1" >/dev/null 2>&1 || true  # No token = 401

    # 404 errors
    make_request GET "$USER_SVC/api/users/999"
    make_request GET "$USER_SVC/api/users/888"
}

test_contract_service() {
    # AGGRESSIVE: Repeated list calls for CACHE HIT metrics
    for i in {1..15}; do
        make_request GET "$CONTRACT_SVC/api/contracts/all?page=0&size=20"  # Hit cache repeatedly
    done

    # Individual contract GETs (cache hits)
    for i in {1..10}; do
        make_request GET "$CONTRACT_SVC/api/contracts/1"
        make_request GET "$CONTRACT_SVC/api/contracts/2"
        make_request GET "$CONTRACT_SVC/api/contracts/3"
    done

    # Create new contracts for latency variance
    make_request POST "$CONTRACT_SVC/api/contracts/generate" \
        '{"templateId":1,"clientId":1,"createdByUserId":1,"fieldValues":{"f1":"v1"}}'
    make_request POST "$CONTRACT_SVC/api/contracts/generate" \
        '{"templateId":1,"clientId":2,"createdByUserId":1,"fieldValues":{"f2":"v2"}}'

    # Updates
    make_request PATCH "$CONTRACT_SVC/api/contracts/1/update-terms" '{"contractValue":50000.00}'
    make_request PATCH "$CONTRACT_SVC/api/contracts/2/update-terms" '{"contractValue":55000.00}'

    # 404s for error rate
    make_request GET "$CONTRACT_SVC/api/contracts/999"
    make_request GET "$CONTRACT_SVC/api/contracts/888"

    # Bad request (400) for error rate
    make_request POST "$CONTRACT_SVC/api/contracts/search" '{"page":-1,"size":0}'
}

test_client_service() {
    # Repeated GETs for cache and latency data
    for i in {1..15}; do
        make_request GET "$CLIENT_SVC/api/clients?page=0&size=20"  # Cache hit
    done

    for i in {1..8}; do
        make_request GET "$CLIENT_SVC/api/clients/1"
        make_request GET "$CLIENT_SVC/api/clients/2"
        make_request GET "$CLIENT_SVC/api/clients/3"
    done

    # Creates for latency variance
    for i in {1..3}; do
        make_request POST "$CLIENT_SVC/api/clients" \
            '{"name":"Client '$i'","registrationNumber":"RON'$i'","active":true}'
    done

    # Updates
    make_request PUT "$CLIENT_SVC/api/clients/1" \
        '{"name":"Updated 1","registrationNumber":"RON123","active":true}'
    make_request PUT "$CLIENT_SVC/api/clients/2" \
        '{"name":"Updated 2","registrationNumber":"RON124","active":true}'

    # Errors
    make_request GET "$CLIENT_SVC/api/clients/999"
    make_request GET "$CLIENT_SVC/api/clients/888"
}

test_negotiation_service() {
    # Creates for latency
    for i in {1..3}; do
        make_request POST "$NEGOTIATION_SVC/api/negotiations" \
            '{"contractId":'$i',"proposedContractValue":48000.00,"proposedContractEndDate":"2026-12-31","notes":"Terms"}'
    done

    # Repeated GETs for histogram data
    for i in {1..10}; do
        make_request GET "$NEGOTIATION_SVC/api/negotiations/1"
        make_request GET "$NEGOTIATION_SVC/api/negotiations/2"
        make_request GET "$NEGOTIATION_SVC/api/negotiations/3"
    done

    # Queries
    for i in {1..3}; do
        make_request GET "$NEGOTIATION_SVC/api/negotiations/contract/$i"
        make_request GET "$NEGOTIATION_SVC/api/negotiations/client/$i"
    done

    # State transitions
    make_request PATCH "$NEGOTIATION_SVC/api/negotiations/1/accept"
    make_request PATCH "$NEGOTIATION_SVC/api/negotiations/2/reject" '{"notes":"Rejected"}'

    # Errors
    make_request GET "$NEGOTIATION_SVC/api/negotiations/999"
}

################################################################################
# Main Loop
################################################################################

main() {
    echo ""
    echo "╔════════════════════════════════════════════════════════════════╗"
    echo "║         AGGRESSIVE Load Test - Populate ALL Dashboards         ║"
    echo "╠════════════════════════════════════════════════════════════════╣"
    echo "║ Duration: ${DURATION}s | Target RPS: ${RPS} | Interval: ${DELAY}s"
    echo "║                                                                ║"
    echo "║ Will generate:                                                 ║"
    echo "║ • Cache hits/misses (contract-service repeated GETs)           ║"
    echo "║ • Latency histograms (P50/P95/P99 data)                        ║"
    echo "║ • JWT failures (401/403 for security metrics)                  ║"
    echo "║ • 404/400 errors for error rate panels                         ║"
    echo "╚════════════════════════════════════════════════════════════════╝"
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

        if [[ $((ITERATION % 5)) -eq 0 ]]; then
            echo "[$(date +'%H:%M:%S')] Iteration $ITERATION | Elapsed: ${ELAPSED}s / ${DURATION}s | Requests: ${TOTAL_REQUESTS}"
        fi
    done

    FINAL_TIME=$(($(date +%s) - START_TIME))
    ACTUAL_RPS=$(echo "scale=2; $TOTAL_REQUESTS / $FINAL_TIME" | bc)

    echo ""
    echo "╔════════════════════════════════════════════════════════════════╗"
    echo "║                    Load Test Complete! ✓                       ║"
    echo "╠════════════════════════════════════════════════════════════════╣"
    echo "║ Duration:       ${FINAL_TIME}s"
    echo "║ Total Requests: ${TOTAL_REQUESTS}"
    echo "║ Actual RPS:     ${ACTUAL_RPS} req/s"
    echo "║                                                                ║"
    echo "║ Expected metrics to populate:                                  ║"
    echo "║ ✓ Cache Hit Rate (contract-service)                            ║"
    echo "║ ✓ Latency P50/P95/P99 (all services)                           ║"
    echo "║ ✓ Apdex scores (all services)                                  ║"
    echo "║ ✓ JWT Validation Failures                                      ║"
    echo "║ ✓ Error Rates (4xx/5xx)                                        ║"
    echo "╚════════════════════════════════════════════════════════════════╝"
    echo ""
    echo "Waiting 45 seconds for Prometheus to scrape..."
    sleep 45
    echo ""
    echo "✓ Done! Refresh Grafana at http://localhost:3001"
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
