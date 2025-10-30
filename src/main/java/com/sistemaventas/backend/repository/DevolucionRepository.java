package com.sistemaventas.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistemaventas.backend.entity.Devolucion;

@Repository
public interface DevolucionRepository extends JpaRepository<Devolucion, Long> {
    
    /**
     * Busca todas las devoluciones de una factura específica
     */
    List<Devolucion> findByFactura_IdFactura(Integer idFactura);
    
    /**
     * Busca una devolución por el ID de pago de Mercado Pago
     */
    Optional<Devolucion> findByPaymentId(String paymentId);
    
    /**
     * Busca todas las devoluciones por estado (PENDIENTE, APROBADA, RECHAZADA)
     */
    List<Devolucion> findByEstado(String estado);
    
    /**
     * Busca todas las devoluciones de un usuario específico
     */
    List<Devolucion> findByUsuarioDevolucion(String usuarioDevolucion);
    
    /**
     * Busca una devolución por el refund_id de Mercado Pago
     */
    Optional<Devolucion> findByRefundId(String refundId);
}
