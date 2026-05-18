#!/bin/bash

################################################################################
# Load Testing Script for CLM Platform Monitoring
#
# Generates realistic HTTP traffic across all monitored services to populate
# Prometheus metrics and Grafana dashboards.
#
# Usage:
#   ./scripts/load-test.sh [DURATION_SECONDS] [REQUESTS_PER_SECOND]
#
# Defaults:
#   DURATION: 300 seconds (5 minutes)
#   RPS: 50 requests/sec
#
# Token: Pre-configured with admin user (can be refreshed via /api/auth/login)
################################################################################

set -euo pipefail

# Configuration
DURATION="${1:-300}"
RPS="${2:-150}"  # Increased from 50 for more data density
HIGH_LOAD="${3:-false}"  # Set to 'true' for aggressive load (RPS x2)

# High-load mode doubles RPS
if [[ "$HIGH_LOAD" == "true" ]]; then
    RPS=$((RPS * 2))
    log_info "HIGH-LOAD MODE ENABLED (RPS doubled to $RPS)"
fi

DELAY=$(echo "scale=4; 1/$RPS" | bc)

# All traffic goes through nginx (services are not exposed directly to host)
BASE="https://localhost"
USER_SVC="$BASE"
CONTRACT_SVC="$BASE"
CLIENT_SVC="$BASE"
NEGOTIATION_SVC="$BASE"

# Fetch a fresh JWT token at startup
TOKEN=$(curl -sk -X POST "$BASE/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"admin@example.com","password":"Admin123!"}' \
    | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

if [[ -z "$TOKEN" ]]; then
    echo "ERROR: Could not obtain JWT token — is nginx/user-service running?"
    exit 1
fi

# Counters
TOTAL_REQUESTS=0
START_TIME=$(date +%s)

################################################################################
# Utility Functions
################################################################################

log_info() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1"
}

make_request() {
    local method=$1
    local url=$2
    local data=${3:-}
    local expected_code=${4:-200}
    local delay_ms=${5:-0}


    TOTAL_REQUESTS=$((TOTAL_REQUESTS + 1))

    if [[ -n "$data" ]]; then
        curl -sk -X "$method" \
            -H "Authorization: Bearer $TOKEN" \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$url" >/dev/null 2>&1 || true
    else
        curl -sk -X "$method" \
            -H "Authorization: Bearer $TOKEN" \
            "$url" >/dev/null 2>&1 || true
    fi

    sleep "$DELAY"
}

################################################################################
# User Service Endpoints
################################################################################

test_user_service() {
    log_info "Testing user-service endpoints..."

    # nginx routes /api/auth/* directly to user-service (JWT token generation metrics)
    make_request POST "$BASE/api/auth/login" '{"email":"admin@example.com","password":"Admin123!"}'
    make_request POST "$BASE/api/auth/login" '{"email":"admin@example.com","password":"Admin123!"}'
    make_request POST "$BASE/api/auth/login" '{"email":"admin@example.com","password":"Admin123!"}'
    make_request POST "$BASE/api/auth/login" '{"email":"admin@example.com","password":"Admin123!"}'
    make_request POST "$BASE/api/auth/login" '{"email":"admin@example.com","password":"Admin123!"}'

    # Failed logins (401 - bad credentials — JWT failure metrics)
    make_request POST "$BASE/api/auth/login" '{"email":"wrong@example.com","password":"BadPass!"}'
    make_request POST "$BASE/api/auth/login" '{"email":"admin@example.com","password":"WrongPass!"}'
    make_request POST "$BASE/api/auth/login" '{"email":"notexist@example.com","password":"Whatever!"}'

    # JWT validation failures on contract-service (invalid/missing tokens)
    curl -sk -X GET "$BASE/api/contracts/all?page=0&size=5" >/dev/null 2>&1 || true
    curl -sk -X GET -H "Authorization: Bearer invalid.token.here" "$BASE/api/contracts/all?page=0&size=5" >/dev/null 2>&1 || true
    curl -sk -X GET -H "Authorization: Bearer expired.fake.token" "$BASE/api/clients?page=0&size=5" >/dev/null 2>&1 || true
    curl -sk -X GET -H "Authorization: Bearer badtoken" "$BASE/api/negotiations/contract/1" >/dev/null 2>&1 || true

}

################################################################################
# Contract Service Endpoints
################################################################################

