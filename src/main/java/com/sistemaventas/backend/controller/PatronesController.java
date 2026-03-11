package com.sistemaventas.backend.controller;

import com.sistemaventas.backend.dto.request.ProductoRequest;
import com.sistemaventas.backend.dto.request.VentaRequest;
import com.sistemaventas.backend.dto.response.VentaResponse;
import com.sistemaventas.backend.entity.Producto;
import com.sistemaventas.backend.facade.VentasFacade;
import com.sistemaventas.backend.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patrones")
@CrossOrigin(origins = "http://localhost:4200")
public class PatronesController {
    
    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private VentasFacade ventasFacade;
    
    /**
     * GET /api/patrones/demo-completa
     * Demostración completa de los 3 patrones de diseño trabajando juntos
     */
    @GetMapping("/demo-completa")
    public ResponseEntity<Map<String, Object>> demostracionCompleta() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            System.out.println("🎯 === DEMOSTRACIÓN COMPLETA DE PATRONES DE DISEÑO ===");
            
            // 1. PATRÓN FACTORY METHOD
            resultado.put("factory_pattern", demostrarFactoryMethod());
            
            // 2. PATRÓN OBSERVER  
            resultado.put("observer_pattern", demostrarObserverPattern());
            
            // 3. PATRÓN FACADE
            resultado.put("facade_pattern", demostrarFacadePattern());
            
            resultado.put("estado", "EXITOSO");
            resultado.put("mensaje", "Demostración completa de los 3 patrones ejecutada correctamente");
            
            System.out.println("🎉 === DEMOSTRACIÓN COMPLETA FINALIZADA ===");
            
