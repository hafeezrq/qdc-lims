package com.qdc.lims.dto;

public record PurchaseItemDTO(
        Long itemId,
        Double quantity,
        Double costPrice) {
}