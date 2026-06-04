
#  stage 1: build ──
FROM eclipse-temurin:25-jdk-alpine AS builder

RUN apk add --no-cache maven

WORKDIR /build

# copy dependency manifest only — layer invalidates only when pom.xml changes
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -q

# copy source and package — breaks only on src changes, deps served from cache
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests -q

#  stage 2: runtime 
FROM eclipse-temurin:25-jre-alpine

# nc for healthcheck
RUN apk add --no-cache netcat-openbsd

RUN addgroup -g 1001 -S appgroup && \
    adduser  -u 1001 -S -G appgroup appuser

WORKDIR /app

COPY --from=builder /build/target/*.jar notifications.jar
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chown appuser:appgroup notifications.jar /docker-entrypoint.sh && \
    chmod +x /docker-entrypoint.sh

USER appuser

EXPOSE 8082

HEALTHCHECK --interval=15s --timeout=5s --retries=5 --start-period=40s \
  CMD nc -z localhost 8082

ENTRYPOINT ["/docker-entrypoint.sh", "java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "notifications.jar"]