test_contract_service() {
    log_info "Testing contract-service endpoints..."

    # Templates (cache hits — GET /api/templates/{id} is @Cacheable)
    make_request GET "$CONTRACT_SVC/api/templates"
    for i in {1..5}; do
        make_request GET "$CONTRACT_SVC/api/templates/$i"
    done
    # Repeat for cache hits
    for i in {1..5}; do
        make_request GET "$CONTRACT_SVC/api/templates/$i"
    done

    # Generate contracts (201)
    for i in {1..3}; do
        make_request POST "$CONTRACT_SVC/api/contracts/generate" \
            "{
                \"templateId\":$i,
                \"clientId\":$i,
                \"createdByUserId\":1,
                \"fieldValues\":{\"field1\":\"value1\",\"field2\":\"value2\"}
            }" 201
    done

    # Get contracts — first pass (cache miss, then hit on repeat)
    for i in {1..10}; do
        make_request GET "$CONTRACT_SVC/api/contracts/$i"
    done
    # Repeat same IDs to generate cache hits
    for i in {1..10}; do
        make_request GET "$CONTRACT_SVC/api/contracts/$i"
    done
    for i in {1..10}; do
        make_request GET "$CONTRACT_SVC/api/contracts/$i"
    done

    # List contracts with various pagination
    make_request GET "$CONTRACT_SVC/api/contracts/all?page=0&size=20"
    make_request GET "$CONTRACT_SVC/api/contracts/all?page=0&size=20"  # Repeat for cache hits
    make_request GET "$CONTRACT_SVC/api/contracts/all?page=1&size=20"
    make_request GET "$CONTRACT_SVC/api/contracts/all?page=2&size=50"

    # Search contracts (POST)
    make_request POST "$CONTRACT_SVC/api/contracts/search" \
        '{"page":0,"size":20}'
    make_request POST "$CONTRACT_SVC/api/contracts/search" \
        '{"page":0,"size":50,"clientId":1}'

    # Update contract terms (PATCH)
    make_request PATCH "$CONTRACT_SVC/api/contracts/1/update-terms" \
        '{"contractValue":50000.00}'
    make_request PATCH "$CONTRACT_SVC/api/contracts/2/update-terms" \
        '{"contractValue":75000.00}'

    # Terminate contract (PUT/204)
    make_request PUT "$CONTRACT_SVC/api/contracts/2/terminate" \
        '{"terminationDate":"2024-12-31","terminationReason":"Mutual agreement"}'
    make_request PUT "$CONTRACT_SVC/api/contracts/3/terminate" \
        '{"terminationDate":"2024-06-30","terminationReason":"End of contract"}'

    # Toggle auto-renewal (multiple times)
    make_request PUT "$CONTRACT_SVC/api/contracts/3/toggle-auto-renew"
    make_request PUT "$CONTRACT_SVC/api/contracts/4/toggle-auto-renew"

    # Renegotiate contract (PATCH)
    make_request PATCH "$CONTRACT_SVC/api/contracts/4/renegotiate" \
        '{"newContractValue":45000.00,"newEndDate":"2026-12-31"}'

    # ============ FAILURES (4xx) ============
    
    # 404 - Not found (multiple)
    make_request GET "$CONTRACT_SVC/api/contracts/999"
    make_request GET "$CONTRACT_SVC/api/contracts/8888"
    make_request PUT "$CONTRACT_SVC/api/contracts/7777/terminate" \
        '{"terminationDate":"2024-12-31","terminationReason":"N/A"}'

    # 400 - Bad request (invalid search)
    make_request POST "$CONTRACT_SVC/api/contracts/search" \
        '{"page":-1,"size":0}'

    # Large page sizes (naturally slower — real latency)
    make_request GET "$CONTRACT_SVC/api/contracts/all?page=0&size=100"
    make_request GET "$CONTRACT_SVC/api/contracts/all?page=1&size=100"
    make_request POST "$CONTRACT_SVC/api/contracts/search" '{"page":0,"size":100}'
}

################################################################################
# Client Service Endpoints
################################################################################

