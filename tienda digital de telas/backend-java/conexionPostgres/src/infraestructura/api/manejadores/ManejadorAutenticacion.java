package infraestructura.api.manejadores;

import aplicacion.servicios.ServicioAutenticacion;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dominio.modelos.Usuario;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Controlador para operaciones de autenticación de usuarios.
 * Maneja el inicio de sesión (login) y el registro (register) de clientes.
 */
public class ManejadorAutenticacion extends ManejadorBase {
    private final ServicioAutenticacion servicioAutenticacion;
    private final Gson gson;

    public ManejadorAutenticacion(ServicioAutenticacion servicioAutenticacion) {
        this.servicioAutenticacion = servicioAutenticacion;
        this.gson = new Gson();
    }

    @Override
    protected void procesarPeticion(HttpExchange intercambio) throws Exception {
        if (!"POST".equals(intercambio.getRequestMethod())) {
            enviarRespuestaJson(intercambio, 405, "{\"error\": \"Método no permitido\"}");
            return;
        }

        String ruta = intercambio.getRequestURI().getPath();
        InputStream cuerpoEntrada = intercambio.getRequestBody();
        String cuerpoTexto = new String(cuerpoEntrada.readAllBytes(), StandardCharsets.UTF_8);
        JsonObject jsonObjeto = JsonParser.parseString(cuerpoTexto).getAsJsonObject();

        // --- POST /api/login o /api/autenticacion/iniciar-sesion ---
        if (ruta.endsWith("/login") || ruta.endsWith("/iniciar-sesion")) {
            String correo = jsonObjeto.has("email") ? jsonObjeto.get("email").getAsString()
                          : (jsonObjeto.has("correo") ? jsonObjeto.get("correo").getAsString() : null);
            String contrasena = jsonObjeto.has("password") ? jsonObjeto.get("password").getAsString()
                              : (jsonObjeto.has("contrasena") ? jsonObjeto.get("contrasena").getAsString() : null);

            Usuario usuario = servicioAutenticacion.iniciarSesion(correo, contrasena);
            String jsonUsuario = gson.toJson(usuario);
            enviarRespuestaJson(intercambio, 200, "{\"success\": true, \"user\": " + jsonUsuario + "}");

        // --- POST /api/register o /api/autenticacion/registro ---
        } else if (ruta.endsWith("/register") || ruta.endsWith("/registro")) {
            String nombre = jsonObjeto.has("name") ? jsonObjeto.get("name").getAsString()
                          : (jsonObjeto.has("nombre") ? jsonObjeto.get("nombre").getAsString() : null);
            String correo = jsonObjeto.has("email") ? jsonObjeto.get("email").getAsString()
                          : (jsonObjeto.has("correo") ? jsonObjeto.get("correo").getAsString() : null);
            String contrasena = jsonObjeto.has("password") ? jsonObjeto.get("password").getAsString()
                              : (jsonObjeto.has("contrasena") ? jsonObjeto.get("contrasena").getAsString() : null);

            boolean aceptaHabeasData = true;
            if (jsonObjeto.has("aceptaHabeasData")) {
                aceptaHabeasData = jsonObjeto.get("aceptaHabeasData").getAsBoolean();
            } else if (jsonObjeto.has("acceptsTerms")) {
                aceptaHabeasData = jsonObjeto.get("acceptsTerms").getAsBoolean();
            } else if (jsonObjeto.has("habeasData")) {
                aceptaHabeasData = jsonObjeto.get("habeasData").getAsBoolean();
            }

            servicioAutenticacion.registrarCliente(nombre, correo, contrasena, aceptaHabeasData);
            enviarRespuestaJson(intercambio, 201, "{\"success\": true, \"message\": \"Usuario registrado exitosamente con consentimiento de Habeas Data\"}");

        // --- POST /api/autenticacion/sso o /api/sso ---
        } else if (ruta.endsWith("/sso")) {
            String correo = jsonObjeto.has("email") ? jsonObjeto.get("email").getAsString()
                          : (jsonObjeto.has("correo") ? jsonObjeto.get("correo").getAsString() : null);
            String nombre = jsonObjeto.has("nombre") ? jsonObjeto.get("nombre").getAsString()
                          : (jsonObjeto.has("name") ? jsonObjeto.get("name").getAsString() : null);

            Usuario usuario = servicioAutenticacion.autenticarOAutoRegistrarSSO(correo, nombre);
            String jsonUsuario = gson.toJson(usuario);
            enviarRespuestaJson(intercambio, 200, "{\"success\": true, \"user\": " + jsonUsuario + "}");

        } else {
            enviarRespuestaJson(intercambio, 404, "{\"error\": \"Ruta no encontrada\"}");
        }
    }
}
