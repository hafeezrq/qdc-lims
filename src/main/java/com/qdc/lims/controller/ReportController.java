package com.qdc.lims.controller;

import com.qdc.lims.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    // URL: GET http://localhost:8080/api/reports/1
    @GetMapping("/{orderId}")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long orderId) {
        byte[] pdfBytes = service.generatePdfReport(orderId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report_" + orderId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}