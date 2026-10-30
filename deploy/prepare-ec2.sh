#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="/opt/minibiblioteca"
COMPOSE_VERSION="v5.6.1"

echo "=== 1. Actualizar metadatos e instalar herramientas ==="
sudo yum makecache -y
sudo yum install -y docker curl tar gzip

echo "=== 2. Habilitar Docker ==="
sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -aG docker ec2-user || true

echo "=== 3. Instalar Docker Compose ==="
sudo mkdir -p /usr/local/lib/docker/cli-plugins
ARCH="$(uname -m)"
case "$ARCH" in
  x86_64) COMPOSE_ARCH="x86_64" ;;
  aarch64) COMPOSE_ARCH="aarch64" ;;
  *) echo "Arquitectura no soportada: $ARCH"; exit 1 ;;
esac

sudo curl -fSL \
  "https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-linux-${COMPOSE_ARCH}" \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

# Algunas instalaciones de Docker buscan plugins en /usr/libexec.
sudo mkdir -p /usr/libexec/docker/cli-plugins
sudo ln -sf \
  /usr/local/lib/docker/cli-plugins/docker-compose \
  /usr/libexec/docker/cli-plugins/docker-compose

echo "=== 4. Crear 1 GiB de swap si la instancia no tiene swap ==="
if ! sudo swapon --show | grep -q .; then
  sudo fallocate -l 1G /swapfile 2>/dev/null || sudo dd if=/dev/zero of=/swapfile bs=1M count=1024
  sudo chmod 600 /swapfile
  sudo mkswap /swapfile
  sudo swapon /swapfile
  if ! grep -q '^/swapfile ' /etc/fstab; then
    echo '/swapfile swap swap defaults 0 0' | sudo tee -a /etc/fstab >/dev/null
  fi
fi

echo "=== 5. Crear carpetas del proyecto ==="
sudo mkdir -p "$PROJECT_ROOT/current" "$PROJECT_ROOT/logs"
sudo chown -R ec2-user:ec2-user "$PROJECT_ROOT"

echo "=== 6. Validar instalación ==="
sudo docker --version
sudo docker info >/dev/null
sudo docker compose version
free -h
df -h /

echo "=== 7. Marcar la EC2 como preparada ==="
touch "$PROJECT_ROOT/.bootstrap-ready"
rm -f "$PROJECT_ROOT/.bootstrap-failed"

echo "EC2 LISTA PARA MINIBIBLIOTECA"
