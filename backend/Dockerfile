# ============================================================
# ETAPA 1 - BUILD
# Maven compila el JAR. Esta etapa NO queda en la imagen final.
# ============================================================
FROM maven:3.10-eclipse-temurin-21-alpine AS build
WORKDIR /build

# Copiamos primero el POM para aprovechar la caché de Docker.
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

# Copiamos el código y construimos el JAR.
COPY src ./src
RUN mvn -B -DskipTests clean package

# ============================================================
# ETAPA 2 - RUNTIME
# Solo contiene Java Runtime + aplicación terminada.
# ============================================================
FROM eclipse-temurin:24-jre-alpine

# curl se utiliza en el healthcheck de Docker Compose.
RUN apk add --no-cache curl \
    && addgroup -S app \
    && adduser -S app -G app

WORKDIR /app
COPY --from=build --chown=app:app /build/target/minibiblioteca-backend-0.0.1-SNAPSHOT.jar app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
