# Motor Textil, Reglas de Negocio y Suite de Pruebas Automatizadas
**Proyecto:** Plataforma Distribuida de Comercializacion y Control Logistico Textil (D&D Textil)  
**Programa de Formacion:** Tecnologo en Analisis y Desarrollo de Software (ADSO) - SENA  
**Estandares:** Domain-Driven Design (DDD), Clean Architecture, Behavior-Driven Development (BDD), JUnit-Style Assertions  
**Version:** 2.0.0 - Documentacion Tecnica Oficial  

---

## 1. Fundamentacion del Motor de Dominio Textil

A diferencia de los sistemas de comercio electronico genericos de bienes discretos (donde los productos se comercializan por unidades enteras indivisibles), una plataforma de telas opera sobre un **bien continuo fraccionable** sujeto a restricciones fisicas e industriales particulares:

1. **Continuidad del Metraje:** Un cliente que solicita 8.5 metros de lino para confeccionar un lote de prendas no puede recibir dos retazos de 4.25 metros de rollos distintos, ya que los patrones de corte quedarian inservibles.
2. **Consistencia Cromatica por Lote de Tintoreria:** Los tejidos producidos en diferentes partidas de tintura presentan desviaciones de color (*shading* o *lot drift*). El abastecimiento de un pedido debe respetar obligatoriamente la homogeneidad de lote.
3. **Control Sistematico de Mermas y Retazos:** Cuando el corte de un rollo deja un sobrante inferior a 1.00 metro lineal, dicho remanente pierde su valor de corte continuo y debe reclasificarse de forma automatica e irreversible como un retazo en liquidacion.
4. **Tarificacion Dinamica por Escala de Volumen:** El precio por metro lineal debe ajustarse de forma automatica segun el volumen solicitado: detal, mayorista (a partir de 15 metros) y rollo cerrado (a partir de 50 metros).

---

## 2. Modelado de Dominio: Entidad `RolloTextil`

Ubicada en el paquete `dominio.modelos.RolloTextil`, esta clase encapsula el estado y las transiciones validas de cada rollo fisico individual almacenado en estanteria.

### 2.1. Definicion de Atributos y Estados
```java
package dominio.modelos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RolloTextil {

    public enum EstadoRollo {
        DISPONIBLE,         // Metraje >= 1.0m, apto para corte continuo o venta completa
        RESERVADO,          // Metraje bloqueado en proceso de corte o pago activo
        RETAZO_LIQUIDADO,   // Metraje < 1.0m, transferido a seccion de mermas/manualidades
        AGOTADO             // Metraje = 0.0m, rollo consumido en su totalidad
    }

    private Integer id;
    private String codigoRollo;         // Identificador univoco de codigo de barras/QR
    private Integer productoId;         // Referencia al catalogo de tela
    private String loteTintoreria;      // Codigo de la partida de tintoreria
    private BigDecimal metrajeInicial;  // Metros brutos con los que llego de fabrica
    private BigDecimal metrajeActual;   // Metros disponibles en tiempo real
    private EstadoRollo estadoRollo;    // Estado operativo en bodega
    private String ubicacionBodega;     // Coordenada fisica de estanteria (ej: PAS-02-EST-04)
    private LocalDateTime creadoEn;
    
    // ... Metodos de comportamiento de negocio ...
}
```

### 2.2. Metodos Nucleares de Comportamiento
- **`cortarMetraje(BigDecimal metrosACortar)`**:
  - Valida que `metrosACortar` sea estrictamente mayor a cero.
  - Valida que `metrosACortar` no exceda `metrajeActual` (prevencion de saldos negativos).
  - Descuenta el metraje con precision de dos decimales (`RoundingMode.HALF_UP`).
  - Si el remanente resultante es exactamente `0.00`, transiciona a `EstadoRollo.AGOTADO`.
  - Si el remanente resultante es menor a `1.00` metro y mayor a `0.00`, transiciona a `EstadoRollo.RETAZO_LIQUIDADO`.
  - Retorna un objeto `ResultadoCorte` con los metros cortados, el remanente y la bandera `esMerma`.

---

## 3. Servicio de Aplicacion: `ServicioCorteTextil`

Ubicado en `aplicacion.servicios.ServicioCorteTextil`, implementa la orquestacion de corte, calculo de tarifas y algoritmos de optimizacion de inventario.

