package com.qdc.lims.controller;

import com.qdc.lims.entity.Patient;
import com.qdc.lims.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService service;

    public PatientController(PatientService service) {
        this.service = service;
    }

    // Endpoint to Register a new Patient
    // URL: POST http://localhost:8080/api/patients
    @PostMapping
    public ResponseEntity<Patient> register(@RequestBody Patient patient) {
        Patient savedPatient = service.registerPatient(patient);
        return ResponseEntity.ok(savedPatient);
    }

    // Simple verification endpoint
    @GetMapping("/hello")
    public String sayHello() {
        return "QDC-LIMS System is Running!";
    }
}