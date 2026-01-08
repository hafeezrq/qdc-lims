package com.qdc.lims.repository;

import com.qdc.lims.entity.LabOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabOrderRepository extends JpaRepository<LabOrder, Long> {
}