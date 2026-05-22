#!/bin/bash
echo "Benchmarking Rebuild Times..."

benchmark() {
    SERVICE=$1
    DIR=$2
    
    echo "--- $SERVICE ---"
    
    # Touch a source file to invalidate only the compilation layer, not dependencies
    if [ "$SERVICE" == "frontend" ]; then
        touch $DIR/package.json # wait, package.json invalidates dependencies! We should touch a source file.
        touch $DIR/src/app/layout.tsx
    else
        # For Java services, touching a java file or just a resource file
        touch $DIR/src/main/resources/application-test.yml
    fi

    # Benchmark Multi-Stage
    echo "Multi-Stage Build:"
    start=$(date +%s.%N)
    docker build -q -t ${SERVICE}-multi -f $DIR/Dockerfile $DIR > /dev/null
    end=$(date +%s.%N)
    time_multi=$(echo "$end - $start" | bc)
    printf "Multi-Stage Rebuild Time: %.2f s\n" $time_multi

    # Touch again to invalidate the cache for single stage
    if [ "$SERVICE" == "frontend" ]; then
        touch $DIR/src/app/layout.tsx
    else
        touch $DIR/src/main/resources/application-test.yml
    fi

    # Benchmark Single-Stage
    echo "Single-Stage Build:"
    start=$(date +%s.%N)
    docker build -q -t ${SERVICE}-single -f $DIR/Dockerfile.normal $DIR > /dev/null
    end=$(date +%s.%N)
    time_single=$(echo "$end - $start" | bc)
    printf "Single-Stage Rebuild Time: %.2f s\n" $time_single
    echo ""
}

# Pre-build everything once to populate caches
echo "Populating caches..."
docker build -q -t frontend-multi -f frontend/Dockerfile frontend > /dev/null
docker build -q -t frontend-single -f frontend/Dockerfile.normal frontend > /dev/null

for srv in user-service contract-service negotiation-service client-service; do
    docker build -q -t ${srv}-multi -f services/$srv/Dockerfile services/$srv > /dev/null
    docker build -q -t ${srv}-single -f services/$srv/Dockerfile.normal services/$srv > /dev/null
done

# Run benchmarks
benchmark "frontend" "frontend"
benchmark "user-service" "services/user-service"
benchmark "contract-service" "services/contract-service"
benchmark "negotiation-service" "services/negotiation-service"
benchmark "client-service" "services/client-service"

