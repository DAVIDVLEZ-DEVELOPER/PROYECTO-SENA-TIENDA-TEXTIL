package infraestructura.configuracion;

/**
 * Configuración centralizada de la pasarela de pagos Wompi (Bancolombia).
 *
 * Las credenciales se leen a través de {@link ConfiguracionEntorno}:
 *   1. Variables de entorno del sistema (Docker / producción).
 *   2. Archivo ".env" en el directorio raíz o del backend (desarrollo local).
 *
 * Credenciales requeridas (Panel Wompi -> Desarrolladores):
 *   - WOMPI_PUBLIC_KEY        Llave pública (pub_test_... / pub_prod_...).
 *   - WOMPI_PRIVATE_KEY       Llave privada (prv_test_... / prv_prod_...). SOLO backend.
 *   - WOMPI_INTEGRITY_SECRET  Secreto de integridad (test_integrity_... / prod_integrity_...). SOLO backend.
 *   - WOMPI_EVENTS_SECRET     Secreto de eventos (test_events_... / prod_events_...). SOLO backend.
 *   - WOMPI_ENV               "sandbox" (por defecto) o "production".
 */
public final class ConfiguracionWompi {

    public static final String MONEDA = "COP";

    private ConfiguracionWompi() {}

    public static String llavePublica()       { return ConfiguracionEntorno.obtener("WOMPI_PUBLIC_KEY"); }
    public static String llavePrivada()       { return ConfiguracionEntorno.obtener("WOMPI_PRIVATE_KEY"); }
    public static String secretoIntegridad()  { return ConfiguracionEntorno.obtener("WOMPI_INTEGRITY_SECRET"); }
    public static String secretoEventos()     { return ConfiguracionEntorno.obtener("WOMPI_EVENTS_SECRET"); }

    public static boolean esProduccion() {
        String entorno = ConfiguracionEntorno.obtener("WOMPI_ENV", "sandbox");
        return "production".equalsIgnoreCase(entorno);
    }

    /** URL base de la API REST de Wompi según el entorno. */
    public static String urlApi() {
        return esProduccion() ? "https://production.wompi.co/v1" : "https://sandbox.wompi.co/v1";
    }

    /** Indica si las credenciales mínimas para cobrar están configuradas. */
    public static boolean estaConfigurada() {
        return noVacio(llavePublica()) && noVacio(secretoIntegridad()) && noVacio(secretoEventos());
    }

    private static boolean noVacio(String s) {
        return s != null && !s.isBlank();
    }
}
