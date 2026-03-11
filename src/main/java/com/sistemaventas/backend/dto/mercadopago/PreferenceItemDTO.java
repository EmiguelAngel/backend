package com.sistemaventas.backend.dto.mercadopago;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PreferenceItemDTO {
    private String title;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String currencyId; // "COP" para Colombia
    private String description;
    private String pictureUrl;
    private String categoryId;
}