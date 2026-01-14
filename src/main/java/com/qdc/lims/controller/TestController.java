package com.qdc.lims.controller;

import com.qdc.lims.entity.TestDefinition;
import com.qdc.lims.repository.TestDefinitionRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for managing test definitions in the system.
 */
@RestController
@RequestMapping("/api/tests")
public class TestController {

    private final TestDefinitionRepository repository;

    /**
     * Constructs a TestController with the specified TestDefinitionRepository.
     *
     * @param repository the repository for test definitions
     */
    public TestController(TestDefinitionRepository repository) {
        this.repository = repository;
    }

    /**
     * Adds a new test definition to the menu.
     *
     * @param test the TestDefinition to add
     * @return the saved TestDefinition
     */
    @PostMapping
    public TestDefinition createTest(@RequestBody TestDefinition test) {
        return repository.save(test);
    }

    /**
     * Retrieves all available test definitions (the menu).
     *
     * @return a list of all TestDefinition entities
     */
    @GetMapping
    public List<TestDefinition> getAllTests() {
        return repository.findAll();
    }
}