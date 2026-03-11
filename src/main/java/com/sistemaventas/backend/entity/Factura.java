package com.sistemaventas.backend.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "FACTURA")
/**
 * Entity (Model in MVC): representa la factura/venta en la capa de dominio.
 * Contiene detalles (detalles de factura), totales y relación con pago/usuario.
 */
public class Factura {
    
    @Id
    @Column(name = "IDFACTURA")
    private Integer idFactura;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IDUSUARIO", referencedColumnName = "IDUSUARIO")
    @NotNull(message = "El usuario es obligatorio")
    @JsonIgnore
    private Usuario usuario;
    
    @Column(name = "IDPAGO")
    private Integer idPago;
    
    @Column(name = "FECHA")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull(message = "La fecha es obligatoria")
    private Date fecha;
    
    @Column(name = "SUBTOTAL", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "El subtotal debe ser mayor a 0")
    private BigDecimal subtotal;
    
    @Column(name = "IVA", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "El IVA debe ser mayor o igual a 0")
    private BigDecimal iva;
    
    @Column(name = "TOTAL", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "El total debe ser mayor a 0")
    private BigDecimal total;
    
    @Column(name = "payment_id", length = 100)
    private String paymentId; // ID del pago en Mercado Pago
    
    @Column(name = "devuelta")
    private Boolean devuelta = false; // Indica si la factura fue devuelta
    
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleFactura> detallesFactura = new ArrayList<>();
    
    @OneToOne(mappedBy = "factura", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Pago pago;
    
    // Constructores
    public Factura() {
        this.fecha = new Date(); // Fecha actual por defecto
    }
    
    public Factura(Integer idFactura, Usuario usuario, Date fecha, 
                   BigDecimal subtotal, BigDecimal iva, BigDecimal total) {
        this.idFactura = idFactura;
        this.usuario = usuario;
        this.fecha = fecha != null ? fecha : new Date();
        this.subtotal = subtotal;
        this.iva = iva;
        this.total = total;
    }
    
    // Getters y Setters
    public Integer getIdFactura() {
        return idFactura;
    }
    
    public void setIdFactura(Integer idFactura) {
        this.idFactura = idFactura;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    public Integer getIdPago() {
        return idPago;
    }
    
    public void setIdPago(Integer idPago) {
        this.idPago = idPago;
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
    
    public List<DetalleFactura> getDetallesFactura() {
        return detallesFactura;
    }
    
    public void setDetallesFactura(List<DetalleFactura> detallesFactura) {
        this.detallesFactura = detallesFactura;
    }
    
    public Pago getPago() {
        return pago;
    }
    
    public void setPago(Pago pago) {
        this.pago = pago;
    }
    
    public String getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
    
    public Boolean getDevuelta() {
        return devuelta;
    }
    
    public void setDevuelta(Boolean devuelta) {
        this.devuelta = devuelta;
    }
    
    // Métodos de utilidad
    public void calcularTotales() {
        this.subtotal = detallesFactura.stream()
                .map(DetalleFactura::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.iva = this.subtotal.multiply(new BigDecimal("0.19")); // IVA 19%
        this.total = this.subtotal.add(this.iva);
    }
    
    public void agregarDetalle(DetalleFactura detalle) {
        this.detallesFactura.add(detalle);
        detalle.setFactura(this);
        calcularTotales();
    }
    
    @Override
    public String toString() {
        return "Factura{" +
                "idFactura=" + idFactura +
                ", fecha=" + fecha +
                ", subtotal=" + subtotal +
                ", iva=" + iva +
                ", total=" + total +
                '}';
    }
}