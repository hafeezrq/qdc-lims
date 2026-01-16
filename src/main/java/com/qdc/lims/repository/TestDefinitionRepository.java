package com.qdc.lims.repository;

import com.qdc.lims.entity.TestDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for TestDefinition entities, providing CRUD operations and custom queries.
 */
public interface TestDefinitionRepository extends JpaRepository<TestDefinition, Long> {
    /**
     * Finds a test definition by its short code.
     *
     * @param shortCode the short code of the test (e.g., "CBC", "GLU-R")
     * @return the TestDefinition with the given short code, or null if not found
     */
    TestDefinition findByShortCode(String shortCode);
}
