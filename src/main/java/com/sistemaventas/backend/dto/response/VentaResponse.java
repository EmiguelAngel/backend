package com.sistemaventas.backend.dto.response;


import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.sistemaventas.backend.entity.DetalleFactura;
import com.sistemaventas.backend.entity.Factura;
import com.sistemaventas.backend.entity.Pago;

public class VentaResponse {
    
    private Integer idFactura;
    private String nombreUsuario;
    private Date fecha;
    private BigDecimal subtotal;
    private BigDecimal iva;
    private BigDecimal total;
    private String metodoPago;
    private List<ItemFactura> items;
    private String estado;
    private String mensaje;
    
    // Datos del comprador/titular
    private String nombreTitular;
    private String numeroTarjetaEnmascarado;
    
    // Constructores
    public VentaResponse() {}
    
    public VentaResponse(Factura factura, Pago pago) {
        this.idFactura = factura.getIdFactura();
        this.nombreUsuario = factura.getUsuario().getNombre();
        this.fecha = factura.getFecha();
        this.subtotal = factura.getSubtotal();
        this.iva = factura.getIva();
        this.total = factura.getTotal();
        this.metodoPago = pago != null ? pago.getMetodoPago() : "No especificado";
        this.items = factura.getDetallesFactura().stream()
                .map(ItemFactura::new)
                .collect(Collectors.toList());
        this.estado = "EXITOSA";
        this.mensaje = "Venta procesada correctamente";
        
        // Datos del comprador/titular (si existen)
        if (pago != null) {
            this.nombreTitular = pago.getNombreTitular();
            this.numeroTarjetaEnmascarado = pago.getNumeroTarjeta();
        }
    }
    
    // Constructor para errores
    public VentaResponse(String estado, String mensaje) {
        this.estado = estado;
        this.mensaje = mensaje;
    }
    
    // Getters y Setters
    public Integer getIdFactura() {
        return idFactura;
    }
    
    public void setIdFactura(Integer idFactura) {
        this.idFactura = idFactura;
    }
    
    public String getNombreUsuario() {
        return nombreUsuario;
    }
    
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
    
    public Date getFecha() {
        return fecha;
    }
    
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public BigDecimal getIva() {
        return iva;
    }
    
    public void setIva(BigDecimal iva) {
        this.iva = iva;
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    public String getMetodoPago() {
        return metodoPago;
    }
    
    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
    
    public List<ItemFactura> getItems() {
        return items;
    }
    
    public void setItems(List<ItemFactura> items) {
        this.items = items;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public String getMensaje() {
        return mensaje;
    }
    
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
    
    public String getNombreTitular() {
        return nombreTitular;
    }
    
    public void setNombreTitular(String nombreTitular) {
        this.nombreTitular = nombreTitular;
    }
    
    public String getNumeroTarjetaEnmascarado() {
        return numeroTarjetaEnmascarado;
    }
    
    public void setNumeroTarjetaEnmascarado(String numeroTarjetaEnmascarado) {
        this.numeroTarjetaEnmascarado = numeroTarjetaEnmascarado;
    }
    
    @Override
    public String toString() {
        return "VentaResponse{" +
                "idFactura=" + idFactura +
                ", nombreUsuario='" + nombreUsuario + '\'' +
                ", fecha=" + fecha +
                ", total=" + total +
                ", metodoPago='" + metodoPago + '\'' +
                ", nombreTitular='" + nombreTitular + '\'' +
                ", numeroTarjetaEnmascarado='" + numeroTarjetaEnmascarado + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
    
    // ==========================================
    // CLASE INTERNA: ItemFactura
    // ==========================================
    public static class ItemFactura {
        private Integer idProducto;
        private String descripcionProducto;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
        
        public ItemFactura() {}
        
        public ItemFactura(DetalleFactura detalle) {
            this.idProducto = detalle.getProducto().getIdProducto();
            this.descripcionProducto = detalle.getProducto().getDescripcion();
            this.cantidad = detalle.getCantidad();
            this.precioUnitario = detalle.getPrecioUnitario();
            this.subtotal = detalle.getSubtotal();
        }
        
        // Getters y Setters
        public Integer getIdProducto() {
            return idProducto;
        }
        
        public void setIdProducto(Integer idProducto) {
            this.idProducto = idProducto;
        }
        
        public String getDescripcionProducto() {
            return descripcionProducto;
        }
        
        public void setDescripcionProducto(String descripcionProducto) {
            this.descripcionProducto = descripcionProducto;
        }
        
        public Integer getCantidad() {
            return cantidad;
        }
        
        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }
        
        public BigDecimal getPrecioUnitario() {
            return precioUnitario;
        }
        
        public void setPrecioUnitario(BigDecimal precioUnitario) {
            this.precioUnitario = precioUnitario;
        }
        
        public BigDecimal getSubtotal() {
            return subtotal;
        }
        
        public void setSubtotal(BigDecimal subtotal) {
            this.subtotal = subtotal;
        }
        
        @Override
        public String toString() {
            return "ItemFactura{" +
                    "idProducto=" + idProducto +
                    ", descripcionProducto='" + descripcionProducto + '\'' +
                    ", cantidad=" + cantidad +
                    ", precioUnitario=" + precioUnitario +
                    ", subtotal=" + subtotal +
                    '}';
        }
    }
}