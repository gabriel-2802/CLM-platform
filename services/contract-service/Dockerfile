
# stage 1: build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

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

# stage 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# libreoffice + fonts for accurate docx→pdf rendering
RUN apk add --no-cache \
    libreoffice \
    font-noto \
    ttf-dejavu \
    ttf-liberation \
    fontconfig \
    wget \
    netcat-openbsd

# Carlito (Calibri substitute) and Caladea (Cambria substitute) — not in Alpine repos
RUN mkdir -p /usr/share/fonts/truetype/carlito \
             /usr/share/fonts/truetype/caladea \
 && for f in Carlito-Regular Carlito-Bold Carlito-Italic Carlito-BoldItalic; do \
      wget -qO "/usr/share/fonts/truetype/carlito/${f}.ttf" \
        "https://raw.githubusercontent.com/googlefonts/carlito/main/fonts/ttf/${f}.ttf"; \
    done \
 && for f in Caladea-Regular Caladea-Bold Caladea-Italic Caladea-BoldItalic; do \
      wget -qO "/usr/share/fonts/truetype/caladea/${f}.ttf" \
        "https://raw.githubusercontent.com/googlefonts/caladea/master/fonts/ttf/${f}.ttf"; \
    done

# fontconfig aliases: MS font names → metric-compatible substitutes
COPY 99-ms-substitutions.conf /etc/fonts/conf.d/99-ms-substitutions.conf

RUN fc-cache -f

# /usr/bin/libreoffice is the alpine package entry-point (symlinks to soffice)
ENV LIBREOFFICE_PATH=/usr/bin/libreoffice
# libreoffice headless needs a writable home directory; /tmp is a tmpfs in production
ENV HOME=/tmp

RUN addgroup -g 1001 -S appgroup && \
    adduser  -u 1001 -S -G appgroup appuser

WORKDIR /app

COPY --from=builder /build/target/*.jar contracts.jar
RUN chown appuser:appgroup contracts.jar

USER appuser

EXPOSE 8081

HEALTHCHECK --interval=15s --timeout=5s --retries=5 --start-period=40s \
  CMD nc -z localhost 8081

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "contracts.jar"]