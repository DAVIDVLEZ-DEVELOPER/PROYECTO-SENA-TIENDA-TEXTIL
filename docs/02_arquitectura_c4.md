# 02. Arquitectura de Software, Modelo C4 y Contratos Técnicos
**Proyecto:** Plataforma Distribuida de Comercialización y Control Logístico Textil (**D&D Textil**)  
**Estándares:** Modelo C4 (Simon Brown), Domain-Driven Design (DDD), Event-Driven Architecture  
**Versión:** 2.0.0 — Sustentación Técnica SENA ADSO  
**Fecha:** Octubre 2026  

---

## 1. Modelo C4 — Vista Arquitectónica Multinivel

### 1.1. Nivel 1: Diagrama de Contexto del Sistema
El diagrama de contexto ilustra la interacción entre los distintos actores humanos, el ecosistema **D&D Textil** y los sistemas externos de soporte transaccional y de comunicación.

```mermaid
C4Context
    title Diagrama de Contexto del Sistema - D&D Textil (Nivel 1)

    Person(cliente, "Cliente / Confeccionista", "Consulta catálogo, calcula metrajes, compra telas fraccionadas y rastrea pedidos.")
    Person(vendedor, "Asesor Comercial / Vendedor", "Publica catálogo, administra stock por lotes y analiza métricas comerciales.")
    Person(operario, "Operario de Bodega / Mesa de Corte", "Escanea códigos QR, ejecuta cortes físicos en rollos y reporta mermas.")
    Person(admin, "Administrador del Sistema", "Monitorea finanzas, aprueba cuentas, gestiona cupones y audita seguridad.")

    System(sistemaTextil, "Ecosistema D&D Textil", "Plataforma integral de venta de telas por metraje continuo, inventario por rollos físicos y orquestación de corte.")

    System_Ext(pasarelaPagos, "Pasarela de Pagos (Wompi / PayU / PSE)", "Procesa cobros mediante tarjetas, transferencias PSE y emite webhooks de confirmación.")
    System_Ext(servicioMensajeria, "Servicio de Notificaciones (Twilio / SendGrid)", "Envía correos electrónicos y mensajes SMS transaccionales con guías de envío y alertas.")

    Rel(cliente, sistemaTextil, "Navega catálogo, compra telas y solicita soporte", "HTTPS / JSON")
    Rel(vendedor, sistemaTextil, "Gestiona productos y monitorea ventas", "HTTPS / JSON")
    Rel(operario, sistemaTextil, "Confirma cortes y registra remanentes", "App Móvil / HTTPS")
    Rel(admin, sistemaTextil, "Administra usuarios, cupones y métricas", "HTTPS / JSON")

    Rel(sistemaTextil, pasarelaPagos, "Inicia transacciones y verifica pagos", "HTTPS REST / Webhooks")
    Rel(sistemaTextil, servicioMensajeria, "Dispara notificaciones de corte y despacho", "API REST Asíncrona")
```

---

### 1.2. Nivel 2: Diagrama de Contenedores
Describe la topología de contenedores independientes, tecnologías de ejecución y protocolos de comunicación inter-servicio.

```mermaid
C4Container
    title Diagrama de Contenedores - D&D Textil (Nivel 2)

    Person(usuario, "Usuario Final", "Cliente, Vendedor, Operario o Administrador.")

    Container(frontendWeb, "Single Page Application (SPA)", "React 18, Vite, TailwindCSS", "Interfaz de usuario reactiva para catálogo, simulador de metraje y tableros administrativos (Puerto 3001).")
    Container(appMovil, "Aplicación Móvil Bodega", "Android WebView / Kotlin", "Cliente móvil optimizado para operarios de mesa de corte con cámara para escaneo de códigos QR.")

    Container(apiGateway, "Backend Java Corporativo", "Java 17, HttpServer Nativo, Pool 32 Hilos", "Procesa lógica de negocio, valida metrajes continuos, gestiona sesiones y expone API REST (Puerto 8081).")

    ContainerDb(postgresDb, "Base de Datos Relacional", "PostgreSQL 17", "Almacena esquema transaccional de usuarios, productos, rollos físicos, mermas, cupones y pedidos (Puerto 5432).")
    ContainerDb(redisCache, "Caché en Memoria y Rate Limiting", "In-Memory Concurrent Map / Redis", "Control de ventanas de tráfico por IP (Rate Limiting 60 req/min) y sesiones volátiles.")
    Container(fileStorage, "Almacenamiento de Multimedia", "Volumen Local /app/uploads", "Persistencia de fotografías de telas en alta resolución y certificados de garantía.")

    Rel(usuario, frontendWeb, "Interactúa vía navegador web", "HTTPS")
    Rel(usuario, appMovil, "Interactúa en terminal móvil en bodega", "HTTPS")

    Rel(frontendWeb, apiGateway, "Consume endpoints REST", "JSON sobre HTTPS (CORS)")
    Rel(appMovil, apiGateway, "Envía confirmaciones de corte y QR", "JSON sobre HTTPS")

    Rel(apiGateway, postgresDb, "Consultas y bloqueos pesimistas (FOR UPDATE)", "JDBC / Pool HikariCP")
    Rel(apiGateway, redisCache, "Valida límites de peticiones y tokens", "Acceso en memoria")
    Rel(apiGateway, fileStorage, "Lee y escribe imágenes en base64", "I/O Sistema de Archivos")
```

