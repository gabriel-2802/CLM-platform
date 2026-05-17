# syntax=docker/dockerfile:1

# stage 1: build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

RUN apk add --no-cache maven

WORKDIR /build

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -q

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests -q

# stage 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache netcat-openbsd

RUN addgroup -g 1001 -S appgroup && \
    adduser  -u 1001 -S -G appgroup appuser

WORKDIR /app

COPY --from=builder /build/target/*.jar negotiation-service.jar
RUN chown appuser:appgroup negotiation-service.jar

USER appuser

EXPOSE 8085

HEALTHCHECK --interval=15s --timeout=5s --retries=5 --start-period=40s \
  CMD nc -z localhost 8085

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "negotiation-service.jar"]
