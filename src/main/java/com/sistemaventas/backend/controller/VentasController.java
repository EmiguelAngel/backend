package com.sistemaventas.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sistemaventas.backend.dto.request.VentaRequest;
import com.sistemaventas.backend.dto.response.VentaResponse;
import com.sistemaventas.backend.entity.Factura;
import com.sistemaventas.backend.facade.VentasFacade;
import com.sistemaventas.backend.service.FacturaPdfService;

import jakarta.validation.Valid;

/**
 * Controller (C in MVC): Endpoints para el flujo de ventas (checkout).
 * Este controlador usa un Facade (`VentasFacade`) para orquestar la lógica
 * (servicios, repositorios, creación de factura). En MVC, el modelo está
 * representado por las entidades/DTOs y la vista por el frontend (Angular).
 */
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
    
    @Autowired
    private FacturaPdfService facturaPdfService;
    
    /**
     * POST /api/ventas/procesar
     * Endpoint principal que usa el PATRÓN FACADE para procesar una venta completa
     */
    @PostMapping("/procesar")
    public ResponseEntity<VentaResponse> procesarVenta(@RequestBody VentaRequest ventaRequest) {
        System.out.println("🛒 === PROCESANDO VENTA REAL ===");
        
        try {
            System.out.println("VentaRequest: " + ventaRequest);
            
            if (ventaRequest == null) {
                System.err.println("❌ VentaRequest es null");
                VentaResponse errorResponse = new VentaResponse("ERROR", "Datos de venta faltantes");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            System.out.println("Usuario: " + ventaRequest.getIdUsuario());
            System.out.println("Items: " + (ventaRequest.getItems() != null ? ventaRequest.getItems().size() : 0));
            System.out.println("Método de pago: " + (ventaRequest.getDatosPago() != null ? ventaRequest.getDatosPago().getMetodoPago() : "null"));
            
            // Debug de productos solicitados
            if (ventaRequest.getItems() != null) {
                for (var item : ventaRequest.getItems()) {
                    System.out.println("  - Producto ID: " + item.getIdProducto() + ", Cantidad: " + item.getCantidad());
                }
            }
            
            // Llamar al VentasFacade real
            System.out.println("🔄 Llamando a VentasFacade...");
            VentaResponse response = ventasFacade.procesarVenta(ventaRequest);
            System.out.println("✅ VentasFacade completado. Estado: " + response.getEstado());
            
            if ("ERROR".equals(response.getEstado())) {
                System.err.println("❌ VentasFacade retornó error: " + response.getMensaje());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            System.out.println("🎉 Venta procesada exitosamente");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            System.err.println("❌ Error de validación: " + e.getMessage());
            // Errores de validación/negocio -> 400 Bad Request
            VentaResponse errorResponse = new VentaResponse("ERROR", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            System.err.println("❌ Error técnico: " + e.getMessage());
            e.printStackTrace();
            VentaResponse errorResponse = new VentaResponse("ERROR", "Error interno del sistema");
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
     * GET /api/ventas/user/{userId} - Obtener ventas por usuario (cajero)
     * Necesario para el historial de ventas del cajero
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<com.sistemaventas.backend.entity.Factura>> obtenerVentasPorUsuario(@org.springframework.web.bind.annotation.PathVariable Integer userId) {
        try {
            List<com.sistemaventas.backend.entity.Factura> facturas = facturaService.buscarPorUsuario(userId);
            return ResponseEntity.ok(facturas);
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo facturas por usuario " + userId + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * GET /api/ventas/{id} - Obtener una venta específica por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<com.sistemaventas.backend.entity.Factura> obtenerVentaPorId(@org.springframework.web.bind.annotation.PathVariable Integer id) {
        try {
            var factura = facturaService.buscarPorId(id);
            if (factura.isPresent()) {
                return ResponseEntity.ok(factura.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo factura " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * GET /api/ventas/{id}/details - Obtener detalles de una venta
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<List<com.sistemaventas.backend.entity.DetalleFactura>> obtenerDetallesVenta(@org.springframework.web.bind.annotation.PathVariable Integer id) {
        try {
            var factura = facturaService.buscarPorId(id);
            if (factura.isPresent()) {
                List<com.sistemaventas.backend.entity.DetalleFactura> detalles = factura.get().getDetallesFactura();
                return ResponseEntity.ok(detalles);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo detalles de factura " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * GET /api/ventas/today - Obtener ventas del día actual
     */
    @GetMapping("/today")
    public ResponseEntity<List<com.sistemaventas.backend.entity.Factura>> obtenerVentasHoy() {
        try {
            List<com.sistemaventas.backend.entity.Factura> facturas = facturaService.obtenerFacturasDeHoy();
            return ResponseEntity.ok(facturas);
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo facturas de hoy: " + e.getMessage());
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
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }
    
    /**
     * Endpoint de prueba simple para verificar conectividad
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Backend funcionando - " + new java.util.Date());
    }

    /**
     * POST /api/ventas/debug - Endpoint para debug del proceso de venta
     */
    @PostMapping("/debug")
    public ResponseEntity<?> debugVenta(@RequestBody(required = false) String rawData) {
        System.out.println("🐛 === DEBUG ENDPOINT ===");
        System.out.println("Raw data recibido: " + rawData);
        
        try {
            return ResponseEntity.ok().body("{\"mensaje\": \"Debug OK\", \"data\": \"" + 
                (rawData != null ? rawData.replace("\"", "\\\"") : "null") + "\"}");
        } catch (Exception e) {
            System.err.println("Error en debug: " + e.getMessage());
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
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
     * GET /api/ventas/factura/{id}/pdf
     * Descargar factura en formato PDF
     */
    @GetMapping("/factura/{id}/pdf")
    public ResponseEntity<byte[]> descargarFacturaPdf(@PathVariable Long id) {
        try {
            System.out.println("📄 Generando PDF para factura ID: " + id);
            
            // Obtener la factura completa con detalles
            Factura factura = facturaService.obtenerFacturaCompleta(id);
            if (factura == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Generar el PDF
            byte[] pdfBytes = facturaPdfService.generarFacturaPdf(factura);
            
            // Configurar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "factura_" + id + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            System.out.println("✅ PDF generado correctamente - " + pdfBytes.length + " bytes");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            System.err.println("❌ Error generando PDF: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
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