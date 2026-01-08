package com.qdc.lims.repository;

import com.qdc.lims.entity.LabOrder;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LabOrderRepository extends JpaRepository<LabOrder, Long> {

    // Find all orders for a patient, sorted by newest first
    List<LabOrder> findByPatientIdOrderByIdDesc(Long patientId);

    // --- NEW DASHBOARD QUERIES ---

    // 1. Processing (Status PENDING, Today)
    List<LabOrder> findByStatusAndOrderDateBetween(String status, LocalDateTime start, LocalDateTime end);

    // 2. Ready for Pickup (Status COMPLETED, Not Delivered, Today)
    List<LabOrder> findByStatusAndIsReportDeliveredFalseAndOrderDateBetween(String status, LocalDateTime start,
            LocalDateTime end);

    // 3. Collected (Delivered True, Delivery Date Today)
    List<LabOrder> findByIsReportDeliveredTrueAndDeliveryDateBetween(LocalDateTime start, LocalDateTime end);

}