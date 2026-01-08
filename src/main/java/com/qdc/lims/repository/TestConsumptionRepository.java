package com.qdc.lims.repository;

import com.qdc.lims.entity.TestConsumption;
import com.qdc.lims.entity.TestDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TestConsumptionRepository extends JpaRepository<TestConsumption, Long> {
    // Find the recipe for a specific test
    List<TestConsumption> findByTest(TestDefinition test);
}