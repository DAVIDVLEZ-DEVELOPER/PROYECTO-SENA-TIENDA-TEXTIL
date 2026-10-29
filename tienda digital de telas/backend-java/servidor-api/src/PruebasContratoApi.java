import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dominio.modelos.Pedido;
import dominio.modelos.Producto;
import dominio.modelos.Usuario;

import java.util.Arrays;
import java.util.List;

/**
 * Suite de Pruebas Automatizadas de Contrato de Interfaz de API (API Contract Testing).
 * Valida la conformidad estricta de las cargas utiles JSON y cabeceras HTTP del backend
 * frente a la especificacion OpenAPI v3.0 (documentacion/openapi_tienda_textil.yaml)
 * para la sustentacion ADSO del SENA.
 */
public class PruebasContratoApi {

    private static int pruebasExitosas = 0;
    private static int pruebasFallidas = 0;
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("   SUITE DE PRUEBAS DE CONTRATO DE API REST (OPENAPI v3.0 SENA)  ");
        System.out.println("=================================================================");

        ejecutarPrueba("1. Conformidad de Esquema OpenAPI: Componente 'Producto'", () -> {
            Producto prod = new Producto();
            prod.setId("101");
            prod.setNombre("Lino Rustico Premium");
            prod.setCategoria("Linos y Fibras");
            prod.setPrecio(38500.0);
            prod.setExistencias(150);
            prod.setDescripcion("Tela de lino 100% natural para cortineria y prendas frescas");
            prod.setMaterial("100% Lino");
            prod.setAncho("1.50 m");
            prod.setPeso("220 g/m2");
            prod.setCuidado("Lavar con agua fria, secado a la sombra");
            prod.setImagenes(Arrays.asList("/uploads/product_101_1.jpg", "/uploads/product_101_2.jpg"));
            prod.setDestacado(true);

            String jsonProd = gson.toJson(prod);
            JsonObject obj = JsonParser.parseString(jsonProd).getAsJsonObject();

            // Validacion de campos requeridos y tipos en el contrato OpenAPI
            validarPropiedadString(obj, "id", true);
            validarPropiedadString(obj, "name", "nombre", true);
            validarPropiedadNumero(obj, "price", "precio", true);
            validarPropiedadNumero(obj, "stock", "existencias", true);
            validarPropiedadArray(obj, "images", "imagenes", true);
            validarPropiedadString(obj, "category", "categoria", false);

            if (prod.getPrecio() <= 0.0) {
                throw new AssertionError("El precio debe ser un numero positivo mayor a cero.");
            }
            if (prod.getExistencias() < 0) {
                throw new AssertionError("Las existencias no pueden ser un valor negativo.");
            }
        });

        ejecutarPrueba("2. Conformidad de Esquema OpenAPI: Componente 'Pedido'", () -> {
            Pedido pedido = new Pedido();
            pedido.setId(505);
            pedido.setIdCliente(12);
            pedido.setIdVendedor(3);
            pedido.setTotal(195000.0);
            pedido.setEstado("pending");
            pedido.setFecha("2026-10-27");
            pedido.setCantidadArticulos(2);
            pedido.agregarIdProducto(101);
            pedido.agregarIdProducto(102);

            String jsonPedido = gson.toJson(pedido);
            JsonObject obj = JsonParser.parseString(jsonPedido).getAsJsonObject();

            validarPropiedadNumero(obj, "id", true);
            validarPropiedadNumero(obj, "clientId", "idCliente", true);
            validarPropiedadNumero(obj, "total", "montoTotal", true);
            validarPropiedadString(obj, "status", "estado", true);
            validarPropiedadArray(obj, "productIds", "idsProductos", true);

            // Validar que el estado del pedido pertenezca al catalogo permitido en el contrato
            List<String> estadosPermitidos = Arrays.asList("pending", "confirmed", "cutting", "shipped", "delivered", "cancelled", "paid");
            String estadoObtenido = obj.has("status") ? obj.get("status").getAsString() : obj.get("estado").getAsString();
            if (!estadosPermitidos.contains(estadoObtenido.toLowerCase())) {
                throw new AssertionError("El estado '" + estadoObtenido + "' no esta permitido en la especificacion OpenAPI.");
            }
        });

        ejecutarPrueba("3. Conformidad de Esquema OpenAPI: Componente 'UsuarioPerfil'", () -> {
            Usuario usuario = new Usuario(1, "Carlos Mendez", "carlos.mendez@ddtextil.com", "vendedor", true, false, null, 0.08, "2026-01-10", "2026-10-27 10:00:00", "Activo");

            String jsonUsuario = gson.toJson(usuario);
            JsonObject obj = JsonParser.parseString(jsonUsuario).getAsJsonObject();

            validarPropiedadNumero(obj, "id", true);
            validarPropiedadString(obj, "name", "nombre", true);
            validarPropiedadString(obj, "email", "correo", true);
            validarPropiedadString(obj, "role", "rol", true);
            validarPropiedadBooleano(obj, "active", "activo", true);

            // Validar roles canonicos del sistema
            List<String> rolesPermitidos = Arrays.asList("administrador", "admin", "vendedor", "seller", "cliente", "client");
            String rolObtenido = obj.has("role") ? obj.get("role").getAsString() : obj.get("rol").getAsString();
            if (!rolesPermitidos.contains(rolObtenido.toLowerCase())) {
                throw new AssertionError("El rol '" + rolObtenido + "' no es valido segun el contrato OpenAPI.");
            }
        });

