# Comandos rápidos

## Local

```bash
mvn -f backend/pom.xml clean verify
APP_VERSION=local APP_PORT=8088 docker compose up -d --build
docker compose ps
curl http://127.0.0.1:8088/api/health
curl http://127.0.0.1:8088/api/libros
APP_VERSION=local APP_PORT=8088 docker compose down
```

## EC2

```bash
cd /opt/minibiblioteca/current
sudo docker compose ps
sudo docker compose logs --tail=100 backend
sudo docker compose logs --tail=100 frontend
sudo docker stats
free -h
df -h /
```
