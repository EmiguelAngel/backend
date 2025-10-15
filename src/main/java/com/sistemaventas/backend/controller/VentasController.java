package com.sistemaventas.backend.controller;

import com.sistemaventas.backend.dto.request.VentaRequest;
import com.sistemaventas.backend.dto.response.VentaResponse;
import com.sistemaventas.backend.facade.VentasFacade;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ventas")
@CrossOrigin(origins = "http://localhost:4200")
public class VentasController {
    
    @Autowired
    private VentasFacade ventasFacade;
    
    /**
     * POST /api/ventas/procesar
     * Endpoint principal que usa el PATRÓN FACADE para procesar una venta completa
     */
    @PostMapping("/procesar")
    public ResponseEntity<VentaResponse> procesarVenta(@Valid @RequestBody VentaRequest ventaRequest) {
        try {
            System.out.println("🛒 Recibida solicitud de venta para usuario: " + ventaRequest.getIdUsuario());
            
            VentaResponse response = ventasFacade.procesarVenta(ventaRequest);
            
            if ("ERROR".equals(response.getEstado())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error en controlador de ventas: " + e.getMessage());
            VentaResponse errorResponse = new VentaResponse("ERROR", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
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