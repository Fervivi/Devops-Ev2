# MiniBiblioteca DevSecOps - EC2 Free Tier

Evaluación Parcial N°2 - Ingeniería DevOps (DOY0101)

Integrantes: Fernando Villalobos, Tomás Peña

Microproyecto académico para practicar un flujo CI/CD completo con GitHub Actions y despliegue en una instancia AWS EC2 pequeña.

## Arquitectura

- **frontend**: HTML/CSS/JavaScript servido por Nginx (puerto 80).
- **backend**: Spring Boot 4.1.2 + Java 21 (puerto 8080, solo red interna).
- **persistencia**: repositorio en memoria para evitar un tercer contenedor de base de datos.
- **orquestación**: Docker Compose.
- **CI/CD**: GitHub Actions.

El navegador solo accede al puerto 80. Nginx reenvía las rutas `/api/` al servicio `backend` dentro de la red de Docker Compose.

## Contenedores

- `backend/Dockerfile`: multi-stage. La primera etapa compila con Maven y la segunda solo contiene el JRE y el JAR, con un usuario sin privilegios.
- `frontend/Dockerfile`: imagen `nginx:alpine` con los archivos estáticos y el proxy hacia el backend.
- `docker-compose.yml`: define los dos servicios, la red interna, el healthcheck del backend, límites de memoria/CPU y el orden de arranque (el frontend espera a que el backend esté healthy).

## Pipeline CI/CD

Archivo: `.github/workflows/ci-cd.yml`. Los jobs se encadenan con `needs`, por lo que si uno falla los siguientes no se ejecutan.

| Job | Qué hace | Se detiene si... |
|---|---|---|
| 1. Snyk - Dependencias | Analiza las dependencias Maven y publica `snyk-report.json` | Hay una vulnerabilidad Critical |
| 2. JUnit Mockito y JaCoCo | Ejecuta las pruebas unitarias y genera el reporte de cobertura | Falla una prueba o la cobertura baja del 80% |
| 3. SonarQube Cloud | Analiza calidad del código, importa la cobertura y espera el Quality Gate | El Quality Gate falla |
| 4. Docker Compose y Smoke Test | Construye las imágenes, levanta el stack y prueba `/api/health` y `/api/libros` | La aplicación no responde |
| 5. Deploy AWS EC2 | Solo en push a `main`: copia las imágenes a la EC2 y ejecuta `docker compose up -d --no-build` | La aplicación no responde en la EC2 |

Flujo de trabajo: se trabaja en la rama `dev`, se crea un Pull Request hacia `main` (se ejecutan los jobs 1 al 4 y el deploy queda Skipped) y al hacer merge se ejecuta todo y se despliega.

Además, **Dependabot** (`.github/dependabot.yml`) revisa cada semana las dependencias de Maven, GitHub Actions y las imágenes Docker, y abre Pull Requests que pasan por el mismo pipeline.

## Seguridad y bloqueos

- Snyk bloquea el pipeline si encuentra una vulnerabilidad Critical. El reporte se publica igual como artifact para revisarlo.
- SonarQube Cloud bloquea si el Quality Gate falla (`sonar.qualitygate.wait=true`).
- Los tokens y la clave SSH se guardan como GitHub Secrets, nunca en el código.
- El backend corre sin root y no se publica a Internet; solo el puerto 80 queda abierto.
- La rama `main` se protege para que no se pueda hacer merge si algún check está en rojo.

## Calidad

- Pruebas unitarias con JUnit 5 y Mockito para el servicio, el controller (MockMvc) y el repositorio.
- JaCoCo genera la cobertura y el build falla si la cobertura de líneas es menor al 80% (regla en `backend/pom.xml`).
- SonarQube Cloud evalúa bugs, vulnerabilidades, code smells, duplicación y cobertura.
- El smoke test valida que la aplicación funciona con Docker antes de desplegar.

## Trazabilidad

- Cada ejecución del pipeline queda asociada a un commit y a su Pull Request o push.
- Las imágenes se etiquetan con el SHA del commit (`APP_VERSION=$GITHUB_SHA`), y la EC2 guarda esa versión en su archivo `.env`, por lo que se puede saber exactamente qué commit está desplegado.
- Los reportes de Snyk y JaCoCo quedan como artifacts de cada ejecución.
- La EC2 no compila nada: despliega las mismas imágenes que se probaron en el pipeline.

