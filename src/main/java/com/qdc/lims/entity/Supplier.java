package com.qdc.lims.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "suppliers")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName; // "Ali Distributors"
    private String contactPerson; // "Mr. Ali"
    private String mobile;
    private String address;
}