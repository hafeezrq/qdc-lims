package com.qdc.lims.controller;

import com.qdc.lims.entity.TestDefinition;
import com.qdc.lims.repository.TestDefinitionRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tests")
public class TestController {

    private final TestDefinitionRepository repository;

    public TestController(TestDefinitionRepository repository) {
        this.repository = repository;
    }

    // 1. Add a new Test to the Menu
    @PostMapping
    public TestDefinition createTest(@RequestBody TestDefinition test) {
        return repository.save(test);
    }

    // 2. View all available tests (The "Menu")
    @GetMapping
    public List<TestDefinition> getAllTests() {
        return repository.findAll();
    }
}