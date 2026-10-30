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
FROM eclipse-temurin:21-jre-alpine

# Trazabilidad: el pipeline inyecta la versión (SHA del commit) en la imagen.
ARG APP_VERSION=local
ARG VCS_REF=local
LABEL org.opencontainers.image.title="minibiblioteca-backend" \
      org.opencontainers.image.description="API REST Spring Boot de MiniBiblioteca" \
      org.opencontainers.image.source="https://github.com/Fervivi/Devops-Ev2" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.revision="${VCS_REF}"

# curl se utiliza en el healthcheck de Docker Compose.
RUN apk add --no-cache curl \
    && addgroup -S app \
    && adduser -S app -G app

WORKDIR /app
COPY --from=build --chown=app:app /build/target/minibiblioteca-backend-0.0.1-SNAPSHOT.jar app.jar

USER app
EXPOSE 8080

# El heap se calcula según la memoria del contenedor (Compose/Kubernetes pueden sobrescribirlo).
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -XX:+UseSerialGC"

HEALTHCHECK --interval=10s --timeout=4s --start-period=30s --retries=6 \
  CMD curl -fsS http://127.0.0.1:8080/api/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
