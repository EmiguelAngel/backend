package com.sistemaventas.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sistemaventas.backend.dto.DevolucionDTO;
import com.sistemaventas.backend.dto.DevolucionRequestDTO;
import com.sistemaventas.backend.service.DevolucionService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/devoluciones")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@Slf4j
public class DevolucionController {

    @Autowired
    private DevolucionService devolucionService;

    /**
     * Endpoint de prueba para verificar que el servicio está funcionando
     * GET /api/devoluciones/ping
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Servicio de devoluciones funcionando - " + java.time.LocalDateTime.now());
    }

    /**
     * Procesar una devolución
     * POST /api/devoluciones/procesar
     */
    @PostMapping("/procesar")
    public ResponseEntity<?> procesarDevolucion(@Valid @RequestBody DevolucionRequestDTO request) {
        try {
            log.info("Recibida solicitud de devolución para factura ID: {}", request.getIdFactura());
            DevolucionDTO devolucion = devolucionService.procesarDevolucion(request);
            return ResponseEntity.ok(devolucion);
        } catch (IllegalStateException e) {
            log.warn("Error de validación al procesar devolución: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error al procesar devolución: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la devolución: " + e.getMessage());
        }
    }

    /**
     * Obtener todas las devoluciones
     * GET /api/devoluciones
     */
    @GetMapping
    public ResponseEntity<List<DevolucionDTO>> obtenerTodasLasDevoluciones() {
        try {
            List<DevolucionDTO> devoluciones = devolucionService.obtenerTodasLasDevoluciones();
            return ResponseEntity.ok(devoluciones);
        } catch (Exception e) {
            log.error("Error al obtener devoluciones: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener devoluciones por factura
     * GET /api/devoluciones/factura/{idFactura}
     */
    @GetMapping("/factura/{idFactura}")
    public ResponseEntity<List<DevolucionDTO>> obtenerDevolucionesPorFactura(@PathVariable Integer idFactura) {
        try {
            List<DevolucionDTO> devoluciones = devolucionService.obtenerDevolucionesPorFactura(idFactura);
            return ResponseEntity.ok(devoluciones);
        } catch (Exception e) {
            log.error("Error al obtener devoluciones de factura {}: {}", idFactura, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener devoluciones por estado
     * GET /api/devoluciones/estado/{estado}
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<DevolucionDTO>> obtenerDevolucionesPorEstado(@PathVariable String estado) {
        try {
            List<DevolucionDTO> devoluciones = devolucionService.obtenerDevolucionesPorEstado(estado);
            return ResponseEntity.ok(devoluciones);
        } catch (Exception e) {
            log.error("Error al obtener devoluciones por estado {}: {}", estado, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener una devolución por ID
     * GET /api/devoluciones/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerDevolucionPorId(@PathVariable Long id) {
        try {
            DevolucionDTO devolucion = devolucionService.obtenerDevolucionPorId(id);
            return ResponseEntity.ok(devolucion);
        } catch (Exception e) {
            log.error("Error al obtener devolución {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener la devolución: " + e.getMessage());
        }
    }
}
