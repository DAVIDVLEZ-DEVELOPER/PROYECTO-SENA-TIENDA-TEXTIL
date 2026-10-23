package aplicacion.servicios;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import infraestructura.configuracion.ConfiguracionWompi;

/**
 * Servicio criptográfico para la pasarela de pagos Wompi (Bancolombia).
 *
 * Implementa:
 *  1. Cálculo de firma de integridad para el Checkout Widget (SHA-256).
 *     Fórmula Wompi: SHA256(referencia + montoEnCentavos + moneda + integridadSecret)
 *
 *  2. Verificación de firma criptográfica de Webhooks de Wompi.
 *     Fórmula Wompi: SHA256(concat(valores de propiedades) + timestamp + eventsSecret)
 */
public class ServicioCriptoWompi {

    /**
     * Genera el hash de integridad SHA-256 requerido por Wompi para iniciar el Widget de pago.
     *
     * @param referencia    Referencia única de la orden (ej. DD-20261021-1234).
     * @param montoCentavos Monto total en centavos (ej. 5000000 para $50.000 COP).
     * @param moneda        Código de moneda ISO, típicamente "COP".
     * @return Hash SHA-256 en formato hexadecimal en minúsculas.
     */
    public static String generarFirmaIntegridad(String referencia, long montoCentavos, String moneda) {
        String secreto = ConfiguracionWompi.secretoIntegridad();
        if (secreto == null || secreto.isBlank()) {
            System.err.println("[WOMPI ALERTA] WOMPI_INTEGRITY_SECRET no está configurado. Usando modo de desarrollo.");
            secreto = "test_integrity_dummy";
        }
        String cadena = referencia + montoCentavos + moneda + secreto;
        return sha256(cadena);
    }

    /**
     * Valida la firma del evento recibido en el Webhook de Wompi.
     *
     * Wompi envía una estructura como:
     * {
     *   "event": "transaction.updated",
     *   "data": { "transaction": { ... } },
     *   "signature": {
     *     "properties": ["transaction.id", "transaction.status", "transaction.amount_in_cents"],
     *     "checksum": "abc123..."
     *   },
     *   "timestamp": 1699999999
     * }
     *
     * @param jsonWebhook Objeto JSON raíz recibido de Wompi.
     * @return true si el checksum coincide; false si es apócrifo o inválido.
     */
    public static boolean validarFirmaWebhook(JsonObject jsonWebhook) {
        if (jsonWebhook == null || !jsonWebhook.has("signature") || !jsonWebhook.has("timestamp")) {
            return false;
        }

        JsonObject signatureObj = jsonWebhook.getAsJsonObject("signature");
        if (!signatureObj.has("properties") || !signatureObj.has("checksum")) {
            return false;
        }

        String checksumRecibido = signatureObj.get("checksum").getAsString();
        long timestamp = jsonWebhook.get("timestamp").getAsLong();
        JsonArray properties = signatureObj.getAsJsonArray("properties");

        JsonObject dataObj = jsonWebhook.has("data") && jsonWebhook.get("data").isJsonObject()
                ? jsonWebhook.getAsJsonObject("data")
                : new JsonObject();

        StringBuilder cadenaParaHash = new StringBuilder();
        for (JsonElement propElem : properties) {
            String rutaPropiedad = propElem.getAsString();
            String valor = extraerValorPorRuta(dataObj, rutaPropiedad);
            if (valor != null) {
                cadenaParaHash.append(valor);
            }
        }

        cadenaParaHash.append(timestamp);

        String secretoEventos = ConfiguracionWompi.secretoEventos();
        if (secretoEventos == null || secretoEventos.isBlank()) {
            System.err.println("[WOMPI ALERTA] WOMPI_EVENTS_SECRET no está configurado.");
            secretoEventos = "test_events_dummy";
        }
        cadenaParaHash.append(secretoEventos);

        String checksumCalculado = sha256(cadenaParaHash.toString());
        return MessageDigest.isEqual(
                checksumCalculado.toLowerCase().getBytes(StandardCharsets.UTF_8),
                checksumRecibido.toLowerCase().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Extrae recursivamente el valor de una ruta con notación de punto (ej: "transaction.id").
     */
    private static String extraerValorPorRuta(JsonObject raiz, String ruta) {
        if (raiz == null || ruta == null || ruta.isBlank()) return "";
        String[] partes = ruta.split("\\.");
        JsonObject actual = raiz;
        for (int i = 0; i < partes.length - 1; i++) {
            if (actual != null && actual.has(partes[i]) && actual.get(partes[i]).isJsonObject()) {
                actual = actual.getAsJsonObject(partes[i]);
            } else {
                return "";
            }
        }
        String claveFinal = partes[partes.length - 1];
        if (actual != null && actual.has(claveFinal) && !actual.get(claveFinal).isJsonNull()) {
            JsonElement el = actual.get(claveFinal);
            return el.isJsonPrimitive() ? el.getAsString() : el.toString();
        }
        return "";
    }

    /**
     * Calcula el digest SHA-256 de una cadena UTF-8 y lo retorna como string hex en minúsculas.
     */
    public static String sha256(String entrada) {
        if (entrada == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(entrada.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(64);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo SHA-256 no disponible en la JVM", e);
        }
    }
}
