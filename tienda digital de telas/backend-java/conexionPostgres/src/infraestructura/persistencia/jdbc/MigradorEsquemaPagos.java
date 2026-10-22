package infraestructura.persistencia.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import infraestructura.configuracion.Conexion;

/**
 * Migración idempotente del esquema para la pasarela de pagos Wompi.
 *
 * Se ejecuta en cada arranque del backend. Todas las sentencias usan IF NOT EXISTS,
 * por lo que es seguro correrla múltiples veces y desde varias réplicas a la vez.
 * Es necesaria porque el script esquema_espanol.sql sólo se ejecuta cuando el
 * volumen de PostgreSQL se crea por primera vez.
 */
public final class MigradorEsquemaPagos {

    private MigradorEsquemaPagos() {}

    public static void ejecutar() {
        String[] sentencias = {
            // Columnas de pago sobre la tabla de pedidos existente
            "ALTER TABLE pedidos ADD COLUMN IF NOT EXISTS referencia_pago VARCHAR(64)",
            "ALTER TABLE pedidos ADD COLUMN IF NOT EXISTS monto_centavos BIGINT",
            "ALTER TABLE pedidos ADD COLUMN IF NOT EXISTS moneda VARCHAR(3) DEFAULT 'COP'",
            "ALTER TABLE pedidos ADD COLUMN IF NOT EXISTS wompi_transaccion_id VARCHAR(100)",
            "ALTER TABLE pedidos ADD COLUMN IF NOT EXISTS pagado_en TIMESTAMP",
            "CREATE UNIQUE INDEX IF NOT EXISTS ux_pedidos_referencia_pago ON pedidos(referencia_pago)",

            // Bitácora de eventos recibidos de Wompi (auditoría y trazabilidad)
            "CREATE TABLE IF NOT EXISTS eventos_pago_wompi (" +
            "  id SERIAL PRIMARY KEY," +
            "  referencia_pago VARCHAR(64)," +
            "  transaccion_id VARCHAR(100)," +
            "  estado VARCHAR(30)," +
            "  origen VARCHAR(30)," +
            "  resultado VARCHAR(60)," +
            "  carga_util TEXT," +
            "  recibido_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")",
            "CREATE INDEX IF NOT EXISTS ix_eventos_pago_referencia ON eventos_pago_wompi(referencia_pago)"
        };

        try (Connection con = Conexion.obtenerConexion(); Statement st = con.createStatement()) {
            for (String sql : sentencias) {
                st.execute(sql);
            }
            System.out.println("[WOMPI] Esquema de pagos verificado/migrado correctamente.");
        } catch (SQLException e) {
            System.err.println("[WOMPI] Error migrando esquema de pagos: " + e.getMessage());
        }
    }
}
