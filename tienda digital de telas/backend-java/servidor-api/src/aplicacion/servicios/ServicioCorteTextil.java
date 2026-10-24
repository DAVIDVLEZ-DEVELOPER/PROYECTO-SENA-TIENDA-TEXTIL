package aplicacion.servicios;

import dominio.modelos.RolloTextil;
import dominio.modelos.EventoMerma;
import java.util.List;
import java.util.Objects;

/**
 * Servicio de Dominio para el Motor de Cálculo Textil, Continuidad de Corte y Tarifas Escalonadas.
 * Implementa las reglas de negocio clave requeridas por el SENA:
 * 1. Validación de metraje continuo vs rollos fragmentados.
 * 2. Algoritmo de mermas y reclasificación de retazos (< 1.0 m).
 * 3. Cálculo de precios escalonados (detal, mayorista, rollo cerrado).
 */
public class ServicioCorteTextil {

    /** Umbral mínimo para considerar un corte continuo como rollo comercializable (1.0 metro) */
    public static final double UMBRAL_RETAZO_METROS = 1.0;

    /** Descuento automático aplicado al retazo sub-metro liquidado (40%) */
    public static final double DESCUENTO_RETAZO_LIQUIDADO = 0.40;

    /** Umbral para tarifa mayorista en metros (>= 15 metros) */
    public static final double UMBRAL_MAYORISTA_METROS = 15.0;

    /** Umbral para tarifa de rollo cerrado en metros (>= 50 metros) */
    public static final double UMBRAL_ROLLO_CERRADO_METROS = 50.0;

    /** Porcentaje de descuento mayorista (15%) */
    public static final double DESCUENTO_MAYORISTA = 0.15;

    /** Porcentaje de descuento rollo cerrado (25%) */
    public static final double DESCUENTO_ROLLO_CERRADO = 0.25;

    // =========================================================================
    // 1. REGLA DE NEGOCIO: VALIDACIÓN DE METRAJE CONTINUO VS ROLLOS FRAGMENTADOS
    // =========================================================================

    public static class ResultadoValidacionCorte {
        private final boolean esValido;
        private final String codigo;
        private final String mensaje;
        private final RolloTextil rolloAsignado;
        private final double metrajeTotalLote;

        public ResultadoValidacionCorte(boolean esValido, String codigo, String mensaje, RolloTextil rolloAsignado, double metrajeTotalLote) {
            this.esValido = esValido;
            this.codigo = codigo;
            this.mensaje = mensaje;
            this.rolloAsignado = rolloAsignado;
            this.metrajeTotalLote = metrajeTotalLote;
        }

        public boolean esValido() { return esValido; }
        public String getCodigo() { return codigo; }
        public String getMensaje() { return mensaje; }
        public RolloTextil getRolloAsignado() { return rolloAsignado; }
        public double getMetrajeTotalLote() { return metrajeTotalLote; }
    }

    /**
     * Valida si existe al menos un rollo continuo en el lote especificado con metraje suficiente.
     * Si la suma total de metros en el lote alcanza pero está fragmentada entre varios rollos,
     * rechaza la reserva automática para evitar empates o mezclas cromáticas no autorizadas.
     */
    public ResultadoValidacionCorte validarMetrajeContinuo(List<RolloTextil> rollos, double metrajeSolicitado, String loteTintoreria) {
        if (metrajeSolicitado <= 0) {
            throw new IllegalArgumentException("El metraje solicitado debe ser mayor a cero.");
        }

        if (rollos == null || rollos.isEmpty()) {
            return new ResultadoValidacionCorte(false, "SIN_STOCK", "No existen rollos registrados para la tela solicitada.", null, 0.0);
        }

        double metrajeTotalLote = 0.0;
        RolloTextil rolloCandidatoContinuo = null;

        for (RolloTextil rollo : rollos) {
            // Filtrar solo rollos disponibles y del mismo lote de tintorería (si se especificó)
            if (!"disponible".equalsIgnoreCase(rollo.getEstado())) {
                continue;
            }
            if (loteTintoreria != null && !loteTintoreria.isBlank() && !Objects.equals(rollo.getLoteTintoreria(), loteTintoreria)) {
                continue;
            }

            metrajeTotalLote += rollo.getMetrajeRemanente();

            // Buscar el primer rollo que pueda suplir el pedido de forma 100% continua
            if (rolloCandidatoContinuo == null && rollo.getMetrajeRemanente() >= metrajeSolicitado) {
                rolloCandidatoContinuo = rollo;
            }
        }

        // Caso 1: Se encontró un rollo con metraje continuo íntegro
        if (rolloCandidatoContinuo != null) {
            return new ResultadoValidacionCorte(
                true,
                "CORTE_CONTINUO_OK",
                "Metraje continuo disponible en rollo " + rolloCandidatoContinuo.getCodigoRollo(),
                rolloCandidatoContinuo,
                metrajeTotalLote
            );
        }

        // Caso 2: El metraje total sumado en el lote alcanza, pero ningún rollo individual tiene los metros continuos
        if (metrajeTotalLote >= metrajeSolicitado) {
            return new ResultadoValidacionCorte(
                false,
                "FRAGMENTED_STOCK",
                "El stock total del lote (" + metrajeTotalLote + " m) es suficiente pero está fragmentado en rollos menores. Requiere división o autorización de empates.",
                null,
                metrajeTotalLote
            );
        }

        // Caso 3: No alcanza ni siquiera sumando todos los rollos
        return new ResultadoValidacionCorte(
            false,
            "INSUFFICIENT_STOCK",
            "Stock insuficiente en el lote. Solicitados: " + metrajeSolicitado + " m, disponibles: " + metrajeTotalLote + " m.",
            null,
            metrajeTotalLote
        );
    }

