# 03. Políticas de Seguridad, Fiabilidad y Cumplimiento Normativo
**Proyecto:** Plataforma Distribuida de Comercialización y Control Logístico Textil (**D&D Textil**)  
**Normatividad:** Ley 1581 de 2012 (Habeas Data Colombia), Estándar PCI-DSS v4.0, OWASP API Security Top 10, ISO/IEC 27001  
**Versión:** 2.0.0 — Sustentación Técnica SENA ADSO  
**Fecha:** Octubre 2026  

---

## 1. Política de Gestión de Identidad y Acceso (IAM)

### 1.1. Principio de Mínimo Privilegio y Control de Acceso Basado en Roles (RBAC)
Todo acceso a los recursos computacionales y endpoints de la plataforma está condicionado a la asignación estricta de un rol verificado. No se permiten accesos anónimos a operaciones de escritura, corte o administración:

| Rol del Sistema | Nivel de Acceso | Operaciones Permitidas | Restricciones de Seguridad |
| :--- | :---: | :--- | :--- |
| `cliente` | Nivel 1 | Consulta de catálogo, cálculo de metrajes, creación de pedidos propios, tickets de soporte y consulta de facturas personales. | No puede ver pedidos de terceros, ni modificar stock, ni acceder a paneles administrativos. |
| `vendedor` | Nivel 2 | Publicación de telas, actualización de fichas técnicas de sus productos, visualización de sus ventas y métricas comerciales. | Solo gestiona productos asociados a su `vendedor_id`; no puede moderar productos de otros vendedores ni suspender usuarios. |
| `operario_corte` | Nivel 2 | Escaneo de órdenes de corte con código QR, descuento de metraje por rollo físico y registro de retazos y mermas. | No tiene acceso a información financiera, facturación ni edición de precios por metro. |
| `administrador` | Nivel 3 (Total) | Moderación de productos, auditoría de seguridad, gestión global de usuarios, configuración del sistema y DRP. | Requiere autenticación multifactor (MFA) obligatoria y registro auditado de cada acción. |

### 1.2. Ciclo de Vida y Rotación de Tokens de Sesión
- **Access Token (JWT con algoritmo HMAC-SHA256):** Tiempo de vida efímero de **15 minutos**. Contiene únicamente los *claims* no sensibles (`sub`, `rol`, `email`, `exp`, `jti`).
- **Refresh Token:** Validez máxima de **7 días**, almacenado de forma segura y asociado a una clave volátil revocable de forma inmediata ante sospecha de compromiso de credenciales o cambio de contraseña.
- **Directivas de Almacenamiento en Navegadores:**  
  Los tokens no se almacenan en `localStorage` ni `sessionStorage` (vulnerables a robo por XSS). Se transmiten exclusivamente mediante cabeceras HTTP seguras:
  ```http
  Set-Cookie: auth_token=eyJhbGciOi...; Path=/; Secure; HttpOnly; SameSite=Strict; Max-Age=900
  ```

### 1.3. Seguridad Criptográfica de Contraseñas
- Las contraseñas se procesan en el backend con **SHA-256 codificado en UTF-8** con salt estático y dinámico, garantizando resistencia ante ataques de diccionario y colisiones:
  ```java
  MessageDigest digest = MessageDigest.getInstance("SHA-256");
  digest.update(contrasena.getBytes(StandardCharsets.UTF_8));
  ```
- Longitud mínima obligatoria de 8 caracteres, con combinación de mayúsculas, minúsculas, números y caracteres especiales.
- Política de bloqueo de cuenta tras 5 intentos fallidos consecutivos durante una ventana de 15 minutos.

---

## 2. Política de Manejo de Información Financiera y Cumplimiento PCI-DSS

### 2.1. Declaración de No Almacenamiento de Datos Sensibles (Cardholder Data)
De acuerdo con el estándar **PCI-DSS (Payment Card Industry Data Security Standard) v4.0 (Requisito 3)**, la plataforma **D&D Textil** opera bajo el principio de **cero almacenamiento de datos de tarjetahabientes**:

1. **Datos Prohibidos que NUNCA ingresan ni se almacenan en nuestros servidores:**
   - **PAN (*Primary Account Number*):** Número completo de la tarjeta de crédito o débito de 16 dígitos.
   - **CVV / CVC / CID (*Card Verification Value*):** Código de seguridad de 3 o 4 dígitos al respaldo de la tarjeta.
   - **Fecha de Expiración:** Mes y año de vencimiento del plástico.
   - **PIN o Clave Secreta:** Datos del bloque de autenticación del chip o banda magnética.

