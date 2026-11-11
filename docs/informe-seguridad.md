# Informe de seguridad (SSDLC)

Cuatro análisis posibles; se deben documentar al menos tres. El proyecto es una aplicación web, por lo que los cuatro aplican.

## 1. SCA — dependencias (Dependabot)
- Configurado en `.github/dependabot.yml` (Maven, Docker y GitHub Actions).
- Dependencias directas: `mysql-connector-j 8.3.0` y `jbcrypt 0.4`.
- Observación manual: `jbcrypt 0.4` es una librería antigua y sin mantenimiento activo; funciona, pero es candidata a reemplazo
  (por ejemplo, por una implementación mantenida de BCrypt).
- **Completar:** pegar aquí las alertas que muestre GitHub → Security → Dependabot y la acción tomada (actualizar / aceptar riesgo).

## 2. SAST — código (SonarLint)
- **Completar:** instalar SonarLint en el IDE, abrir el proyecto y registrar los hallazgos y las correcciones.
- Revisión manual realizada sobre el código original (corregida en esta versión):

| Hallazgo | Riesgo | Corrección |
|---|---|---|
| Usuario `root` y contraseña vacía como valores por defecto en `DatabaseConnection` | Credenciales por defecto | Sin valores por defecto: `DB_USER` y `DB_PASS` son obligatorias |
| Los DAO capturaban `SQLException`, imprimían el mensaje por consola y devolvían `false` o listas vacías | Errores ocultos y fuga de detalles técnicos | `CreaException` con mensaje seguro; el detalle solo va al log |
| La nota (1–12) y la fecha no se validaban | Datos inválidos en la base | `Validador` (rango, formato, longitud, valores permitidos) |
| Un estudiante podría consultar datos ajenos si se invocaba la capa de datos con otro ID | Control de acceso roto | `PermisosUtil.alcance`: el servicio fuerza el ID del estudiante autenticado |
| Las operaciones de escritura no verificaban el rol | Elevación de privilegios | `PermisosUtil.exigirGestion` en todos los servicios que modifican datos |
| El hash de la contraseña viajaba en el objeto `Usuario` | Exposición innecesaria | Se eliminó el atributo `contrasena` del modelo |

## 3. Escaneo de la imagen (Trivy)
```bash
docker build -t crea3:local .
trivy image crea3:local
```
- **Completar:** pegar el resumen (cantidad de CVE por severidad) y qué se hizo con las críticas/altas
  (actualizar imagen base, reconstruir, aceptar si no hay corrección disponible).

## 4. DAST — OWASP ZAP
Ejecutar ZAP **solo contra la URL propia** (`http://localhost:8080` o la de Render) y documentar los resultados:
```bash
docker run --rm --network host -t ghcr.io/zaproxy/zaproxy:stable zap-baseline.py -t http://localhost:8080
```
- Controles web ya implementados: contenido escapado contra XSS (`Vistas.esc`), token CSRF en cada POST, cookie `HttpOnly` + `SameSite=Strict`
  (+ `Secure` bajo HTTPS), sesión nueva al iniciar sesión, cabeceras `Content-Security-Policy`, `X-Frame-Options`, `X-Content-Type-Options`,
  `Referrer-Policy`, y errores internos sin detalle para el usuario.
- **Completar:** pegar las alertas de ZAP y la corrección o justificación de cada una.

## Hardening mínimo
| Control | Estado | Dónde |
|---|---|---|
| Sin contraseñas en el código | ✅ | `DatabaseConnection` lee variables de entorno |
| Sin contraseñas en el Dockerfile | ✅ | `Dockerfile` no define credenciales |
| `.env` fuera de Git, `.env.example` sin valores | ✅ | `.gitignore`, `.env.example` |
| Secretos en Kubernetes | ✅ | `k8s/secret.yaml.example` (`secret.yaml` está en `.gitignore`) |
| Contenedor con usuario no root | ✅ | `USER app` en el `Dockerfile` |
| Conexión a Aiven solo por SSL | ✅ | `DB_SSL_MODE=REQUIRED` |
| Contraseñas con hash | ✅ | BCrypt (`PasswordUtil`) |
| Consultas parametrizadas | ✅ | `PreparedStatement` en todos los DAO |
| Sin secretos en el historial de Git | ⚠️ verificar | Ver abajo |

Verificar el historial antes de entregar:
```bash
git log -p | grep -iE "password|passwd|secret|token" | head
```
Si aparece alguna credencial real en commits anteriores, hay que cambiarla en el servicio (Aiven) y limpiar el historial.
