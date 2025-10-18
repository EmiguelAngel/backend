package com.sistemaventas.backend.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistemaventas.backend.entity.DetalleFactura;
import com.sistemaventas.backend.entity.Factura;
import com.sistemaventas.backend.repository.FacturaRepository;

@Service
@Transactional
public class FacturaService {
    
    @Autowired
    private FacturaRepository facturaRepository;
    
    // Guardar factura completa con detalles
    public Factura guardarFactura(Factura factura) {
        try {
            // Generar ID si no existe
            if (factura.getIdFactura() == null) {
                factura.setIdFactura(generarIdFactura());
            }
            
            // Asegurar que la fecha esté establecida
            if (factura.getFecha() == null) {
                factura.setFecha(new Date());
            }
            
            // Generar IDs para los detalles si no existen
            if (factura.getDetallesFactura() != null) {
                int contadorDetalle = 1;
                for (DetalleFactura detalle : factura.getDetallesFactura()) {
                    if (detalle.getIdDetalle() == null) {
                        detalle.setIdDetalle(generarIdDetalle() + contadorDetalle);
                        contadorDetalle++;
                    }
                    detalle.setFactura(factura); // Asegurar relación bidireccional
                }
            }
            
            // Calcular totales antes de guardar
            factura.calcularTotales();

            // Validar factura antes de guardar
            validarFactura(factura);
            
            return facturaRepository.save(factura);
            
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar factura: " + e.getMessage());
        }
    }
    
    // Obtener todas las facturas
    public List<Factura> obtenerTodasLasFacturas() {
        return facturaRepository.findAll();
    }
    
    // Buscar factura por ID
    public Optional<Factura> buscarPorId(Integer id) {
        return facturaRepository.findById(id);
    }
    
    // Obtener factura completa con detalles para PDF
    public Factura obtenerFacturaCompleta(Long id) {
        Optional<Factura> facturaOpt = facturaRepository.findById(id.intValue());
        if (facturaOpt.isPresent()) {
            Factura factura = facturaOpt.get();
            // Las relaciones deberían cargarse automáticamente por JPA
            // Si no, podrías forzar la carga con:
            // Hibernate.initialize(factura.getDetallesFactura());
            return factura;
        }
        return null;
    }
    
    // Buscar facturas por usuario
    public List<Factura> buscarPorUsuario(Integer idUsuario) {
        return facturaRepository.findByUsuarioId(idUsuario);
    }
    
    // Buscar facturas por fecha
    public List<Factura> buscarPorFecha(Date fecha) {
        return facturaRepository.findByFecha(fecha);
    }
    
    // Buscar facturas entre fechas
    public List<Factura> buscarEntreFechas(Date fechaInicio, Date fechaFin) {
        return facturaRepository.findByFechaBetween(fechaInicio, fechaFin);
    }
    
    // Buscar facturas por total mayor a
    public List<Factura> buscarPorTotalMayorA(BigDecimal total) {
        return facturaRepository.findByTotalGreaterThan(total);
    }
    
    // Eliminar factura
    public boolean eliminarFactura(Integer id) {
        if (facturaRepository.existsById(id)) {
            facturaRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    // Obtener total de ventas del día
    public BigDecimal obtenerTotalVentasDelDia(Date fecha) {
        return facturaRepository.sumTotalVentasByFecha(fecha);
    }
    
    // Obtener facturas de hoy
    public List<Factura> obtenerFacturasDeHoy() {
        Date hoy = new Date();
        return facturaRepository.findFacturasDeHoy(hoy);
    }
    
    // Generar reporte de ventas
    @SuppressWarnings("deprecation")
    public void generarReporteVentas(Date fechaInicio, Date fechaFin) {
        System.out.println("=== REPORTE DE VENTAS ===");
        System.out.println("Período: " + fechaInicio + " a " + fechaFin);
        
        List<Factura> facturas = buscarEntreFechas(fechaInicio, fechaFin);
        
        BigDecimal totalVentas = facturas.stream()
                .map(Factura::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalIVA = facturas.stream()
                .map(Factura::getIva)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        System.out.println("Número de facturas: " + facturas.size());
        System.out.println("Total ventas: $" + totalVentas);
        System.out.println("Total IVA: $" + totalIVA);
        System.out.println("Promedio por factura: $" + 
                (!facturas.isEmpty() ? totalVentas.divide(new BigDecimal(facturas.size()), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO));
        
        System.out.println("=== FIN REPORTE ===");
    }
    
    // Generar nuevo ID de factura
    private Integer generarIdFactura() {
        List<Factura> facturas = facturaRepository.findAll();
        if (facturas.isEmpty()) {
            return 1;
        }
        return facturas.stream()
                .mapToInt(Factura::getIdFactura)
                .max()
                .orElse(0) + 1;
    }
    
    // Generar nuevo ID de detalle (base)
    private Integer generarIdDetalle() {
        // En una implementación real, esto debería consultar la tabla DETALLEFACTURA
        // Por ahora, usar un número base y agregar contador
        return (int) (System.currentTimeMillis() % 10000);
    }
    
    // Validar factura antes de guardar
    private void validarFactura(Factura factura) {
        if (factura.getUsuario() == null) {
            throw new IllegalArgumentException("La factura debe tener un usuario asociado");
        }
        
        if (factura.getDetallesFactura() == null || factura.getDetallesFactura().isEmpty()) {
            throw new IllegalArgumentException("La factura debe tener al menos un detalle");
        }
        
        if (factura.getTotal() == null || factura.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El total de la factura debe ser mayor a 0");
        }
    }
}