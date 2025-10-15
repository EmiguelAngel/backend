package com.sistemaventas.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sistemaventas.backend.dto.request.VentaRequest;
import com.sistemaventas.backend.dto.response.VentaResponse;
import com.sistemaventas.backend.facade.VentasFacade;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ventas")
@CrossOrigin(origins = "http://localhost:4200")
public class VentasController {
    
    @Autowired
    private VentasFacade ventasFacade;
    
    @Autowired
    private com.sistemaventas.backend.service.UsuarioService usuarioService;
    
    @Autowired
    private com.sistemaventas.backend.service.ProductoService productoService;
    
    @Autowired
    private com.sistemaventas.backend.service.FacturaService facturaService;
    
    /**
     * POST /api/ventas/procesar
     * Endpoint principal que usa el PATRÓN FACADE para procesar una venta completa
     */
    @PostMapping("/procesar")
    public ResponseEntity<VentaResponse> procesarVenta(@RequestBody VentaRequest ventaRequest) {
        try {
            System.out.println("🛒 === LOGS DE DEPURACIÓN ===");
            System.out.println("Request recibido: " + ventaRequest);
            System.out.println("ID Usuario: " + ventaRequest.getIdUsuario());
            System.out.println("Items count: " + (ventaRequest.getItems() != null ? ventaRequest.getItems().size() : "null"));
            System.out.println("Datos de pago: " + ventaRequest.getDatosPago());
            
            VentaResponse response = ventasFacade.procesarVenta(ventaRequest);
            
            if ("ERROR".equals(response.getEstado())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error en controlador de ventas: " + e.getMessage());
            //            e.printStackTrace();
            VentaResponse errorResponse = new VentaResponse("ERROR", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * GET /api/ventas - Obtener todas las ventas (facturas)
     * Necesario para el dashboard y reportes
     */
    @GetMapping
    public ResponseEntity<List<com.sistemaventas.backend.entity.Factura>> obtenerTodasLasVentas() {
        try {
            List<com.sistemaventas.backend.entity.Factura> facturas = facturaService.obtenerTodasLasFacturas();
            return ResponseEntity.ok(facturas);
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo facturas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * Endpoint de prueba para diagnosticar problemas
     */
    @PostMapping("/test-simple")
    public ResponseEntity<String> testSimple(@RequestBody VentaRequest ventaRequest) {
        try {
            System.out.println("🛒 === TEST SIMPLE ===");
            System.out.println("VentaRequest: " + ventaRequest);
            
            // Verificar usuario
            var usuario = usuarioService.buscarPorId(ventaRequest.getIdUsuario());
            System.out.println("Usuario encontrado: " + usuario.isPresent());
            
            // Verificar productos
            if (ventaRequest.getItems() != null) {
                for (var item : ventaRequest.getItems()) {
                    var producto = productoService.buscarPorId(item.getIdProducto());
                    System.out.println("Producto " + item.getIdProducto() + " encontrado: " + producto.isPresent());
                }
            }
            
            return ResponseEntity.ok("✅ Verificaciones básicas completadas");
            
        } catch (Exception e) {
            System.err.println("❌ Error en test simple: " + e.getMessage());
            // e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }
    
    /**
     * Manejador de errores de validación
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<VentaResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.toList());
        
        System.err.println("❌ Errores de validación: " + errors);
        
        String errorMessage = "Errores de validación: " + String.join(", ", errors);
        VentaResponse errorResponse = new VentaResponse("ERROR", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * POST /api/ventas/validar
     * Validar una venta sin procesarla (útil para checkout)
     */
    @PostMapping("/validar")
    public ResponseEntity<?> validarVenta(@Valid @RequestBody VentaRequest ventaRequest) {
        try {
            ventasFacade.validarVentaCompleta(ventaRequest);
            return ResponseEntity.ok().body("Venta válida - puede procesarse correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error de validación: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }
    
    /**
     * GET /api/ventas/demo-facade
     * Demostrar el patrón Facade con una venta de ejemplo
     */
    @GetMapping("/demo-facade")
    public ResponseEntity<VentaResponse> demostrarFacade() {
        try {
            VentaResponse response = ventasFacade.demostrarPatronFacade();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            VentaResponse errorResponse = new VentaResponse("ERROR", "Error en demostración: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}