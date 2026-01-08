package com.qdc.lims.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class LabOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to the Patient
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor referringDoctor; // Visible to Receptionist ("Ref By: Dr. Bilal")

    private LocalDateTime orderDate;

    private String status; // "PENDING", "COMPLETED"

    private Double totalAmount; // Calculated automatically

    // ---------- Update: To incorporate accounting ----------//
    private Double discountAmount = 0.0; // e.g. 100
    private Double taxAmount = 0.0; // (Optional, usually 0 in labs)
    private Double paidAmount = 0.0; // e.g. 500 (Patient paid half)
    private Double balanceDue = 0.0; // e.g. 400 (Remaining)

    // One Order = Many Tests (Results)
    // "CascadeType.ALL" means if we save the Order, it auto-saves the Result rows
    // too.
    @OneToMany(mappedBy = "labOrder", cascade = CascadeType.ALL)
    private List<LabResult> results = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.orderDate = LocalDateTime.now();
        this.status = "PENDING";
    }

    // Helper to auto-calculate balance before saving
    @PreUpdate
    public void calculateBalance() {
        if (discountAmount == null)
            discountAmount = 0.0;
        if (paidAmount == null)
            paidAmount = 0.0;
        if (totalAmount == null)
            totalAmount = 0.0;

        this.balanceDue = totalAmount - discountAmount - paidAmount;
    }

}