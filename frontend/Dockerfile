# Imagen oficial de Nginx que corre SIN root (usuario nginx, uid 101) y escucha en 8080.
FROM nginxinc/nginx-unprivileged:alpine

# Trazabilidad: el pipeline inyecta la versión (SHA del commit) en la imagen.
ARG APP_VERSION=local
ARG VCS_REF=local
LABEL org.opencontainers.image.title="minibiblioteca-frontend" \
      org.opencontainers.image.description="Frontend Nginx de MiniBiblioteca" \
      org.opencontainers.image.source="https://github.com/Fervivi/Devops-Ev2" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.revision="${VCS_REF}"

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY index.html styles.css app.js /usr/share/nginx/html/

EXPOSE 8080

# Docker marca el contenedor como unhealthy si Nginx deja de responder.
HEALTHCHECK --interval=15s --timeout=3s --start-period=5s --retries=3 \
  CMD wget -q -O /dev/null http://127.0.0.1:8080/healthz || exit 1