---

### 1.3. Nivel 3: Diagrama de Componentes (Módulo de Inventario y Corte Textil)
Detalla la estructura interna de clases, capas y flujo de ejecución bajo Clean Architecture:

```mermaid
graph TD
    subgraph Infraestructura_API ["Capa de Infraestructura: API HTTP"]
        Handler["ManejadorInventario / ManejadorPedidos"]
        BaseHandler["ManejadorBase (RateLimit, CORS, CorrelationId)"]
        Handler -.->|Hereda| BaseHandler
    end

    subgraph Aplicacion_Servicios ["Capa de Aplicación: Casos de Uso"]
        ServicioCorte["ServicioCorteTextil<br/>• validarMetrajeContinuo()<br/>• procesarCorteYRemanente()<br/>• calcularTarifaEscalonada()"]
        ServicioAuth["ServicioAutenticacion<br/>• hashearContrasena()<br/>• autenticar()"]
        ServicioFactura["ServicioFactura<br/>• generarFacturaXmlSimulada()<br/>• numeroAPalabras()"]
    end

    subgraph Dominio_Modelos ["Capa de Dominio: Entidades Puras"]
        Rollo["RolloTextil (id, metraje, lote)"]
        Producto["Producto"]
        Pedido["Pedido"]
        EventoMerma["EventoMerma"]
    end

    subgraph Infraestructura_Persistencia ["Capa de Infraestructura: Persistencia JDBC"]
        InventarioDAO["InventarioDAO"]
        PedidoDAO["PedidoDAO (SELECT ... FOR UPDATE)"]
        ProductoDAO["ProductoDAO"]
        ConexionPool["Conexion (Pool HikariCP)"]
    end

    Handler -->|Invoca Caso de Uso| ServicioCorte
    Handler -->|Invoca Caso de Uso| ServicioFactura
    Handler -->|Invoca Caso de Uso| ServicioAuth

    ServicioCorte -->|Opera sobre| Rollo
    ServicioCorte -->|Aplica reglas sobre| EventoMerma
    ServicioCorte -->|Consulta persistencia| InventarioDAO
    ServicioCorte -->|Reserva transaccional| PedidoDAO

    InventarioDAO -->|Solicita Conexión| ConexionPool
    PedidoDAO -->|Solicita Conexión| ConexionPool
    ProductoDAO -->|Solicita Conexión| ConexionPool
```

---

## 2. Diseño de Datos y Diagrama Entidad-Relación (DER)

### 2.1. Diagrama Entidad-Relación (Mermaid)

