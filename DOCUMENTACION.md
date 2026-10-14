# Documentación del Proyecto D&D Textil

Bienvenido al portal central de documentación de **D&D Textil**. Aquí encontrarás información detallada sobre la plataforma, estructurada en secciones modulares para facilitar la consulta técnica y funcional.

---

## Índice General

Para consultar un apartado de interés, haz clic en el enlace correspondiente:

1.  ### [01. Descripción General y Roles de Usuario](file:///c:/Users/Anderson%20Moreno/Downloads/PROYECTO-SENA-TIENDA-TEXTIL-/documentacion/01-descripcion-y-roles.md)
    *   Propósito y descripción general del sistema.
    *   Matriz de roles y permisos (Cliente, Vendedor y Administrador).
    *   Mapa de navegación e interfaces públicas y privadas.

2.  ### [02. Arquitectura del Backend (Java)](file:///c:/Users/Anderson%20Moreno/Downloads/PROYECTO-SENA-TIENDA-TEXTIL-/documentacion/02-arquitectura-backend.md)
    *   Estructura y diseño modular de Vanilla Java 17 sin frameworks.
    *   Organización de directorios por capas lógicas (Arquitectura Limpia).
    *   Aplicación práctica de principios SOLID en el backend.
    *   Catálogo y especificaciones de Endpoints de la API REST.

3.  ### [03. Arquitectura del Frontend (React)](file:///c:/Users/Anderson%20Moreno/Downloads/PROYECTO-SENA-TIENDA-TEXTIL-/documentacion/03-arquitectura-frontend.md)
    *   Stack tecnológico principal (React 18, Vite, Tailwind CSS, Framer Motion).
    *   Estructura del árbol de directorios `src/`.
    *   Manejo de estado global con Context API (Autenticación, Carrito y Métricas).
    *   Enrutamiento local, protección de rutas y animaciones.

4.  ### [04. Diseño y Estructura de la Base de Datos](file:///c:/Users/Anderson%20Moreno/Downloads/PROYECTO-SENA-TIENDA-TEXTIL-/documentacion/04-base-de-datos.md)
    *   Esquema y diagramación física de PostgreSQL 14+.
    *   Tablas nucleares, secundarias y agregados de analíticas.
    *   Manejo del pool de conexiones JDBC y resiliencia ante caídas de red.
    *   Algoritmos matemáticos y fórmulas de la Calculadora de Metraje.

5.  ### [05. Guía de Despliegue y Configuración](file:///c:/Users/Anderson%20Moreno/Downloads/PROYECTO-SENA-TIENDA-TEXTIL-/documentacion/05-despliegue-y-configuracion.md)
    *   Despliegue rápido mediante Docker y Docker Compose.
    *   Gestión y definición de Variables de Entorno (`.env`).
    *   Guía de comandos rápidos y mantenimiento de contenedores.
    *   Instrucciones para despliegue manual en servidores locales.

6.  ### [06. Soporte, Solución de Problemas y Extras](file:///c:/Users/Anderson%20Moreno/Downloads/PROYECTO-SENA-TIENDA-TEXTIL-/documentacion/06-soporte-y-extras.md)
    *   Preguntas Frecuentes (FAQ).
    *   Guía de resolución de errores comunes (solución a problemas de credenciales).
    *   Arquitectura y detalles de la aplicación móvil (Android Wrapper).
    *   Directrices de contribución al repositorio de código.

7.  ### [07. Estándares Internacionales de Calidad (ISO/IEC 25010) y DevSecOps](file:///c:/Users/Anderson%20Moreno/Downloads/PROYECTO-SENA-TIENDA-TEXTIL-/documentacion/07-estandares-calidad-iso25010-seguridad.md)
    *   Evaluación y cumplimiento de la Norma ISO/IEC 25010 en comercio textil.
    *   Estrategia de pirámide de pruebas (QA) y métricas de SonarQube / SAST.
    *   Arquitectura de seguridad integral DevSecOps (API Gateway, Rate Limiting, OWASP ASVS).
    *   Cumplimiento normativo de comercio electrónico: PCI-DSS y Ley 1581 de 2012 (Habeas Data).
    *   Control de concurrencia y bloqueo pesimista (`SELECT ... FOR UPDATE`) en inventario textil.
    *   Observabilidad: Endpoints de salud (`/health/live`, `/health/ready`), Correlation ID y logs JSON.

