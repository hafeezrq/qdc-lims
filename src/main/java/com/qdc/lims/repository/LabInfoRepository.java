package com.qdc.lims.repository;

import com.qdc.lims.entity.LabInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabInfoRepository extends JpaRepository<LabInfo, Long> {
    // Standard CRUD is enough
}