## Por qué este diseño es apto para una EC2 pequeña

La EC2 **no ejecuta Maven ni construye imágenes**. GitHub Actions realiza el trabajo pesado y transfiere dos imágenes ya listas. En la EC2 solo se ejecutan Nginx y Spring Boot.

## Configuración en GitHub

Settings → Secrets and variables → Actions:

| Tipo | Nombre |
|---|---|
| Secret | `SNYK_TOKEN` |
| Secret | `SONAR_TOKEN` |
| Secret | `AWS_HOST` |
| Secret | `AWS_USER` |
| Secret | `AWS_SSH_KEY` |
| Variable | `SONAR_PROJECT_KEY` |
| Variable | `SONAR_ORGANIZATION` |

## Comandos locales

```bash
mvn -B -f backend/pom.xml clean verify
APP_VERSION=local APP_PORT=8088 docker compose up -d --build
curl http://127.0.0.1:8088/api/health
curl http://127.0.0.1:8088/api/libros
```

## Preparación EC2

Security Group: abrir 22/TCP (SSH) y 80/TCP (web). No abrir 8080.

Ejecutar una sola vez dentro de la EC2:

```bash
bash deploy/prepare-ec2.sh
```

## Conclusiones

El proyecto MiniBiblioteca integra contenedores, pruebas automatizadas y análisis de seguridad en un mismo flujo de GitHub Actions. Las etapas están conectadas para detener el proceso cuando una validación falla, lo que permite detectar problemas antes de llegar al despliegue. Las 12 pruebas y la regla de cobertura mínima del 80% establecen una base verificable para controlar la calidad del backend.

Docker Compose permite coordinar el frontend y el backend mediante una red compartida y un healthcheck. La construcción de imágenes en GitHub Actions reduce el trabajo que debe realizar la EC2, mientras que los límites de memoria y CPU mantienen el diseño cercano a los recursos de una instancia pequeña. El uso del SHA del commit como versión permite relacionar las imágenes con el código que las originó.

La configuración del pipeline debe acompañarse de evidencia de su ejecución: análisis aprobados, pruebas exitosas y comprobación de la aplicación desplegada. El repositorio en memoria simplifica este laboratorio, pero los datos no persisten al reiniciar el backend. Estas condiciones delimitan el alcance académico de la solución y los aspectos que habría que revisar para un uso más amplio.

### Reflexión personal - Fernando Villalobos

Al trabajar en MiniBiblioteca pude relacionar las pruebas, la seguridad y los contenedores con el proceso de entrega de una aplicación. La revisión del pipeline me ayudó a comprender la función de cada etapa y por qué un error debe impedir que continúen las siguientes. También pude distinguir entre una prueba que falla, una vulnerabilidad detectada y un problema de configuración, como la ausencia de un token.

Mi participación se centró en revisar y ajustar el pipeline, incorporar pruebas y comprobar el funcionamiento local con Docker. Este trabajo me permitió comprender mejor cómo los reportes de cobertura y los logs sirven para revisar resultados y encontrar la causa de un fallo. Como aspecto por mejorar, considero necesario registrar las evidencias de cada validación y comprobar el recorrido completo hasta EC2, para respaldar con resultados lo que describe la documentación.

### Reflexión personal - Tomás Peña

Durante el proyecto MiniBiblioteca pude reforzar mis conocimientos sobre Docker y GitHub Actions, principalmente en la organización de los contenedores y la automatización del pipeline CI/CD. Al revisar la estructura y configuración de la aplicación, pude comprender mejor cómo se comunican el frontend y el backend, además de la importancia de realizar pruebas y validaciones antes de desplegar los cambios.

También pude analizar el funcionamiento de herramientas como Snyk, SonarQube y JaCoCo, comprendiendo cómo ayudan a detectar vulnerabilidades y evaluar la calidad del código. Esta experiencia me permitió entender mejor la relación entre las pruebas automatizadas, la seguridad y el despliegue en AWS EC2.
