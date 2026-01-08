package com.qdc.lims.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "inventory_items")
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String itemName; // e.g., "Yellow Top Tube"

    private Double currentStock; // Using Double to handle liquids (e.g. 500.0 ml)

    private Double minThreshold; // e.g., 50.0. If stock drops below this, ALERT!

    private String unit; // e.g., "pcs", "ml", "strips"
}