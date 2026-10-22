package infraestructura.configuracion;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuración centralizada de la pasarela de pagos Wompi (Bancolombia).
 *
 * Las credenciales se leen, en orden de prioridad:
 *   1. Variables de entorno del sistema (Docker / producción).
 *   2. Archivo ".env" en el directorio de trabajo del backend (desarrollo local).
 *
 * Credenciales requeridas (Panel Wompi → Desarrolladores):
 *   - WOMPI_PUBLIC_KEY        Llave pública (pub_test_... / pub_prod_...). Se envía al navegador.
 *   - WOMPI_PRIVATE_KEY       Llave privada (prv_test_... / prv_prod_...). SOLO backend.
 *   - WOMPI_INTEGRITY_SECRET  Secreto de integridad (test_integrity_... / prod_integrity_...). SOLO backend.
 *   - WOMPI_EVENTS_SECRET     Secreto de eventos (test_events_... / prod_events_...). SOLO backend.
 *   - WOMPI_ENV               "sandbox" (por defecto) o "production".
 */
public final class ConfiguracionWompi {

    private static final Map<String, String> VALORES_ARCHIVO = cargarArchivoEnv();

    public static final String MONEDA = "COP";

    private ConfiguracionWompi() {}

    public static String llavePublica()       { return obtener("WOMPI_PUBLIC_KEY"); }
    public static String llavePrivada()       { return obtener("WOMPI_PRIVATE_KEY"); }
    public static String secretoIntegridad()  { return obtener("WOMPI_INTEGRITY_SECRET"); }
    public static String secretoEventos()     { return obtener("WOMPI_EVENTS_SECRET"); }

    public static boolean esProduccion() {
        String entorno = obtener("WOMPI_ENV");
        return entorno != null && entorno.trim().equalsIgnoreCase("production");
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

    private static String obtener(String clave) {
        String valor = System.getenv(clave);
        if (valor != null && !valor.isBlank()) return valor.trim();
        valor = VALORES_ARCHIVO.get(clave);
        return valor != null ? valor.trim() : null;
    }

    /** Parser mínimo de archivos .env (CLAVE=valor, ignora comentarios y líneas vacías). */
    private static Map<String, String> cargarArchivoEnv() {
        Map<String, String> valores = new HashMap<>();
        Path[] posiblesRutas = {
            Paths.get(".env"),
            Paths.get("../.env"),
            Paths.get("../../.env"),
            Paths.get("../../../.env")
        };
        Path ruta = null;
        for (Path p : posiblesRutas) {
            if (Files.exists(p)) {
                ruta = p;
                break;
            }
        }
        if (ruta == null) return valores;
        try {
            List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
            for (String linea : lineas) {
                String l = linea.trim();
                if (l.isEmpty() || l.startsWith("#") || !l.contains("=")) continue;
                int idx = l.indexOf('=');
                String clave = l.substring(0, idx).trim();
                String valor = l.substring(idx + 1).trim();
                if (valor.length() >= 2 && ((valor.startsWith("\"") && valor.endsWith("\""))
                        || (valor.startsWith("'") && valor.endsWith("'")))) {
                    valor = valor.substring(1, valor.length() - 1);
                }
                valores.put(clave, valor);
            }
        } catch (IOException e) {
            System.err.println("[WOMPI] No se pudo leer el archivo .env: " + e.getMessage());
        }
        return valores;
    }
}
