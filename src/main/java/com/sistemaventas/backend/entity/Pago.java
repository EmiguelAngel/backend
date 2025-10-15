package com.sistemaventas.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// ==========================================
// ENTIDAD PAGO
// ==========================================
@SuppressWarnings("")
@Entity
@Table(name = "PAGO")
public class Pago {
    
    @Id
    @Column(name = "IDPAGO")
    private Integer idPago;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDFACTURA", referencedColumnName = "IDFACTURA")
    @NotNull(message = "La factura es obligatoria")
    @JsonIgnore
    private Factura factura;
    
    @NotBlank(message = "El método de pago es obligatorio")
    @Size(max = 256, message = "El método de pago no puede exceder 256 caracteres")
    @Column(name = "METODOPAGO", length = 256)
    private String metodoPago;
    
    @Column(name = "MONTO", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "El monto debe ser mayor a 0")
    @NotNull(message = "El monto es obligatorio")
    private BigDecimal monto;
    
    // Constructores
    public Pago() {}
    
    public Pago(Integer idPago, Factura factura, String metodoPago, BigDecimal monto) {
        this.idPago = idPago;
        this.factura = factura;
        this.metodoPago = metodoPago;
        this.monto = monto;
    }
    
    // Getters y Setters
    public Integer getIdPago() {
        return idPago;
    }
    
    public void setIdPago(Integer idPago) {
        this.idPago = idPago;
    }
    
    public Factura getFactura() {
        return factura;
    }
    
    public void setFactura(Factura factura) {
        this.factura = factura;
    }
    
    public String getMetodoPago() {
        return metodoPago;
    }
    
    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
    
    public BigDecimal getMonto() {
        return monto;
    }
    
    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
    
    @Override
    public String toString() {
        return "Pago{" +
                "idPago=" + idPago +
                ", metodoPago='" + metodoPago + '\'' +
                ", monto=" + monto +
                '}';
    }
}