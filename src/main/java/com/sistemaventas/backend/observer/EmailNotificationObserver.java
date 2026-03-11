package com.sistemaventas.backend.observer;

import com.sistemaventas.backend.entity.Producto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// ==========================================
// OBSERVER 1: Notificaciones por Email
// ==========================================
@Component
public class EmailNotificationObserver implements InventarioObserver {
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    @Override
    public void onStockChange(Producto producto, int stockAnterior, int nuevoStock) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("📧 [EMAIL] " + timestamp + " - Cambio de stock en " + producto.getDescripcion() + 
                          " (" + stockAnterior + " → " + nuevoStock + ")");
        
        // Aquí iría la lógica real para enviar email
        // Por ejemplo: emailService.sendStockChangeNotification(producto, stockAnterior, nuevoStock);
    }
    
    @Override
    public void onStockBajo(Producto producto, int stockActual) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("📧 [EMAIL ALERTA] " + timestamp + " - STOCK BAJO: " + producto.getDescripcion() + 
                          " (Quedan solo " + stockActual + " unidades)");
        
        // Enviar email a administradores
        enviarEmailStockBajo(producto, stockActual);
    }
    
    @Override
    public void onProductoAgotado(Producto producto) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("📧 [EMAIL CRÍTICO] " + timestamp + " - PRODUCTO AGOTADO: " + producto.getDescripcion());
        
        // Enviar email urgente
        enviarEmailProductoAgotado(producto);
    }
    
    @Override
    public void onProductoRestockado(Producto producto, int nuevoStock) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("📧 [EMAIL INFO] " + timestamp + " - Producto restockado: " + producto.getDescripcion() + 
                          " (Nuevo stock: " + nuevoStock + ")");
    }
    
    private void enviarEmailStockBajo(Producto producto, int stockActual) {
        // Simulación de envío de email
        System.out.println("   📤 Enviando email a: admin@sistemaventas.com");
        System.out.println("   📝 Asunto: [ALERTA] Stock bajo - " + producto.getDescripcion());
        System.out.println("   📄 Mensaje: El producto " + producto.getDescripcion() + 
                          " tiene stock bajo (" + stockActual + " unidades). Considere realizar pedido.");
    }
    
    private void enviarEmailProductoAgotado(Producto producto) {
        // Simulación de envío de email urgente
        System.out.println("   🚨 Enviando email URGENTE a: admin@sistemaventas.com, compras@sistemaventas.com");
        System.out.println("   📝 Asunto: [URGENTE] Producto agotado - " + producto.getDescripcion());
        System.out.println("   📄 Mensaje: El producto " + producto.getDescripcion() + 
                          " está completamente agotado. Acción inmediata requerida.");
    }
}