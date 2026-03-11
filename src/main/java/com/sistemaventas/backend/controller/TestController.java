package com.sistemaventas.backend.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "http://localhost:4200")
public class TestController {
    
    @GetMapping("/ping")
    public String ping() {
        return "✅ Backend funcionando correctamente - " + java.time.LocalDateTime.now();
    }
    
    @PostMapping("/echo")
    public String echo(@RequestBody String body) {
        System.out.println("📨 Recibido: " + body);
        return "📢 Echo: " + body;
    }
    
    @PostMapping("/venta-test")
    public String ventaTest(@RequestBody String body) {
        System.out.println("🛒 Datos de venta recibidos: " + body);
        try {
            // Solo registrar que llegó la petición
            return "✅ Petición de venta recibida correctamente. Datos: " + body;
        } catch (Exception e) {
            System.err.println("❌ Error en venta-test: " + e.getMessage());
            return "❌ Error: " + e.getMessage();
        }
    }
    
    @PostMapping("/venta-objeto")
    public String ventaObjeto(@RequestBody com.sistemaventas.backend.dto.request.VentaRequest ventaRequest) {
        System.out.println("🛒 VentaRequest como objeto: " + ventaRequest);
        try {
            return "✅ VentaRequest parseado correctamente: " + ventaRequest.toString();
        } catch (Exception e) {
            System.err.println("❌ Error parseando VentaRequest: " + e.getMessage());
            return "❌ Error: " + e.getMessage();
        }
    }
}