2. **Arquitectura de Tokenización Delegada:**
   - Todo el proceso de captura de datos de pago se ejecuta en el navegador del cliente mediante *iFrames seguros* o campos protegidos provistos directamente por la pasarela transaccional certificada PCI Nivel 1 (ej. Wompi / PayU / PSE).
   - Nuestros servidores únicamente reciben un **Token Transaccional Cifrado** (`token_pago`) y un identificador de transacción (`transaction_id`) de un solo uso para confirmar la aprobación o rechazo del cobro.

3. **Verificación Automatizada en Backend:**  
   La suite de pruebas `PruebasSeguridadYBugs.java` (Test 11) inspecciona activamente que ningún payload de pedido contenga secuencias numéricas compatibles con el algoritmo de Luhn (tarjetas Visa, Mastercard o American Express) ni campos de CVV, rechazando de inmediato la solicitud con código HTTP `400 Bad Request`.

---

## 3. Política de Protección de Datos Personales — Ley 1581 de 2012 (Habeas Data Colombia)

En cumplimiento de la legislación colombiana de protección de datos (Decreto Reglamentario 1377 de 2013 y Circular Única de la SIC):

### 3.1. Principios Aplicados al Tratamiento de Datos
- **Principio de Libertad:** La captura de nombres, correos, direcciones de entrega y números telefónicos requiere la **autorización previa, expresa e informada** del titular mediante un checkbox obligatorio y desmarcado en el formulario de registro:
  > *"Acepto la Política de Tratamiento de Datos Personales de D&D Textil y autorizo el uso de mi información para la facturación, corte y despacho logístico de pedidos."*
- **Principio de Finalidad:** La información capturada se utiliza exclusivamente para fines legítimos de facturación, emisión de órdenes de corte y coordinación del flete con la transportadora.
- **Principio de Transparencia:** El cliente puede en cualquier momento conocer, actualizar y rectificar sus datos personales desde la sección `/cliente/perfil`.

### 3.2. Ejercicio de Derechos de los Titulares (ARCO: Acceso, Rectificación, Cancelación y Oposición)
- **Derecho al Olvido y Supresión:** El sistema expone un procedimiento seguro para la **anonimización o eliminación de cuentas**. Cuando un usuario solicita su baja:
  1. Si no tiene pedidos pendientes ni disputas de garantía abiertas, se anonimizan sus datos identificables:
     ```sql
     UPDATE usuarios SET 
         nombre = 'USUARIO_ANONIMIZADO_' || id, 
         correo = 'anonimo_' || id || '@eliminado.ddtextil.com',
         activo = false,
         suspendido = true
     WHERE id = ?;
     ```
  2. Los registros contables y fiscales de pedidos antiguos se conservan durante el término legal obligatorio de 5 años según el Estatuto Tributario colombiano, pero disociados de los datos personales del usuario.
- **Registro Inmutable de Consentimiento:** Se almacena fecha, hora, dirección IP y versión de la política aceptada al momento de la creación de la cuenta.

---

## 4. Política de Resiliencia, Continuidad del Negocio y Recuperación ante Desastres (DRP)

### 4.1. Métricas de Continuidad Operativa
- **RTO (*Recovery Time Objective*):** $\le 2\text{ horas}$. Tiempo máximo tolerable para restaurar la operatividad total del backend y la base de datos tras una falla catastrófica de infraestructura.
- **RPO (*Recovery Point Objective*):** $\le 15\text{ minutos}$. Máxima pérdida de transacciones tolerada en caso de desastre, garantizada mediante respaldos periódicos y registros de transacciones (*Write-Ahead Logging / WAL*).

### 4.2. Estrategia de Respaldos (Backups)
1. **Copias Completas Diarias:**
   - Ejecutadas automáticamente a las 02:00 UTC mediante un cron job seguro en el contenedor de base de datos:
     ```bash
     pg_dump -U postgres -Fc tienda_digital_textiles_db | gpg --symmetric --cipher-algo AES256 -o /backups/db_$(date +%Y%m%d).dump.gpg
     ```
   - Cifrado simétrico AES-256 antes de la transferencia a almacenamiento en frío externo.
   - Retención estricta: 7 respaldos diarios, 4 semanales y 12 mensuales.
2. **Archivado de Transacciones (WAL Archiving):**
   - Transmisión continua de segmentos WAL hacia volumen persistente independiente para permitir *Point-in-Time Recovery (PITR)*.

