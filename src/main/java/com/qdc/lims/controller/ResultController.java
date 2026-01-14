package com.qdc.lims.controller;

import com.qdc.lims.dto.ResultEntryRequest;
import com.qdc.lims.entity.LabResult;
import com.qdc.lims.service.ResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling lab result entry operations.
 */
@RestController
@RequestMapping("/api/results")
public class ResultController {

    private final ResultService service;

    /**
     * Constructs a ResultController with the specified ResultService.
     *
     * @param service the ResultService to handle result operations
     */
    public ResultController(ResultService service) {
        this.service = service;
    }

    /**
     * Enters a new lab result based on the provided request data.
     *
     * @param request the result entry request data
     * @return ResponseEntity containing the updated LabResult
     */
    @PostMapping("/enter")
    public ResponseEntity<LabResult> enterResult(@RequestBody ResultEntryRequest request) {
        LabResult updatedResult = service.enterResult(request);
        return ResponseEntity.ok(updatedResult);
    }
}