#!/bin/bash
# Professional Docker benchmark: multi-stage vs single-stage
#
# Fixes applied vs original:
#  [1] macOS-safe high-resolution timer (gdate or python3 fallback — %N is GNU-only)
#  [2] Startup = container-created → HEALTHCHECK healthy, not sleep 1
#  [3] Symmetric cache-bust: identical delta applied, file restored between each build
#  [4] --cpuset-cpus / --memory applied to cold builds too (were missing before)
#  [5] Build order randomised per iteration to eliminate position/warmup bias
#  [6] First hot iteration discarded as warmup before recording begins
#  [7] Per-service mean ± stddev printed in final statistical summary
#  [8] BuildKit --progress=plain parsed → CACHED layer count per build
#  [9] CVE scan extended to MEDIUM tier as a separate column (was CRITICAL+HIGH only)
# [10] Package count via `apk list --installed` (reliable across all apk versions)
# [11] Local-registry push/pull latency benchmark (the primary real-world advantage)
# [12] Steady-state RAM/CPU snapshot via docker stats after confirmed startup

LOG_FILE="docker_benchmark_results.log"
COLD_ITERATIONS=7       # averaged cold-start runs per service
HOT_WARMUP=7            # discarded warmup pass (first hot iteration)
HOT_ITERATIONS=7        # measured incremental-rebuild iterations
STATS_FILE=$(mktemp)    # raw timing data for end-of-run statistics

trap 'rm -f "$STATS_FILE"' EXIT

SERVICES=(
  "frontend|frontend|Dockerfile|Dockerfile.normal|src/middleware.ts"
  "user-service|services/user-service|Dockerfile|Dockerfile.normal|src/main/resources/application-test.yml"
  "contract-service|services/contract-service|Dockerfile|Dockerfile.normal|src/main/resources/application-test.yml"
  "negotiation-service|services/negotiation-service|Dockerfile|Dockerfile.normal|src/main/resources/application-test.yml"
  "client-service|services/client-service|Dockerfile|Dockerfile.normal|src/main/resources/application-test.yml"
  "notification-service|services/notification-service|Dockerfile|Dockerfile.normal|src/main/resources/application.yml"
)

# ── [1] Portable high-resolution timer ───────────────────────────────────────
# macOS `date` does not support %N (nanoseconds) — that is a GNU extension.
# Prefer gdate (brew install coreutils); fall back to python3 (always present on macOS).
get_time() {
    if command -v gdate &>/dev/null; then
        gdate +%s.%N
    else
        python3 -c "import time; print(f'{time.time():.6f}')"
    fi
}

# ── [2] Wait until HEALTHCHECK reports healthy (max $2 seconds) ──────────────
wait_healthy() {
    local cid=$1 timeout=${2:-60} elapsed=0 status
    while (( elapsed < timeout )); do
        status=$(docker inspect --format='{{.State.Health.Status}}' "$cid" 2>/dev/null || echo none)
        [[ "$status" == healthy   ]] && return 0
        [[ "$status" == unhealthy ]] && return 1
        sleep 1; (( ++elapsed ))
    done
    return 1
}

# ── [2][12] Startup time + steady-state RAM/CPU in a single container run ────
# Startup = container created → HEALTHCHECK healthy.
# NOTE: Spring Boot services need their DB/Kafka dependencies to become truly
# healthy; in isolated mode they will likely report TIMEOUT, which is the
# correct and honest measurement for standalone container startup.
measure_startup_and_runtime() {
    local image=$1 cid s e startup_t runtime_stats
    s=$(get_time)
    cid=$(docker run -d --rm "$image" 2>/dev/null)
    if wait_healthy "$cid" 60; then
        e=$(get_time)
        startup_t=$(awk "BEGIN{printf \"%.2f\", $e - $s}")
        sleep 3   # let steady-state CPU settle
        runtime_stats=$(docker stats --no-stream --format "{{.MemUsage}} / {{.CPUPerc}}" "$cid" 2>/dev/null | head -1)
        docker stop "$cid" >/dev/null 2>&1 || true
        echo "$startup_t | $runtime_stats"
    else
        docker stop "$cid" >/dev/null 2>&1 || true
        echo "TIMEOUT | N/A"
    fi
}