    // =========================================================================
    // 2. REGLA DE NEGOCIO: PROCESAMIENTO DE CORTE Y RECLASIFICACIÓN DE MERMAS
    // =========================================================================

    public static class ResultadoProcesamientoCorte {
        private final double metrajeCortado;
        private final double metrajeRemanente;
        private final String estadoFinalRollo;
        private final boolean esRetazoLiquidado;
        private final EventoMerma eventoMerma;
        private final double precioPorMetroRetazo;

        public ResultadoProcesamientoCorte(double metrajeCortado, double metrajeRemanente, String estadoFinalRollo, boolean esRetazoLiquidado, EventoMerma eventoMerma, double precioPorMetroRetazo) {
            this.metrajeCortado = metrajeCortado;
            this.metrajeRemanente = metrajeRemanente;
            this.estadoFinalRollo = estadoFinalRollo;
            this.esRetazoLiquidado = esRetazoLiquidado;
            this.eventoMerma = eventoMerma;
            this.precioPorMetroRetazo = precioPorMetroRetazo;
        }

        public double getMetrajeCortado() { return metrajeCortado; }
        public double getMetrajeRemanente() { return metrajeRemanente; }
        public String getEstadoFinalRollo() { return estadoFinalRollo; }
        public boolean esRetazoLiquidado() { return esRetazoLiquidado; }
        public EventoMerma getEventoMerma() { return eventoMerma; }
        public double getPrecioPorMetroRetazo() { return precioPorMetroRetazo; }
    }

    /**
     * Aplica el corte físico a un rollo, calcula el remanente con precisión de 2 decimales
     * y aplica la reclasificación automática a merma o retazo liquidado si el remanente < 1.0 metro.
     */
    public ResultadoProcesamientoCorte procesarCorteYRemanente(RolloTextil rollo, double metrajeACortar, double precioBasePorMetro) {
        if (rollo == null) {
            throw new IllegalArgumentException("El rollo no puede ser nulo.");
        }
        if (metrajeACortar <= 0) {
            throw new IllegalArgumentException("El metraje a cortar debe ser mayor a cero.");
        }
        if (metrajeACortar > rollo.getMetrajeRemanente()) {
            throw new IllegalStateException("Sobrecorte no permitido: solicitado " + metrajeACortar + " m, disponible " + rollo.getMetrajeRemanente() + " m.");
        }

        double remanente = Math.round((rollo.getMetrajeRemanente() - metrajeACortar) * 100.0) / 100.0;
        rollo.setMetrajeRemanente(remanente);

        boolean esRetazo = false;
        EventoMerma eventoMerma = null;
        double precioRetazo = 0.0;
        String nuevoEstado;

        if (remanente == 0.0) {
            nuevoEstado = "agotado";
        } else if (remanente < UMBRAL_RETAZO_METROS) {
            // Remanente inferior a 1 metro: reclasificación automática
            nuevoEstado = "retazo_liquidado";
            esRetazo = true;
            precioRetazo = Math.round(precioBasePorMetro * (1.0 - DESCUENTO_RETAZO_LIQUIDADO) * 100.0) / 100.0;

            eventoMerma = new EventoMerma();
            eventoMerma.setIdLote(rollo.getCodigoRollo());
            eventoMerma.setMetros(remanente);
            eventoMerma.setMotivo("remanente_sub_metro");
            eventoMerma.setDescripcion("Remanente de " + remanente + " m reclasificado automáticamente a retazo con 40% de descuento");
        } else {
            nuevoEstado = "disponible";
        }

        rollo.setEstado(nuevoEstado);

        return new ResultadoProcesamientoCorte(metrajeACortar, remanente, nuevoEstado, esRetazo, eventoMerma, precioRetazo);
    }

