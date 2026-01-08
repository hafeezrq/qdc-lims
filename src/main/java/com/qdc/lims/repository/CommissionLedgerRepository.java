package com.qdc.lims.repository;

import com.qdc.lims.entity.CommissionLedger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommissionLedgerRepository extends JpaRepository<CommissionLedger, Long> {
}