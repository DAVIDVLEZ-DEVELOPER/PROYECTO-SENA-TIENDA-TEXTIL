package infraestructura.api.manejadores;

import com.sun.net.httpserver.HttpExchange;
import infraestructura.configuracion.Conexion;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.time.Instant;

/**
 * Controlador de Endpoints de Salud (Health Checks) para Kubernetes, Docker y Monitoreo SENA.
 * Expone:
 * - /health/live  (Liveness Probe: Proceso activo y receptivo)
 * - /health/ready (Readiness Probe: Conexión a Base de Datos PostgreSQL operativa)
 * - /api/health   (Reporte consolidado con métricas de JVM y persistencia)
 */
public class ManejadorSalud extends ManejadorBase {

    @Override
    protected void procesarPeticion(HttpExchange intercambio) throws Exception {
        if (!"GET".equalsIgnoreCase(intercambio.getRequestMethod())) {
            enviarRespuestaJson(intercambio, 405, "{\"error\":\"Método no permitido. Use GET.\"}");
            return;
        }

        String ruta = intercambio.getRequestURI().getPath();
        long tiempoUptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        long memoriaTotalMb = Runtime.getRuntime().totalMemory() / (1024 * 1024);
        long memoriaLibreMb = Runtime.getRuntime().freeMemory() / (1024 * 1024);
        long memoriaUsadaMb = memoriaTotalMb - memoriaLibreMb;

        // --- LIVENESS PROBE: /health/live ---
        if (ruta.endsWith("/live")) {
            String jsonLive = "{"
                    + "\"status\":\"UP\","
                    + "\"timestamp\":\"" + Instant.now().toString() + "\","
                    + "\"service\":\"ddtextil-backend\","
                    + "\"uptime_ms\":" + tiempoUptimeMs + ","
                    + "\"jvm_memory\":{"
                    + "\"used_mb\":" + memoriaUsadaMb + ","
                    + "\"free_mb\":" + memoriaLibreMb + ","
                    + "\"total_mb\":" + memoriaTotalMb
                    + "}"
                    + "}";
            enviarRespuestaJson(intercambio, 200, jsonLive);
            return;
        }

        // --- READINESS PROBE: /health/ready y REPORTE CONSOLIDADO: /api/health ---
        boolean baseDatosOperativa = false;
        String detalleErrorBd = null;
        try (Connection conexion = Conexion.obtenerConexion()) {
            if (conexion != null && conexion.isValid(2)) {
                baseDatosOperativa = true;
            } else {
                detalleErrorBd = "Conexión inválida o nula tras timeout de 2 segundos.";
            }
        } catch (Exception ex) {
            detalleErrorBd = ex.getMessage();
        }

        int codigoHttp = baseDatosOperativa ? 200 : 503;
        String estadoGlobal = baseDatosOperativa ? "UP" : "DOWN";

        String jsonRespuesta = "{"
                + "\"status\":\"" + estadoGlobal + "\","
                + "\"timestamp\":\"" + Instant.now().toString() + "\","
                + "\"service\":\"ddtextil-backend\","
                + "\"checks\":["
                + "{\"name\":\"jvm_process\",\"status\":\"UP\",\"uptime_ms\":" + tiempoUptimeMs + "},"
                + "{\"name\":\"postgresql_database\",\"status\":\"" + (baseDatosOperativa ? "UP" : "DOWN") + "\""
                + (detalleErrorBd != null ? ",\"error\":\"" + detalleErrorBd.replace("\"", "\\\"") + "\"" : "")
                + "}"
                + "]"
                + "}";

        enviarRespuestaJson(intercambio, codigoHttp, jsonRespuesta);
    }
}
