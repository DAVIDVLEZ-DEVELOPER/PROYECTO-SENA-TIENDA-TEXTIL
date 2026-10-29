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
 * Gestor centralizado de variables de configuración y entorno para D&D Textil.
 * Prioridad de resolución:
 * 1. Variables de entorno del sistema operativo / contenedor (Docker / Kubernetes / Producción).
 * 2. Archivo ".env" local ubicado en la raíz del proyecto o directorios padre (Desarrollo local).
 * 3. Valor por defecto parametrizado.
 */
public final class ConfiguracionEntorno {

    private static final Map<String, String> VALORES_ARCHIVO = cargarArchivoEnv();

    private ConfiguracionEntorno() {}

    /**
     * Devuelve el primer valor no vacío entre las claves indicadas, o null si ninguna existe.
     *
     * @param claves Claves a consultar en orden de prioridad.
     * @return Valor encontrado recortado, o null.
     */
    public static String obtener(String... claves) {
        for (String clave : claves) {
            String valor = System.getenv(clave);
            if (valor == null || valor.isBlank()) {
                valor = VALORES_ARCHIVO.get(clave);
            }
            if (valor != null && !valor.isBlank()) {
                return valor.trim();
            }
        }
        return null;
    }

    /**
     * Devuelve el valor de la clave especificada o el valor por defecto si no está definida.
     *
     * @param clave Clave de entorno.
     * @param valorPorDefecto Valor predeterminado de respaldo.
     * @return Valor configurado o valorPorDefecto.
     */
    public static String obtener(String clave, String valorPorDefecto) {
        String val = obtener(clave);
        return (val != null && !val.isBlank()) ? val : valorPorDefecto;
    }

    /**
     * Devuelve el valor numérico entero asociado a una clave, o el valor por defecto.
     *
     * @param clave Clave de entorno.
     * @param valorPorDefecto Entero de respaldo ante ausencia o error de parseo.
     * @return Entero parseado o valorPorDefecto.
     */
    public static int obtenerEntero(String clave, int valorPorDefecto) {
        String val = obtener(clave);
        if (val == null || val.isBlank()) {
            return valorPorDefecto;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return valorPorDefecto;
        }
    }

    /**
     * Devuelve el valor booleano asociado a una clave, o el valor por defecto.
     *
     * @param clave Clave de entorno.
     * @param valorPorDefecto Booleano de respaldo ante ausencia.
     * @return Booleano parseado o valorPorDefecto.
     */
    public static boolean obtenerBooleano(String clave, boolean valorPorDefecto) {
        String val = obtener(clave);
        if (val == null || val.isBlank()) {
            return valorPorDefecto;
        }
        return Boolean.parseBoolean(val.trim());
    }

    /**
     * Parser robusto para archivos .env que ignora comentarios y comillas envolventes.
     */
    private static Map<String, String> cargarArchivoEnv() {
        Map<String, String> valores = new HashMap<>();
        Path[] posiblesRutas = {
            Paths.get(".env"),
            Paths.get("../.env"),
            Paths.get("../../.env"),
            Paths.get("../../../.env")
        };

        for (Path ruta : posiblesRutas) {
            if (!Files.exists(ruta)) {
                continue;
            }
            try {
                List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
                for (String linea : lineas) {
                    String l = linea.trim();
                    if (l.isEmpty() || l.startsWith("#") || !l.contains("=")) {
                        continue;
                    }
                    int idx = l.indexOf('=');
                    String clave = l.substring(0, idx).trim();
                    String valor = l.substring(idx + 1).trim();
                    if (valor.length() >= 2 && ((valor.startsWith("\"") && valor.endsWith("\""))
                            || (valor.startsWith("'") && valor.endsWith("'")))) {
                        valor = valor.substring(1, valor.length() - 1);
                    }
                    valores.put(clave, valor);
                }
                break;
            } catch (IOException e) {
                System.err.println("[CONFIG] Advertencia: No se pudo leer el archivo .env en " + ruta + ": " + e.getMessage());
            }
        }
        return valores;
    }
}