### 3.1. Validacion de Continuidad de Metraje
```java
public boolean validarContinuidadMetraje(List<RolloTextil> rollos, BigDecimal metrosRequeridos) {
    if (metrosRequeridos == null || metrosRequeridos.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("El metraje solicitado debe ser estrictamente positivo.");
    }
    // Verifica si existe al menos un rollo disponible con metraje suficiente
    return rollos.stream()
            .anyMatch(r -> r.estaDisponible() && r.getMetrajeActual().compareTo(metrosRequeridos) >= 0);
}
```

### 3.2. Algoritmo de Seleccion de Rollo Optimo (Minimizacion de Merma)
Para evitar fragmentar rollos grandes e intactos cuando existen rollos ya iniciados que satisfacen la demanda, el motor aplica una estrategia de **Mejor Ajuste (*Best Fit*)**:
1. Filtra los rollos en estado `DISPONIBLE` que pertenezcan al lote de tintoreria solicitado y posean `metrajeActual >= metrosRequeridos`.
2. Ordena los candidatos de forma ascendente segun la diferencia `(metrajeActual - metrosRequeridos)`.
3. Selecciona el rollo cuyo sobrante sea minimo, preservando los rollos de 50 o 100 metros para pedidos de volumen mayorista.

### 3.3. Motor de Liquidacion de Precios por Volumen
El calculo del valor total a facturar aplica las siguientes reglas contractuales:

| Escala de Compra | Rango de Metraje | Porcentaje de Descuento | Condicion de Aplicacion |
| :--- | :--- | :--- | :--- |
| **Venta al Detal** | $0.01\text{ m} \le \text{Metros} < 15.00\text{ m}$ | $0\%$ (Precio base regular) | Aplica para compras domesticas o confeccion individual. |
| **Venta Mayorista** | $15.00\text{ m} \le \text{Metros} < 50.00\text{ m}$ | $12\%$ de descuento sobre precio base | Aplica para talleres de confeccion y pequenas fabricas. |
| **Rollo Cerrado** | $\text{Metros} \ge 50.00\text{ m}$ | $25\%$ de descuento sobre precio base | Venta de pieza industrial completa sin costo de corte. |
| **Retazo en Liquidacion** | Remanente $< 1.00\text{ m}$ | $40\%$ de descuento obligatorio | Venta de remanente para manualidades o muestrarios. |

---

## 4. Matriz de la Suite de Pruebas Unitarias del Motor Textil

Implementada en `src/PruebasReglasNegocioTextil.java`, esta suite verifica de forma exhaustiva las 14 reglas de negocio criticas del dominio textil sin dependencias de base de datos ni red:

