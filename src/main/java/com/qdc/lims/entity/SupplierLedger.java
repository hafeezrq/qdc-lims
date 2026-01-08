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
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    private LocalDate date;
    private String description; // "Purchased 100 Glucose Kits"

    // Money Logic
    private Double billAmount = 0.0; // Amount we owe them (Credit)
    private Double paidAmount = 0.0; // Amount we paid them (Debit)

    // We can calculate balance dynamically: SUM(bill) - SUM(paid)
}