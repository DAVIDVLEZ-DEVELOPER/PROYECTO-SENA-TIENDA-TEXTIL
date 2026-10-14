# 01. Especificación de Requerimientos de Software (SRS) — Norma IEEE 830 & BDD
**Proyecto:** Plataforma Distribuida de Comercialización y Control Logístico Textil (**D&D Textil**)  
**Programa de Formación:** Tecnólogo en Análisis y Desarrollo de Software (ADSO) — SENA  
**Versión:** 2.0.0 — Producción y Sustentación Técnica  
**Fecha:** Octubre 2026  

---

## 1. Fase de Investigación y Fundamentación (Marco del Proyecto)

### 1.1. Planteamiento del Problema
En el sector textil tradicional colombiano (conglomerados comerciales y distribuidores de confección como el sector de Alquería en Bogotá, El Hueco en Medellín o el Centro de Cali), los procesos de comercialización, corte y despacho de telas operan con un alto grado de empirismo y manualidad. Esta dinámica genera ineficiencias estructurales críticas:

1. **Errores humanos en la medición de rollos:** La venta por metraje fraccionado depende de mediciones manuales con metro de madera o contadores mecánicos sin calibración electrónica, provocando discrepancias de hasta un $\pm 4\%$ entre el inventario registrado en libros y la existencia física real en bodega.
2. **Generación descontrolada de mermas y retazos:** Cuando un cliente solicita una fracción de rollo (por ejemplo, 3.5 metros de un rollo de 4.2 metros), queda un remanente de 0.7 metros. En los almacenes tradicionales, estos retazos menores a un metro quedan desarticulados, sin precio diferencial ni etiquetado, acumulándose como merma oculta y depreciación financiera.
3. **Inconsistencias de tonalidad por lotes de tintorería (*Color Shading / Lot Drift*):** Las telas teñidas industrialmente presentan variaciones microscópicas de pH, temperatura y absorción de pigmento entre diferentes partidas o lotes de fabricación (*batches*). Vender 10 metros continuos a un confeccionista suministrando 6 metros del Lote A y 4 metros del Lote B produce prendas con paneles de distinto tono cromático, provocando devoluciones forzadas, reclamos de garantía y pérdidas directas de reputación.
4. **Tiempos muertos y cuellos de botella en bodega:** La búsqueda física de rollos sin identificación unívoca por código de barras o QR causa demoras de hasta 45 minutos por pedido, impidiendo satisfacer la demanda del comercio electrónico moderno.

### 1.2. Justificación
La automatización y digitalización del ciclo de vida textil mediante una arquitectura orientada a servicios desacoplados resuelve estas problemáticas de raíz:

- **Impacto Económico:** Reduce las pérdidas por merma no contabilizada en más de un $18\%$, transformando retazos remanentes ($< 1.0\text{ m}$) en productos de salida rápida con precios de liquidación automática para manualidades y muestrarios.
- **Impacto Operativo:** Garantiza la **integridad de tono** al validar que el metraje solicitado se asigne exclusivamente a rollos del mismo lote de tintorería y que el metraje continuo no se fragmente sin el consentimiento expreso del comprador.
- **Trazabilidad en Tiempo Real:** Facilita la sincronización del inventario entre el catálogo web, la aplicación móvil y la mesa de corte en bodega, aplicando bloqueos transaccionales pesimistas y orquestación distribuida para evitar la sobreventa (*overselling*).

### 1.3. Objetivos del Proyecto

#### Objetivo General
Desarrollar una plataforma integral distribuida bajo principios de Domain-Driven Design (DDD) y arquitectura modular orientada a servicios para la comercialización, reserva concurrente y control logístico de textiles con consistencia transaccional y alta disponibilidad.

#### Objetivos Específicos
1. **Diseñar y modelar** una arquitectura modular desacoplada basada en Clean Architecture y Domain-Driven Design, separando los dominios de Catálogo, Reserva de Inventario, Pagos, Corte Logístico y Soporte al Cliente.
2. **Implementar un motor de cálculo de metraje y corte**, capaz de verificar la continuidad del tejido en rollos individuales, aplicar tarifas escalonadas (detal, mayorista, rollo cerrado) y reclasificar mermas en tiempo real.
3. **Integrar un flujo transaccional seguro** con orquestación tipo Saga y mecanismos de compensación ante fallos de pago, garantizando la concurrencia sin sobreventa mediante bloqueos en base de datos.
4. **Desplegar un pipeline automatizado de Integración y Entrega Continua (CI/CD)** en GitHub Actions con análisis de calidad estática, pruebas automatizadas de seguridad y contenedorización en Docker.

