package com.sistemaventas.backend.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sistemaventas.backend.entity.Factura;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Integer> {
    
    // Buscar facturas por usuario
    @Query("SELECT f FROM Factura f WHERE f.usuario.idUsuario = :idUsuario")
    List<Factura> findByUsuarioId(@Param("idUsuario") Integer idUsuario);
    
    // Buscar facturas por fecha exacta
    List<Factura> findByFecha(Date fecha);
    
    // Buscar facturas entre fechas
    List<Factura> findByFechaBetween(Date fechaInicio, Date fechaFin);
    
    // Buscar facturas por total mayor a
    List<Factura> findByTotalGreaterThan(BigDecimal total);
    
    // Buscar facturas por total menor a
    List<Factura> findByTotalLessThan(BigDecimal total);
    
    // Buscar facturas por rango de total
    List<Factura> findByTotalBetween(BigDecimal totalMin, BigDecimal totalMax);
    
    // Buscar facturas de hoy
    @Query("SELECT f FROM Factura f WHERE DATE(f.fecha) = DATE(:fecha)")
    List<Factura> findFacturasDeHoy(@Param("fecha") Date fecha);
    
    // Buscar facturas de un mes específico
    @Query("SELECT f FROM Factura f WHERE MONTH(f.fecha) = :mes AND YEAR(f.fecha) = :ano")
    List<Factura> findFacturasPorMes(@Param("mes") int mes, @Param("ano") int ano);
    
    // Sumar total de ventas por fecha
    @Query("SELECT SUM(f.total) FROM Factura f WHERE DATE(f.fecha) = DATE(:fecha)")
    BigDecimal sumTotalVentasByFecha(@Param("fecha") Date fecha);
    
    // Sumar total de ventas entre fechas
    @Query("SELECT SUM(f.total) FROM Factura f WHERE f.fecha BETWEEN :fechaInicio AND :fechaFin")
    BigDecimal sumTotalVentasBetweenFechas(@Param("fechaInicio") Date fechaInicio, @Param("fechaFin") Date fechaFin);
    
    // Contar facturas por usuario
    @Query("SELECT COUNT(f) FROM Factura f WHERE f.usuario.idUsuario = :idUsuario")
    Long countByUsuarioId(@Param("idUsuario") Integer idUsuario);
    
    // Obtener facturas ordenadas por fecha descendente
    List<Factura> findAllByOrderByFechaDesc();
    
    // Obtener facturas ordenadas por total descendente
    List<Factura> findAllByOrderByTotalDesc();
    
    // Buscar facturas con IVA mayor a
    List<Factura> findByIvaGreaterThan(BigDecimal iva);
    
    // Obtener promedio de ventas
    @Query("SELECT AVG(f.total) FROM Factura f")
    BigDecimal findPromedioVentas();
    
    // Buscar facturas con productos específicos
    @Query("SELECT DISTINCT f FROM Factura f JOIN f.detallesFactura d WHERE d.producto.idProducto = :idProducto")
    List<Factura> findFacturasConProducto(@Param("idProducto") Integer idProducto);
    
    // Obtener facturas con mayor número de items
    @Query("SELECT f FROM Factura f ORDER BY SIZE(f.detallesFactura) DESC")
    List<Factura> findFacturasConMasItems();
    
    // Buscar facturas por método de pago
    @Query("SELECT f FROM Factura f WHERE f.pago.metodoPago = :metodoPago")
    List<Factura> findByMetodoPago(@Param("metodoPago") String metodoPago);
}