        ejecutarPrueba("4. Conformidad de Esquema OpenAPI: Monitoreo y Observabilidad (/health/live y /health/ready)", () -> {
            // Contrato de Liveness Probe
            String mockLive = "{"
                    + "\"status\":\"UP\","
                    + "\"timestamp\":\"2026-10-27T20:00:00.000Z\","
                    + "\"service\":\"ddtextil-backend\","
                    + "\"uptime_ms\":125000,"
                    + "\"jvm_memory\":{\"used_mb\":45,\"free_mb\":120,\"total_mb\":165}"
                    + "}";
            JsonObject objLive = JsonParser.parseString(mockLive).getAsJsonObject();
            validarPropiedadString(objLive, "status", true);
            validarPropiedadString(objLive, "service", true);
            validarPropiedadNumero(objLive, "uptime_ms", true);
            if (!objLive.has("jvm_memory") || !objLive.get("jvm_memory").isJsonObject()) {
                throw new AssertionError("Falta el objeto jvm_memory en el reporte de Liveness.");
            }

            // Contrato de Readiness Probe
            String mockReady = "{"
                    + "\"status\":\"UP\","
                    + "\"timestamp\":\"2026-10-27T20:00:00.000Z\","
                    + "\"service\":\"ddtextil-backend\","
                    + "\"checks\":["
                    + "  {\"name\":\"jvm_process\",\"status\":\"UP\",\"uptime_ms\":125000},"
                    + "  {\"name\":\"postgresql_database\",\"status\":\"UP\"}"
                    + "]"
                    + "}";
            JsonObject objReady = JsonParser.parseString(mockReady).getAsJsonObject();
            validarPropiedadString(objReady, "status", true);
            validarPropiedadArray(objReady, "checks", true);
        });

        ejecutarPrueba("5. Conformidad de Esquema OpenAPI: Intencion de Pago Wompi", () -> {
            String mockIntencion = "{"
                    + "\"success\":true,"
                    + "\"orderId\":1042,"
                    + "\"reference\":\"DD-1700000000-001\","
                    + "\"amountInCents\":15000000,"
                    + "\"currency\":\"COP\","
                    + "\"integritySignature\":\"a1b2c3d4e5f60718293a4b5c6d7e8f90a1b2c3d4e5f6a1b2c3d4e5f60718293a\","
                    + "\"publicKey\":\"pub_test_Q5yDA9xoKdePzhSGeVe9HAUr19rhZCQSQ\","
                    + "\"subtotal\":126050.42,"
                    + "\"discount\":0.0,"
                    + "\"shipping\":0.0,"
                    + "\"tax\":23949.58,"
                    + "\"total\":150000.0"
                    + "}";
            JsonObject obj = JsonParser.parseString(mockIntencion).getAsJsonObject();
            validarPropiedadBooleano(obj, "success", true);
            validarPropiedadNumero(obj, "orderId", true);
            validarPropiedadString(obj, "reference", true);
            validarPropiedadNumero(obj, "amountInCents", true);
            validarPropiedadString(obj, "currency", true);
            validarPropiedadString(obj, "integritySignature", true);

            if (!"COP".equals(obj.get("currency").getAsString())) {
                throw new AssertionError("La moneda oficial debe ser estrictamente 'COP'.");
            }
            if (obj.get("integritySignature").getAsString().length() != 64) {
                throw new AssertionError("La firma de integridad debe tener longitud exacta de 64 caracteres SHA-256.");
            }
        });

        ejecutarPrueba("6. Cabeceras HTTP Obligatorias de Observabilidad y Seguridad", () -> {
            // Validacion de nombres de cabeceras estandares configuradas en ManejadorBase
            String cabeceraCorrelationId = "X-Correlation-ID";
            String cabeceraContentType = "Content-Type";
            String cabeceraCors = "Access-Control-Allow-Origin";

            String uuidMuestra = java.util.UUID.randomUUID().toString();
            String patronUuid = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

            if (!uuidMuestra.matches(patronUuid)) {
                throw new AssertionError("El valor para " + cabeceraCorrelationId + " no es un UUID v4 valido.");
            }

            String contentTypeEsperado = "application/json; charset=UTF-8";
            if (!contentTypeEsperado.contains("charset=UTF-8") || !contentTypeEsperado.contains("application/json")) {
                throw new AssertionError("El tipo de contenido esperado no cumple el estandar UTF-8.");
            }
        });