### 1.4. Alcance y Delimitación

#### Módulos Cubiertos en el Alcance
- **Catálogo Textil Avanzado:** Fichas técnicas granulares (composición porcentual, gramaje $\text{g/m}^2$, ancho de pieza, elasticidad, tipo de tejido y cuidados de lavado).
- **Inventario Granular por Rollos Físicos:** Trazabilidad unívoca por rollo (`id_rollo`, metraje total, metraje remanente, número de lote de tintorería y ubicación en estantería).
- **Cotizador y Carrito Inteligente:** Simulador de metraje para confección (cortinas, vestidos, manteles) con cálculo de desperdicio técnico recomendado ($+10\%$).
- **Gestión Transaccional de Pedidos:** Reserva de metraje continuo, validación de cupones con reglas de negocio y cálculo automatizado de impuestos y envíos.
- **Mesa de Corte y Despacho:** Generación de órdenes de corte con código QR para escaneo por parte del operario de bodega.
- **Portal Administrativo y Vendedor:** Tableros de control con métricas de ventas diarias, por departamento y registro de mermas.

#### Delimitación (Fuera del Alcance Inicial)
- Integración directa mediante protocolos industriales (OPC-UA / Modbus) con telares mecánicos o maquinaria pesada de tejeduría.
- Conexión propietaria vía EDI con ERPs legados monolíticos de gran escala (SAP R/3 o AS/400).
- Despachos aduaneros internacionales automatizados; la plataforma opera inicialmente en territorio nacional colombiano.

---

## 2. Matriz de Requerimientos Funcionales (RF) — IEEE 830

| Código | Nombre del Requerimiento | Descripción Detallada | Rol Autorizado | Prioridad |
| :---: | :--- | :--- | :---: | :---: |
| **RF-01** | Registro y autenticación bajo RBAC | El sistema debe permitir el registro e inicio de sesión de usuarios clasificándolos en cuatro roles: `cliente`, `vendedor`, `operario_corte` y `administrador`, aplicando hashing SHA-256 en UTF-8 y tokens de sesión. | Todos | **Alta** |
| **RF-02** | Ficha técnica y catálogo textil granular | El sistema debe registrar y exponer las propiedades físicas y químicas de cada tela: composición (ej. 100% lino, 65/35 poliéster-algodón), gramaje ($\text{g/m}^2$), ancho útil, elasticidad y recomendaciones de cuidado. | Vendedor, Admin | **Alta** |
| **RF-03** | Trazabilidad de inventario por rollo físico | Cada unidad física de inventario debe registrarse como un rollo con identificador único (`id_rollo`), metraje inicial, metraje remanente, código de lote de tintorería (`lote_tintoreria`) y estado (`disponible`, `en_corte`, `agotado`). | Operario, Admin | **Alta** |
| **RF-04** | Validación de metraje continuo | Al procesar una solicitud de compra, el motor debe validar que el metraje solicitado quepa de manera íntegra y sin empates en un solo rollo del lote seleccionado; si no es posible, debe rechazar la reserva automática y sugerir opciones de división al cliente. | Sistema, Cliente | **Crítica** |
| **RF-05** | Reclasificación automática de mermas y retazos | Cuando tras un corte el metraje remanente de un rollo resulte inferior a $1.0\text{ m}$, el sistema debe dar de baja el rollo del inventario regular y registrarlo automáticamente en el inventario de retazos con precio de liquidación o en la tabla de mermas. | Sistema, Operario | **Media** |
| **RF-06** | Transaccionalidad de pedidos y compensación Saga | La confirmación de un pedido debe ejecutar una reserva transaccional atómica con bloqueo pesimista en base de datos. Si el pago es rechazado o cancelado, el sistema debe ejecutar una compensación liberando el metraje bloqueado en menos de 60 segundos. | Sistema, Cliente | **Crítica** |
| **RF-07** | Generación de orden de corte con código QR | Al confirmarse el pago, el sistema debe generar una orden de corte que incluya un identificador escaneable (código QR/Barras), especificando el ID de rollo exacto, lote de tintorería, metros a cortar y mesa de preparación asignada. | Operario, Bodeguero | **Alta** |

