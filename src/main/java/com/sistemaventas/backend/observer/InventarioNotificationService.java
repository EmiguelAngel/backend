package com.sistemaventas.backend.observer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sistemaventas.backend.entity.Producto;

@Service
public class InventarioNotificationService implements InventarioSubject {

    public static int getSTOCK_MINIMO_DEFAULT() {
        return STOCK_MINIMO_DEFAULT;
    }
    
    private final List<InventarioObserver> observadores = new ArrayList<>();
    private static final int STOCK_MINIMO_DEFAULT = 10;
    private static final int STOCK_CRITICO = 5;
    
    // Constructor que registra automáticamente los observadores
    public InventarioNotificationService(List<InventarioObserver> observers) {
        this.observadores.addAll(observers);
        System.out.println("InventarioNotificationService inicializado con " + observers.size() + " observadores");
    }
    
    @Override
    public void agregarObservador(InventarioObserver observer) {
        if (!observadores.contains(observer)) {
            observadores.add(observer);
            System.out.println("Observador agregado: " + observer.getClass().getSimpleName());
        }
    }
    
    @Override
    public void eliminarObservador(InventarioObserver observer) {
        observadores.remove(observer);
        System.out.println("Observador eliminado: " + observer.getClass().getSimpleName());
    }
    
    @Override
    public void notificarCambioStock(Producto producto, int stockAnterior, int nuevoStock) {
        System.out.println("=== NOTIFICANDO CAMBIO DE STOCK ===");
        System.out.println("Producto: " + producto.getDescripcion());
        System.out.println("Stock anterior: " + stockAnterior + " -> Nuevo stock: " + nuevoStock);
        
        for (InventarioObserver observer : observadores) {
            try {
                observer.onStockChange(producto, stockAnterior, nuevoStock);
            } catch (Exception e) {
                System.err.println("Error en observador " + observer.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
        
        // Verificar condiciones especiales
        if (nuevoStock == 0) {
            notificarProductoAgotado(producto);
        } else if (nuevoStock <= STOCK_CRITICO) {
            notificarStockBajo(producto, nuevoStock);
        } else if (stockAnterior <= STOCK_CRITICO && nuevoStock > STOCK_CRITICO) {
            notificarProductoRestockado(producto, nuevoStock);
        }
    }
    
    @Override
    public void notificarStockBajo(Producto producto, int stockActual) {
        System.out.println("⚠️  ALERTA: STOCK BAJO - " + producto.getDescripcion() + " (Stock: " + stockActual + ")");
        
        for (InventarioObserver observer : observadores) {
            try {
                observer.onStockBajo(producto, stockActual);
            } catch (Exception e) {
                System.err.println("Error en observador " + observer.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }
    
    @Override
    public void notificarProductoAgotado(Producto producto) {
        System.out.println("🚨 CRÍTICO: PRODUCTO AGOTADO - " + producto.getDescripcion());
        
        for (InventarioObserver observer : observadores) {
            try {
                observer.onProductoAgotado(producto);
            } catch (Exception e) {
                System.err.println("Error en observador " + observer.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }
    
    @Override
    public void notificarProductoRestockado(Producto producto, int nuevoStock) {
        System.out.println("✅ RESTOCKADO: " + producto.getDescripcion() + " (Nuevo stock: " + nuevoStock + ")");
        
        for (InventarioObserver observer : observadores) {
            try {
                observer.onProductoRestockado(producto, nuevoStock);
            } catch (Exception e) {
                System.err.println("Error en observador " + observer.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }
    
    // Método público para ser usado por los servicios
    public void procesarCambioStock(Producto producto, int stockAnterior) {
        int nuevoStock = producto.getCantidadDisponible();
        notificarCambioStock(producto, stockAnterior, nuevoStock);
    }
    
    // Método para obtener información de observadores registrados
    public List<String> obtenerObservadoresRegistrados() {
        return observadores.stream()
                .map(obs -> obs.getClass().getSimpleName())
                .toList();
    }
    
    // Método para demostrar el patrón Observer
    public void demostrarPatronObserver() {
        System.out.println("=== DEMOSTRACIÓN PATRÓN OBSERVER ===");
        System.out.println("Observadores registrados: " + obtenerObservadoresRegistrados());
        System.out.println("Total observadores: " + observadores.size());
        
        // Simular cambios de stock
        Producto productoDemo = new Producto();
        productoDemo.setIdProducto(999);
        productoDemo.setDescripcion("Producto Demo Observer");
        productoDemo.setCategoria("Demo");
        productoDemo.setCantidadDisponible(15);
        
        // Simular reducción gradual de stock
        notificarCambioStock(productoDemo, 20, 15);
        
        productoDemo.setCantidadDisponible(8);
        notificarCambioStock(productoDemo, 15, 8);
        
        productoDemo.setCantidadDisponible(3);
        notificarCambioStock(productoDemo, 8, 3);
        
        productoDemo.setCantidadDisponible(0);
        notificarCambioStock(productoDemo, 3, 0);
        
        // Simular restock
        productoDemo.setCantidadDisponible(25);
        notificarCambioStock(productoDemo, 0, 25);
        
        System.out.println("=== FIN DEMOSTRACIÓN ===");
    }

    public List<InventarioObserver> getObservadores() {
        return observadores;
    }
}