| ID | Nombre de la Prueba | Descripcion del Escenario Evaluado | Entrada de Datos | Resultado Esperado | Estado |
| :---: | :--- | :--- | :--- | :--- | :---: |
| **P-01** | Creacion valida de rollo | Instanciacion de rollo con codigo, lote y metraje inicial. | Codigo: `ROL-001`, Lote: `LOT-2026-A`, Metros: `50.0` | Rollo en estado `DISPONIBLE`, metraje actual `50.0`. | **EXITO** |
| **P-02** | Rechazo de metraje no positivo | Validacion de frontera para metrajes negativos o iguales a cero. | Metros: `-5.0` y `0.0` | `IllegalArgumentException` lanzada y capturada. | **EXITO** |
| **P-03** | Deteccion de merma al cortar | Corte que deja un remanente menor a 1.0 metro lineal. | Rollo de `5.0m`, corte de `4.2m` (sobran `0.8m`) | Remanente = `0.8m`, bandera `esMerma = true`. | **EXITO** |
| **P-04** | Clasificacion a `RETAZO_LIQUIDADO` | Reclasificacion sistematica de estado tras generar merma. | Rollo de `5.0m`, corte de `4.2m` | `rollo.getEstadoRollo() == RETAZO_LIQUIDADO`. | **EXITO** |
| **P-05** | Bloqueo por metraje insuficiente | Intento de cortar mas metros de los existentes en el rollo. | Rollo de `10.0m`, intento de corte de `12.5m` | `IllegalStateException` lanzada, metraje inalterado. | **EXITO** |
| **P-06** | Agotamiento exacto de rollo | Corte que consume con precision la totalidad del rollo. | Rollo de `15.0m`, corte de `15.0m` (sobran `0.0m`) | Remanente = `0.0m`, `rollo.getEstadoRollo() == AGOTADO`. | **EXITO** |
| **P-07** | Validacion de continuidad | Solicitud de metraje continuo mayor al rollo maximo disponible. | Rollos de `8.0m` y `6.0m`, solicitud de `10.0m` | Retorna `false` (impide fragmentar el pedido). | **EXITO** |
| **P-08** | Consistencia de lote de tintoreria | Rechazo de abastecimiento cruzado entre lotes quimicos distintos. | Rollo Lote A (`20m`), pedido que exige Lote B | Retorna `false` (protege la homogeneidad cromatica). | **EXITO** |
| **P-09** | Tarifa Detal (< 15 metros) | Calculo de precio sin descuento para metrajes inferiores a 15m. | Precio base: `$25,000 COP`, Metros: `5.0m` | Total: `$125,000 COP` (Descuento: `$0`). | **EXITO** |
| **P-10** | Tarifa Mayorista (>= 15 metros) | Aplicacion automatica del 12% de descuento por volumen. | Precio base: `$20,000 COP`, Metros: `20.0m` | Total: `$352,000 COP` (Descuento: `$48,000 COP`). | **EXITO** |
| **P-11** | Tarifa Rollo Cerrado (>= 50 metros)| Aplicacion automatica del 25% de descuento por rollo completo. | Precio base: `$30,000 COP`, Metros: `50.0m` | Total: `$1,125,000 COP` (Descuento: `$375,000 COP`). | **EXITO** |
| **P-12** | Descuento obligatorio de retazos | Aplicacion estricta del 40% de descuento en retazos liquidados. | Precio base: `$40,000 COP`, Retazo: `0.75m` | Total: `$18,000 COP` (Valor regular: `$30,000 COP`). | **EXITO** |
| **P-13** | Seleccion de rollo optimo (Best Fit)| Eleccion del rollo que minimiza la merma residual. | Rollos de `50m`, `12m` y `6m`; pedido de `5.5m` | Selecciona rollo de `6m` (sobrante optimo de `0.5m`). | **EXITO** |
| **P-14** | Margen tecnico de confeccion (+10%)| Calculo de calculadora con holgura para encogimiento y dobladillo. | Metraje patron: `2.0m`, factor: `1.10` | Metros recomendados: `2.20m`. | **EXITO** |

---

## 5. Matriz de la Suite de Pruebas de Seguridad y Casos Extremos

Implementada en `src/PruebasSeguridadIntegridad.java`, esta suite verifica 12 escenarios criticos de ciberseguridad, validacion de entradas maliciosas y tolerancia a fallos:

| ID | Categoria de Prueba | Escenario y Vector Evaluado | Comportamiento del Sistema y Mitigacion | Estado |
| :---: | :--- | :--- | :--- | :---: |
| **S-01** | Criptografia | Hashing SHA-256 de contrasenas con caracteres UTF-8 y tildes. | Genera hash hexadecimal determinista de 64 caracteres; no almacena texto plano. | **EXITO** |
| **S-02** | Sanitizacion Web | Prevencion de Inyeccion SQL en busquedas por categoria. | Rechaza o parametriza caracteres peligrosos (`' OR '1'='1`). | **EXITO** |
| **S-03** | Sanitizacion Web | Neutralizacion de Cross-Site Scripting (XSS) en resenas. | Limpieza de etiquetas `<script>` y payloads de manipulacion DOM. | **EXITO** |
| **S-04** | Validacion de Entrada | Bloqueo de precios negativos o con multiplicacion flotante erronea. | Valida `precio > 0` usando `BigDecimal` para evitar anomalías monetarias. | **EXITO** |
| **S-05** | Control de Acceso (RBAC)| Intento de creacion de productos por usuarios con rol `cliente`. | Interceptor HTTP retorna codigo `403 Forbidden`. | **EXITO** |
| **S-06** | Integridad Transaccional| Deteccion y bloqueo de cupones con fecha de expiracion caducada. | Valida `LocalDateTime.now().isBefore(fechaExpiracion)`. Retorna error. | **EXITO** |
| **S-07** | Integridad Transaccional| Validacion de compra minima exigida por cupones comerciales. | Si subtotal < minimo_compra, rechaza aplicacion del beneficio. | **EXITO** |
| **S-08** | Resiliencia de Red | Manejo defensivo de caidas transitorias en pool de base de datos. | HikariCP captura timeout y expone probe `/health/ready` como 503. | **EXITO** |
| **S-09** | Concurrencia | Proteccion ante condiciones de carrera en reserva de metraje. | Bloqueo pesimista `SELECT ... FOR UPDATE` impide saldo negativo. | **EXITO** |
| **S-10** | Manejo de Excepciones | Prevencion de NullPointerException ante payloads JSON incompletos. | Deserializador Gson valida nulabilidad y asigna valores predeterminados. | **EXITO** |
| **S-11** | Proteccion de Privacidad | Verificacion de cero almacenamiento de PAN y CVV (PCI-DSS). | Esquema de base de datos carece de columnas para datos bancarios. | **EXITO** |
| **S-12** | Auditoria y Trazabilidad| Inmutabilidad de los registros de auditoria administrativa. | Acciones criticas generan registros con ID de usuario y marca de tiempo. | **EXITO** |

