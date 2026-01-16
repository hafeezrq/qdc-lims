package com.qdc.lims.repository;

import com.qdc.lims.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for Doctor entities, providing standard CRUD operations.
 */
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}
