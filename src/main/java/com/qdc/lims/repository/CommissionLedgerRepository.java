package com.qdc.lims.repository;

import com.qdc.lims.entity.CommissionLedger;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for CommissionLedger entities, providing queries for commission tracking.
 */
public interface CommissionLedgerRepository extends JpaRepository<CommissionLedger, Long> {

    /**
     * Finds all commission records for a specific doctor with the given status.
     *
     * @param doctorId the ID of the doctor
     * @param status the payment status (e.g., "UNPAID", "PAID")
     * @return list of CommissionLedger entries
     */
    List<CommissionLedger> findByDoctorIdAndStatus(Long doctorId, String status);

    /**
     * Finds all commission records with the given status across all doctors.
     *
     * @param status the payment status (e.g., "UNPAID", "PAID")
     * @return list of CommissionLedger entries
     */
    List<CommissionLedger> findByStatus(String status);

}