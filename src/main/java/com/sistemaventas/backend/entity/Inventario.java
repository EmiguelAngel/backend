package com.sistemaventas.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

@Entity
@Table(name = "INVENTARIO")
public class Inventario {
    
    @Id
    @Column(name = "IDINVENTARIO")
    private Integer idInventario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDPRODUCTO", referencedColumnName = "IDPRODUCTO")
    @NotNull(message = "El producto es obligatorio")
    private Producto producto;
    
    @Column(name = "FECHAACTUALIZACION")
    @Temporal(TemporalType.DATE)
    @NotNull(message = "La fecha de actualización es obligatoria")
    private Date fechaActualizacion;
    
    @Column(name = "CANTIDADDISPONIBLE")
    @Min(value = 0, message = "La cantidad disponible no puede ser negativa")
    private Integer cantidadDisponible;
    
    // Constructores
    public Inventario() {
        this.fechaActualizacion = new Date(); // Fecha actual por defecto
    }
    
    public Inventario(Integer idInventario, Producto producto, 
                     Date fechaActualizacion, Integer cantidadDisponible) {
        this.idInventario = idInventario;
        this.producto = producto;
        this.fechaActualizacion = fechaActualizacion != null ? fechaActualizacion : new Date();
        this.cantidadDisponible = cantidadDisponible;
    }
    
    // Getters y Setters
    public Integer getIdInventario() {
        return idInventario;
    }
    
    public void setIdInventario(Integer idInventario) {
        this.idInventario = idInventario;
    }
    
    public Producto getProducto() {
        return producto;
    }
    
    public void setProducto(Producto producto) {
        this.producto = producto;
    }
    
    public Date getFechaActualizacion() {
        return fechaActualizacion;
    }
    
    public void setFechaActualizacion(Date fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
    
    public Integer getCantidadDisponible() {
        return cantidadDisponible;
    }
    
    public void setCantidadDisponible(Integer cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
        this.fechaActualizacion = new Date(); // Actualizar fecha cuando cambie la cantidad
    }
    
    // Métodos de utilidad
    public void actualizarCantidad(Integer nuevaCantidad) {
        this.cantidadDisponible = nuevaCantidad;
        this.fechaActualizacion = new Date();
    }
    
    public void reducirInventario(Integer cantidad) {
        if (this.cantidadDisponible != null && this.cantidadDisponible >= cantidad) {
            this.cantidadDisponible -= cantidad;
            this.fechaActualizacion = new Date();
        } else {
            throw new RuntimeException("Inventario insuficiente");
        }
    }
    
    public void aumentarInventario(Integer cantidad) {
        if (this.cantidadDisponible == null) {
            this.cantidadDisponible = cantidad;
        } else {
            this.cantidadDisponible += cantidad;
        }
        this.fechaActualizacion = new Date();
    }
    
    @Override
    public String toString() {
        return "Inventario{" +
                "idInventario=" + idInventario +
                ", fechaActualizacion=" + fechaActualizacion +
                ", cantidadDisponible=" + cantidadDisponible +
                '}';
    }
}