test_client_service() {
    log_info "Testing client-service endpoints..."

    # List template fields (GET)
    make_request GET "$CLIENT_SVC/api/clients/template-fields"
    make_request GET "$CLIENT_SVC/api/clients/template-fields"  # Repeat for cache

    # List clients (pagination, multiple times for cache)
    make_request GET "$CLIENT_SVC/api/clients?page=0&size=20"
    make_request GET "$CLIENT_SVC/api/clients?page=0&size=20"  # Cache hit
    make_request GET "$CLIENT_SVC/api/clients?page=1&size=20"
    make_request GET "$CLIENT_SVC/api/clients?page=0&size=50"
    make_request GET "$CLIENT_SVC/api/clients?page=0&size=100"

    # Get single clients (repeated for cache hits)
    for i in {1..15}; do
        make_request GET "$CLIENT_SVC/api/clients/$i"
    done

    # Create clients (201)
    for i in {1..3}; do
        make_request POST "$CLIENT_SVC/api/clients" \
            "{\"name\":\"New Client $i\",\"registrationNumber\":\"RON1234$i\",\"active\":true}"
    done

    # Update clients (PUT)
    make_request PUT "$CLIENT_SVC/api/clients/1" \
        '{"name":"Updated Client","registrationNumber":"RON12345","active":true}'
    make_request PUT "$CLIENT_SVC/api/clients/2" \
        '{"name":"Another Update","registrationNumber":"RON99999","active":false}'

    # Partial updates (PATCH)
    make_request PATCH "$CLIENT_SVC/api/clients/2" '{"active":false}'
    make_request PATCH "$CLIENT_SVC/api/clients/3" '{"active":true}'

    # Client assignments
    make_request GET "$CLIENT_SVC/api/clients/1/users"
    make_request POST "$CLIENT_SVC/api/clients/1/users/1" "" 201
    make_request POST "$CLIENT_SVC/api/clients/2/users/2" "" 201
    make_request DELETE "$CLIENT_SVC/api/clients/1/users/1"

    # Histories (financial)
    make_request GET "$CLIENT_SVC/api/clients/1/histories"
    make_request GET "$CLIENT_SVC/api/clients/1/histories"  # Cache hit
    make_request GET "$CLIENT_SVC/api/clients/1/histories/2024"
    make_request GET "$CLIENT_SVC/api/clients/2/histories/2023"
    make_request PUT "$CLIENT_SVC/api/clients/1/histories/2024" \
        '{"balance":100000.00,"spent":45000.00}'
    make_request PUT "$CLIENT_SVC/api/clients/2/histories/2023" \
        '{"balance":200000.00,"spent":120000.00}'
    make_request DELETE "$CLIENT_SVC/api/clients/1/histories/2023"

    # Work points
    make_request GET "$CLIENT_SVC/api/clients/1/work-points"
    make_request GET "$CLIENT_SVC/api/clients/1/work-points"  # Cache hit
    make_request GET "$CLIENT_SVC/api/clients/1/work-points/1"
    make_request GET "$CLIENT_SVC/api/clients/2/work-points/1"
    make_request POST "$CLIENT_SVC/api/clients/1/work-points" \
        '{"address":"Work Address","city":"Bucharest","postalCode":"010000"}'
    make_request PUT "$CLIENT_SVC/api/clients/1/work-points/1" \
        '{"address":"Updated Address","city":"Bucharest","postalCode":"010000"}'
    make_request DELETE "$CLIENT_SVC/api/clients/1/work-points/1"

    # Details
    make_request GET "$CLIENT_SVC/api/clients/1/details"
    make_request GET "$CLIENT_SVC/api/clients/1/details"  # Cache hit
    make_request PUT "$CLIENT_SVC/api/clients/1/details" \
        '{"certifications":"ISO 9001","licenses":"Active"}'
    make_request PATCH "$CLIENT_SVC/api/clients/1/details" \
        '{"notes":"Some notes"}'

    # Delete clients
    make_request DELETE "$CLIENT_SVC/api/clients/10"
    make_request DELETE "$CLIENT_SVC/api/clients/11"

    # ============ FAILURES (4xx/5xx) ============
    
    # 404 - Not found
    make_request GET "$CLIENT_SVC/api/clients/999"
    make_request GET "$CLIENT_SVC/api/clients/8888/histories"
    make_request GET "$CLIENT_SVC/api/clients/7777/work-points"
    make_request DELETE "$CLIENT_SVC/api/clients/6666"

    # 400 - Bad request
    make_request POST "$CLIENT_SVC/api/clients" '{"name":"","registrationNumber":""}'

    # Large page sizes (naturally slower — real latency)
    make_request GET "$CLIENT_SVC/api/clients?page=0&size=100"
    make_request GET "$CLIENT_SVC/api/clients?page=0&size=200"
}

