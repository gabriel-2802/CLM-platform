# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

RUN apk add --no-cache maven

WORKDIR /build

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -q

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests -q

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache netcat-openbsd

RUN addgroup -g 1001 -S appgroup && \
    adduser  -u 1001 -S -G appgroup appuser

WORKDIR /app

COPY --from=builder /build/target/*.jar user-service.jar
RUN chown appuser:appgroup user-service.jar

USER appuser

EXPOSE 8083

HEALTHCHECK --interval=15s --timeout=5s --retries=5 --start-period=40s \
  CMD nc -z localhost 8083

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "user-service.jar"]
