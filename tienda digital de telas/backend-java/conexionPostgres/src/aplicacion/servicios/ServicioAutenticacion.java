package aplicacion.servicios;

import dominio.modelos.Usuario;
import dominio.repositorios.RepositorioUsuario;
import dominio.excepciones.ExcepcionDominio;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * Servicio de Autenticación de la capa de aplicación.
 */
public class ServicioAutenticacion {
    private final RepositorioUsuario repositorioUsuario;

    public ServicioAutenticacion(RepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
    }

    /**
     * Autentica un usuario mediante su correo y contraseña.
     * @param correo Correo electrónico del usuario.
     * @param contrasena Contraseña en texto plano.
     * @return Usuario autenticado.
     */
    public Usuario iniciarSesion(String correo, String contrasena) {
        if (correo == null || correo.isBlank() || contrasena == null || contrasena.isBlank()) {
            throw new ExcepcionDominio("El correo y la contraseña son obligatorios.");
        }

        String hash = generarHashContrasena(contrasena);
        Optional<Usuario> usuarioOpt = repositorioUsuario.buscarPorCorreoYValidarContrasena(correo, hash);

        if (usuarioOpt.isEmpty()) {
            throw new ExcepcionDominio("Credenciales incorrectas.");
        }

        Usuario usuario = usuarioOpt.get();
        if (usuario.isSuspendido()) {
            throw new ExcepcionDominio("Cuenta suspendida: " + usuario.getMotivoSuspension());
        }

        repositorioUsuario.actualizarUltimoAcceso(usuario.getId());
        return usuario;
    }

    /**
     * Registra un nuevo usuario con rol de cliente verificando consentimiento de Habeas Data.
     * @param nombre Nombre completo.
     * @param correo Correo electrónico.
     * @param contrasena Contraseña en texto plano.
     * @param aceptaHabeasData Consentimiento explícito según Ley 1581 de 2012.
     */
    public void registrarCliente(String nombre, String correo, String contrasena, boolean aceptaHabeasData) {
        if (!aceptaHabeasData) {
            throw new ExcepcionDominio("Es obligatorio aceptar expresamente la Política de Tratamiento de Datos Personales (Ley 1581 de 2012 - Habeas Data).");
        }

        if (nombre == null || nombre.isBlank() || correo == null || correo.isBlank() || contrasena == null || contrasena.isBlank()) {
            throw new ExcepcionDominio("Todos los campos obligatorios deben ser completados.");
        }

        Usuario nuevoUsuario = new Usuario(0, nombre, correo, "cliente", true, false, null, null, null, null, null);
        String contrasenaCifrada = generarHashContrasena(contrasena);

        boolean guardadoExitoso = repositorioUsuario.guardar(nuevoUsuario, contrasenaCifrada);
        if (!guardadoExitoso) {
            throw new ExcepcionDominio("Error al registrar usuario. Es posible que el correo ya exista.");
        }
    }

    /**
     * Sobrecarga de compatibilidad para registrar cliente.
     */
    public void registrarCliente(String nombre, String correo, String contrasena) {
        registrarCliente(nombre, correo, contrasena, true);
    }

    /**
     * Regla de Seguridad PCI-DSS:
     * Verifica que ninguna cadena contenga números de tarjeta de crédito (PAN) ni códigos CVV.
     * La arquitectura delega el procesamiento al SDK de la pasarela y solo admite tokens transaccionales.
     */
    public static boolean contieneDatosTarjetaSensibles(String texto) {
        if (texto == null || texto.isBlank()) return false;
        // Detección de posibles números de tarjeta de crédito (PAN entre 13 y 19 dígitos)
        boolean contienePAN = texto.matches(".*\\b(?:\\d[ -]*?){13,19}\\b.*");
        // Detección de claves de seguridad CVV/CVC
        boolean contieneCVV = texto.toLowerCase().matches(".*(?:cvv|cvc|codigo_seguridad)[\\s:=]+[0-9]{3,4}.*");
        return contienePAN || contieneCVV;
    }

    /**
     * Genera un hash criptográfico SHA-256 a partir de una contraseña en texto plano con UTF-8.
     * @param contrasena Contraseña en texto plano.
     * @return Cadena en formato hexadecimal con el hash SHA-256.
     */
    public static String generarHashContrasena(String contrasena) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(contrasena.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] bytes = digest.digest();
            StringBuilder constructorCadena = new StringBuilder();
            for (byte b : bytes) {
                constructorCadena.append(String.format("%02x", b));
            }
            return constructorCadena.toString();
        } catch (NoSuchAlgorithmException excepcion) {
            throw new RuntimeException("SHA-256 no disponible en la plataforma JVM", excepcion);
        }
    }
}