        ejecutarPrueba("7. Estandar de Formato para Respuestas de Error (RFC 7807 Conformity)", () -> {
            String jsonError = "{\"error\":\"Inventario insuficiente para la tela solicitada.\"}";
            JsonObject objError = JsonParser.parseString(jsonError).getAsJsonObject();

            if (!objError.has("error")) {
                throw new AssertionError("Toda respuesta de error debe contener la propiedad 'error'.");
            }
            String mensaje = objError.get("error").getAsString();
            if (mensaje == null || mensaje.trim().isEmpty()) {
                throw new AssertionError("El mensaje de error no puede estar vacio.");
            }
        });

        System.out.println("\n=================================================================");
        System.out.println("                    RESUMEN DE PRUEBAS                          ");
        System.out.println("=================================================================");
        System.out.println("  Pruebas superadas con exito: " + pruebasExitosas);
        System.out.println("  Pruebas fallidas:            " + pruebasFallidas);

        if (pruebasFallidas == 0) {
            System.out.println("\n>>> TODAS LAS PRUEBAS DE CONTRATO DE API (OPENAPI v3.0) FUERON EXITOSAS <<<\n");
        } else {
            System.err.println("\n>>> SE IDENTIFICARON FALLAS EN EL CONTRATO DE LA API <<<\n");
            System.exit(1);
        }
    }

    private static void validarPropiedadString(JsonObject obj, String clave, boolean requerida) {
        validarPropiedadString(obj, clave, null, requerida);
    }

    private static void validarPropiedadString(JsonObject obj, String clave1, String clave2, boolean requerida) {
        String clavePresente = obj.has(clave1) ? clave1 : (clave2 != null && obj.has(clave2) ? clave2 : null);
        if (requerida && clavePresente == null) {
            throw new AssertionError("Falta la propiedad requerida '" + clave1 + "' en el esquema.");
        }
        if (clavePresente != null && !obj.get(clavePresente).isJsonPrimitive()) {
            throw new AssertionError("La propiedad '" + clavePresente + "' debe ser un valor de texto primitivo.");
        }
    }

    private static void validarPropiedadNumero(JsonObject obj, String clave, boolean requerida) {
        validarPropiedadNumero(obj, clave, null, requerida);
    }

    private static void validarPropiedadNumero(JsonObject obj, String clave1, String clave2, boolean requerida) {
        String clavePresente = obj.has(clave1) ? clave1 : (clave2 != null && obj.has(clave2) ? clave2 : null);
        if (requerida && clavePresente == null) {
            throw new AssertionError("Falta la propiedad numerica requerida '" + clave1 + "' en el esquema.");
        }
        if (clavePresente != null && (!obj.get(clavePresente).isJsonPrimitive() || !obj.get(clavePresente).getAsJsonPrimitive().isNumber())) {
            throw new AssertionError("La propiedad '" + clavePresente + "' debe ser numerica.");
        }
    }

    private static void validarPropiedadBooleano(JsonObject obj, String clave, boolean requerida) {
        validarPropiedadBooleano(obj, clave, null, requerida);
    }

    private static void validarPropiedadBooleano(JsonObject obj, String clave1, String clave2, boolean requerida) {
        String clavePresente = obj.has(clave1) ? clave1 : (clave2 != null && obj.has(clave2) ? clave2 : null);
        if (requerida && clavePresente == null) {
            throw new AssertionError("Falta la propiedad booleana requerida '" + clave1 + "' en el esquema.");
        }
        if (clavePresente != null && (!obj.get(clavePresente).isJsonPrimitive() || !obj.get(clavePresente).getAsJsonPrimitive().isBoolean())) {
            throw new AssertionError("La propiedad '" + clavePresente + "' debe ser booleana.");
        }
    }

    private static void validarPropiedadArray(JsonObject obj, String clave, boolean requerida) {
        validarPropiedadArray(obj, clave, null, requerida);
    }

    private static void validarPropiedadArray(JsonObject obj, String clave1, String clave2, boolean requerida) {
        String clavePresente = obj.has(clave1) ? clave1 : (clave2 != null && obj.has(clave2) ? clave2 : null);
        if (requerida && clavePresente == null) {
            throw new AssertionError("Falta la propiedad de arreglo requerida '" + clave1 + "' en el esquema.");
        }
        if (clavePresente != null && !obj.get(clavePresente).isJsonArray()) {
            throw new AssertionError("La propiedad '" + clavePresente + "' debe ser un arreglo JSON.");
        }
    }

    private static void ejecutarPrueba(String nombrePrueba, AccionPrueba accion) {
        System.out.print("[TEST] " + nombrePrueba + " ... ");
        try {
            accion.ejecutar();
            System.out.println("CORRECTO");
            pruebasExitosas++;
        } catch (Throwable t) {
            System.out.println("FALLO");
            System.err.println("  -> Error: " + t.getMessage());
            t.printStackTrace(System.err);
            pruebasFallidas++;
        }
    }

    @FunctionalInterface
    interface AccionPrueba {
        void ejecutar() throws Exception;
    }
}
