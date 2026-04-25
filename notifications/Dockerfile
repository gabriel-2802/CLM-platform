# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

RUN apk add --no-cache maven

WORKDIR /build

# Copy POM first — dependency layer is only invalidated when pom.xml changes
COPY pom.xml .
# pom.xml targets Java 25; override to 21 to match the JDK and runtime image
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -q -Djava.version=21

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests -q -Djava.version=21

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# nc for healthcheck
RUN apk add --no-cache netcat-openbsd

RUN addgroup -g 1001 -S appgroup && \
    adduser  -u 1001 -S -G appgroup appuser

WORKDIR /app

COPY --from=builder /build/target/*.jar notifications.jar
RUN chown appuser:appgroup notifications.jar

USER appuser

EXPOSE 8082

HEALTHCHECK --interval=15s --timeout=5s --retries=5 --start-period=40s \
  CMD nc -z localhost 8082

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "notifications.jar"]
