# Documentación del Proyecto D&D Textil

Bienvenido al portal central de documentación de **D&D Textil**. Aquí encontrarás información detallada sobre la plataforma, estructurada en secciones modulares para facilitar la consulta técnica y funcional.

---

## 🏛️ Documentación Formal de Sustentación SENA ADSO (`/docs`)

Los documentos formales exigidos para la evaluación del tecnólogo ADSO según normas IEEE 830, ISO/IEC 25010 y Modelo C4 se encuentran centralizados en la carpeta `/docs`:

*   📋 **[01. Especificación de Requerimientos de Software (IEEE 830 y BDD)](file:///c:/Users/Anderson%20Moreno/Downloads/PROYECTO-SENA-TIENDA-TEXTIL-/docs/01_especificacion_requerimientos.md)**: Planteamiento del problema textil, justificación económica, matriz RF-01 a RF-07, RNF-01 a RNF-05 y escenarios Gherkin BDD para corte continuo y mermas.
*   📐 **[02. Arquitectura de Software y Modelo C4](file:///c:/Users/Anderson%20Moreno/Downloads/PROYECTO-SENA-TIENDA-TEXTIL-/docs/02_arquitectura_c4.md)**: Diagramas Mermaid C4 (Contexto N1, Contenedores N2, Componentes N3), DER individual, Diccionario de Datos y orquestación Saga.
*   🛡️ **[03. Políticas de Seguridad, Fiabilidad y Resiliencia](file:///c:/Users/Anderson%20Moreno/Downloads/PROYECTO-SENA-TIENDA-TEXTIL-/docs/03_politicas_seguridad_resiliencia.md)**: IAM, cumplimiento PCI-DSS (cero almacenamiento PAN/CVV), Habeas Data (Ley 1581 de 2012), DRP (RTO <= 2h, RPO <= 15min) y mitigación OWASP API Security.
*   🚀 **[04. Manual Técnico de Instalación y Despliegue](file:///c:/Users/Anderson%20Moreno/Downloads/PROYECTO-SENA-TIENDA-TEXTIL-/docs/Manual_Tecnico_Instalacion.md)**: Requisitos de hardware, variables de entorno, orquestación Docker Compose y validación de probes de salud.
*   📄 **[05. Contrato de Interfaz OpenAPI v3.0](file:///c:/Users/Anderson%20Moreno/Downloads/PROYECTO-SENA-TIENDA-TEXTIL-/docs/openapi_tienda_textil.yaml)**: Especificación Swagger/OpenAPI v3.0 de todos los endpoints de la API REST.

---

## Índice General de Módulos Técnicos (`/documentacion`)

Para consultar los manuales modulares de cada subsistema:

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