```mermaid
erDiagram
    USUARIOS ||--o{ PRODUCTOS : "publica"
    USUARIOS ||--o{ PEDIDOS : "realiza (cliente)"
    USUARIOS ||--o{ PEDIDOS : "atiende (vendedor)"
    USUARIOS ||--o{ ITEMS_CARRITO : "posee"
    USUARIOS ||--o{ RESENAS : "escribe"
    USUARIOS ||--o{ TICKETS_SOPORTE : "radica"

    CATEGORIAS ||--o{ PRODUCTOS : "clasifica"
    CATEGORIAS ||--o{ CATEGORIAS_CUPON : "aplica_a"

    PRODUCTOS ||--o{ IMAGENES_PRODUCTO : "contiene"
    PRODUCTOS ||--o{ LOTES_INVENTARIO : "abastecido_por"
    PRODUCTOS ||--o{ EVENTOS_MERMA : "registra"
    PRODUCTOS ||--o{ ITEMS_PEDIDO : "incluido_en"
    PRODUCTOS ||--o{ ITEMS_CARRITO : "agregado_en"
    PRODUCTOS ||--o{ RESENAS : "recibe"

    LOTES_INVENTARIO ||--o{ ROLLOS_TEXTIL : "genera"

    PEDIDOS ||--|{ ITEMS_PEDIDO : "contiene"
    CUPONES ||--o{ CATEGORIAS_CUPON : "restringe"

    USUARIOS {
        int id PK
        string nombre
        string correo UK
        string contrasena_hash
        string rol
        boolean activo
        boolean suspendido
        numeric tasa_comision
        timestamp registrado_en
        timestamp ultimo_acceso
    }

    PRODUCTOS {
        int id PK
        string nombre
        int categoria_id FK
        numeric precio
        int vendedor_id FK
        string material
        string ancho
        string peso
        text cuidados
        int stock
        boolean activo
        string estado_moderacion
    }

    LOTES_INVENTARIO {
        int id PK
        int producto_id FK
        string numero_lote
        numeric metros_recibidos
        numeric costo_por_metro
        string proveedor
        date fecha_recepcion
    }

    ROLLOS_TEXTIL {
        int id PK
        int producto_id FK
        int lote_id FK
        string codigo_rollo UK
        numeric metraje_inicial
        numeric metraje_remanente
        string lote_tintoreria
        string estado
        string ubicacion_bodega
    }

    EVENTOS_MERMA {
        int id PK
        int producto_id FK
        numeric metros_perdidos
        string motivo
        numeric impacto_costo
        timestamp creado_en
    }

    PEDIDOS {
        int id PK
        int usuario_id FK
        string nombre_cliente
        string correo_cliente
        numeric monto_total
        numeric subtotal
        numeric costo_envio
        numeric monto_descuento
        string codigo_cupon
        string estado
        string estado_pago
        string numero_guia
    }

    ITEMS_PEDIDO {
        int id PK
        int pedido_id FK
        int producto_id FK
        numeric cantidad
        numeric precio_por_metro
        numeric subtotal
    }
```

---

## 3. Diccionario de Datos Exhaustivo

### 3.1. Tabla: `usuarios`
| Campo | Tipo | Longitud | Nulo | Clave | Descripción de Negocio |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `id` | `SERIAL` | 4 B | NO | **PK** | Identificador único secuencial del usuario. |
| `nombre` | `VARCHAR` | 100 | NO | - | Nombre completo o razón social del usuario. |
| `correo` | `VARCHAR` | 150 | NO | **UK** | Correo electrónico corporativo o personal (login). |
| `contrasena_hash`| `VARCHAR` | 255 | NO | - | Hash criptográfico SHA-256 en UTF-8 con salt. |
| `rol` | `VARCHAR` | 50 | NO | - | Rol RBAC: `cliente`, `vendedor`, `operario_corte`, `administrador`. |
| `activo` | `BOOLEAN` | 1 B | NO | - | Estado de activación de la cuenta (por defecto `true`). |
| `suspendido` | `BOOLEAN` | 1 B | NO | - | Indicador de suspensión administrativa de la cuenta. |
| `motivo_suspension` | `TEXT` | Variable | SÍ | - | Razón justificada de la suspensión disciplinaria. |
| `tasa_comision` | `NUMERIC` | 5,2 | SÍ | - | Porcentaje de comisión pactado con el vendedor (ej. 5.50%). |
| `registrado_en` | `TIMESTAMP` | 8 B | NO | - | Fecha y hora exacta de registro en el sistema. |
| `ultimo_acceso` | `TIMESTAMP` | 8 B | SÍ | - | Fecha y hora de la última autenticación exitosa. |

### 3.2. Tabla: `productos`
| Campo | Tipo | Longitud | Nulo | Clave | Descripción de Negocio |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `id` | `SERIAL` | 4 B | NO | **PK** | Identificador unívoco del producto textil. |
| `nombre` | `VARCHAR` | 200 | NO | - | Nombre comercial del tejido (ej. "Lino Italiano Puro"). |
| `categoria_id` | `INTEGER` | 4 B | SÍ | **FK** | Referencia a la tabla `categorias(id)`. |
| `precio` | `NUMERIC` | 12,2 | NO | - | Precio base de venta por metro lineal en COP. |
| `vendedor_id` | `INTEGER` | 4 B | SÍ | **FK** | Referencia a `usuarios(id)` propietario del artículo. |
| `descripcion` | `TEXT` | Variable | SÍ | - | Descripción comercial y cualidades estéticas del tejido. |
| `material` | `VARCHAR` | 100 | SÍ | - | Composición textil (ej. 100% Algodón Peinado). |
| `ancho` | `VARCHAR` | 50 | SÍ | - | Ancho útil del rollo (ej. "1.50 metros"). |
| `peso` | `VARCHAR` | 50 | SÍ | - | Gramaje del tejido (ej. "280 g/m²"). |
| `cuidados` | `TEXT` | Variable | SÍ | - | Instrucciones de lavado, planchado y secado. |
| `stock` | `INTEGER` | 4 B | NO | - | Existencia física total agregada en metros lineales. |
| `destacado` | `BOOLEAN` | 1 B | NO | - | Flag para exhibición en banner principal o destacados. |
| `activo` | `BOOLEAN` | 1 B | NO | - | Visibilidad en catálogo público para clientes. |
| `estado_moderacion` | `VARCHAR` | 50 | NO | - | Estado de aprobación: `pending`, `approved`, `rejected`. |

