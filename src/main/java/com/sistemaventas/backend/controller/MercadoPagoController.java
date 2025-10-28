package com.sistemaventas.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sistemaventas.backend.dto.mercadopago.PreferenceDTO;
import com.sistemaventas.backend.service.MercadoPagoService;

@RestController
@RequestMapping("/api/payments")
public class MercadoPagoController {

    @Autowired
    private MercadoPagoService mercadoPagoService;

    @PostMapping("/create-preference")
    public ResponseEntity<?> createPreference(@RequestBody PreferenceDTO request) {
        try {
            // Log de la petición
            System.out.println("=== MERCADOPAGO REQUEST ===");
            System.out.println("Items count: " + (request.getItems() != null ? request.getItems().size() : "null"));
            System.out.println("External reference: " + request.getExternalReference());
            System.out.println("Back URLs: " + request.getBackUrls());
            
            if (request.getItems() == null || request.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Items no puede estar vacío"));
            }
            
            // Log de cada item
            for (int i = 0; i < request.getItems().size(); i++) {
                var item = request.getItems().get(i);
                System.out.println("Item " + i + ": " + 
                    "title=" + item.getTitle() + 
                    ", quantity=" + item.getQuantity() + 
                    ", unitPrice=" + item.getUnitPrice() +
                    ", currencyId=" + item.getCurrencyId());
                
                if (item.getTitle() == null || item.getTitle().isEmpty()) {
                    return ResponseEntity.badRequest().body(new ErrorResponse("Item " + i + ": title es requerido"));
                }
                if (item.getQuantity() == null || item.getQuantity() <= 0) {
                    return ResponseEntity.badRequest().body(new ErrorResponse("Item " + i + ": quantity debe ser mayor a 0"));
                }
                if (item.getUnitPrice() == null || item.getUnitPrice().doubleValue() <= 0) {
                    return ResponseEntity.badRequest().body(new ErrorResponse("Item " + i + ": unitPrice debe ser mayor a 0"));
                }
            }
            
            String preferenceId = mercadoPagoService.createPreference(request);
            System.out.println("Preference created: " + preferenceId);
            return ResponseEntity.ok(new PreferenceResponse(preferenceId));
        } catch (Exception e) {
            System.err.println("Error creating preference: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    // Clase para respuesta de error
    private static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }

    // Clase interna para la respuesta
    private static class PreferenceResponse {
        private String preferenceId;
        
        public PreferenceResponse(String preferenceId) {
            this.preferenceId = preferenceId;
        }
        
        public String getPreferenceId() {
            return preferenceId;
        }
        
        public void setPreferenceId(String preferenceId) {
            this.preferenceId = preferenceId;
        }
    }
    
    @PostMapping("/test-credentials")
    public ResponseEntity<?> testCredentials() {
        System.out.println("=== TESTING MERCADOPAGO CREDENTIALS ===");
        try {
            // Crear una preferencia mínima de prueba
            var testDTO = new PreferenceDTO();
            var item = new com.sistemaventas.backend.dto.mercadopago.PreferenceItemDTO();
            item.setTitle("Test Product");
            item.setQuantity(1);
            item.setUnitPrice(new java.math.BigDecimal("1000"));
            item.setCurrencyId("COP");
            item.setDescription("Test");
            
            testDTO.setItems(java.util.Arrays.asList(item));
            testDTO.setExternalReference("TEST-" + System.currentTimeMillis());
            
            // No enviar backUrls para la prueba
            var backUrls = new PreferenceDTO.BackUrlsDTO();
            backUrls.setSuccess("https://example.com/success");
            backUrls.setPending("https://example.com/pending");
            backUrls.setFailure("https://example.com/failure");
            testDTO.setBackUrls(backUrls);
            
            System.out.println("Calling MercadoPago service...");
            String preferenceId = mercadoPagoService.createPreference(testDTO);
            System.out.println("Success! Preference ID: " + preferenceId);
            
            return ResponseEntity.ok().body(java.util.Map.of(
                "status", "success",
                "message", "Credenciales válidas - Preference creada correctamente",
                "preferenceId", preferenceId
            ));
        } catch (Exception e) {
            System.err.println("Error testing credentials: " + e.getMessage());
            e.printStackTrace();
            
            String causeMsg = "N/A";
            if (e.getCause() != null) {
                causeMsg = e.getCause().getMessage();
            }
            
            return ResponseEntity.status(400).body(java.util.Map.of(
                "status", "error",
                "message", e.getMessage() != null ? e.getMessage() : "Unknown error",
                "cause", causeMsg,
                "type", e.getClass().getSimpleName()
            ));
        }
    }

    @PostMapping("/notifications")
    public ResponseEntity<?> handleNotification(
            @RequestParam("type") String type,
            @RequestParam("data.id") String id) {
        mercadoPagoService.handleNotification(type, id);
        return ResponseEntity.ok().build();
    }
}