    // =========================================================================
    // 3. REGLA DE NEGOCIO: CÁLCULO DE PRECIOS ESCALONADOS
    // =========================================================================

    public static class TarifaCalculada {
        private final String tipoTarifa; // "DETAL", "MAYORISTA", "ROLLO_CERRADO"
        private final double metrajeTotal;
        private final double precioBasePorMetro;
        private final double porcentajeDescuento;
        private final double precioUnitarioFinal;
        private final double subtotalBruto;
        private final double montoDescuento;
        private final double totalNeto;

        public TarifaCalculada(String tipoTarifa, double metrajeTotal, double precioBasePorMetro, double porcentajeDescuento, double precioUnitarioFinal, double subtotalBruto, double montoDescuento, double totalNeto) {
            this.tipoTarifa = tipoTarifa;
            this.metrajeTotal = metrajeTotal;
            this.precioBasePorMetro = precioBasePorMetro;
            this.porcentajeDescuento = porcentajeDescuento;
            this.precioUnitarioFinal = precioUnitarioFinal;
            this.subtotalBruto = subtotalBruto;
            this.montoDescuento = montoDescuento;
            this.totalNeto = totalNeto;
        }

        public String getTipoTarifa() { return tipoTarifa; }
        public double getMetrajeTotal() { return metrajeTotal; }
        public double getPrecioBasePorMetro() { return precioBasePorMetro; }
        public double getPorcentajeDescuento() { return porcentajeDescuento; }
        public double getPrecioUnitarioFinal() { return precioUnitarioFinal; }
        public double getSubtotalBruto() { return subtotalBruto; }
        public double getMontoDescuento() { return montoDescuento; }
        public double getTotalNeto() { return totalNeto; }
    }

    /**
     * Calcula la tarifa escalonada según el volumen de compra:
     * - Menor a 15 metros: Detal (0% descuento)
     * - Entre 15 y 49.99 metros: Mayorista (15% descuento)
     * - 50 o más metros: Rollo Cerrado (25% descuento)
     */
    public TarifaCalculada calcularPrecioEscalonado(double precioBasePorMetro, double metrajeTotal) {
        if (precioBasePorMetro <= 0) {
            throw new IllegalArgumentException("El precio base por metro debe ser mayor a cero.");
        }
        if (metrajeTotal <= 0) {
            throw new IllegalArgumentException("El metraje total debe ser mayor a cero.");
        }

        String tipoTarifa;
        double porcentajeDescuento;

        if (metrajeTotal >= UMBRAL_ROLLO_CERRADO_METROS) {
            tipoTarifa = "ROLLO_CERRADO";
            porcentajeDescuento = DESCUENTO_ROLLO_CERRADO;
        } else if (metrajeTotal >= UMBRAL_MAYORISTA_METROS) {
            tipoTarifa = "MAYORISTA";
            porcentajeDescuento = DESCUENTO_MAYORISTA;
        } else {
            tipoTarifa = "DETAL";
            porcentajeDescuento = 0.0;
        }

        double precioUnitario = Math.round(precioBasePorMetro * (1.0 - porcentajeDescuento) * 100.0) / 100.0;
        double subtotalBruto = Math.round(precioBasePorMetro * metrajeTotal * 100.0) / 100.0;
        double totalNeto = Math.round(precioUnitario * metrajeTotal * 100.0) / 100.0;
        double montoDescuento = Math.round((subtotalBruto - totalNeto) * 100.0) / 100.0;

        return new TarifaCalculada(
            tipoTarifa,
            metrajeTotal,
            precioBasePorMetro,
            porcentajeDescuento,
            precioUnitario,
            subtotalBruto,
            montoDescuento,
            totalNeto
        );
    }
}
