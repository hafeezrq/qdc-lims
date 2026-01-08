package com.qdc.lims.repository;

import com.qdc.lims.entity.TestDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestDefinitionRepository extends JpaRepository<TestDefinition, Long> {
    // We might want to find a test by its short code later
    // e.g., repository.findByShortCode("CBC");
    TestDefinition findByShortCode(String shortCode);
}