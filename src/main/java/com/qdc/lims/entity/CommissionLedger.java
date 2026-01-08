package com.qdc.lims.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "commission_ledger")
public class CommissionLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // This specific field name 'labOrder' creates the method 'setLabOrder()'
    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private LabOrder labOrder;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    private Double totalBillAmount;
    private Double commissionPercentage;
    private Double calculatedAmount;

    private LocalDate transactionDate;
    private String status;

    @PrePersist
    protected void onCreate() {
        this.transactionDate = LocalDate.now();
        this.status = "UNPAID";
    }
}