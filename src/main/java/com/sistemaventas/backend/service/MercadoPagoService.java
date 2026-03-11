package com.sistemaventas.backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import com.sistemaventas.backend.dto.mercadopago.PreferenceDTO;
import com.sistemaventas.backend.dto.mercadopago.PreferenceItemDTO;

import jakarta.annotation.PostConstruct;

@Service
public class MercadoPagoService {
    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoService.class);

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @Value("${mercadopago.notification.url}")
    private String notificationUrl;

    @PostConstruct
    public void initialize() {
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    public String createPreference(PreferenceDTO preferenceDTO) {
        try {
            logger.info("Creating payment preference for external reference: {}", 
                       preferenceDTO.getExternalReference());
            
            // Log items with all details
            logger.info("Items count: {}", preferenceDTO.getItems().size());
            for (PreferenceItemDTO item : preferenceDTO.getItems()) {
                logger.info("Item: title={}, quantity={}, unitPrice={}, currencyId={}, description={}, pictureUrl={}, categoryId={}", 
                    item.getTitle(), item.getQuantity(), item.getUnitPrice(), item.getCurrencyId(),
                    item.getDescription(), item.getPictureUrl(), item.getCategoryId());
            }
            
            // Log back URLs
            if (preferenceDTO.getBackUrls() != null) {
                logger.info("Back URLs - success: {}, pending: {}, failure: {}", 
                    preferenceDTO.getBackUrls().getSuccess(),
                    preferenceDTO.getBackUrls().getPending(),
                    preferenceDTO.getBackUrls().getFailure());
            } else {
                logger.warn("⚠️ Back URLs are NULL! This will cause autoReturn to fail.");
            }
            
            PreferenceClient client = new PreferenceClient();
            
            // Crear un descriptor más descriptivo
            String statementDescriptor = "POS Ventas";
            
            // Convertir items y calcular total para validación
            List<PreferenceItemRequest> items = preferenceDTO.getItems().stream()
                .map(this::convertToPreferenceItem)
                .collect(Collectors.toList());
            
            // Log del total calculado
            BigDecimal totalCalculado = preferenceDTO.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            logger.info("Total calculado de todos los items: ${}", totalCalculado);
            
            // Construir preferencia CON backUrls pero SIN autoReturn
            // MP acepta backUrls con localhost, pero no autoReturn con localhost
            // El usuario verá un botón "Volver a la tienda" en la página de MP
            var builder = PreferenceRequest.builder()
                .items(items)
                .externalReference(preferenceDTO.getExternalReference())
                .statementDescriptor(statementDescriptor)
                .binaryMode(false);
            
            // Agregar backUrls si están disponibles (para el botón "Volver")
            if (preferenceDTO.getBackUrls() != null && 
                preferenceDTO.getBackUrls().getSuccess() != null) {
                logger.info("✅ Configurando backUrls (sin autoReturn)");
                builder.backUrls(buildBackUrls(preferenceDTO));
                // NO agregamos autoReturn porque MP rechaza localhost con autoReturn
            } else {
                logger.warn("⚠️ BackUrls no disponibles");
            }
            
            var preferenceRequest = builder.build();
            
            logger.info("📦 PreferenceRequest contiene {} items", items.size());
            for (int i = 0; i < items.size(); i++) {
                PreferenceItemRequest item = items.get(i);
                logger.info("  Item {} en PreferenceRequest: title={}, quantity={}, unitPrice={}", 
                    i, item.getTitle(), item.getQuantity(), item.getUnitPrice());
            }

            logger.info("Sending request to MercadoPago (notification URL commented for local testing)");
            Preference preference = client.create(preferenceRequest);
            logger.info("Payment preference created successfully with ID: {}", preference.getId());
            
            // Verificar la respuesta de MP
            if (preference.getItems() != null) {
                logger.info("✅ MP confirmó recepción de {} items en la preferencia", preference.getItems().size());
            }
            
            return preference.getId();
        } catch (com.mercadopago.exceptions.MPApiException mpException) {
            logger.error("=== MercadoPago API Error ===");
            logger.error("Status Code: {}", mpException.getStatusCode());
            logger.error("API Response: {}", mpException.getApiResponse());
            logger.error("Error message: {}", mpException.getMessage());
            logger.error("Full exception: ", mpException);
            logger.error("================================");
            
            System.err.println("=== MP API ERROR ===");
            System.err.println("Status: " + mpException.getStatusCode());
            System.err.println("Message: " + mpException.getMessage());
            System.err.println("Response: " + mpException.getApiResponse());
            System.err.println("====================");
            
            String errorMsg = "Error de Mercado Pago [" + mpException.getStatusCode() + "]: " + mpException.getMessage();
            if (mpException.getApiResponse() != null) {
                errorMsg += " - Response: " + mpException.getApiResponse().getContent();
            }
            throw new RuntimeException(errorMsg, mpException);
        } catch (Exception e) {
            logger.error("=== General Error ===");
            logger.error("Error type: {}", e.getClass().getName());
            logger.error("Error message: {}", e.getMessage());
            logger.error("Full stack trace: ", e);
            logger.error("================================");
            
            System.err.println("=== GENERAL ERROR ===");
            System.err.println("Type: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=====================");
            
            throw new RuntimeException("Error general: " + e.getMessage(), e);
        }
    }

    private PreferenceItemRequest convertToPreferenceItem(PreferenceItemDTO item) {
        return PreferenceItemRequest.builder()
            .title(item.getTitle())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .currencyId(item.getCurrencyId() != null ? item.getCurrencyId() : "COP") // Default a COP (Colombia)
            .description(item.getDescription())
            .pictureUrl(item.getPictureUrl())
            .categoryId(item.getCategoryId())
            .build();
    }

    private com.mercadopago.client.preference.PreferenceBackUrlsRequest buildBackUrls(PreferenceDTO preferenceDTO) {
        return PreferenceBackUrlsRequest.builder()
            .success(preferenceDTO.getBackUrls().getSuccess())
            .pending(preferenceDTO.getBackUrls().getPending())
            .failure(preferenceDTO.getBackUrls().getFailure())
            .build();
    }

    public void handleNotification(String notificationType, String notificationId) {
        // Implementar lógica de notificaciones
        // Este método se llamará cuando MP envíe una notificación
    }
}