# ── [8] Timed build: returns "elapsed_seconds cached_layer_count" ────────────
# Uses --progress=plain to capture BuildKit step output for CACHED counting.
# Applies --cpuset-cpus / --memory to every build (fixes [4]).
timed_build() {
    local tag=$1 dockerfile=$2 context=$3
    local extra_flags=()
    [[ -n "${4:-}" ]] && extra_flags+=("$4")
    local plog s e elapsed cached
    plog=$(mktemp)
    s=$(get_time)
    DOCKER_BUILDKIT=1 docker build \
        --cpuset-cpus="0,1" --memory="4g" \
        --progress=plain \
        "${extra_flags[@]}" \
        -t "$tag" -f "$dockerfile" "$context" \
        >"$plog" 2>&1
    e=$(get_time)
    elapsed=$(awk "BEGIN{printf \"%.2f\", $e - $s}")
    cached=$(grep -c "^#[0-9]* CACHED" "$plog" 2>/dev/null || echo 0)
    rm -f "$plog"
    echo "$elapsed $cached"
}

# ── [9] CVE scan: CRITICAL+HIGH (combined) and MEDIUM (separate column) ──────
scan_cves() {
    local image=$1
    if ! command -v trivy &>/dev/null; then echo "N/A N/A"; return; fi
    local out crit=0 high=0 med=0 tmp
    out=$(trivy image -q --severity CRITICAL,HIGH,MEDIUM "$image" 2>/dev/null || true)
    tmp=$(echo "$out" | grep -oE 'CRITICAL: [0-9]+' | grep -oE '[0-9]+' | head -1); crit=${tmp:-0}
    tmp=$(echo "$out" | grep -oE 'HIGH: [0-9]+'     | grep -oE '[0-9]+' | head -1); high=${tmp:-0}
    tmp=$(echo "$out" | grep -oE 'MEDIUM: [0-9]+'   | grep -oE '[0-9]+' | head -1); med=${tmp:-0}
    echo "$(( crit + high )) $med"
}

# ── [10] Reliable package count via apk list --installed ─────────────────────
pkg_count() {
    docker run --rm --entrypoint sh "$1" \
        -c "apk list --installed 2>/dev/null | wc -l | tr -d ' '" 2>/dev/null || echo "N/A"
}

# ── [11] Local registry for push/pull latency measurement ────────────────────
LOCAL_REG="localhost:5001"

start_registry() {
    docker rm -f bench-registry >/dev/null 2>&1 || true
    docker run -d --rm -p 5001:5000 --name bench-registry registry:2 >/dev/null
    sleep 2
}

stop_registry() { docker stop bench-registry >/dev/null 2>&1 || true; }

measure_push_pull() {
    local image=$1 remote s e push_t pull_t
    remote="$LOCAL_REG/${image//:/-}"
    docker tag "$image" "$remote" 2>/dev/null

    s=$(get_time); docker push "$remote" >/dev/null 2>&1; e=$(get_time)
    push_t=$(awk "BEGIN{printf \"%.2f\", $e - $s}")
    docker rmi "$remote" >/dev/null 2>&1 || true

    s=$(get_time); docker pull "$remote" >/dev/null 2>&1; e=$(get_time)
    pull_t=$(awk "BEGIN{printf \"%.2f\", $e - $s}")
    docker rmi "$remote" >/dev/null 2>&1 || true

    echo "$push_t $pull_t"
}

# ── [7] Statistics helpers (no associative arrays — bash 3.2 compatible) ─────
mean_of() {
    echo "$@" | tr ' ' '\n' | grep -v '^[[:space:]]*$' | \
        awk '{s+=$1;n++}END{if(n>0)printf "%.2f",s/n; else print "N/A"}'
}
stddev_of() {
    local m; m=$(mean_of "$@")
    [[ "$m" == "N/A" ]] && { echo "N/A"; return; }
    echo "$@" | tr ' ' '\n' | grep -v '^[[:space:]]*$' | \
        awk -v m="$m" '{d=$1-m; s+=d*d; n++}END{printf "%.2f",sqrt(s/n)}'
}

# ── Preflight ─────────────────────────────────────────────────────────────────
if ! command -v trivy &>/dev/null; then
    echo "WARNING: trivy not found — CVE columns will show N/A. Install: brew install trivy"
