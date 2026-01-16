package com.qdc.lims.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entity representing a system user with authentication credentials and role-based access.
 * Passwords are stored using BCrypt hashing. Roles can be comma-separated for multiple access levels.
 */
@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // Will be Hashed (Encrypted)

    @Column(nullable = false)
    private String role; // ROLE_ADMIN, ROLE_RECEPTION, ROLE_LAB

    private String fullName;

    private boolean active = true;
}