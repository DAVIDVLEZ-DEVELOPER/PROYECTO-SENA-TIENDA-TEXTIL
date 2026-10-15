<div align="center">

# D&D Textil

### Ecosistema tecnológico para la industria textil

Plataforma integral para la comercialización, distribución y gestión de inventario de telas, construida con una arquitectura reactiva, un backend corporativo ligero y un modelo relacional en tiempo real.

<p>
  <img src="https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB" />
  <img src="https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white" />
  <img src="https://img.shields.io/badge/TailwindCSS-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white" />
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" />
  <img src="https://img.shields.io/badge/ISO%2FIEC_25010-Software_Quality-green?style=for-the-badge" />
  <img src="https://img.shields.io/badge/DevSecOps-OWASP_ASVS-blueviolet?style=for-the-badge" />
  <img src="https://img.shields.io/badge/SENA-Calidad_Aprobada-39A900?style=for-the-badge" />
</p>

<p>
  <img src="https://img.shields.io/github/languages/top/Jhonmoreno000/PROYECTO-SENA-TIENDA-TEXTIL-" />
  <img src="https://img.shields.io/github/languages/count/Jhonmoreno000/PROYECTO-SENA-TIENDA-TEXTIL-" />
  <img src="https://img.shields.io/github/last-commit/Jhonmoreno000/PROYECTO-SENA-TIENDA-TEXTIL-" />
  <img src="https://img.shields.io/github/repo-size/Jhonmoreno000/PROYECTO-SENA-TIENDA-TEXTIL-" />
</p>

</div>

---

## Visión general

Un solo ecosistema que une tienda en línea, paneles de administración y control de inventario. Los tres bloques comparten una misma base de datos y una API REST, de modo que cada cambio en catálogo, reseñas o apartados del inicio se refleja al instante en todas las interfaces.

**Lo esencial en pocas líneas**

- Tienda pública con carrito, reseñas de clientes y apartados dinámicos (nuevas colecciones, telas exclusivas y ofertas).
- Panel administrativo para gestionar usuarios, productos, carrusel, apartados del inicio, inventario, pedidos y facturación.
- Despliegue completo con Docker Compose: tres contenedores listos en un solo comando.

## Arquitectura

```
┌──────────────────────────┐     ┌──────────────────────────┐
│  Frontend  React + Vite  │     │  Frontend  App Android   │
│  http://localhost:3001   │     │  WebView + Kotlin        │
└────────────┬─────────────┘     └────────────┬─────────────┘
             │  REST / JSON                    │  REST / JSON
             ▼                                ▼
┌────────────────────────────────────────────────────────────┐
│              Backend  Java (HTTPServer + JDBC)             │
│                http://localhost:8081/api/*                 │
└────────────────────────────┬───────────────────────────────┘
                             │  JDBC
                             ▼
┌────────────────────────────────────────────────────────────┐
│              PostgreSQL 17 (Docker volume)                 │
└────────────────────────────────────────────────────────────┘
```

## Características

| Área | Detalle |
| :--- | :--- |
| Tienda | Catálogo con filtros, fichas de tela, reseñas reales guardadas en base de datos, carrito y checkout. |
| Home dinámico | Carrusel y apartados del inicio (nuevas colecciones, exclusivas, ofertas) editables desde el panel admin. |
| Gestión | Productos, usuarios, inventario por lotes, alertas de stock, pedidos y facturación electrónica con PDF. |
| Roles | Cliente, vendedor y administrador con accesos diferenciados a cada panel. |
| Despliegue | Docker Compose levanta frontend, backend y base de datos con una sola orden. |

## Inicio rápido

### Requisitos

- Docker Engine v24 o superior
- Docker Compose v2 o superior

### Despliegue

```bash
# Clonar el repositorio
git clone https://github.com/Jhonmoreno000/PROYECTO-SENA-TIENDA-TEXTIL-
cd PROYECTO-SENA-TIENDA-TEXTIL-

# Configurar variables de entorno
cp .env.example .env

# Levantar los tres servicios en segundo plano
docker compose up -d
```

### Puertos

| Servicio | Acceso |
| :--- | :--- |
| Frontend de la aplicacion | http://localhost:3001 (Docker) / http://localhost:5173 (Dev) |
| Backend API (REST) | http://localhost:8081 |
| Base de Datos PostgreSQL | localhost:5432 |

### Detener y limpiar

```bash
# Detener los contenedores
docker compose down

# Detener y eliminar los datos persistidos (reset total)
docker compose down -v
```

## Credenciales de prueba

Usuarios sembrados en la base de datos para validar los niveles de acceso del software.

| Rol | Correo electronico | Contrasena |
| :--- | :--- | :--- |
| Cliente | `cliente@ddtextil.com` | `cliente123` |
| Vendedor | `vendedor@ddtextil.com` | `vendedor123` |
| Administrador | `admin@ddtextil.com` | `admin123` |

## Calidad de Software y Suite de Pruebas Automatizadas

El proyecto implementa los estandares de la norma **ISO/IEC 25010** y las directrices de aseguramiento de calidad del **SENA**. Cuenta con una suite integral de 26 pruebas automatizadas (14 de reglas de negocio textiles y 12 de seguridad y casos extremos):

```bash
# Navegar al directorio del backend
cd "tienda digital de telas/backend-java/conexionPostgres"

# Ejecutar las suites de pruebas automatizadas
java -cp "lib/*;bin" PruebasReglasNegocioTextil
java -cp "lib/*;bin" PruebasSeguridadYBugs
```

### Probes de Observabilidad (Health Checks)
* **Liveness Probe**: `GET http://localhost:8081/health/live`
* **Readiness Probe**: `GET http://localhost:8081/health/ready`
* **Reporte Global**: `GET http://localhost:8081/api/health`

## Portal Central de Documentacion Tecnica

El cuerpo documental oficial del sistema esta estructurado de forma modular en la carpeta `documentacion/`:

* **[Indice General de Documentacion](DOCUMENTACION.md)**
* 01. **[Especificacion de Requerimientos de Software (IEEE 830 y BDD)](documentacion/01-especificacion-requerimientos-ieee830.md)**
* 02. **[Arquitectura de Software, Modelo C4 y Backend Java](documentacion/02-arquitectura-software-c4-y-backend.md)**
* 03. **[Arquitectura del Frontend y Gestion de Roles de Usuario](documentacion/03-arquitectura-frontend-y-roles.md)**
* 04. **[Diseno, Estructura y Esquema de Base de Datos Relacional](documentacion/04-base-de-datos-diseno-y-esquema.md)** | **[Esquema Relacional y Arquitectura MVC](BASE%20DE%20DATOS/README.md)**
* 05. **[Politicas de Seguridad, Resiliencia y Calidad de Software](documentacion/05-politicas-seguridad-resiliencia-y-calidad.md)**
* 06. **[Manual Tecnico de Instalacion, Configuracion y Despliegue](documentacion/06-manual-tecnico-instalacion-y-despliegue.md)**
* 07. **[Motor Textil, Reglas de Negocio y Suite de Pruebas Automatizadas](documentacion/07-motor-textil-reglas-negocio-y-pruebas.md)**
* 08. **[Contrato de Interfaz OpenAPI v3.0](documentacion/openapi_tienda_textil.yaml)**

---

<div align="center">

<sub>Proyecto formativo SENA · D&D Textil · Calidad ISO/IEC 25010 & DevSecOps</sub>

</div>