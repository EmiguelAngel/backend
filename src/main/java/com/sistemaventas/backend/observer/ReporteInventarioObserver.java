package com.sistemaventas.backend.observer;

import com.sistemaventas.backend.entity.Producto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// ==========================================
// OBSERVER 2: Reportes y Auditoría
// ==========================================
@Component
public class ReporteInventarioObserver implements InventarioObserver {
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    @Override
    public void onStockChange(Producto producto, int stockAnterior, int nuevoStock) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("📊 [REPORTE] " + timestamp + " - Registrando cambio de inventario:");
        System.out.println("   🏷️  Producto: " + producto.getDescripcion() + " (ID: " + producto.getIdProducto() + ")");
        System.out.println("   📦 Stock: " + stockAnterior + " → " + nuevoStock);
        System.out.println("   📈 Variación: " + (nuevoStock - stockAnterior));
        
        // Aquí se guardaría en una tabla de auditoría
        registrarEnAuditoria(producto, stockAnterior, nuevoStock);
    }
    
    @Override
    public void onStockBajo(Producto producto, int stockActual) {
        System.out.println("📊 [REPORTE ALERTA] Agregando a reporte de stock bajo:");
        System.out.println("   🏷️  Producto: " + producto.getDescripcion());
        System.out.println("   📦 Stock actual: " + stockActual);
        System.out.println("   ⚠️  Estado: REQUIERE RESTOCK");
        
        agregarAReporteStockBajo(producto, stockActual);
    }
    
    @Override
    public void onProductoAgotado(Producto producto) {
        System.out.println("📊 [REPORTE CRÍTICO] Agregando a reporte de productos agotados:");
        System.out.println("   🏷️  Producto: " + producto.getDescripcion());
        System.out.println("   🚨 Estado: AGOTADO");
        System.out.println("   📅 Fecha: " + LocalDateTime.now().format(formatter));
        
        agregarAReporteProductosAgotados(producto);
    }
    
    @Override
    public void onProductoRestockado(Producto producto, int nuevoStock) {
        System.out.println("📊 [REPORTE INFO] Producto restockado exitosamente:");
        System.out.println("   🏷️  Producto: " + producto.getDescripcion());
        System.out.println("   📦 Nuevo stock: " + nuevoStock);
        System.out.println("   ✅ Estado: DISPONIBLE");
    }
    
    private void registrarEnAuditoria(Producto producto, int stockAnterior, int nuevoStock) {
        // Simulación de registro en base de datos de auditoría
        System.out.println("   💾 Guardando en tabla AUDITORIA_INVENTARIO...");
        System.out.println("   🏷️  Producto: " + producto.getDescripcion() + " (ID: " + producto.getIdProducto() + ")");
        System.out.println("   📦 Stock anterior: " + stockAnterior);
        System.out.println("   📦 Nuevo stock: " + nuevoStock);
        // Aquí iría: auditoriaRepository.save(new AuditoriaInventario(...));
    }
    
    private void agregarAReporteStockBajo(Producto producto, int stockActual) {
        // Simulación de agregar a reporte
        System.out.println("   📋 Agregando a REPORTE_STOCK_BAJO...");
        System.out.println("   🏷️  Producto: " + producto.getDescripcion() + " (ID: " + producto.getIdProducto() + ")");
        System.out.println("   📦 Stock actual: " + stockActual);
        // Aquí iría la lógica para generar reportes
    }
    
    private void agregarAReporteProductosAgotados(Producto producto) {
        // Simulación de agregar a reporte crítico
        System.out.println("   🚨 Agregando a REPORTE_PRODUCTOS_AGOTADOS...");
        System.out.println("   🏷️  Producto: " + producto.getDescripcion() + " (ID: " + producto.getIdProducto() + ")");
        // Aquí iría la lógica para reportes críticos
    }
}