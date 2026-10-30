#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="/opt/minibiblioteca"

test -f "$PROJECT_ROOT/.bootstrap-ready"
sudo docker info >/dev/null
sudo docker compose version
free -h
df -h /

echo "EC2 preparada correctamente."
