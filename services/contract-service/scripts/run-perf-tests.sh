#!/usr/bin/env bash
# =============================================================================
# run-perf-tests.sh — CLM Contract-Service Gatling Load Test Runner
# =============================================================================
#
# Usage:
#   ./scripts/run-perf-tests.sh [OPTIONS]
#
# Options (all optional — sensible defaults shown):
#   --base-url          URL of the application under test
#                       default: https://localhost  (nginx proxy, self-signed cert)
#   --jwt-secret        HMAC-SHA256 JWT secret (must match the running app)
#                       default: dev-shared-jwt-secret-...
#   --ramp-rps          Target arrival rate at the end of ramp-up (users/sec)
#                       default: 5
#   --steady-rps        Constant arrival rate during steady state (users/sec)
#                       default: 5
#   --steady-sec        Duration of the steady-state phase (seconds)
#                       default: 300
#   --stress-rps        Peak arrival rate during the stress surge (users/sec)
#                       default: 25
#   --contract-id-max   Highest contract ID to include in feeder round-robin
#                       default: 50
#   --template-id-max   Highest template ID to include in feeder round-robin
#                       default: 10
#   --archive-dir       Directory where the HTML report + simulation.log are
#                       archived after the run. Useful for CI artefact upload.
#                       default: ./target/perf-results-archive
#   --help              Print this message and exit
#
# Examples:
#   # Quick smoke test against localhost
#   ./scripts/run-perf-tests.sh
#
#   # Staging run — heavier load, explicit secret
#   ./scripts/run-perf-tests.sh \
#     --base-url http://staging.internal:8080 \
#     --jwt-secret "prod-secret-change-me" \
#     --ramp-rps 20 --steady-rps 20 --stress-rps 100 \
#     --contract-id-max 500 --template-id-max 50
#
#   # CI pipeline run (Jenkins / GitHub Actions)
#   ./scripts/run-perf-tests.sh \
#     --base-url "$APP_URL" \
#     --jwt-secret "$JWT_SECRET" \
#     --archive-dir "$WORKSPACE/perf-reports"
# =============================================================================
set -euo pipefail

# ── Defaults ──────────────────────────────────────────────────────────────────
BASE_URL="https://localhost"
JWT_SECRET="dev-shared-jwt-secret-used-by-both-nextauth-and-spring-change-before-deploy"
RAMP_RPS=5
RAMP_SEC=120
STEADY_RPS=5
STEADY_SEC=300
STRESS_RPS=25
STRESS_RAMP_SEC=30
STRESS_HOLD_SEC=60
STRESS_COOL_SEC=30
CONTRACT_ID_MAX=50
TEMPLATE_ID_MAX=10
CLIENT_ID_MAX=30
P95_MS=2000
P99_MS=5000
MAX_ERROR_PCT=1.0

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
ARCHIVE_DIR="${PROJECT_DIR}/target/perf-results-archive"

# ── Argument parsing ──────────────────────────────────────────────────────────
while [[ $# -gt 0 ]]; do
  case $1 in
    --base-url)          BASE_URL="$2";         shift 2 ;;
    --jwt-secret)        JWT_SECRET="$2";       shift 2 ;;
    --ramp-rps)          RAMP_RPS="$2";         shift 2 ;;
    --ramp-sec)          RAMP_SEC="$2";         shift 2 ;;
    --steady-rps)        STEADY_RPS="$2";       shift 2 ;;
    --steady-sec)        STEADY_SEC="$2";       shift 2 ;;
    --stress-rps)        STRESS_RPS="$2";       shift 2 ;;
    --contract-id-max)   CONTRACT_ID_MAX="$2";  shift 2 ;;
    --template-id-max)   TEMPLATE_ID_MAX="$2";  shift 2 ;;
    --client-id-max)     CLIENT_ID_MAX="$2";    shift 2 ;;
    --archive-dir)       ARCHIVE_DIR="$2";      shift 2 ;;
    --p95-ms)            P95_MS="$2";           shift 2 ;;
    --p99-ms)            P99_MS="$2";           shift 2 ;;
    --max-error-pct)     MAX_ERROR_PCT="$2";    shift 2 ;;
    --help)
      head -n 60 "${BASH_SOURCE[0]}" | tail -n +2 | sed 's/^# \?//'
      exit 0
      ;;
    *) echo "Unknown option: $1"; exit 1 ;;
  esac
done

# ── Pre-flight check: can we reach the application? ───────────────────────────
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  CLM Contract-Service — Performance Test"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Target : $BASE_URL"
echo "  Profile: ramp $RAMP_RPS rps → steady $STEADY_RPS rps → stress $STRESS_RPS rps"
echo "  Data   : contractIds 1–$CONTRACT_ID_MAX  templateIds 1–$TEMPLATE_ID_MAX"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# /actuator/** is blocked at the nginx proxy (returns 403).
# Instead we probe nginx itself: GET / hits the Next.js frontend and returns 200
# when the full stack is up.  The contract-service reachability is validated by
# the first Gatling request; a cold-start failure surfaces in the report.
echo "→ Checking nginx proxy at ${BASE_URL}/ ..."
HTTP_STATUS=$(curl -sk -o /dev/null -w "%{http_code}" \
  --connect-timeout 5 --max-time 10 \
  "${BASE_URL}/" || true)

