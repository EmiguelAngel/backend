package com.sistemaventas.backend.dto.mercadopago;

import lombok.Data;
import java.util.List;

@Data
public class PreferenceDTO {
    private List<PreferenceItemDTO> items;
    private String externalReference;
    private BackUrlsDTO backUrls;
    private String notificationUrl;
    private String paymentMethods;
    private Boolean autoReturn;
    
    @Data
    public static class BackUrlsDTO {
        private String success;
        private String pending;
        private String failure;
    }
}