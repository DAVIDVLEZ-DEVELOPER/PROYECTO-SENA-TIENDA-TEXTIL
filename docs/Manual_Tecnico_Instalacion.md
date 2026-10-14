# Manual Técnico de Instalación, Configuración y Despliegue
**Proyecto:** Plataforma Distribuida de Comercialización y Control Logístico Textil (**D&D Textil**)  
**Programa de Formación:** Tecnólogo en Análisis y Desarrollo de Software (ADSO) — SENA  
**Versión:** 2.0.0 — Guía de Despliegue Reproducible  
**Fecha:** Octubre 2026  

---

## 1. Requisitos del Sistema

### 1.1. Requisitos de Hardware
- **Procesador (CPU):** Arquitectura x86_64 o ARM64 (mínimo 2 núcleos físicos, recomendado 4 núcleos).
- **Memoria RAM:** Mínimo 4 GB (recomendado 8 GB para ejecución simultánea de contenedores Docker y base de datos).
- **Almacenamiento en Disco:** Mínimo 5 GB de espacio libre disponible (para imágenes Docker, datos de PostgreSQL y multimedia).

### 1.2. Requisitos de Software
Para un despliegue estándar mediante contenedorización (recomendado para evaluación):
- **Docker Engine:** Versión 24.0 o superior.
- **Docker Compose:** Versión v2.20 o superior.
- **Git:** Versión 2.30 o superior.

*(Opcional: Si se desea compilar y ejecutar de forma nativa sin Docker:)*
- **Java Development Kit (JDK):** Versión 17 LTS (Eclipse Temurin / OpenJDK).
- **Node.js:** Versión 18 LTS o 20 LTS.
- **Motor PostgreSQL:** Versión 15 a 17 (puerto estándar 5432).

---

## 2. Configuración de Variables de Entorno

El sistema utiliza un archivo de configuración centralizado `.env` en la raíz del proyecto. Si no existe, créalo copiando la plantilla oficial:

```bash
cp .env.example .env
```

### Contenido de `.env` para Entornos de Evaluación:
```ini
# Configuración del Motor PostgreSQL
POSTGRES_DB=tienda_digital_textiles_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_PORT=5432

# Conectividad JDBC del Backend Java
DB_URL=jdbc:postgresql://db:5432/tienda_digital_textiles_db
DB_USER=postgres
DB_PASSWORD=postgres

# Configuración del Servidor HTTP
PORT=8081
JWT_SECRET=SenaAdsoTextilSecuritySecretKey#2026_UltraSecureTokenSignatureKey

# URL del Frontend Web
FRONTEND_URL=http://localhost:3001
```

---

## 3. Despliegue Automatizado con Docker Compose (Paso a Paso)

El despliegue oficial levanta la arquitectura de tres niveles (Base de Datos, Backend y Frontend) con redes aisladas y comprobaciones de salud en un único comando.

### Paso 1: Clonar el Repositorio
```bash
git clone https://github.com/Jhonmoreno000/PROYECTO-SENA-TIENDA-TEXTIL-.git
cd PROYECTO-SENA-TIENDA-TEXTIL-
```

### Paso 2: Construir e Iniciar los Servicios
Ejecuta la orquestación en segundo plano (`-d`) forzando la compilación limpia (`--build`):
```bash
docker compose up -d --build
```

### Paso 3: Verificar el Estado de Salud (*Healthchecks*)
Comprueba que todos los contenedores alcancen el estado `healthy`:
```bash
docker compose ps
```

*Salida esperada:*
```text
NAME                IMAGE                     COMMAND                  SERVICE    STATUS
ddtextil-db         postgres:17               "docker-entrypoint.s…"   db         Up (healthy)   0.0.0.0:5432->5432/tcp
ddtextil-backend    ddtextil-backend:latest   "java -Djava.net.pre…"   backend    Up (healthy)   0.0.0.0:8081->8081/tcp
ddtextil-frontend   ddtextil-frontend:latest  "/docker-entrypoint.…"   frontend   Up (healthy)   0.0.0.0:3001->80/tcp
```

