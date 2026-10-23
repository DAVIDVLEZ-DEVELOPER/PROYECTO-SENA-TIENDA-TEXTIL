package infraestructura.persistencia.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import infraestructura.configuracion.Conexion;
import dominio.modelos.Cupon;

/**
 * DAO para Cupones de Descuento.
 * Opera sobre la tabla 'cupones'.
 */
public class CuponDAO {

    /**
     * Recupera todos los cupones registrados.
     */
    public List<Cupon> obtenerTodosLosCupones() {
        List<Cupon> cupones = new ArrayList<>();
        String consulta = "SELECT id, code, tipo_descuento, valor_descuento, expira_en, compra_minima, usos_maximos, solo_primera_vez, conteo_usos, active FROM cupones ORDER BY id DESC";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement pst = con.prepareStatement(consulta);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Cupon cupon = new Cupon();
                cupon.setId(rs.getInt("id"));
                cupon.setCodigo(rs.getString("code"));
                cupon.setTipoDescuento(rs.getString("tipo_descuento"));
                cupon.setValorDescuento(rs.getDouble("valor_descuento"));
                
                if (rs.getTimestamp("expira_en") != null) {
                    cupon.setFechaExpiracion(rs.getTimestamp("expira_en").toString());
                }

                cupon.setConteoUso(rs.getInt("conteo_usos"));
                cupon.setActivo(rs.getBoolean("active"));

                double minP = rs.getDouble("compra_minima");
                if (!rs.wasNull()) {
                    cupon.getReglas().setCompraMinima(minP);
                }

                int maxU = rs.getInt("usos_maximos");
                if (!rs.wasNull()) {
                    cupon.getReglas().setUsosMaximos(maxU);
                }

                cupon.getReglas().setSoloPrimeraCompra(rs.getBoolean("solo_primera_vez"));

                cupones.add(cupon);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Error obteniendo cupones: " + e.getMessage());
        }
        return cupones;
    }

    /**
     * Inserta un nuevo cupón en la base de datos.
     */
    public boolean agregarCupon(Cupon cupon) {
        String consulta = "INSERT INTO cupones (codigo, code, tipo_descuento, valor_descuento, expira_en, compra_minima, usos_maximos, solo_primera_vez, activo, active) VALUES (?, ?, ?, ?, CAST(? AS TIMESTAMP), ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement pst = con.prepareStatement(consulta)) {
            pst.setString(1, cupon.getCodigo());
            pst.setString(2, cupon.getCodigo());
            pst.setString(3, cupon.getTipoDescuento());
            pst.setDouble(4, cupon.getValorDescuento());
            
            if (cupon.getFechaExpiracion() != null && !cupon.getFechaExpiracion().isEmpty()) {
                pst.setString(5, cupon.getFechaExpiracion() + " 23:59:59");
            } else {
                pst.setNull(5, Types.TIMESTAMP);
            }

            if (cupon.getReglas().getCompraMinima() != null) {
                pst.setDouble(6, cupon.getReglas().getCompraMinima());
            } else {
                pst.setNull(6, Types.NUMERIC);
            }

            if (cupon.getReglas().getUsosMaximos() != null) {
                pst.setInt(7, cupon.getReglas().getUsosMaximos());
            } else {
                pst.setNull(7, Types.INTEGER);
            }

            pst.setBoolean(8, cupon.getReglas().isSoloPrimeraCompra());
            pst.setBoolean(9, true);
            pst.setBoolean(10, true);

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERROR] Error insertando cupon: " + e.getMessage());
            return false;
        }
    }

    /**
     * Desactiva un cupón existente (soft-delete).
     */
    public boolean desactivarCupon(int id) {
        String consulta = "UPDATE cupones SET activo = false, active = false WHERE id = ?";
        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement pst = con.prepareStatement(consulta)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERROR] Error desactivando cupon: " + e.getMessage());
            return false;
        }
    }

    // Métodos alias para compatibilidad
    public List<Cupon> getAllCoupons() { return obtenerTodosLosCupones(); }
    public boolean addCoupon(Cupon coupon) { return agregarCupon(coupon); }
    public boolean deactivateCoupon(int id) { return desactivarCupon(id); }
}

