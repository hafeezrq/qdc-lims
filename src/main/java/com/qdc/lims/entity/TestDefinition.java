package com.qdc.lims.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "test_definitions")
public class TestDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String testName;

    private String shortCode;

    private Double price;

    private String unit;

    private Double minRange;
    private Double maxRange;

    private String department;

    // MAKE SURE THERE ARE NO INVENTORY FIELDS HERE!
}