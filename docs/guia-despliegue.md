# Guía de despliegue

Cada sección indica qué capturas hay que agregar (marcadas con 📸). Requisitos: Docker, Git y, para el Taller 4, minikube y kubectl.

## 0. Preparación
```bash
git clone <URL-del-repositorio> && cd <carpeta>
cp .env.example .env        # completar los valores; .env no se sube a Git
```
Valores sugeridos para local: `DB_HOST=localhost`, `DB_PORT=3306`, `DB_NAME=crea3_db`, `DB_USER=crea3`, `DB_PASS=<elegir>`,
`MYSQL_ROOT_PASSWORD=<elegir>`, `DB_SSL_MODE=PREFERRED`, `GITHUB_USER=<usuario en minúsculas>`.

## 1. Nivel 1 — Docker y docker compose
```bash
docker compose up --build
```
- La base se crea con los scripts de `sql/` (solo la primera vez que se crea el volumen).
- La aplicación queda en http://localhost:8080 (ingresar con `docente@utu.edu.uy` / `admin123`).
- Para volver a cargar los scripts desde cero: `docker compose down -v`.

📸 `docker compose up` funcionando y la app guardando un dato (por ejemplo, una calificación).

### Publicar la imagen en ghcr.io
El workflow `.github/workflows/publicar-imagen.yml` publica la imagen en cada `push` a `main`.
Para hacerlo a mano:
```bash
echo <TOKEN_con_write:packages> | docker login ghcr.io -u <usuario> --password-stdin
docker build -t ghcr.io/<usuario>/crea3:latest .
docker push ghcr.io/<usuario>/crea3:latest
```
Luego, en GitHub → Packages → crea3, hacer el paquete público (o el clúster necesitará credenciales para descargarlo).

📸 La imagen publicada en ghcr.io.

## 2. Nivel 2 — Base de datos en Aiven
1. Crear una cuenta en Aiven y un servicio **MySQL** del plan gratuito.
2. En la consola, copiar *Host*, *Port*, *User*, *Password* y el nombre de la base (`defaultdb`) o crear `crea3_db`.
3. Cargar los scripts contra Aiven:
   ```bash
   mysql -h <host> -P <port> -u <user> -p --ssl-mode=REQUIRED <base> < sql/01-crear-tablas.sql
   mysql -h <host> -P <port> -u <user> -p --ssl-mode=REQUIRED <base> < sql/02-datos-prueba.sql
   ```
4. En `.env`: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASS` de Aiven y `DB_SSL_MODE=REQUIRED`.
5. Ejecutar la app contra Aiven (`./crea3` o `docker compose up app` con el `.env` de Aiven), iniciar sesión, crear un dato y verificarlo con un `SELECT` directo sobre Aiven.
6. Desplegar en Render (sección siguiente) y comprobar que se ven los mismos datos.

Si el servicio de Aiven está detenido por inactividad, se reactiva desde la consola.

📸 Base en Aiven y `SELECT` mostrando datos cargados desde la aplicación.

### Despliegue en Render (HTTPS)
1. Subir el repositorio a GitHub y, en Render, crear un **New → Web Service** (plan *Free*, sin tarjeta) desde ese repositorio.
2. *Runtime*: **Docker** (usa el `Dockerfile` de la raíz). Render define `PORT` solo. También se puede usar el `render.yaml` del repositorio (**New → Blueprint**).
3. En *Environment* cargar `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASS` (los de Aiven) y `DB_SSL_MODE=REQUIRED`.
4. *Health Check Path*: `/health`.
5. Al terminar el despliegue, abrir la URL `https://<nombre>.onrender.com` (candado visible) e iniciar sesión.

El plan gratuito apaga el servicio tras 15 minutos sin uso: abrir la URL antes de la defensa.

📸 La app por HTTPS en Render, con el candado visible.

### Despliegue en Railway (alternativa a Render, también con HTTPS)
1. En Railway: **New Project → Deploy from GitHub repo** y elegir el repositorio. Railway detecta el `Dockerfile` (y usa `railway.json`).
2. En *Variables* cargar `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASS` (los de Aiven) y `DB_SSL_MODE=REQUIRED`. Railway define `PORT` solo.
3. En *Settings → Networking* pulsar **Generate Domain**: queda `https://<nombre>.up.railway.app` (candado visible).
4. Verificar `https://<nombre>.up.railway.app/health` (debe responder `OK`) e iniciar sesión.

📸 La app por HTTPS en Railway, con el candado visible (si se usa en lugar de Render).

## 3. Nivel 3 — Kubernetes (minikube)
```bash
minikube start
cp k8s/secret.yaml.example k8s/secret.yaml        # editar los valores
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/configmap.yaml
kubectl create configmap crea3-sql --from-file=sql/
kubectl apply -f k8s/db-deployment.yaml -f k8s/db-service.yaml
kubectl apply -f k8s/app-deployment.yaml -f k8s/app-service.yaml
kubectl get pods,svc
```
Editar antes `image:` en `k8s/app-deployment.yaml` con su usuario de GitHub.

Verificación (la app completa queda en http://localhost:8080):
```bash
kubectl port-forward svc/crea3-app 8080:8080 &
curl -i http://localhost:8080/health        # 200 OK
```
Probes: la base usa `mysqladmin ping`; la app usa `GET /health`.

📸 `kubectl get pods,svc` con todo en `Running` y el `curl` a `/health`.

## 4. Seguridad
Ver [informe-seguridad.md](informe-seguridad.md).

## 5. Prueba de la guía por otro grupo
Anotar aquí fecha, grupo que la probó y los problemas encontrados.