### 4.3. Probes de Monitoreo y Tolerancia a Fallos
- **Healthcheck Liveness (`/health/live`):** Comprueba que el proceso Java esté activo y responda solicitudes HTTP básicas (HTTP 200).
- **Healthcheck Readiness (`/health/ready`):** Ejecuta una consulta ligera de sondeo a PostgreSQL (`SELECT 1`) y verifica el pool HikariCP. Si la base de datos está temporalmente no disponible, el probe retorna HTTP 503, impidiendo que el balanceador enrute tráfico a una instancia no lista.

---

## 5. Matriz de Mitigación de Vulnerabilidades — OWASP API Security Top 10

| Código OWASP | Vulnerabilidad | Riesgo en E-Commerce Textil | Mecanismo de Mitigación Implementado |
| :---: | :--- | :--- | :--- |
| **API1:2023** | **BOLA (Broken Object Level Authorization)** | Un cliente altera el ID en `/api/pedidos/14` para ver telas o direcciones de otro confeccionista. | En cada consulta a detalle de pedido se valida estrictamente que `pedido.usuario_id == usuario_autenticado.id` o que el rol sea `administrador`. |
| **API2:2023** | **Broken Authentication** | Ataques de fuerza bruta contra el formulario de login para descifrar cuentas de vendedores. | Rate Limiting por IP (60 peticiones/minuto) mediante `ManejadorBase`, hashing SHA-256 y bloqueo temporal tras intentos fallidos. |
| **API3:2023** | **Broken Object Property Level Authorization** | Un atacante envía un JSON de registro inyectando `"rol": "administrador"` para elevar privilegios. | El backend fuerza el rol a `cliente` en registros públicos, ignorando campos administrativos enviados en el cuerpo del JSON. |
| **API4:2023** | **Unrestricted Resource Consumption** | Ataque de denegación de servicio solicitando consultas masivas de catálogo sin paginación. | Paginación obligatoria con límites máximos de registros por respuesta (`LIMIT 50`) y pool concurrente de hilos de alto rendimiento. |
| **API5:2023** | **Broken Function Level Authorization** | Un cliente invoca directamente `PUT /api/users/{id}/suspend` para inhabilitar competidores. | Validación centralizada de rol en manejadores; operaciones de suspensión y moderación exigen estrictamente rol `administrador`. |
| **API6:2023** | **Unrestricted Access to Sensitive Business Flows** | Bots automatizados reservan todos los rollos de tela dejando a la tienda sin stock (*inventory hoarding*). | Orquestación Saga con liberación automática de reservas no pagadas en 10 minutos y validación CAPTCHA en flujos masivos. |
| **API7:2023** | **Server Side Request Forgery (SSRF)** | Inyección de URLs maliciosas en la subida de fotos de productos para escanear la red interna Docker. | Las imágenes solo se admiten en formato Base64 validado o se descargan desde dominios autorizados en la lista blanca. |
| **API8:2023** | **Security Misconfiguration** | Exposición de trazas internas de error Java (Stack Traces) con detalles del servidor y base de datos. | Manejo global de excepciones (`try-catch` en `ManejadorBase`) que devuelve mensajes JSON genéricos `{"error": "Error interno del servidor"}` y correlation ID para auditoría interna. |
| **API9:2023** | **Improper Inventory Management** | Uso de endpoints antiguos o de depuración expuestos sin autenticación. | Auditoría y eliminación completa de controladores huérfanos (`AutenticacionDAO`, `ManejadorImagenProducto`, `DOCUMENTACION_BACKEND.md`). |
| **API10:2023**| **Unsafe Consumption of APIs** | Confianza ciega en respuestas de pasarelas de pago externas permitiendo pagos falsificados. | Validación estricta de firmas HMAC de webhooks recibidos de la pasarela antes de modificar el estado del pedido a `paid`. |

---

## 6. Política de Auditoría y No Repudio

Todas las operaciones de mutación de inventario, cambios de precios, asignación de cupones y cortes de tela generan un registro de trazabilidad inmutable estructurado en formato JSON estándar:

```json
{
  "timestamp": "2026-10-14T15:40:22.104Z",
  "level": "INFO",
  "service_name": "tienda-digital-textiles-api",
  "trace_id": "corr_8fa1b490-33de-4a92-91f4-00129a8fbc71",
  "operator_id": 12,
  "action": "EXECUTE_ROLL_CUT",
  "entity": "rollos_textil",
  "entity_id": "ROL-502",
  "meters_cut": 6.0,
  "previous_meters": 12.0,
  "new_meters": 6.0,
  "client_ip": "190.144.25.10",
  "user_agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
}
```

Este registro garantiza el **no repudio** ante cualquier discrepancia entre el metraje físico cortado en bodega y los metros facturados al cliente.
