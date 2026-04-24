# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk-noble AS build

RUN apt-get update && apt-get install -y --no-install-recommends maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Cache dependencies before copying full source
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk-noble

# Install LibreOffice headless + fonts for DOCX/PDF conversion
RUN apt-get update && apt-get install -y --no-install-recommends \
    libreoffice-headless \
    libreoffice-writer \
    fonts-liberation \
    fonts-dejavu \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Override the Mac-specific libreoffice path from application.properties
ENV LIBREOFFICE_PATH=/usr/bin/soffice

# Database connection — teammates must supply these at runtime
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/clm_platform
ENV SPRING_DATASOURCE_USERNAME=clm_user
ENV SPRING_DATASOURCE_PASSWORD=clm_password

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]