### Paso 4: Validar Endpoints de Salud
Ejecuta solicitudes HTTP directas a los probes del backend:
```bash
# Probar Liveness (Servidor Vivo)
curl -i http://localhost:8081/health/live

# Probar Readiness (Base de Datos Conectada)
curl -i http://localhost:8081/health/ready
```
Ambos deben retornar `HTTP/1.1 200 OK` con payload `{"status":"UP"}`.

### Paso 5: Acceso a las Aplicaciones
- **Tienda Web y Panel Administrativo:** [http://localhost:3001](http://localhost:3001)
- **API REST Backend:** [http://localhost:8081/api/products](http://localhost:8081/api/products)
- **Base de Datos PostgreSQL:** `localhost:5432`, Base: `tienda_digital_textiles_db`

---

## 4. Ejecución y Desarrollo Local sin Docker (Entorno Nativo)

Si el instructor o desarrollador desea compilar y depurar el código directamente en su máquina anfitriona:

### 4.1. Base de Datos
1. Iniciar el servicio local de PostgreSQL.
2. Crear la base de datos e importar el esquema oficial:
   ```bash
   psql -U postgres -c "CREATE DATABASE tienda_digital_textiles_db;"
   psql -U postgres -d tienda_digital_textiles_db -f "BASE DE DATOS/01_esquema_espanol.sql"
   psql -U postgres -d tienda_digital_textiles_db -f "BASE DE DATOS/02_datos_iniciales.sql"
   ```

### 4.2. Compilación y Ejecución del Backend Java
1. Navegar a la carpeta del backend:
   ```bash
   cd "tienda digital de telas/backend-java/conexionPostgres"
   ```
2. Compilar todas las clases con codificación UTF-8 hacia la carpeta `bin/`:
   ```bash
   mkdir bin
   javac -encoding UTF-8 -cp "lib/*" -d bin src/dominio/excepciones/*.java src/dominio/modelos/*.java src/dominio/repositorios/*.java src/aplicacion/servicios/*.java src/infraestructura/configuracion/*.java src/infraestructura/persistencia/jdbc/*.java src/infraestructura/api/manejadores/*.java src/infraestructura/api/*.java src/*.java
   ```
3. Ejecutar la suite de pruebas unitarias textiles:
   ```bash
   java -cp "bin;lib/*" PruebasReglasNegocioTextil
   ```
4. Ejecutar la suite de seguridad y concurrencia:
   ```bash
   java -cp "bin;lib/*" PruebasSeguridadYBugs
   ```
5. Iniciar el servidor API en el puerto 8081:
   ```bash
   java -cp "bin;lib/*" App
   ```

### 4.3. Ejecución del Frontend React
1. En otra terminal, ingresar a la carpeta del frontend:
   ```bash
   cd "tienda digital de telas"
   npm install
   npm run dev
   ```
2. La aplicación estará disponible en [http://localhost:5173](http://localhost:5173) o [http://localhost:3001](http://localhost:3001).

---

## 5. Solución de Problemas Frecuentes (Troubleshooting)

### 5.1. Error: "Puerto ya en uso" (Port 5432, 8081 o 3001)
- **Causa:** Existe una instancia previa de PostgreSQL local o un proceso Node/Java activo.
- **Solución en Windows (PowerShell):**
  ```powershell
  # Identificar el proceso ocupando el puerto 8081
  Get-Process -Id (Get-NetTCPConnection -LocalPort 8081).OwningProcess | Stop-Process -Force
  ```
- **Solución en Linux / macOS:**
  ```bash
  sudo lsof -i :8081
  kill -9 <PID>
  ```

### 5.2. Reinicio y Reconstrucción Limpia de Docker
Si la base de datos contiene datos corruptos de pruebas anteriores, limpia los volúmenes y reconstruye:
```bash
docker compose down -v
docker compose up -d --build
```
Esto forzará a PostgreSQL a reejecutar los scripts de inicialización de `BASE DE DATOS/` desde cero.
