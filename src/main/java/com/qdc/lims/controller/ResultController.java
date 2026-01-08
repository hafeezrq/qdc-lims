package com.qdc.lims.controller;

import com.qdc.lims.dto.ResultEntryRequest;
import com.qdc.lims.entity.LabResult;
import com.qdc.lims.service.ResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/results")
public class ResultController {

    private final ResultService service;

    public ResultController(ResultService service) {
        this.service = service;
    }

    @PostMapping("/enter")
    public ResponseEntity<LabResult> enterResult(@RequestBody ResultEntryRequest request) {
        LabResult updatedResult = service.enterResult(request);
        return ResponseEntity.ok(updatedResult);
    }
}