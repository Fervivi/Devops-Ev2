# Comandos rápidos

## Local

```bash
mvn -f backend/pom.xml clean verify
APP_VERSION=local APP_PORT=8088 docker compose up -d --build
docker compose ps
curl http://127.0.0.1:8088/api/health
curl http://127.0.0.1:8088/api/libros

# Escalar el backend
APP_VERSION=local APP_PORT=8088 BACKEND_REPLICAS=2 docker compose up -d --no-build

APP_VERSION=local APP_PORT=8088 docker compose down
```

## Kubernetes (con un clúster local, por ejemplo kind)

```bash
kind create cluster --name minibiblioteca
kind load docker-image minibiblioteca-backend:local minibiblioteca-frontend:local --name minibiblioteca
kubectl apply -k k8s/
kubectl -n minibiblioteca get deploy,pods,svc,hpa,pdb
kubectl -n minibiblioteca port-forward service/frontend 8081:80
kind delete cluster --name minibiblioteca
```

## EC2

```bash
cd /opt/minibiblioteca/current
sudo docker compose ps
sudo docker compose logs --tail=100 backend
sudo docker compose logs --tail=100 frontend
cat /opt/minibiblioteca/logs/deployments.log
cat .env                       # versión (SHA) desplegada
sudo docker stats --no-stream
free -h
df -h /
```

## Rollback manual en EC2

```bash
cd /opt/minibiblioteca/current
cp .env.previous .env
cp docker-compose.previous.yml docker-compose.yml
sudo docker compose up -d --no-build
```