---

## 6. Registro de Ejecucion y Certificacion de Resultados

A continuacion se presenta la transcripcion de la ejecucion directa de los dos bancos de pruebas ejecutados en el entorno de desarrollo:

```text
================================================================================
REPORTE DE EJECUCION DE PRUEBAS DE CALIDAD DE SOFTWARE
Proyecto: D&D Textil - Tienda Digital de Telas
Fecha de Certificacion: 2026-10-14
Compilador: OpenJDK 64-Bit Server VM (build 17.0.12+7)
================================================================================

[SUITE 1: PruebasReglasNegocioTextil]
Ejecutando verificacion de reglas de negocio textiles...
  [OK] Regla 1: Rollo creado con estado DISPONIBLE y trazabilidad de lote.
  [OK] Regla 2: Excepcion controlada ante solicitudes de metraje <= 0.
  [OK] Regla 3: Deteccion de merma residual (sobrante < 1.00m).
  [OK] Regla 4: Transicion automatica a RETAZO_LIQUIDADO verificada.
  [OK] Regla 5: Proteccion contra sobrecorte (stock insuficiente).
  [OK] Regla 6: Consumo total de rollo resulta en estado AGOTADO.
  [OK] Regla 7: Validacion de continuidad de tela en un solo rollo fisico.
  [OK] Regla 8: Garantia de homogeneidad cromatica por lote de tintoreria.
  [OK] Regla 9: Escala Detal (sin descuento) liquidada correctamente.
  [OK] Regla 10: Escala Mayorista (12% desc.) liquidada correctamente.
  [OK] Regla 11: Escala Rollo Cerrado (25% desc.) liquidada correctamente.
  [OK] Regla 12: Descuento del 40% aplicado obligatoriamente a retazos.
  [OK] Regla 13: Algoritmo Best-Fit asigna rollo optimo con menor desperdicio.
  [OK] Regla 14: Calculadora con margen del 10% validada con precision decimal.
Pruebas de Negocio: 14/14 APROBADAS (100.0%)

[SUITE 2: PruebasSeguridadIntegridad]
Ejecutando verificacion de seguridad, integridad y tolerancia a fallos...
  [OK] Seguridad 1: Hashing SHA-256 en UTF-8 consistente y unidireccional.
  [OK] Seguridad 2: Sanitizacion contra inyeccion SQL en consultas de catalogo.
  [OK] Seguridad 3: Sanitizacion contra scripts maliciosos XSS en resenas.
  [OK] Seguridad 4: Rechazo de precios negativos y redondeos de coma flotante.
  [OK] Seguridad 5: Control de acceso RBAC bloquea clientes en rutas de vendedor.
  [OK] Seguridad 6: Bloqueo de cupones expirados segun reloj del sistema.
  [OK] Seguridad 7: Validacion estricta de monto minimo de compra para cupones.
  [OK] Seguridad 8: Degradacion elegante ante indisponibilidad de base de datos.
  [OK] Seguridad 9: Prevencion de condiciones de carrera en inventario de rollos.
  [OK] Seguridad 10: Nulabilidad defensiva en deserializacion de objetos JSON.
  [OK] Seguridad 11: Conformidad PCI-DSS v4.0 (ausencia de columnas PAN/CVV).
  [OK] Seguridad 12: Registro inmutable de eventos de auditoria y mermas.
Pruebas de Seguridad: 12/12 APROBADAS (100.0%)

================================================================================
RESUMEN GENERAL:
Total de Pruebas Unitarias y de Seguridad: 26
Pruebas Aprobadas: 26 (100%)
Pruebas Fallidas: 0 (0%)
Dictamen Tecnico: APTO PARA SUSTENTACION Y DESPLIEGUE EN PRODUCCION
================================================================================
```
