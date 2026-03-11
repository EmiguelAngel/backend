package com.sistemaventas.backend.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sistemaventas.backend.entity.Pago;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {
    
    // Buscar pagos por método de pago
    List<Pago> findByMetodoPago(String metodoPago);
    
    // Buscar pagos por método de pago (ignorando mayúsculas)
    List<Pago> findByMetodoPagoIgnoreCase(String metodoPago);
    
    // Buscar pagos por monto mayor a
    List<Pago> findByMontoGreaterThan(BigDecimal monto);
    
    // Buscar pagos por monto menor a
    List<Pago> findByMontoLessThan(BigDecimal monto);
    
    // Buscar pagos por rango de monto
    List<Pago> findByMontoBetween(BigDecimal montoMin, BigDecimal montoMax);
    
    // Obtener métodos de pago únicos
    @Query("SELECT DISTINCT p.metodoPago FROM Pago p ORDER BY p.metodoPago")
    List<String> findDistinctMetodosPago();
    
    // Sumar total de pagos por método
    @Query("SELECT SUM(p.monto) FROM Pago p WHERE p.metodoPago = :metodoPago")
    BigDecimal sumMontoByMetodoPago(@Param("metodoPago") String metodoPago);
    
    // Contar pagos por método
    @Query("SELECT COUNT(p) FROM Pago p WHERE p.metodoPago = :metodoPago")
    Long countByMetodoPago(@Param("metodoPago") String metodoPago);
    
    // Obtener total de todos los pagos
    @Query("SELECT SUM(p.monto) FROM Pago p")
    BigDecimal sumTotalPagos();
    
    // Buscar pagos de una factura específica
    @Query("SELECT p FROM Pago p WHERE p.factura.idFactura = :idFactura")
    List<Pago> findByFacturaId(@Param("idFactura") Integer idFactura);
}