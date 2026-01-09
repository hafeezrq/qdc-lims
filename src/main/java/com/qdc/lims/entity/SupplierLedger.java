package com.qdc.lims.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "supplier_ledger")
public class SupplierLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    private LocalDate transactionDate;

    private String description; // e.g. "Inv-999 Purchase" or "Cash Payment"

    private String invoiceNumber; // Optional, for cross-checking paper bills

    // Money Logic
    private Double billAmount = 0.0; // Money we OWE (Credit) - Increases Balance
    private Double paidAmount = 0.0; // Money we PAID (Debit) - Decreases Balance

    @PrePersist
    protected void onCreate() {
        if (transactionDate == null)
            transactionDate = LocalDate.now();
    }
}