################################################################################
# Negotiation Service Endpoints
################################################################################

test_negotiation_service() {
    log_info "Testing negotiation-service endpoints..."

    # Create negotiations (201, multiple)
    for i in {1..3}; do
        make_request POST "$NEGOTIATION_SVC/api/negotiations" \
            "{\"contractId\":$i,\"proposedContractValue\":48000.00,\"proposedContractEndDate\":\"2026-12-31\",\"notes\":\"Proposing new terms $i\"}"
    done

    # Get negotiations (cache hits)
    for i in {1..15}; do
        make_request GET "$NEGOTIATION_SVC/api/negotiations/$i"
    done

    # Get by contract (multiple times)
    for i in {1..5}; do
        make_request GET "$NEGOTIATION_SVC/api/negotiations/contract/$i"
    done

    # Get by client
    for i in {1..5}; do
        make_request GET "$NEGOTIATION_SVC/api/negotiations/client/$i"
    done

    # Accept negotiations (PATCH - 200)
    make_request PATCH "$NEGOTIATION_SVC/api/negotiations/1/accept"
    make_request PATCH "$NEGOTIATION_SVC/api/negotiations/3/accept"

    # Reject negotiations (PATCH - 200)
    make_request PATCH "$NEGOTIATION_SVC/api/negotiations/2/reject" \
        '{"notes":"Terms not acceptable"}'
    make_request PATCH "$NEGOTIATION_SVC/api/negotiations/4/reject" \
        '{"notes":"Unacceptable terms"}'

    # Update notes (PATCH)
    make_request PATCH "$NEGOTIATION_SVC/api/negotiations/5/notes" \
        '{"notes":"Revised notes"}'
    make_request PATCH "$NEGOTIATION_SVC/api/negotiations/6/notes" \
        '{"notes":"Updated terms"}'

    # Trigger notifications (multiple)
    make_request POST "$NEGOTIATION_SVC/api/negotiations/notifications/trigger"
    make_request POST "$NEGOTIATION_SVC/api/negotiations/notifications/trigger"

    # ============ FAILURES (4xx) ============
    
    # 404 errors
    make_request GET "$NEGOTIATION_SVC/api/negotiations/999"
    make_request GET "$NEGOTIATION_SVC/api/negotiations/8888"
    make_request GET "$NEGOTIATION_SVC/api/negotiations/contract/999"
    make_request GET "$NEGOTIATION_SVC/api/negotiations/client/999"

    # 400 - Bad request
    make_request POST "$NEGOTIATION_SVC/api/negotiations" \
        '{"contractId":-1,"proposedContractValue":-100,"proposedContractEndDate":"invalid"}'

}

################################################################################
# Main Load Testing Loop
################################################################################

main() {
    log_info "Starting load test (duration: ${DURATION}s, target RPS: ${RPS})"
    log_info "Services via nginx: https://localhost -> user/contract/client/negotiation"
    echo ""

    END_TIME=$((START_TIME + DURATION))
    ITERATION=0

    while [[ $(date +%s) -lt $END_TIME ]]; do
        ITERATION=$((ITERATION + 1))
        ELAPSED=$(($(date +%s) - START_TIME))

        # Rotate through all services
        test_user_service
        test_contract_service
        test_client_service
        test_negotiation_service

        # Progress indicator
        if [[ $((ITERATION % 5)) -eq 0 ]]; then
            log_info "Progress: ${ELAPSED}s / ${DURATION}s | Total requests: ${TOTAL_REQUESTS}"
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
    echo "Metrics now available in Grafana dashboards:"
    echo "  • HTTP Request Rate"
    echo "  • Error Rates (4xx, 5xx)"
    echo "  • Latency Percentiles (P50, P95, P99)"
    echo "  • Cache Hit/Miss Rates"
    echo "  • JWT Validation Metrics"
    echo "  • Apdex Scores"
    echo "  • JVM & Connection Pool Metrics"
    echo ""
    echo "Access Grafana at: https://localhost/grafana/"
    echo "Prometheus: internal (via data-net)"
}

# Check dependencies
if ! command -v curl &>/dev/null; then
    echo "ERROR: curl not found. Please install curl."
    exit 1
fi

if ! command -v bc &>/dev/null; then
    echo "ERROR: bc not found. Please install bc."
    exit 1
fi

# Run main function
main