            return ResponseEntity.ok(resultado);
            
        } catch (Exception e) {
            resultado.put("estado", "ERROR");
            resultado.put("mensaje", "Error en la demostración: " + e.getMessage());
            return ResponseEntity.status(500).body(resultado);
        }
    }
    
    /**
     * GET /api/patrones/factory
     * Demostrar solo el patrón Factory Method
     */
    @GetMapping("/factory")
    public ResponseEntity<Map<String, Object>> demoFactoryMethod() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            Map<String, Object> factory = demostrarFactoryMethod();
            resultado.putAll(factory);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            resultado.put("estado", "ERROR");
            resultado.put("mensaje", e.getMessage());
            return ResponseEntity.status(500).body(resultado);
        }
    }
    
    /**
     * GET /api/patrones/observer
     * Demostrar solo el patrón Observer
     */
    @GetMapping("/observer")
    public ResponseEntity<Map<String, Object>> demoObserverPattern() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            Map<String, Object> observer = demostrarObserverPattern();
            resultado.putAll(observer);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            resultado.put("estado", "ERROR");
            resultado.put("mensaje", e.getMessage());
            return ResponseEntity.status(500).body(resultado);
        }
    }
    
    /**
     * GET /api/patrones/facade
     * Demostrar solo el patrón Facade
     */
    @GetMapping("/facade")
    public ResponseEntity<Map<String, Object>> demoFacadePattern() {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            Map<String, Object> facade = demostrarFacadePattern();
            resultado.putAll(facade);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            resultado.put("estado", "ERROR");
            resultado.put("mensaje", e.getMessage());
            return ResponseEntity.status(500).body(resultado);
        }
    }
    
    /**
     * POST /api/patrones/venta-completa
     * Procesar una venta real usando todos los patrones
     */
    @PostMapping("/venta-completa")
    public ResponseEntity<VentaResponse> ventaCompletaConPatrones(@RequestBody VentaRequest ventaRequest) {
        try {
            System.out.println("🛒 === PROCESANDO VENTA CON TODOS LOS PATRONES ===");
            
            // Esto usa automáticamente:
            // - Factory Method (si se crean productos nuevos)
            // - Observer (para notificaciones de stock)
            // - Facade (para coordinar toda la operación)
            
            VentaResponse response = ventasFacade.procesarVenta(ventaRequest);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            VentaResponse errorResponse = new VentaResponse("ERROR", "Error procesando venta: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    // ==========================================
    // MÉTODOS PRIVADOS PARA CADA PATRÓN
    // ==========================================
    
    @SuppressWarnings("UnnecessaryTemporaryOnConversionFromString")
    private Map<String, Object> demostrarFactoryMethod() {
        Map<String, Object> resultado = new HashMap<>();
        List<Map<String, Object>> productosCreados = new ArrayList<>();
        
        System.out.println("🏭 DEMOSTRANDO FACTORY METHOD PATTERN:");
        
        try {
            // Crear productos usando diferentes factories
            String[][] productosDemo = {
                {"Arroz Integral Premium", "2800", "120", "granos"},
                {"Aceite de Coco Orgánico", "5500", "60", "aceites"},
                {"Yogurt Natural", "3200", "40", "lácteos"},
                {"Pan Artesanal", "1800", "25", "panadería"},
                {"Miel de Abeja", "8500", "15", "endulzantes"}
            };
            
            for (String[] prod : productosDemo) {
                ProductoRequest request = new ProductoRequest();
                request.setDescripcion(prod[0]);
                request.setPrecioUnitario(new BigDecimal(prod[1]));
                request.setCantidadDisponible(Integer.parseInt(prod[2]));
                request.setCategoria(prod[3]);
                
                Producto productoCreado = productoService.crearProducto(request);
                
                Map<String, Object> prodInfo = new HashMap<>();
                prodInfo.put("id", productoCreado.getIdProducto());
                prodInfo.put("descripcion", productoCreado.getDescripcion());
                prodInfo.put("categoria", productoCreado.getCategoria());
                prodInfo.put("precio", productoCreado.getPrecioUnitario());
                prodInfo.put("factory_usado", prod[3] + "ProductoFactory");
                
                productosCreados.add(prodInfo);
                
                System.out.println("   ✓ " + prod[3] + "Factory creó: " + productoCreado.getDescripcion());
            }
            
            resultado.put("patron", "Factory Method");
            resultado.put("productos_creados", productosCreados);
            resultado.put("factories_utilizadas", 5);
            resultado.put("estado", "EXITOSO");
            
        } catch (NumberFormatException e) {
            resultado.put("estado", "ERROR");
            resultado.put("error", e.getMessage());
        }
        
        return resultado;
    }
    
    private Map<String, Object> demostrarObserverPattern() {
        Map<String, Object> resultado = new HashMap<>();
        
        System.out.println("👁️ DEMOSTRANDO OBSERVER PATTERN:");
        
        try {
            // Simular cambios de stock que activarán observers
            Integer[] productosAModificar = {1, 2, 3}; // IDs de productos existentes
            Integer[] nuevasCantidades = {5, 0, 15}; // Stock bajo, agotado, restockado
            
            List<Map<String, Object>> cambiosRealizados = new ArrayList<>();
            
            for (int i = 0; i < productosAModificar.length; i++) {
                try {
                    Producto productoActualizado = productoService.actualizarStock(
                            productosAModificar[i], 
                            nuevasCantidades[i]
                    );
                    
                    Map<String, Object> cambio = new HashMap<>();
                    cambio.put("producto_id", productosAModificar[i]);
                    cambio.put("producto_nombre", productoActualizado.getDescripcion());
                    cambio.put("nuevo_stock", nuevasCantidades[i]);
                    cambio.put("observadores_notificados", "EmailObserver, ReporteObserver, PushObserver");
                    
                    cambiosRealizados.add(cambio);
                    
                    System.out.println("   ✓ Stock actualizado para: " + productoActualizado.getDescripcion() + 
                                     " -> " + nuevasCantidades[i] + " (Observadores notificados)");
                    
                } catch (Exception e) {
                    System.out.println("   ⚠️ No se pudo actualizar producto ID " + productosAModificar[i]);
                }
            }
            
            resultado.put("patron", "Observer");
            resultado.put("cambios_realizados", cambiosRealizados);
            resultado.put("observadores_activos", 3);
            resultado.put("estado", "EXITOSO");
            
        } catch (Exception e) {
            resultado.put("estado", "ERROR");
            resultado.put("error", e.getMessage());
        }
        
        return resultado;
    }
    
    private Map<String, Object> demostrarFacadePattern() {
        Map<String, Object> resultado = new HashMap<>();
        
        System.out.println("🏢 DEMOSTRANDO FACADE PATTERN:");
        
        try {
            // Crear una venta que demuestre el facade coordinando múltiples servicios
            VentaRequest ventaDemo = new VentaRequest();
            ventaDemo.setIdUsuario(2); // Cajero Juan Reyes
            
            List<VentaRequest.ItemVenta> items = new ArrayList<>();
            items.add(new VentaRequest.ItemVenta(1, 1)); // 1 Arroz
            items.add(new VentaRequest.ItemVenta(3, 2)); // 2 Azúcar
            ventaDemo.setItems(items);
            
            VentaRequest.DatosPago pago = new VentaRequest.DatosPago("Tarjeta Crédito");
            pago.setNumeroTarjeta("4532123456789012");
            pago.setNombreTitular("Juan Pérez");
            pago.setCodigoSeguridad("123");
            ventaDemo.setDatosPago(pago);
            
            // Procesar usando el Facade
            VentaResponse response = ventasFacade.procesarVenta(ventaDemo);
            
            resultado.put("patron", "Facade");
            resultado.put("servicios_coordinados", "UsuarioService, ProductoService, FacturaService, PagoService, NotificationService");
            resultado.put("venta_procesada", true);
            resultado.put("factura_id", response.getIdFactura());
            resultado.put("total_venta", response.getTotal());
            resultado.put("metodo_pago", response.getMetodoPago());
            resultado.put("estado", response.getEstado());
            
            System.out.println("   ✓ Facade coordinó 5 servicios para procesar venta ID: " + response.getIdFactura());
            
        } catch (Exception e) {
            resultado.put("estado", "ERROR");
            resultado.put("error", e.getMessage());
            System.out.println("   ❌ Error en Facade: " + e.getMessage());
        }
        
        return resultado;
    }
}