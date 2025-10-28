package com.sistemaventas.backend.facade;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sistemaventas.backend.dto.request.VentaRequest;
import com.sistemaventas.backend.dto.response.VentaResponse;
import com.sistemaventas.backend.entity.DetalleFactura;
import com.sistemaventas.backend.entity.Factura;
import com.sistemaventas.backend.entity.Pago;
import com.sistemaventas.backend.entity.Producto;
import com.sistemaventas.backend.entity.Usuario;
import com.sistemaventas.backend.observer.InventarioNotificationService;
import com.sistemaventas.backend.service.FacturaService;
import com.sistemaventas.backend.service.PagoService;
import com.sistemaventas.backend.service.ProductoService;
import com.sistemaventas.backend.service.UsuarioService;

/**
 * PATRÓN FACADE
 * 
 * Esta clase simplifica las operaciones complejas de venta coordinando múltiples servicios:
 * - ProductoService (validación de stock)
 * - UsuarioService (validación de usuarios)
 * - FacturaService (creación de facturas)
 * - PagoService (procesamiento de pagos)
 * - InventarioNotificationService (notificaciones Observer)
 */
@Service
public class VentasFacade {
    
    @Autowired
    @SuppressWarnings("FieldMayBeFinal")
    private ProductoService productoService;
    
    @Autowired
    @SuppressWarnings("FieldMayBeFinal")
    private UsuarioService usuarioService;
    
    @Autowired
    @SuppressWarnings("FieldMayBeFinal")
    private FacturaService facturaService;
    
    @Autowired
    @SuppressWarnings("FieldMayBeFinal")
    private PagoService pagoService;


    public VentasFacade(FacturaService facturaService, InventarioNotificationService notificationService, PagoService pagoService, ProductoService productoService, UsuarioService usuarioService) {
        this.facturaService = facturaService;
        this.pagoService = pagoService;
        this.productoService = productoService;
        this.usuarioService = usuarioService;
    }
    
    /**
     * MÉTODO PRINCIPAL DEL FACADE
     * Procesa una venta completa coordinando todos los servicios necesarios
     */
    @org.springframework.transaction.annotation.Transactional
    public VentaResponse procesarVenta(VentaRequest ventaRequest) {
        System.out.println("🛒 === INICIANDO PROCESAMIENTO DE VENTA ===");
        System.out.println("Usuario: " + ventaRequest.getIdUsuario());
        System.out.println("Items: " + ventaRequest.getItems().size());
        System.out.println("Método de pago: " + ventaRequest.getDatosPago().getMetodoPago());
        
        try {
            // PASO 1: Validar usuario
            Usuario usuario = validarUsuario(ventaRequest.getIdUsuario());
            System.out.println("✅ Usuario validado: " + usuario.getNombre());
            
            // PASO 2: Validar disponibilidad de productos y calcular totales
            List<DetalleValidado> detallesValidados = validarYCalcularProductos(ventaRequest.getItems());
            BigDecimal subtotal = calcularSubtotal(detallesValidados);
            System.out.println("✅ Productos validados - Subtotal: $" + subtotal);
            
            // PASO 3: Crear factura con detalles (sin guardar aún)
            Factura factura = crearFactura(usuario, detallesValidados, subtotal);
            System.out.println("✅ Factura creada - ID temporal: " + factura.getIdFactura() + ", Total: $" + factura.getTotal());
            
            // PASO 4: Guardar factura primero (necesaria para el pago)
            Factura facturaGuardada = facturaService.guardarFactura(factura);
            System.out.println("✅ Factura guardada - ID final: " + facturaGuardada.getIdFactura());
            
            // PASO 5: Procesar pago (ahora con factura ya persistida)
            Pago pago = procesarPago(ventaRequest.getDatosPago(), facturaGuardada.getTotal(), facturaGuardada);
            System.out.println("✅ Pago procesado - Método: " + pago.getMetodoPago());
            
            // PASO 6: Actualizar la factura con el ID del pago
            facturaGuardada.setIdPago(pago.getIdPago());
            facturaGuardada = facturaService.guardarFactura(facturaGuardada);
            System.out.println("✅ Factura actualizada con pago");
            
            // PASO 7: Actualizar inventario y notificar observadores
            actualizarInventarioYNotificar(detallesValidados);
            System.out.println("✅ Inventario actualizado y notificaciones enviadas");
            
            // PASO 8: Crear respuesta exitosa
            VentaResponse response = new VentaResponse(facturaGuardada, pago);
            
            System.out.println("🎉 === VENTA PROCESADA EXITOSAMENTE ===");
            System.out.println("Factura ID: " + response.getIdFactura());
            System.out.println("Total: $" + response.getTotal());
            
            return response;
            
        } catch (RuntimeException e) {
            System.err.println("❌ Error de negocio procesando venta: " + e.getMessage());
            // Para errores de negocio, no hacer rollback, solo devolver error
            throw e;  // Re-lanzar para que el controlador lo maneje
        } catch (Exception e) {
            System.err.println("❌ Error técnico procesando venta: " + e.getMessage());
            e.printStackTrace();
            // Para errores técnicos, hacer rollback
            throw new RuntimeException("Error técnico en el procesamiento: " + e.getMessage(), e);
        }
    }
    
