package com.sistemaventas.backend.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "DETALLEFACTURA")
public class DetalleFactura {
    
    @Id
    @Column(name = "IDDETALLE")
    private Integer idDetalle;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDPRODUCTO", referencedColumnName = "IDPRODUCTO")
    @NotNull(message = "El producto es obligatorio")
    @JsonIgnore
    private Producto producto;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDFACTURA", referencedColumnName = "IDFACTURA")
    @NotNull(message = "La factura es obligatoria")
    @JsonIgnore
    private Factura factura;
    
    @Column(name = "CANTIDAD")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    @NotNull(message = "La cantidad es obligatoria")
    private Integer cantidad;
    
    @Column(name = "PRECIOUNITARIO", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "El precio unitario debe ser mayor a 0")
    @NotNull(message = "El precio unitario es obligatorio")
    private BigDecimal precioUnitario;
    
    @Column(name = "SUBTOTAL", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "El subtotal debe ser mayor a 0")
    private BigDecimal subtotal;
    
    // Constructores
    public DetalleFactura() {
        // Constructor vacío
    }

    public DetalleFactura(Integer idDetalle, Producto producto, Factura factura,
                         Integer cantidad, BigDecimal precioUnitario) {
        this.idDetalle = idDetalle;
        this.producto = producto;
        this.factura = factura;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        // Calcular subtotal sólo si ambos valores existen
        if (cantidad != null && precioUnitario != null) {
            this.subtotal = precioUnitario.multiply(new BigDecimal(cantidad));
        }
    }
    
    // Getters y Setters
    public Integer getIdDetalle() {
        return idDetalle;
    }
    
    public void setIdDetalle(Integer idDetalle) {
        this.idDetalle = idDetalle;
    }
    
    public Producto getProducto() {
        return producto;
    }
    
    public void setProducto(Producto producto) {
        this.producto = producto;
    }
    
    public Factura getFactura() {
        return factura;
    }
    
    public void setFactura(Factura factura) {
        this.factura = factura;
    }
    
    public Integer getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
        this.subtotal = this.precioUnitario.multiply(new BigDecimal(cantidad));
    }
    
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
    
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
        this.subtotal = precioUnitario.multiply(new BigDecimal(this.cantidad));
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    // Método de utilidad para calcular subtotal automáticamente
    public void calcularSubtotal() {
        this.subtotal = this.precioUnitario.multiply(new BigDecimal(this.cantidad));
    }
    
    @Override
    public String toString() {
        return "DetalleFactura{" +
                "idDetalle=" + idDetalle +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", subtotal=" + subtotal +
                '}';
    }
}