---

## 3. Matriz de Requerimientos No Funcionales (RNF) — Norma ISO/IEC 25010

| Código | Característica ISO | Métrica y Criterio de Aceptación | Mecanismo Técnico de Validación |
| :---: | :--- | :--- | :--- |
| **RNF-01** | **Eficiencia de Desempeño (Comportamiento Temporal)** | El tiempo de respuesta en consultas del catálogo textil y cotización de metraje debe ser $\le 200\text{ ms}$ en el percentil 95 ($p95$) bajo condiciones normales de carga. | Índices compuestos B-Tree en PostgreSQL, consultas SQL optimizadas y compresión Gzip/HTTP. |
| **RNF-02** | **Fiabilidad y Concurrencia** | El sistema debe soportar un mínimo de 50 transacciones concurrentes de compra sobre el mismo producto sin incurrir en sobreventa (*race conditions* ni balances negativos). | Bloqueo pesimista en base de datos (`SELECT ... FOR UPDATE`), transacciones aisladas JDBC y pool HikariCP con 32+ hilos. |
| **RNF-03** | **Seguridad (Confidencialidad e Integridad)** | Los datos personales y credenciales deben cifrarse con SHA-256 en reposo; no se almacenarán datos de tarjetas de crédito (PCI-DSS); transporte 100% sobre TLS 1.3 con headers CSP y X-Frame-Options. | Validación con suite `PruebasSeguridadYBugs.java`, meta-tags de seguridad en frontend y desinfección de entrada anti-XSS y anti-SQLi. |
| **RNF-04** | **Disponibilidad y Resiliencia** | La plataforma debe garantizar una disponibilidad mínima del $99.5\%$ anual. La degradación de servicios secundarios (envío de correos o reportes de analytics) no debe suspender el flujo de cobro y corte. | Arquitectura desacoplada, probes de salud (`/health/live`, `/health/ready`) y orquestación con Docker Compose restart policies. |
| **RNF-05** | **Mantenibilidad** | El código fuente debe estructurarse bajo Clean Architecture con separación estricta de capas; cobertura de pruebas unitarias $\ge 80\%$ y deuda técnica Grado A según SonarQube. | División en paquetes `dominio`, `aplicacion`, `infraestructura`; suites de pruebas automatizadas y nombres canónicos en español. |

---

## 4. Historias de Usuario con Criterios de Aceptación (Formato BDD / Gherkin)

### Historia de Usuario 1: Reserva de Metraje Continuo
**Como** cliente confeccionista  
**Quiero** comprar 6 metros continuos de tela Lino Crudo  
**Para** garantizar que la confección del vestido no tenga empates de costura ni variaciones de tono cromático.

```gherkin
Característica: Reserva de metraje continuo para compra de tela
  Como cliente de confección
  Quiero comprar 6 metros continuos de Lino Crudo
  Para asegurar que la prenda no tenga empates ni cortes visibles

  Escenario: Rollo con metraje suficiente en un solo lote
    Dado que existe el rollo "ROL-502" con 12.0 metros disponibles del lote "LOT-2026-A"
    Cuando solicito una compra de 6.0 metros continuos de tela
    Entonces el sistema reserva exitosamente 6.0 metros del rollo "ROL-502"
    Y el remanente del rollo "ROL-502" se actualiza a 6.0 metros
    Y el pedido queda vinculado al lote de tintorería "LOT-2026-A"
    Y no se genera evento de merma

  Escenario: Metraje total en bodega suficiente pero fragmentado entre rollos distintos
    Dado que existen dos rollos en bodega:
      | id_rollo | metraje_disponible | lote_tintoreria |
      | ROL-101  | 4.0                | LOT-2026-A      |
      | ROL-102  | 4.0                | LOT-2026-B      |
    Cuando solicito una compra de 6.0 metros continuos de tela
    Entonces el sistema rechaza la reserva automática en un solo rollo
    Y responde con un código de advertencia "FRAGMENTED_STOCK"
    Y sugiere al cliente dividir la compra o esperar abastecimiento de un rollo completo
```

---

