package com.sistemaventas.backend.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProductoRequest {
    
    private Integer idProducto;
    
    @Min(value = 0, message = "La cantidad disponible no puede ser negativa")
    private Integer cantidadDisponible;
    
    @DecimalMin(value = "0.0", message = "El precio unitario debe ser mayor a 0")
    private BigDecimal precioUnitario;
    
    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 1024, message = "La descripción no puede exceder 1024 caracteres")
    private String descripcion;
    
    @NotBlank(message = "La categoría es obligatoria")
    @Size(max = 100, message = "La categoría no puede exceder 100 caracteres")
    private String categoria;
    
    // Constructores
    public ProductoRequest() {}
    
    public ProductoRequest(Integer idProducto, Integer cantidadDisponible, 
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
    
    @Override
    public String toString() {
        return "ProductoRequest{" +
                "idProducto=" + idProducto +
                ", cantidadDisponible=" + cantidadDisponible +
                ", precioUnitario=" + precioUnitario +
                ", descripcion='" + descripcion + '\'' +
                ", categoria='" + categoria + '\'' +
                '}';
    }
}