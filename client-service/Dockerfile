FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache netcat-openbsd

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