### Historia de Usuario 2: Reclasificación Automática de Mermas y Retazos
**Como** operario de corte en bodega  
**Quiero** que el sistema detecte cuando un rollo queda con menos de 1.0 metro de tela  
**Para** no vender fragmentos inservibles y trasladarlos automáticamente al inventario de retazos con descuento.

```gherkin
Característica: Reclasificación automática de retazos tras el corte
  Como operario de corte
  Quiero que los remanentes menores a un metro se clasifiquen como retazos
  Para optimizar el inventario y prevenir reclamos por retazos residuales

  Escenario: Corte que deja un remanente menor al umbral crítico
    Dado que el rollo "ROL-88" tiene un metraje remanente de 4.5 metros
    Cuando se confirma un corte de 3.8 metros para el pedido "PED-9901"
    Entonces el remanente matemático es de 0.7 metros
    Y como 0.7 metros es menor al umbral de 1.0 metro
    Entonces el rollo "ROL-88" pasa a estado "retazo_liquidado"
    Y se registra un evento en la tabla "eventos_merma" con motivo "remanente_sub_metro"
    Y el retazo se publica en la sección de ofertas con un 40% de descuento automático

  Escenario: Corte que deja un remanente comercializable
    Dado que el rollo "ROL-90" tiene un metraje remanente de 15.0 metros
    Cuando se confirma un corte de 10.0 metros para el pedido "PED-9902"
    Entonces el remanente matemático es de 5.0 metros
    Y como 5.0 metros es mayor o igual a 1.0 metro
    Entonces el rollo "ROL-90" se mantiene en estado "disponible" con 5.0 metros
    Y no se genera ningún evento de merma
```

---

### Historia de Usuario 3: Precios Escalonados por Volumen
**Como** cliente mayorista o diseñador industrial  
**Quiero** recibir tarifas automáticas diferenciales según el volumen de tela comprado  
**Para** obtener rentabilidad en pedidos de escala sin depender de negociaciones manuales.

```gherkin
Característica: Cálculo automático de precios escalonados
  Como comprador de gran volumen
  Quiero tarifas reducidas por tramos de metraje
  Para incentivar compras al por mayor y de rollos completos

  Escenario: Compra al detal (menos de 15 metros)
    Dado que el precio base por metro de la tela "Seda Francesa" es de $30.000 COP
    Cuando agrego al carrito 8 metros de tela
    Entonces el sistema liquida el precio a $30.000 COP por metro
    Y el subtotal resultante es de $240.000 COP

  Escenario: Compra mayorista (entre 15 y 49.9 metros)
    Dado que el precio base por metro de la tela "Seda Francesa" es de $30.000 COP
    Cuando agrego al carrito 20 metros de tela
    Entonces el sistema aplica un 15% de descuento mayorista ($25.500 COP por metro)
    Y el subtotal resultante es de $510.000 COP

  Escenario: Compra de rollo cerrado (50 o más metros)
    Dado que el precio base por metro de la tela "Seda Francesa" es de $30.000 COP
    Cuando agrego al carrito un rollo cerrado de 60 metros
    Entonces el sistema aplica un 25% de descuento por rollo cerrado ($22.500 COP por metro)
    Y el subtotal resultante es de $1.350.000 COP
```

---

## 5. Matriz de Trazabilidad de Requerimientos vs Componentes de Software

| Requerimiento | Caso de Uso / Servicio de Aplicación | Componente de Infraestructura | Tabla Base de Datos |
| :---: | :--- | :--- | :--- |
| **RF-01** | `ServicioAutenticacion.java` | `ManejadorAutenticacion.java` | `usuarios` |
| **RF-02** | `ManejadorProductos.java` | `ProductoDAO.java` | `productos`, `categorias` |
| **RF-03** | `ServicioCorteTextil.java` | `InventarioDAO.java` | `lotes_inventario`, `rollos_textil` |
| **RF-04** | `ServicioCorteTextil.validarMetrajeContinuo()` | `PedidoDAO.java` | `rollos_textil`, `lotes_inventario` |
| **RF-05** | `ServicioCorteTextil.procesarCorteYRemanente()` | `InventarioDAO.java` | `eventos_merma`, `umbrales_stock` |
| **RF-06** | `ManejadorPedidos.java` | `PedidoDAO.java` | `pedidos`, `items_pedido` |
| **RF-07** | `ServicioFactura.java` | `ManejadorFactura.java` | `pedidos` (campo `numero_guia`) |
