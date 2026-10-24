package dominio.modelos;

/**
 * Datos relevantes de una transacción de Wompi, ya sea recibida por webhook
 * o consultada directamente a la API de Wompi.
 *
 * @param id               Identificador de la transacción en Wompi.
 * @param referencia       Referencia de pago generada por nuestro backend.
 * @param estado           PENDING, APPROVED, DECLINED, VOIDED o ERROR.
 * @param montoCentavos    Monto cobrado en centavos.
 * @param moneda           Moneda (COP).
 * @param metodoPago       Tipo de método de pago (CARD, NEQUI, PSE, BANCOLOMBIA_TRANSFER, ...).
 */
public record TransaccionWompi(
        String id,
        String referencia,
        String estado,
        long montoCentavos,
        String moneda,
        String metodoPago) {
}
