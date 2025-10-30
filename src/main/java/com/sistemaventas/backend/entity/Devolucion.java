package com.sistemaventas.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "devolucion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Devolucion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iddevolucion")
    private Long idDevolucion;
    
    @ManyToOne
    @JoinColumn(name = "idfactura", nullable = false)
    private Factura factura;
    
    @Column(name = "payment_id", length = 100)
    private String paymentId;
    
    @Column(name = "refund_id", length = 100)
    private String refundId;
    
    @Column(name = "monto_devuelto", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoDevuelto;
    
    @Column(name = "motivo", length = 500)
    private String motivo;
    
    @Builder.Default
    @Column(name = "estado", length = 50)
    private String estado = "PENDIENTE"; // PENDIENTE, APROBADA, RECHAZADA
    
    @Builder.Default
    @Column(name = "fecha_devolucion")
    private LocalDateTime fechaDevolucion = LocalDateTime.now();
    
    @Column(name = "usuario_devolucion", length = 100)
    private String usuarioDevolucion;
}
