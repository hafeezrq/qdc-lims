package com.qdc.lims.controller;

import com.qdc.lims.entity.Patient;
import com.qdc.lims.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService service;

    /**
     * Constructs a PatientController with the specified PatientService.
     *
     * @param service the PatientService to handle patient operations
     */
    public PatientController(PatientService service) {
        this.service = service;
    }

    /**
     * Registers a new patient in the system.
     *
     * @param patient the patient entity to register
     * @return ResponseEntity containing the saved patient
     */
    // Endpoint to Register a new Patient
    // URL: POST http://localhost:8080/api/patients
    @PostMapping
    public ResponseEntity<Patient> register(@RequestBody Patient patient) {
        Patient savedPatient = service.registerPatient(patient);
        return ResponseEntity.ok(savedPatient);
    }

    /**
     * Simple verification endpoint to check if the system is running.
     *
     * @return a status message string
     */
    @GetMapping("/hello")
    public String sayHello() {
        return "QDC-LIMS System is Running!";
    }
}