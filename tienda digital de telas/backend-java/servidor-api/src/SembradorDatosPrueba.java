import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import infraestructura.configuracion.Conexion;

/**
 * Inicializador y Sembrador de Datos (Seeder) PostgreSQL para D&D Textil.
 * 
 * Inserta la informacion inicial completa en las tablas relacionales oficiales en espanol:
 *  - Cuentas de usuarios (Administrador, Vendedores, Clientes).
 *  - Categorias textiles base.
 *  - Catalogo inicial de productos con especificaciones e imagenes.
 *  - Lotes de inventario iniciales.
 *  - Cupones de descuento activos.
 *  - Secciones y diapositivas de portada (Home y Carrusel).
 *  - Umbrales de alerta de stock y configuracion global del sistema.
 */
public class SembradorDatosPrueba {

    private static String encriptarContrasena(String contrasenaPlana) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(contrasenaPlana.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] resumen = digest.digest();
            StringBuilder cadenaHex = new StringBuilder();
            for (byte b : resumen) {
                cadenaHex.append(String.format("%02x", b));
            }
            return cadenaHex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo SHA-256 no disponible en la JVM", e);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Iniciando poblado de datos de prueba (D&D Textil)...");

        Connection conexion = Conexion.obtenerConexion();
        if (conexion == null) {
            System.err.println("ERROR: No se pudo conectar a la base de datos PostgreSQL.");
            return;
        }

        try {
            conexion.setAutoCommit(false);
            Statement sentencia = conexion.createStatement();

            // 1. Limpieza ordenada de tablas respetando dependencias de llaves foraneas
            System.out.println("[1/10] Limpiando datos anteriores en tablas en espanol...");
            sentencia.executeUpdate("TRUNCATE TABLE eventos_merma, lineas_pedido, items_carrito, imagenes_producto, reportes_error, tickets_soporte, ventas_diarias, ventas_region, actividad_reciente, eventos_pago_wompi CASCADE");
            sentencia.executeUpdate("TRUNCATE TABLE pedidos CASCADE");
            sentencia.executeUpdate("TRUNCATE TABLE lotes_inventario CASCADE");
            sentencia.executeUpdate("TRUNCATE TABLE rollos_textil CASCADE");
            sentencia.executeUpdate("TRUNCATE TABLE productos CASCADE");
            sentencia.executeUpdate("TRUNCATE TABLE cupones CASCADE");
            sentencia.executeUpdate("TRUNCATE TABLE categorias CASCADE");
            sentencia.executeUpdate("TRUNCATE TABLE umbrales_stock CASCADE");
            sentencia.executeUpdate("TRUNCATE TABLE banner_global CASCADE");
            sentencia.executeUpdate("TRUNCATE TABLE secciones_inicio CASCADE");
            sentencia.executeUpdate("TRUNCATE TABLE diapositivas_carrusel CASCADE");
            sentencia.executeUpdate("TRUNCATE TABLE configuracion_sistema CASCADE");
            sentencia.executeUpdate("TRUNCATE TABLE usuarios CASCADE");

            // 2. Insertar Usuarios del Sistema
            System.out.println("[2/10] Insertando usuarios con credenciales SHA-256 en UTF-8...");
            String sqlInsertarUsuario = "INSERT INTO usuarios (nombre, correo, contrasena_hash, rol, activo, tasa_comision) VALUES (?, ?, ?, ?, true, ?)";
            
            int idAdmin = 0, idVendedor1 = 0, idVendedor2 = 0, idCliente = 0;

            try (PreparedStatement pst = conexion.prepareStatement(sqlInsertarUsuario, Statement.RETURN_GENERATED_KEYS)) {
                // Administrador
                pst.setString(1, "Anderson Moreno");
                pst.setString(2, "admin@ddtextil.com");
                pst.setString(3, encriptarContrasena("admin123"));
                pst.setString(4, "administrador");
                pst.setNull(5, java.sql.Types.DOUBLE);
                pst.executeUpdate();
                try (ResultSet rs = pst.getGeneratedKeys()) { if (rs.next()) idAdmin = rs.getInt(1); }

                // Vendedor 1
                pst.setString(1, "Carlos Perez");
                pst.setString(2, "vendedor@ddtextil.com");
                pst.setString(3, encriptarContrasena("vendedor123"));
                pst.setString(4, "vendedor");
                pst.setDouble(5, 5.0);
                pst.executeUpdate();
                try (ResultSet rs = pst.getGeneratedKeys()) { if (rs.next()) idVendedor1 = rs.getInt(1); }

                // Vendedor 2
                pst.setString(1, "Maria Gonzalez");
                pst.setString(2, "maria.gonzalez@ddtextil.com");
                pst.setString(3, encriptarContrasena("vendedor123"));
                pst.setString(4, "vendedor");
                pst.setDouble(5, 4.5);
                pst.executeUpdate();
                try (ResultSet rs = pst.getGeneratedKeys()) { if (rs.next()) idVendedor2 = rs.getInt(1); }

                // Cliente General
                pst.setString(1, "Juan Cliente");
                pst.setString(2, "cliente@gmail.com");
                pst.setString(3, encriptarContrasena("cliente123"));
                pst.setString(4, "cliente");
                pst.setNull(5, java.sql.Types.DOUBLE);
                pst.executeUpdate();
                try (ResultSet rs = pst.getGeneratedKeys()) { if (rs.next()) idCliente = rs.getInt(1); }

                // Cliente Demo (compatibilidad con botones rapidos de UI)
                pst.setString(1, "Cliente Demo D&D");
                pst.setString(2, "cliente@ddtextil.com");
                pst.setString(3, encriptarContrasena("cliente123"));
                pst.setString(4, "cliente");
                pst.setNull(5, java.sql.Types.DOUBLE);
                pst.executeUpdate();
            }

            // 3. Insertar Categorias Textiles
            System.out.println("[3/10] Insertando categorias textiles base...");
            String[] listaCategorias = {"Algodon", "Seda", "Lino", "Lana", "Poliester", "Terciopelo", "Dril", "Mezclilla"};
            String sqlInsertarCategoria = "INSERT INTO categorias (nombre, descripcion) VALUES (?, ?)";
            try (PreparedStatement pst = conexion.prepareStatement(sqlInsertarCategoria)) {
                for (String categoria : listaCategorias) {
                    pst.setString(1, categoria);
                    pst.setString(2, "Telas de alta calidad tipo " + categoria);
                    pst.executeUpdate();
                }
            }

            // 4. Insertar Catalogo de Productos
            System.out.println("[4/10] Insertando catalogo de productos y rollos textiles...");
            Object[][] datosProductos = {
                {"Algodon Premium",       "Algodon",    25000.0, idVendedor1, "Tela de algodon 100% natural, ideal para camisas y blusas de alta durabilidad.", "Algodon 100%", "1.50m", "150g/m2", "Lavar en frio, no blanquear", 100, true, true, false, false, "/uploads/product_12_1775767112691.jpg"},
                {"Seda Italiana",         "Seda",       85000.0, idVendedor1, "Seda pura con acabado brillante, perfecta para vestidos de gala y eventos especiales.", "Seda pura", "1.40m", "80g/m2", "Limpieza en seco unicamente", 50, true, false, true, false, "/uploads/product_16_1775766690228.webp"},
                {"Lino Organico",         "Lino",       45000.0, idVendedor2, "Lino fresco y transpirable para ropa de verano, cortinas y manteleria fina.", "Lino organico", "1.50m", "200g/m2", "No usar secadora", 80, false, true, false, true, "/uploads/product_14_1775766898975.webp"},
                {"Lana Merino",           "Lana",       65000.0, idVendedor2, "Lana suave y calida de alta calidad para abrigos, trajes y prendas invernales.", "Lana Merino 100%", "1.45m", "320g/m2", "Lavar a mano con agua tibia", 40, false, false, true, false, "/uploads/product_14_1775766898975.webp"},
                {"Poliester Deportivo",   "Poliester",  18000.0, idVendedor1, "Tela sintetica de secado rapido ideal para uniformes y ropa deportiva.", "Poliester 100%", "1.60m", "130g/m2", "Lavado maquina ciclo normal", 150, true, false, false, true, "/uploads/product_15_1775767332095.jpg"},
                {"Terciopelo Azul Noche", "Terciopelo", 95000.0, idVendedor2, "Terciopelo espeso de tacto suave para tapiceria y alta costura.", "Poliester / Algodon", "1.40m", "380g/m2", "Limpieza profesional en seco", 30, true, true, true, false, "/uploads/product_10_1775766963190.webp"},
                {"Dril Elastico Natural", "Dril",       38000.0, idVendedor1, "Dril resistente con Spandex para pantalones de trabajo y chaquetas.", "Algodon 98% / Spandex 2%", "1.50m", "280g/m2", "Lavar a temperatura ambiente", 120, false, false, false, false, "/uploads/product_10_1775767265179.jpg"},
                {"Mezclilla Denim Heavy", "Mezclilla",  42000.0, idVendedor2, "Mezclilla rigida tradicional de 14oz para jeaneado clasico y chaquetas.", "Algodon 100%", "1.50m", "420g/m2", "Lavar al reves con colores similares", 90, true, true, false, true, "/uploads/product_11_1775767027618.jpg"}
            };

            String sqlInsertarProd = "INSERT INTO productos (nombre, categoria_id, precio, vendedor_id, descripcion, material, ancho, peso, cuidados, stock, destacado, activo, estado_moderacion, es_nueva_coleccion, es_exclusivo, es_oferta) " +
                                  "VALUES (?, (SELECT id FROM categorias WHERE nombre = ? LIMIT 1), ?, ?, ?, ?, ?, ?, ?, ?, ?, true, 'approved', ?, ?, ?)";
            String sqlInsertarImg = "INSERT INTO imagenes_producto (producto_id, url_imagen, orden_visualizacion) VALUES (?, ?, 0)";
            String sqlInsertarLote = "INSERT INTO lotes_inventario (producto_id, metros_recibidos, costo_metro, proveedor, fecha_recepcion) VALUES (?, ?, ?, ?, CURRENT_DATE)";
            String sqlInsertarRollo = "INSERT INTO rollos_textil (producto_id, codigo_rollo, metraje_inicial, metraje_remanente, lote_tintoreria, estado, ubicacion_bodega) VALUES (?, ?, ?, ?, ?, 'disponible', ?)";

            try (PreparedStatement pstProd = conexion.prepareStatement(sqlInsertarProd, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement pstImg = conexion.prepareStatement(sqlInsertarImg);
                 PreparedStatement pstLote = conexion.prepareStatement(sqlInsertarLote);
                 PreparedStatement pstRollo = conexion.prepareStatement(sqlInsertarRollo)) {

                int contadorRollo = 1;
                for (Object[] fila : datosProductos) {
                    pstProd.setString(1, (String) fila[0]);
                    pstProd.setString(2, (String) fila[1]);
                    pstProd.setDouble(3, (Double) fila[2]);
                    pstProd.setInt(4, (Integer) fila[3]);
                    pstProd.setString(5, (String) fila[4]);
                    pstProd.setString(6, (String) fila[5]);
                    pstProd.setString(7, (String) fila[6]);
                    pstProd.setString(8, (String) fila[7]);
                    pstProd.setString(9, (String) fila[8]);
                    pstProd.setInt(10, (Integer) fila[9]);
                    pstProd.setBoolean(11, (Boolean) fila[10]);
                    pstProd.setBoolean(12, (Boolean) fila[11]);
                    pstProd.setBoolean(13, (Boolean) fila[12]);
                    pstProd.setBoolean(14, (Boolean) fila[13]);
                    pstProd.executeUpdate();

                    int idProdGenerado = 0;
                    try (ResultSet rs = pstProd.getGeneratedKeys()) {
                        if (rs.next()) idProdGenerado = rs.getInt(1);
                    }

                    if (idProdGenerado > 0) {
                        pstImg.setInt(1, idProdGenerado);
                        pstImg.setString(2, (String) fila[14]);
                        pstImg.executeUpdate();

                        pstLote.setInt(1, idProdGenerado);
                        pstLote.setDouble(2, (Integer) fila[9]);
                        pstLote.setDouble(3, ((Double) fila[2]) * 0.6);
                        pstLote.setString(4, "Proveedor Textil Central");
                        pstLote.executeUpdate();

                        // Rollo textil fisico asociado
                        pstRollo.setInt(1, idProdGenerado);
                        pstRollo.setString(2, String.format("ROL-%03d", contadorRollo));
                        pstRollo.setDouble(3, (Integer) fila[9]);
                        pstRollo.setDouble(4, (Integer) fila[9]);
                        pstRollo.setString(5, "LOT-2026-A" + contadorRollo);
                        pstRollo.setString(6, "PAS-0" + (contadorRollo % 4 + 1) + "-EST-02");
                        pstRollo.executeUpdate();
                        contadorRollo++;
                    }
                }
            }

            // 5. Insertar Cupones de Descuento
            System.out.println("[5/10] Insertando cupones comerciales...");
            String sqlInsertarCupon = "INSERT INTO cupones (codigo, valor_descuento, usos_maximos, activo) VALUES (?, ?, ?, true)";
            try (PreparedStatement pst = conexion.prepareStatement(sqlInsertarCupon)) {
                pst.setString(1, "DESCUENTO10");
                pst.setDouble(2, 10.0);
                pst.setInt(3, 100);
                pst.executeUpdate();

                pst.setString(1, "BIENVENIDA20");
                pst.setDouble(2, 20.0);
                pst.setInt(3, 50);
                pst.executeUpdate();
            }

            // 6. Configurar Secciones del Inicio
            System.out.println("[6/10] Insertando secciones de la pagina de inicio...");
            String sqlInsertarSeccion = "INSERT INTO secciones_inicio (clave, titulo, subtitulo, activo, orden) VALUES (?, ?, ?, true, ?)";
            try (PreparedStatement pst = conexion.prepareStatement(sqlInsertarSeccion)) {
                pst.setString(1, "nuevas");
                pst.setString(2, "Nuevas Colecciones");
                pst.setString(3, "Lo ultimo en tendencias de moda y textiles");
                pst.setInt(4, 1);
                pst.executeUpdate();

                pst.setString(1, "exclusivas");
                pst.setString(2, "Telas Exclusivas");
                pst.setString(3, "Materiales de alta costura e importacion especial");
                pst.setInt(4, 2);
                pst.executeUpdate();

                pst.setString(1, "ofertas");
                pst.setString(2, "Ofertas Especiales");
                pst.setString(3, "Descuentos por metraje y liquidacion de inventario");
                pst.setInt(4, 3);
                pst.executeUpdate();
            }

            // 7. Insertar Diapositivas del Carrusel
            System.out.println("[7/10] Insertando diapositivas del carrusel...");
            String sqlInsertarDiapositiva = "INSERT INTO diapositivas_carrusel (titulo, subtitulo, imagen, cta, clave_seccion, activo, orden) VALUES (?, ?, ?, ?, ?, true, ?)";
            try (PreparedStatement pst = conexion.prepareStatement(sqlInsertarDiapositiva)) {
                pst.setString(1, "Gran Coleccion 2026");
                pst.setString(2, "Telas de Lino y Algodon 100% natural con hasta 20% de descuento por escala.");
                pst.setString(3, "/images/carousel-nueva-coleccion.png");
                pst.setString(4, "Ver Coleccion");
                pst.setString(5, "nuevas");
                pst.setInt(6, 1);
                pst.executeUpdate();

                pst.setString(1, "Seda Italiana y Terciopelo de Lujo");
                pst.setString(2, "Elegancia y textura unica para tus confecciones de alta costura.");
                pst.setString(3, "/images/carousel-telas-exclusivas.png");
                pst.setString(4, "Explorar Exclusivos");
                pst.setString(5, "exclusivas");
                pst.setInt(6, 2);
                pst.executeUpdate();
            }

            // 8. Configurar Umbrales de Stock
            System.out.println("[8/10] Configurando umbrales de alerta de stock...");
            String sqlInsertarUmbral = "INSERT INTO umbrales_stock (tipo_tela, metros_minimos, alerta_activa) VALUES (?, ?, ?)";
            try (PreparedStatement pst = conexion.prepareStatement(sqlInsertarUmbral)) {
                for (String categoria : listaCategorias) {
                    pst.setString(1, categoria);
                    pst.setDouble(2, 20.0);
                    pst.setBoolean(3, true);
                    pst.executeUpdate();
                }
            }

            // 9. Banner Global y Configuracion
            System.out.println("[9/10] Configurando banner y parametros globales...");
            String sqlInsertarBanner = "INSERT INTO banner_global (activo, mensaje, tipo_banner) VALUES (?, ?, ?)";
            try (PreparedStatement pst = conexion.prepareStatement(sqlInsertarBanner)) {
                pst.setBoolean(1, true);
                pst.setString(2, "Envio gratis en compras superiores a $200.000 COP a todo el pais.");
                pst.setString(3, "info");
                pst.executeUpdate();
            }

            String sqlInsertarConfig = "INSERT INTO configuracion_sistema (clave, valor) VALUES (?, ?) ON CONFLICT (clave) DO UPDATE SET valor = EXCLUDED.valor";
            try (PreparedStatement pst = conexion.prepareStatement(sqlInsertarConfig)) {
                pst.setString(1, "configuracion_sistema");
                pst.setString(2, "{\"nombreSitio\":\"D&D Textil\",\"tasaIva\":0.19,\"costoEnvio\":15000,\"umbralEnvioGratis\":200000,\"umbralStockBajo\":20}");
                pst.executeUpdate();
            }

            // 10. Metricas de Venta para Analitica
            System.out.println("[10/10] Insertando metricas de ventas iniciales...");
            sentencia.executeUpdate("CREATE TABLE IF NOT EXISTS metricas_ventas_erp (" +
                               "id SERIAL PRIMARY KEY, " +
                               "fecha_registro DATE NOT NULL UNIQUE, " +
                               "ventas_reales NUMERIC(15, 2) NOT NULL DEFAULT 0.00, " +
                               "ventas_objetivo NUMERIC(15, 2) NOT NULL DEFAULT 0.00, " +
                               "margen_beneficio NUMERIC(5, 2) NOT NULL DEFAULT 0.00, " +
                               "creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            sentencia.executeUpdate("INSERT INTO metricas_ventas_erp (fecha_registro, ventas_reales, ventas_objetivo, margen_beneficio) VALUES " +
                               "(CURRENT_DATE - INTERVAL '3 days', 2100000, 1650000, 38.1), " +
                               "(CURRENT_DATE - INTERVAL '2 days', 1900000, 1700000, 36.4), " +
                               "(CURRENT_DATE - INTERVAL '1 days', 2400000, 1800000, 41.0), " +
                               "(CURRENT_DATE, 1400000, 1850000, 35.5) " +
                               "ON CONFLICT (fecha_registro) DO NOTHING");

            conexion.commit();
            System.out.println("\n=== BASE DE DATOS POSTGRESQL POBLADA EXITOSAMENTE ===");
            System.out.println("Cuentas preparadas para verificacion:");
            System.out.println("  1. Administrador: admin@ddtextil.com / admin123");
            System.out.println("  2. Vendedor 1:    vendedor@ddtextil.com / vendedor123");
            System.out.println("  3. Vendedor 2:    maria.gonzalez@ddtextil.com / vendedor123");
            System.out.println("  4. Cliente:       cliente@ddtextil.com o cliente@gmail.com / cliente123");

        } catch (Exception e) {
            System.err.println("ERROR poblando la base de datos: " + e.getMessage());
            e.printStackTrace();
            try {
                conexion.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
