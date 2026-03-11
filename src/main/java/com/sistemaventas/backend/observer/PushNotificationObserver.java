package com.sistemaventas.backend.observer;

import com.sistemaventas.backend.entity.Producto;
import org.springframework.stereotype.Component;

// ==========================================
// OBSERVER 3: Notificaciones Push/SMS
// ==========================================
@Component
public class PushNotificationObserver implements InventarioObserver {
    
    @Override
    public void onStockChange(Producto producto, int stockAnterior, int nuevoStock) {
        // Solo notificar cambios significativos
        int diferencia = Math.abs(nuevoStock - stockAnterior);
        if (diferencia >= 10) {
            System.out.println("📱 [PUSH] Cambio significativo de stock: " + producto.getDescripcion() + 
                              " (Δ: " + (nuevoStock - stockAnterior) + ")");
        }
    }
    
    @Override
    public void onStockBajo(Producto producto, int stockActual) {
        System.out.println("📱 [PUSH ALERTA] Stock bajo en " + producto.getDescripcion() + 
                          " (" + stockActual + " unidades)");
        enviarNotificacionPush("Stock Bajo", "⚠️ " + producto.getDescripcion() + " tiene stock bajo");
    }
    
    @Override
    public void onProductoAgotado(Producto producto) {
        System.out.println("📱 [PUSH CRÍTICO] Producto agotado: " + producto.getDescripcion());
        enviarNotificacionPush("Producto Agotado", "🚨 " + producto.getDescripcion() + " está agotado");
        enviarSMS("Producto agotado: " + producto.getDescripcion());
    }
    
    @Override
    public void onProductoRestockado(Producto producto, int nuevoStock) {
        System.out.println("📱 [PUSH INFO] ✅ " + producto.getDescripcion() + " restockado (" + nuevoStock + " unidades)");
        enviarNotificacionPush("Restock Exitoso", "✅ " + producto.getDescripcion() + " disponible nuevamente");
    }
    
    private void enviarNotificacionPush(String titulo, String mensaje) {
        System.out.println("   📱 Push → " + titulo + ": " + mensaje);
        // Aquí iría la integración con Firebase, OneSignal, etc.
    }
    
    private void enviarSMS(String mensaje) {
        System.out.println("   📲 SMS → +573187425471: " + mensaje);
        // Aquí iría la integración con Twilio, AWS SNS, etc.
    }
}