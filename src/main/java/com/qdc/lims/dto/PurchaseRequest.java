package com.qdc.lims.dto;

import java.util.List;

public record PurchaseRequest(
        Long supplierId,
        String invoiceNumber,
        List<PurchaseItemDTO> items,
        Double amountPaidNow, // e.g. 5000.0
        String paymentMode // "Cash", "Cheque", "Online"
) {
}
