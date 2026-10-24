package dominio.modelos;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo de Dominio que representa una unidad física de rollo de tela en bodega.
 * Garantiza la trazabilidad por metraje continuo y lote de tintorería.
 */
public class RolloTextil {

    @SerializedName(value = "id", alternate = {"idRollo"})
    private int id;

    @SerializedName(value = "productId", alternate = {"productoId", "idProducto"})
    private int productoId;

    @SerializedName(value = "rollCode", alternate = {"codigoRollo"})
    private String codigoRollo;

    @SerializedName(value = "initialMeters", alternate = {"metrajeInicial"})
    private double metrajeInicial;

    @SerializedName(value = "remainingMeters", alternate = {"metrajeRemanente"})
    private double metrajeRemanente;

    @SerializedName(value = "dyeLot", alternate = {"loteTintoreria"})
    private String loteTintoreria;

    @SerializedName(value = "status", alternate = {"estado"})
    private String estado; // "disponible", "reservado", "retazo_liquidado", "agotado"

    @SerializedName(value = "warehouseLocation", alternate = {"ubicacionBodega"})
    private String ubicacionBodega;

    public RolloTextil() {}

    public RolloTextil(int id, int productoId, String codigoRollo, double metrajeInicial, double metrajeRemanente, String loteTintoreria, String estado) {
        this.id = id;
        this.productoId = productoId;
        this.codigoRollo = codigoRollo;
        this.metrajeInicial = metrajeInicial;
        this.metrajeRemanente = metrajeRemanente;
        this.loteTintoreria = loteTintoreria;
        this.estado = estado;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }

    public String getCodigoRollo() { return codigoRollo; }
    public void setCodigoRollo(String codigoRollo) { this.codigoRollo = codigoRollo; }

    public double getMetrajeInicial() { return metrajeInicial; }
    public void setMetrajeInicial(double metrajeInicial) { this.metrajeInicial = metrajeInicial; }

    public double getMetrajeRemanente() { return metrajeRemanente; }
    public void setMetrajeRemanente(double metrajeRemanente) { this.metrajeRemanente = metrajeRemanente; }

    public String getLoteTintoreria() { return loteTintoreria; }
    public void setLoteTintoreria(String loteTintoreria) { this.loteTintoreria = loteTintoreria; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getUbicacionBodega() { return ubicacionBodega; }
    public void setUbicacionBodega(String ubicacionBodega) { this.ubicacionBodega = ubicacionBodega; }
}
