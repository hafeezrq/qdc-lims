package com.qdc.lims.repository;

import com.qdc.lims.entity.CommissionLedger;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommissionLedgerRepository extends JpaRepository<CommissionLedger, Long> {

    // Find all unpaid records for a specific doctor
    List<CommissionLedger> findByDoctorIdAndStatus(Long doctorId, String status);

    // Find all unpaid records in the whole system (for the dashboard summary)
    List<CommissionLedger> findByStatus(String status);

}