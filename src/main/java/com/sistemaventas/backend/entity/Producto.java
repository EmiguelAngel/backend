package com.sistemaventas.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PRODUCTO")
public class Producto {
    
    @Id
    @Column(name = "IDPRODUCTO")
    private Integer idProducto;
    
    @Column(name = "CANTIDADDISPONIBLE")
    @Min(value = 0, message = "La cantidad disponible no puede ser negativa")
    private Integer cantidadDisponible;
    
    @Column(name = "PRECIOUNITARIO", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "El precio unitario debe ser mayor a 0")
    private BigDecimal precioUnitario;
    
    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 1024, message = "La descripción no puede exceder 1024 caracteres")
    @Column(name = "DESCRIPCION", length = 1024)
    private String descripcion;
    
    @NotBlank(message = "La categoría es obligatoria")
    @Size(max = 100, message = "La categoría no puede exceder 100 caracteres")
    @Column(name = "CATEGORIA", length = 100)
    private String categoria;
    
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DetalleFactura> detallesFactura = new ArrayList<>();
    
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inventario> inventarios = new ArrayList<>();
    
    // Constructores
    public Producto() {}
    
    public Producto(Integer idProducto, Integer cantidadDisponible, 
                   BigDecimal precioUnitario, String descripcion, String categoria) {
        this.idProducto = idProducto;
        this.cantidadDisponible = cantidadDisponible;
        this.precioUnitario = precioUnitario;
        this.descripcion = descripcion;
        this.categoria = categoria;
    }
    
    // Getters y Setters
    public Integer getIdProducto() {
        return idProducto;
    }
    
    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }
    
    public Integer getCantidadDisponible() {
        return cantidadDisponible;
    }
    
    public void setCantidadDisponible(Integer cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
    }
    
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
    
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    public List<DetalleFactura> getDetallesFactura() {
        return detallesFactura;
    }
    
    public void setDetallesFactura(List<DetalleFactura> detallesFactura) {
        this.detallesFactura = detallesFactura;
    }
    
    public List<Inventario> getInventarios() {
        return inventarios;
    }
    
    public void setInventarios(List<Inventario> inventarios) {
        this.inventarios = inventarios;
    }
    
    // Métodos de utilidad
    public boolean tieneStock() {
        return this.cantidadDisponible != null && this.cantidadDisponible > 0;
    }
    
    public boolean tieneStockSuficiente(int cantidadRequerida) {
        return this.cantidadDisponible != null && this.cantidadDisponible >= cantidadRequerida;
    }
    
    public void reducirStock(int cantidad) {
        if (this.cantidadDisponible != null && this.cantidadDisponible >= cantidad) {
            this.cantidadDisponible -= cantidad;
        } else {
            throw new RuntimeException("Stock insuficiente para el producto: " + this.descripcion);
        }
    }
    
    public void aumentarStock(int cantidad) {
        if (this.cantidadDisponible == null) {
            this.cantidadDisponible = cantidad;
        } else {
            this.cantidadDisponible += cantidad;
        }
    }
    
    @Override
    public String toString() {
        return "Producto{" +
                "idProducto=" + idProducto +
                ", cantidadDisponible=" + cantidadDisponible +
                ", precioUnitario=" + precioUnitario +
                ", descripcion='" + descripcion + '\'' +
                ", categoria='" + categoria + '\'' +
                '}';
    }
}