### 3.3. Tabla: `rollos_textil`
| Campo | Tipo | Longitud | Nulo | Clave | Descripción de Negocio |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `id` | `SERIAL` | 4 B | NO | **PK** | Identificador primario del rollo físico. |
| `producto_id` | `INTEGER` | 4 B | NO | **FK** | Relación con el producto textil en `productos(id)`. |
| `lote_id` | `INTEGER` | 4 B | NO | **FK** | Relación con el lote de compra en `lotes_inventario(id)`. |
| `codigo_rollo` | `VARCHAR` | 50 | NO | **UK** | Código único de barras o QR físico (ej. "ROL-2026-0089"). |
| `metraje_inicial` | `NUMERIC` | 10,2 | NO | - | Longitud original con la que llegó el rollo de fábrica (m). |
| `metraje_remanente`| `NUMERIC`| 10,2 | NO | - | Metros exactos disponibles actualmente en el rollo. |
| `lote_tintoreria` | `VARCHAR` | 50 | NO | - | Partida o tintura química (ej. "TINT-AZUL-L44"). |
| `estado` | `VARCHAR` | 30 | NO | - | Estado: `disponible`, `reservado`, `retazo_liquidado`, `agotado`. |
| `ubicacion_bodega` | `VARCHAR` | 50 | SÍ | - | Coordenada en estantería (ej. "EST-04-NIVEL-2"). |

### 3.4. Tabla: `eventos_merma`
| Campo | Tipo | Longitud | Nulo | Clave | Descripción de Negocio |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `id` | `SERIAL` | 4 B | NO | **PK** | Identificador del registro de merma. |
| `producto_id` | `INTEGER` | 4 B | NO | **FK** | Referencia a `productos(id)`. |
| `metros_perdidos` | `NUMERIC` | 10,2 | NO | - | Metros descontados del inventario por desperdicio o retazo. |
| `motivo` | `VARCHAR` | 100 | NO | - | Causa: `remanente_sub_metro`, `corte_defectuoso`, `mancha_fabrica`. |
| `impacto_costo` | `NUMERIC` | 10,2 | SÍ | - | Valorización monetaria de la pérdida según costo de compra. |
| `creado_en` | `TIMESTAMP` | 8 B | NO | - | Fecha y hora en la que se reportó la merma. |

### 3.5. Tabla: `pedidos`
| Campo | Tipo | Longitud | Nulo | Clave | Descripción de Negocio |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `id` | `SERIAL` | 4 B | NO | **PK** | Identificador numérico del pedido comercial. |
| `usuario_id` | `INTEGER` | 4 B | SÍ | **FK** | Cuenta de cliente compradora en `usuarios(id)`. |
| `nombre_cliente` | `VARCHAR` | 150 | NO | - | Nombre de la persona que recibe el despacho. |
| `correo_cliente` | `VARCHAR` | 150 | NO | - | Correo para envío de factura y guía de rastreo. |
| `telefono_cliente` | `VARCHAR` | 50 | SÍ | - | Teléfono de contacto para la transportadora. |
| `direccion_envio` | `TEXT` | Variable | NO | - | Nomenclatura física completa de entrega. |
| `ciudad_envio` | `VARCHAR` | 100 | SÍ | - | Ciudad de destino (ej. Bogotá D.C., Medellín). |
| `departamento_envio`| `VARCHAR`| 100 | SÍ | - | Departamento colombiano (ej. Cundinamarca, Antioquia). |
| `monto_total` | `NUMERIC` | 12,2 | NO | - | Valor final total pagado por el cliente en COP. |
| `subtotal` | `NUMERIC` | 12,2 | SÍ | - | Sumatoria de precios de telas antes de impuestos y flete. |
| `costo_envio` | `NUMERIC` | 12,2 | SÍ | - | Flete liquidado según destino y volumen de tela. |
| `monto_descuento` | `NUMERIC` | 12,2 | SÍ | - | Descuento aplicado por cupón promocional o volumen. |
| `estado` | `VARCHAR` | 50 | NO | - | Estado: `pending`, `cutting`, `shipped`, `delivered`, `cancelled`. |
| `estado_pago` | `VARCHAR` | 50 | NO | - | Estado de cobro: `pending`, `paid`, `refunded`, `failed`. |
| `numero_guia` | `VARCHAR` | 100 | SÍ | - | Número de rastreo de transportadora o identificador QR de corte. |

