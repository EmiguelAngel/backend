package com.sistemaventas.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class VentaRequest {
    
    @NotNull(message = "El ID del usuario es obligatorio")
    private Integer idUsuario;
    
    @NotEmpty(message = "La venta debe tener al menos un item")
    @Valid
    private List<ItemVenta> items;
    
    @NotNull(message = "Los datos de pago son obligatorios")
    @Valid
    private DatosPago datosPago;
    
    // Constructores
    public VentaRequest() {}
    
    public VentaRequest(Integer idUsuario, List<ItemVenta> items, DatosPago datosPago) {
        this.idUsuario = idUsuario;
        this.items = items;
        this.datosPago = datosPago;
    }
    
    // Getters y Setters
    public Integer getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public List<ItemVenta> getItems() {
        return items;
    }
    
    public void setItems(List<ItemVenta> items) {
        this.items = items;
    }
    
    public DatosPago getDatosPago() {
        return datosPago;
    }
    
    public void setDatosPago(DatosPago datosPago) {
        this.datosPago = datosPago;
    }
    
    @Override
    public String toString() {
        return "VentaRequest{" +
                "idUsuario=" + idUsuario +
                ", items=" + items +
                ", datosPago=" + datosPago +
                '}';
    }
    
    // ==========================================
    // CLASE INTERNA: ItemVenta
    // ==========================================
    public static class ItemVenta {
        
        @NotNull(message = "El ID del producto es obligatorio")
        private Integer idProducto;
        
        @NotNull(message = "La cantidad es obligatoria")
        private Integer cantidad;
        
        // Constructores
        public ItemVenta() {}
        
        public ItemVenta(Integer idProducto, Integer cantidad) {
            this.idProducto = idProducto;
            this.cantidad = cantidad;
        }
        
        // Getters y Setters
        public Integer getIdProducto() {
            return idProducto;
        }
        
        public void setIdProducto(Integer idProducto) {
            this.idProducto = idProducto;
        }
        
        public Integer getCantidad() {
            return cantidad;
        }
        
        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }
        
        @Override
        public String toString() {
            return "ItemVenta{" +
                    "idProducto=" + idProducto +
                    ", cantidad=" + cantidad +
                    '}';
        }
    }
    
    // ==========================================
    // CLASE INTERNA: DatosPago
    // ==========================================
    public static class DatosPago {
        
        @NotNull(message = "El método de pago es obligatorio")
        private String metodoPago;
        
        private String numeroTarjeta;
        private String nombreTitular;
        private String codigoSeguridad;
        private String mesVencimiento;
        private String anoVencimiento;
        
        // Constructores
        public DatosPago() {}
        
        public DatosPago(String metodoPago) {
            this.metodoPago = metodoPago;
        }
        
        // Getters y Setters
        public String getMetodoPago() {
            return metodoPago;
        }
        
        public void setMetodoPago(String metodoPago) {
            this.metodoPago = metodoPago;
        }
        
        public String getNumeroTarjeta() {
            return numeroTarjeta;
        }
        
        public void setNumeroTarjeta(String numeroTarjeta) {
            this.numeroTarjeta = numeroTarjeta;
        }
        
        public String getNombreTitular() {
            return nombreTitular;
        }
        
        public void setNombreTitular(String nombreTitular) {
            this.nombreTitular = nombreTitular;
        }
        
        public String getCodigoSeguridad() {
            return codigoSeguridad;
        }
        
        public void setCodigoSeguridad(String codigoSeguridad) {
            this.codigoSeguridad = codigoSeguridad;
        }
        
        public String getMesVencimiento() {
            return mesVencimiento;
        }
        
        public void setMesVencimiento(String mesVencimiento) {
            this.mesVencimiento = mesVencimiento;
        }
        
        public String getAnoVencimiento() {
            return anoVencimiento;
        }
        
        public void setAnoVencimiento(String anoVencimiento) {
            this.anoVencimiento = anoVencimiento;
        }
        
        @Override
        public String toString() {
            return "DatosPago{" +
                    "metodoPago='" + metodoPago + '\'' +
                    ", numeroTarjeta='" + (numeroTarjeta != null ? "****" + numeroTarjeta.substring(Math.max(0, numeroTarjeta.length() - 4)) : null) + '\'' +
                    ", nombreTitular='" + nombreTitular + '\'' +
                    '}';
        }
    }
}