if [[ "$HTTP_STATUS" != "200" ]]; then
  echo ""
  echo "⚠  WARNING: nginx proxy returned HTTP $HTTP_STATUS (expected 200)"
  echo "   Check that the Docker stack is running: docker compose ps"
  echo "   Continuing in 5 seconds — press Ctrl-C to abort."
  sleep 5
else
  echo "   ✓ nginx proxy is UP (HTTP 200)"
fi

# ── Run Gatling ───────────────────────────────────────────────────────────────
echo ""
echo "→ Starting Gatling simulation …"
echo ""

START_TS=$(date +%s)

# Maven passes all -D flags through to the Gatling JVM via the plugin's
# <systemProperties> configuration block in the perf-test profile.
mvn -f "${PROJECT_DIR}/pom.xml" \
  -Pperf-test \
  gatling:test \
  "-Dgatling.baseUrl=${BASE_URL}" \
  "-Dgatling.jwtSecret=${JWT_SECRET}" \
  "-Dgatling.ramp.targetRps=${RAMP_RPS}" \
  "-Dgatling.ramp.durationSec=${RAMP_SEC}" \
  "-Dgatling.steady.rps=${STEADY_RPS}" \
  "-Dgatling.steady.durationSec=${STEADY_SEC}" \
  "-Dgatling.stress.rps=${STRESS_RPS}" \
  "-Dgatling.stress.rampSec=${STRESS_RAMP_SEC}" \
  "-Dgatling.stress.holdSec=${STRESS_HOLD_SEC}" \
  "-Dgatling.stress.cooldownSec=${STRESS_COOL_SEC}" \
  "-Dgatling.assert.p95Ms=${P95_MS}" \
  "-Dgatling.assert.p99Ms=${P99_MS}" \
  "-Dgatling.assert.maxErrorPct=${MAX_ERROR_PCT}" \
  "-Dgatling.data.contractIdMax=${CONTRACT_ID_MAX}" \
  "-Dgatling.data.templateIdMax=${TEMPLATE_ID_MAX}" \
  "-Dgatling.data.clientIdMax=${CLIENT_ID_MAX}" \
  || GATLING_EXIT=$?

END_TS=$(date +%s)
ELAPSED=$(( END_TS - START_TS ))

# ── Archive results ───────────────────────────────────────────────────────────
RESULTS_BASE="${PROJECT_DIR}/target/gatling-results"
RUN_DIR=$(find "${RESULTS_BASE}" -mindepth 1 -maxdepth 1 -type d \
          | sort | tail -n 1)

if [[ -z "$RUN_DIR" ]]; then
  echo "⚠  No results directory found under ${RESULTS_BASE}."
else
  TIMESTAMP=$(basename "$RUN_DIR")
  DEST="${ARCHIVE_DIR}/${TIMESTAMP}"
  mkdir -p "${DEST}"

  echo ""
  echo "→ Archiving results to ${DEST} …"

  # HTML report
  if [[ -f "${RUN_DIR}/index.html" ]]; then
    cp -r "${RUN_DIR}"/* "${DEST}/"
    echo "   ✓ HTML report  : ${DEST}/index.html"
  fi

  # Raw simulation.log (parseable by external tooling)
  if [[ -f "${RUN_DIR}/simulation.log" ]]; then
    gzip -k "${RUN_DIR}/simulation.log" 2>/dev/null || true
    cp "${RUN_DIR}/simulation.log"    "${DEST}/simulation.log"
    [[ -f "${RUN_DIR}/simulation.log.gz" ]] && \
      cp "${RUN_DIR}/simulation.log.gz" "${DEST}/simulation.log.gz"
    echo "   ✓ Raw event log: ${DEST}/simulation.log"
  fi

  # Write a small JSON manifest for CI dashboard consumption
  cat > "${DEST}/manifest.json" <<EOF
{
  "timestamp": "${TIMESTAMP}",
  "baseUrl": "${BASE_URL}",
  "durationSec": ${ELAPSED},
  "profile": {
    "rampRps": ${RAMP_RPS},
    "rampSec": ${RAMP_SEC},
    "steadyRps": ${STEADY_RPS},
    "steadySec": ${STEADY_SEC},
    "stressRps": ${STRESS_RPS},
    "stressHoldSec": ${STRESS_HOLD_SEC}
  },
  "thresholds": {
    "p95Ms": ${P95_MS},
    "p99Ms": ${P99_MS},
    "maxErrorPct": ${MAX_ERROR_PCT}
  }
}
EOF
  echo "   ✓ Manifest     : ${DEST}/manifest.json"
fi

# ── Summary ───────────────────────────────────────────────────────────────────
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Run completed in ${ELAPSED}s"

if [[ "${GATLING_EXIT:-0}" -ne 0 ]]; then
  echo "  Result : ✗ ASSERTION FAILURES (see report for details)"
  echo "  NOTE   : Tests ran to completion — only post-run assertions failed."
else
  echo "  Result : ✓ ALL ASSERTIONS PASSED"
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Propagate Gatling's exit code so CI marks the build correctly.
exit "${GATLING_EXIT:-0}"
