package com.qdc.lims.repository;

import com.qdc.lims.entity.LabOrder;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LabOrderRepository extends JpaRepository<LabOrder, Long> {

    // Find all orders for a patient, sorted by newest first
    List<LabOrder> findByPatientIdOrderByIdDesc(Long patientId);

}