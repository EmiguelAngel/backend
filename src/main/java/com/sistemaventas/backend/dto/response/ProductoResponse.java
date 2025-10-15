package com.sistemaventas.backend.dto.response;

import java.math.BigDecimal;

public class ProductoResponse {
    private Integer idProducto;
    private String descripcion;
    private BigDecimal precioUnitario;
    private Integer cantidadDisponible;
    private String categoria;

    // Constructors
    public ProductoResponse() {}

    public ProductoResponse(Integer idProducto, String descripcion, BigDecimal precioUnitario, 
                           Integer cantidadDisponible, String categoria) {
        this.idProducto = idProducto;
        this.descripcion = descripcion;
        this.precioUnitario = precioUnitario;
        this.cantidadDisponible = cantidadDisponible;
        this.categoria = categoria;
    }

    // Getters and Setters
    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Integer getCantidadDisponible() {
        return cantidadDisponible;
    }

    public void setCantidadDisponible(Integer cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}