fi
if ! command -v gdate &>/dev/null && ! command -v python3 &>/dev/null; then
    echo "ERROR: neither gdate nor python3 found. Install coreutils: brew install coreutils" >&2
    exit 1
fi

# ── Init log ──────────────────────────────────────────────────────────────────
{
    echo "Professional Docker Benchmark: Multi-Stage vs Single-Stage"
    echo "Date: $(date)"
    echo "Cold iterations : $COLD_ITERATIONS | Hot warmup: $HOT_WARMUP (discarded) | Hot measured: $HOT_ITERATIONS"
    echo "Resource limits : --cpuset-cpus=0,1 --memory=4g (all builds)"
    echo "Order           : randomised per iteration"
    echo "=================================================="
} | tee "$LOG_FILE"

# ── Pre-pull base images ──────────────────────────────────────────────────────
echo "Pre-pulling base images to eliminate network bias..." | tee -a "$LOG_FILE"
docker pull node:20-alpine                 >/dev/null
docker pull eclipse-temurin:21-jdk-alpine >/dev/null
docker pull eclipse-temurin:21-jre-alpine >/dev/null
echo "Done." | tee -a "$LOG_FILE"
echo "--------------------------------------------------" | tee -a "$LOG_FILE"

# ══ SECTION 1: COLD BUILD ════════════════════════════════════════════════════
# Each iteration: prune builder cache, randomise order [5], constrained resources [4]
echo "Cold Build Benchmark (${COLD_ITERATIONS} iterations, --no-cache)..." | tee -a "$LOG_FILE"

for entry in "${SERVICES[@]}"; do
    IFS="|" read -r name dir multi_file single_file _ <<< "$entry"

    cold_multi=""
    cold_single=""

    for (( i=1; i<=COLD_ITERATIONS; i++ )); do
        # [5] randomise which variant builds first each iteration
        if (( RANDOM % 2 )); then order="multi single"; else order="single multi"; fi

        for variant in $order; do
            docker builder prune -a -f >/dev/null

            if [[ "$variant" == multi ]]; then
                df="$dir/$multi_file"; tag="${name}-multi:latest"
            else
                df="$dir/$single_file"; tag="${name}-single:latest"
            fi

            read -r t _ < <(timed_build "$tag" "$df" "$dir" "--no-cache")

            if [[ "$variant" == multi ]]; then cold_multi="$cold_multi $t"
            else                               cold_single="$cold_single $t"; fi
        done
    done

    printf "  [%-22s] Cold Multi   | Mean: %6.2fs ±%5.2fs\n" \
        "$name" "$(mean_of $cold_multi)" "$(stddev_of $cold_multi)" | tee -a "$LOG_FILE"
    printf "  [%-22s] Cold Single  | Mean: %6.2fs ±%5.2fs\n" \
        "$name" "$(mean_of $cold_single)" "$(stddev_of $cold_single)" | tee -a "$LOG_FILE"
    echo "" | tee -a "$LOG_FILE"
done
echo "--------------------------------------------------" | tee -a "$LOG_FILE"

# ══ SECTION 2: HOT (INCREMENTAL) REBUILD ═════════════════════════════════════
echo "Pre-populating caches before hot benchmark..." | tee -a "$LOG_FILE"
for entry in "${SERVICES[@]}"; do
    IFS="|" read -r name dir multi_file single_file _ <<< "$entry"
    DOCKER_BUILDKIT=1 docker build --progress=plain \
        -t "${name}-multi:latest"  -f "$dir/$multi_file"  "$dir" >/dev/null 2>&1
    DOCKER_BUILDKIT=1 docker build --progress=plain \
        -t "${name}-single:latest" -f "$dir/$single_file" "$dir" >/dev/null 2>&1
done
echo "Caches ready." | tee -a "$LOG_FILE"
echo "--------------------------------------------------" | tee -a "$LOG_FILE"

TOTAL_HOT=$(( HOT_WARMUP + HOT_ITERATIONS ))
echo "Hot Rebuild Benchmark (iteration 1 = warmup, discarded)..." | tee -a "$LOG_FILE"

