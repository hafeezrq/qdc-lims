package com.qdc.lims.dto;

// We only need the Result ID (which row to update) and the Value.
public record ResultEntryRequest(
        Long resultId,
        String value) {
}