    /**
     * PASO 1: Validar que el usuario existe y puede realizar ventas
     */
    private Usuario validarUsuario(Integer idUsuario) {
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(idUsuario);
        
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + idUsuario);
        }
        
        Usuario usuario = usuarioOpt.get();
        
        // Validar que el usuario puede realizar ventas (opcional)
        if (usuario.getRol() != null && usuario.getRol().getNombreRol().equals("Administrador")) {
            System.out.println("⚠️ Venta realizada por administrador: " + usuario.getNombre());
        }
        
        return usuario;
    }
    
    /**
     * PASO 2: Validar productos y calcular precios
     */
    private List<DetalleValidado> validarYCalcularProductos(List<VentaRequest.ItemVenta> items) {
        List<DetalleValidado> detallesValidados = new ArrayList<>();
        
        for (VentaRequest.ItemVenta item : items) {
            System.out.println("🔍 Procesando item - Producto ID: " + item.getIdProducto() + ", Cantidad: " + item.getCantidad());
            
            // Buscar producto
            Optional<Producto> productoOpt = productoService.buscarPorId(item.getIdProducto());
            
            if (productoOpt.isEmpty()) {
                throw new RuntimeException("Producto no encontrado con ID: " + item.getIdProducto());
            }
            
            Producto producto = productoOpt.get();
            System.out.println("📦 Producto encontrado: " + producto.getDescripcion());
            System.out.println("💰 Precio unitario: " + producto.getPrecioUnitario());
            System.out.println("📊 Stock disponible: " + producto.getCantidadDisponible());
            
            // Validar que el producto tiene precio
            if (producto.getPrecioUnitario() == null) {
                System.err.println("❌ ERROR: Producto sin precio - " + producto.getDescripcion() + " (ID: " + producto.getIdProducto() + ")");
                throw new RuntimeException(
                        "El producto '%s' (ID: %d) no tiene precio configurado".formatted(
                                producto.getDescripcion(), producto.getIdProducto())
                );
            }
            
            // Validar que el precio es positivo
            if (producto.getPrecioUnitario().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException(
                        "El producto '%s' tiene un precio inválido: $%s".formatted(
                                producto.getDescripcion(), producto.getPrecioUnitario())
                );
            }
            
            // Validar stock disponible
            if (!producto.tieneStockSuficiente(item.getCantidad())) {
                throw new RuntimeException(
                        "Stock insuficiente para '%s'. Disponible: %d, Solicitado: %d".formatted(
                                producto.getDescripcion(), producto.getCantidadDisponible(), item.getCantidad())
                );
            }
            
            // Calcular subtotal del item
            BigDecimal subtotalItem = producto.getPrecioUnitario().multiply(new BigDecimal(item.getCantidad()));
            
            detallesValidados.add(new DetalleValidado(producto, item.getCantidad(), subtotalItem));
            
            System.out.println("   ✓ " + producto.getDescripcion() + " x" + item.getCantidad() + " = $" + subtotalItem);
        }
        
        return detallesValidados;
    }
    
    /**
     * Calcular subtotal de todos los items
     */
    private BigDecimal calcularSubtotal(List<DetalleValidado> detalles) {
        return detalles.stream()
                .map(DetalleValidado::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * PASO 3: Crear factura con todos los detalles
     */
    private Factura crearFactura(Usuario usuario, List<DetalleValidado> detallesValidados, BigDecimal subtotal) {
        Factura factura = new Factura();
        factura.setUsuario(usuario);
        factura.setFecha(new Date());
        factura.setSubtotal(subtotal);
        
        // Calcular IVA (19%)
        BigDecimal iva = subtotal.multiply(new BigDecimal("0.19"));
        factura.setIva(iva);
        
        // Calcular total
        BigDecimal total = subtotal.add(iva);
        factura.setTotal(total);
        
        // Crear detalles de factura
        List<DetalleFactura> detallesFactura = new ArrayList<>();
        
        for (DetalleValidado detalle : detallesValidados) {
            DetalleFactura detalleFactura = new DetalleFactura();
            detalleFactura.setProducto(detalle.getProducto());
            detalleFactura.setFactura(factura);
            detalleFactura.setCantidad(detalle.getCantidad());
            detalleFactura.setPrecioUnitario(detalle.getProducto().getPrecioUnitario());
            detalleFactura.setSubtotal(detalle.getSubtotal());
            
            detallesFactura.add(detalleFactura);
        }
        
        factura.setDetallesFactura(detallesFactura);
        
        return factura;
    }
    
    /**
     * PASO 4: Procesar pago usando PagoService
     */
    private Pago procesarPago(VentaRequest.DatosPago datosPago, BigDecimal total, Factura factura) {
        try {
            return pagoService.procesarPago(datosPago, total, factura);
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar el pago: " + e.getMessage());
        }
    }
    
    /**
     * PASO 5: Actualizar inventario y notificar observadores
     */
    private void actualizarInventarioYNotificar(List<DetalleValidado> detallesValidados) {
        System.out.println("📦 === ACTUALIZANDO INVENTARIO ===");
        System.out.println("Total de items a actualizar: " + detallesValidados.size());
        
        int index = 1;
        for (DetalleValidado detalle : detallesValidados) {
            try {
                System.out.println("\n🔄 Item " + index + "/" + detallesValidados.size());
                System.out.println("   Producto: " + detalle.getProducto().getDescripcion());
                System.out.println("   ID: " + detalle.getProducto().getIdProducto());
                System.out.println("   Cantidad a reducir: " + detalle.getCantidad());
                System.out.println("   Stock actual: " + detalle.getProducto().getCantidadDisponible());
                
                // Reducir stock usando ProductoService (que ya tiene Observer integrado)
                productoService.reducirStock(detalle.getProducto().getIdProducto(), detalle.getCantidad());
                
                System.out.println("   ✅ Stock reducido exitosamente");
                index++;
                
            } catch (Exception e) {
                System.err.println("   ❌ ERROR reduciendo stock: " + e.getMessage());
                throw new RuntimeException("Error actualizando stock para " + 
                        detalle.getProducto().getDescripcion() + ": " + e.getMessage());
            }
        }
        
        System.out.println("\n✅ === INVENTARIO ACTUALIZADO COMPLETAMENTE ===");
    }
    
    /**
     * Método adicional: Validar venta antes de procesar
     */
    public void validarVentaCompleta(VentaRequest ventaRequest) {
        // Validaciones previas sin procesar la venta
        validarUsuario(ventaRequest.getIdUsuario());
        validarYCalcularProductos(ventaRequest.getItems());
        
        System.out.println("✅ Validación completa exitosa - La venta puede procesarse");
    }
    
    /**
     * Método para demostrar el patrón Facade
     */
    public VentaResponse demostrarPatronFacade() {
        System.out.println("=== DEMOSTRACIÓN PATRÓN FACADE ===");
        
        // Crear venta de demostración
        VentaRequest ventaDemo = crearVentaDemo();
        
        System.out.println("Procesando venta de demostración...");
        VentaResponse resultado = procesarVenta(ventaDemo);
        
        System.out.println("=== FIN DEMOSTRACIÓN ===");
        return resultado;
    }
    
    /**
     * Crear venta de demostración
     */
    private VentaRequest crearVentaDemo() {
        VentaRequest venta = new VentaRequest();
        venta.setIdUsuario(2); // Cajero Juan Reyes
        
        // Items de demostración
        List<VentaRequest.ItemVenta> items = new ArrayList<>();
        items.add(new VentaRequest.ItemVenta(1, 2)); // 2 Arroz
        items.add(new VentaRequest.ItemVenta(3, 1)); // 1 Azúcar
        
        venta.setItems(items);
        
        // Datos de pago
        VentaRequest.DatosPago pago = new VentaRequest.DatosPago("Efectivo");
        venta.setDatosPago(pago);
        
        return venta;
    }
    
    // ==========================================
    // CLASE INTERNA: DetalleValidado
    // ==========================================
    private static class DetalleValidado {
        private final Producto producto;
        private final Integer cantidad;
        private final BigDecimal subtotal;
        
        public DetalleValidado(Producto producto, Integer cantidad, BigDecimal subtotal) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.subtotal = subtotal;
        }
        
        public Producto getProducto() { return producto; }
        public Integer getCantidad() { return cantidad; }
        public BigDecimal getSubtotal() { return subtotal; }
    }
}