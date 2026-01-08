package com.qdc.lims.dto;

import java.util.List;

public record OrderRequest(
        Long patientId,
        Long doctorId, // Can be null if Self/Walk-in
        List<Long> testIds,
        Double discount,
        Double cashPaid) {
}