---

## 4. Especificación de Eventos Asíncronos y Orquestación Saga

En un entorno desacoplado, la creación de un pedido coordina múltiples dominios mediante eventos de integración:

```mermaid
sequenceDiagram
    autonumber
    actor Cliente
    participant Gateway as ServidorApi / Pedidos
    participant Saga as Orquestador Saga Pedido
    participant DB as PostgreSQL (PedidoDAO / InventarioDAO)
    participant Pasarela as Pasarela Pagos
    participant Bodega as Servicio Mesa de Corte

    Cliente->>Gateway: POST /api/orders (solicitud 6m Lino continuo)
    Gateway->>Saga: Iniciar Saga: CrearPedido
    Saga->>DB: Bloqueo Pesimista (SELECT FOR UPDATE) & Reserva Rollo
    DB-->>Saga: Rollo "ROL-502" reservado (remanente de 12m a 6m)
    
    Saga->>Pasarela: Solicitar Cobro Transaccional ($180.000 COP)
    alt Pago Aprobado
        Pasarela-->>Saga: Pago Exitoso (TransactionId: TX-8874)
        Saga->>DB: Confirmar Pedido (estado='cutting', estado_pago='paid')
        Saga->>Bodega: Emitir Evento OrderCreatedEvent (Generar Orden Corte QR)
        Saga-->>Gateway: Pedido Exitoso (201 Created)
        Gateway-->>Cliente: Confirmación de Pedido con Guía
    else Pago Rechazado o Timeout (Compensación Saga)
        Pasarela-->>Saga: Error de Cobro (Fondos Insuficientes / Rechazo)
        Saga->>DB: Transacción Compensatoria: Reintegrar 6m al Rollo "ROL-502"
        Saga->>DB: Actualizar Pedido (estado='cancelled', estado_pago='failed')
        Saga-->>Gateway: Error 400 (Pago No Procesado)
        Gateway-->>Cliente: Notificación de rechazo con stock liberado
    end
```

### 4.1. Payloads JSON de Eventos Canónicos

#### Evento: `OrderCreatedEvent`
```json
{
  "eventId": "evt_998471a2-c15e-42ab-89cd-48e02d0cfb88",
  "eventType": "ORDER_CREATED_FOR_CUTTING",
  "timestamp": "2026-10-14T15:30:00Z",
  "correlationId": "corr_c8a7b921-9310-4f81-a987-99b8214fa761",
  "payload": {
    "orderId": 1042,
    "customer": {
      "name": "María Confecciones SAS",
      "email": "produccion@mariaconfecciones.com",
      "city": "Medellín"
    },
    "items": [
      {
        "productId": 14,
        "productName": "Lino Crudo Puro",
        "requestedMeters": 6.0,
        "assignedRollId": "ROL-502",
        "dyeLot": "LOT-2026-A",
        "unitPrice": 30000.0,
        "subtotal": 180000.0
      }
    ],
    "cuttingStation": "MESA-02-NORTE",
    "qrCodePayload": "DND-ORDER-1042-ROL502-6M"
  }
}
```

#### Evento: `RollStockDepletedEvent` (Alerta de Merma o Retazo)
```json
{
  "eventId": "evt_31818bfa-5120-449e-b991-b1e840a1b802",
  "eventType": "ROLL_RECLASSIFIED_AS_REMNANT",
  "timestamp": "2026-10-14T15:35:10Z",
  "payload": {
    "rollId": "ROL-88",
    "productId": 7,
    "productName": "Seda Estampada Flora",
    "remainingMeters": 0.75,
    "thresholdLimit": 1.0,
    "actionTaken": "MOVED_TO_REMNANT_DISCOUNT",
    "discountApplied": 0.40,
    "remnantUnitPrice": 15000.0,
    "operatorId": 4
  }
}
```
