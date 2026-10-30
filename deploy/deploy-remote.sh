#!/usr/bin/env bash
# Se ejecuta EN LA EC2, invocado por el job "deploy" de GitHub Actions.
# Carga las imágenes ya construidas, levanta el stack y, si el health check falla,
# vuelve automáticamente a la versión anterior (rollback).
#
# Uso: deploy-remote.sh <carpeta_paquete> <actor> <url_run>
set -euo pipefail

PAQUETE="${1:?Falta la carpeta del paquete}"
ACTOR="${2:-desconocido}"
RUN_URL="${3:-}"

ROOT="/opt/minibiblioteca"
CURRENT="$ROOT/current"
LOG="$ROOT/logs/deployments.log"

leer_version() {
  grep '^APP_VERSION=' "$1" 2>/dev/null | cut -d= -f2 || true
}

registrar() {
  # Una línea por evento: fecha | resultado | versión | anterior | actor | run
  printf '%s | %-8s | %s | prev=%s | %s | %s\n' \
    "$(date -u +%Y-%m-%dT%H:%M:%SZ)" "$1" "$NUEVA" "${ANTERIOR:-ninguna}" "$ACTOR" "$RUN_URL" | tee -a "$LOG"
}

esperar_salud() {
  for i in $(seq 1 30); do
    if curl -fsS http://127.0.0.1/api/health >/dev/null 2>&1; then
      return 0
    fi
    echo "Esperando aplicación... intento $i/30"
    sleep 5
  done
  return 1
}

mkdir -p "$CURRENT" "$ROOT/logs"
NUEVA="$(leer_version "$PAQUETE/.env")"
ANTERIOR="$(leer_version "$CURRENT/.env")"
echo "Versión nueva: $NUEVA | versión actual: ${ANTERIOR:-ninguna}"

# 1. Respaldar la configuración actual para un posible rollback.
if [ -f "$CURRENT/.env" ]; then
  cp "$CURRENT/.env" "$CURRENT/.env.previous"
  cp "$CURRENT/docker-compose.yml" "$CURRENT/docker-compose.previous.yml"
fi

# 2. Instalar la nueva configuración y cargar las imágenes (sin compilar nada en la EC2).
cp "$PAQUETE/docker-compose.yml" "$CURRENT/docker-compose.yml"
cp "$PAQUETE/.env" "$CURRENT/.env"
gzip -dc "$PAQUETE/minibiblioteca-images.tar.gz" | sudo docker load

# 3. Levantar la nueva versión.
cd "$CURRENT"
sudo docker compose up -d --no-build --remove-orphans

if esperar_salud; then
  registrar "OK"
  sudo docker compose ps

  # 4. Limpiar imágenes antiguas, conservando la versión nueva y la anterior (para rollback).
  sudo docker images --format '{{.Repository}}:{{.Tag}}' \
    | grep -E '^minibiblioteca-(backend|frontend):' \
    | grep -v -E ":(${NUEVA}${ANTERIOR:+|$ANTERIOR})$" \
    | xargs -r sudo docker rmi || true
  sudo docker image prune -f >/dev/null
  rm -rf "$PAQUETE"
  exit 0
fi

echo "ERROR: la versión $NUEVA no respondió."
registrar "FALLO"
sudo docker compose ps || true
sudo docker compose logs --tail=120 || true

# 5. Rollback automático a la versión anterior, si existe.
if [ -n "${ANTERIOR:-}" ] && [ -f "$CURRENT/.env.previous" ]; then
  echo "Ejecutando rollback a $ANTERIOR..."
  cp "$CURRENT/.env.previous" "$CURRENT/.env"
  cp "$CURRENT/docker-compose.previous.yml" "$CURRENT/docker-compose.yml"
  sudo docker compose up -d --no-build --remove-orphans
  if esperar_salud; then
    registrar "ROLLBACK"
  else
    registrar "ROLLBACK-FALLIDO"
  fi
fi

exit 1
