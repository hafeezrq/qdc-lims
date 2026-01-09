package com.qdc.lims.repository;

import com.qdc.lims.entity.SupplierLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupplierLedgerRepository extends JpaRepository<SupplierLedger, Long> {
    // Find history for one supplier
    List<SupplierLedger> findBySupplierIdOrderByTransactionDateDesc(Long supplierId);
}