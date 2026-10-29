package infraestructura.configuracion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestión de conexiones a la base de datos PostgreSQL para D&D Textil.
 * Utiliza variables de entorno mediante ConfiguracionEntorno con fallbacks seguros.
 */
public class Conexion {

    /** URL de la base de datos PostgreSQL. */
    private static final String URL_BD = ConfiguracionEntorno.obtener(
        "DB_URL",
        "jdbc:postgresql://localhost:5432/tienda_digital_textiles_db"
    );

    /** Usuario de conexión a la base de datos. */
    private static final String USUARIO_BD = ConfiguracionEntorno.obtener(
        "DB_USER",
        "postgres"
    );

    /** Contraseña de conexión a la base de datos. */
    private static final String CONTRASENA_BD = ConfiguracionEntorno.obtener(
        "DB_PASSWORD",
        "mysecretpassword"
    );

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR CRITICO] No se encontro el driver JDBC de PostgreSQL.");
            e.printStackTrace();
        }
    }

    private Conexion() {}

    /**
     * Obtiene una nueva conexión activa a la base de datos.
     *
     * @return Conexión SQL abierta.
     * @throws SQLException Si ocurre un fallo de autenticación o conexión de red.
     */
    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL_BD, USUARIO_BD, CONTRASENA_BD);
    }

    /**
     * Método alias para compatibilidad.
     */
    public static Connection getConnection() throws SQLException {
        return obtenerConexion();
    }
}