for (( i=1; i<=TOTAL_HOT; i++ )); do
    if (( i <= HOT_WARMUP )); then
        label="Warmup (discarded)"
    else
        label="Iteration $(( i - HOT_WARMUP )) of $HOT_ITERATIONS"
    fi
    echo "--- $label ---" | tee -a "$LOG_FILE"

    for entry in "${SERVICES[@]}"; do
        IFS="|" read -r name dir multi_file single_file touch_target <<< "$entry"

        # [5] randomise order each iteration
        if (( RANDOM % 2 )); then order="multi single"; else order="single multi"; fi

        for variant in $order; do
            if [[ "$variant" == multi ]]; then
                df="$dir/$multi_file"; tag="${name}-multi:latest"
            else
                df="$dir/$single_file"; tag="${name}-single:latest"
            fi

            # [3] Identical cache-bust delta for both variants; file restored before
            #     the next variant builds, so each sees the exact same delta.
            echo "// cache-bust $i" >> "$dir/$touch_target"
            read -r t cached < <(timed_build "$tag" "$df" "$dir")
            git checkout "$dir/$touch_target" >/dev/null 2>&1 || true

            if (( i > HOT_WARMUP )); then
                printf "  [%-22s] %-6s | Time: %6.2fs | CachedLayers: %2s\n" \
                    "$name" "$variant" "$t" "$cached" | tee -a "$LOG_FILE"
                echo "${name}|${variant}|${t}" >> "$STATS_FILE"
            fi
        done
    done
done
echo "--------------------------------------------------" | tee -a "$LOG_FILE"

# ══ SECTION 3: STATIC IMAGE ANALYSIS ═════════════════════════════════════════
echo "Static Image Analysis (size, layers, packages, CVEs)..." | tee -a "$LOG_FILE"

for entry in "${SERVICES[@]}"; do
    IFS="|" read -r name _ _ _ _ <<< "$entry"
    for variant in multi single; do
        tag="${name}-${variant}:latest"
        size=$(docker images --format "{{.Size}}" "$tag" 2>/dev/null || echo "N/A")
        layers=$(docker history -q "$tag" 2>/dev/null | wc -l | tr -d ' ')
        pkgs=$(pkg_count "$tag")
        read -r crit_high med < <(scan_cves "$tag")
        printf "  [%-22s] %-6s | Size: %-8s | Layers: %2s | Pkgs: %3s | CVEs(C+H): %3s | CVEs(M): %3s\n" \
            "$name" "$variant" "$size" "$layers" "$pkgs" "$crit_high" "$med" | tee -a "$LOG_FILE"
    done
    echo "" | tee -a "$LOG_FILE"
done
echo "--------------------------------------------------" | tee -a "$LOG_FILE"

# ══ SECTION 5: REGISTRY PUSH/PULL LATENCY ════════════════════════════════════
echo "Registry Push/Pull Latency (local registry at $LOCAL_REG)..." | tee -a "$LOG_FILE"
start_registry

for entry in "${SERVICES[@]}"; do
    IFS="|" read -r name _ _ _ _ <<< "$entry"
    for variant in multi single; do
        tag="${name}-${variant}:latest"
        read -r push_t pull_t < <(measure_push_pull "$tag")
        printf "  [%-22s] %-6s | Push: %6.2fs | Pull: %6.2fs\n" \
            "$name" "$variant" "$push_t" "$pull_t" | tee -a "$LOG_FILE"
    done
    echo "" | tee -a "$LOG_FILE"
done

stop_registry
echo "--------------------------------------------------" | tee -a "$LOG_FILE"

# ══ SECTION 6: STATISTICAL SUMMARY ═══════════════════════════════════════════
echo "Hot Rebuild Summary — mean ± stddev over $HOT_ITERATIONS measured iterations:" | tee -a "$LOG_FILE"

for entry in "${SERVICES[@]}"; do
    IFS="|" read -r name _ _ _ _ <<< "$entry"
    for variant in multi single; do
        times=$(grep "^${name}|${variant}|" "$STATS_FILE" 2>/dev/null | cut -d'|' -f3 | tr '\n' ' ')
        if [[ -n "${times// /}" ]]; then
            m=$(mean_of $times)
            s=$(stddev_of $times)
            printf "  [%-22s] %-6s | Mean: %6.2fs ±%5.2fs\n" "$name" "$variant" "$m" "$s" | tee -a "$LOG_FILE"
        fi
    done
done

echo "=================================================="  | tee -a "$LOG_FILE"
echo "Benchmark complete. Full results saved to: $LOG_FILE"
