package com.qdc.lims.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entity representing a supplier from whom inventory items are purchased.
 */
@Entity
@Data
@Table(name = "suppliers")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String companyName; // e.g. "Ali Distributors"

    private String contactPerson; // e.g. "Mr. Ali"

    private String mobile;

    private String address;

    @Column(nullable = false)
    private boolean active = true;
}