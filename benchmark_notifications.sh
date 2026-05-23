#!/bin/bash
DIR="services/notification-service"

echo "Building initial Multi-Stage..."
docker build -q -t notif-multi -f $DIR/Dockerfile $DIR > /dev/null

echo "Building initial Single-Stage..."
docker build -q -t notif-single -f $DIR/Dockerfile.normal $DIR > /dev/null

# Get sizes
size_multi=$(docker images --format "{{.Size}}" notif-multi:latest)
size_single=$(docker images --format "{{.Size}}" notif-single:latest)
echo "Multi Size: $size_multi"
echo "Single Size: $size_single"

# Rebuild Multi
echo "" >> $DIR/src/main/resources/application.yml
start=$(date +%s.%N)
docker build -q -t notif-multi -f $DIR/Dockerfile $DIR > /dev/null
end=$(date +%s.%N)
echo "Multi Rebuild: $(echo "$end - $start" | bc) s"

# Rebuild Single
echo "" >> $DIR/src/main/resources/application.yml
start=$(date +%s.%N)
docker build -q -t notif-single -f $DIR/Dockerfile.normal $DIR > /dev/null
end=$(date +%s.%N)
echo "Single Rebuild: $(echo "$end - $start" | bc) s"

