package com.qdc.lims.controller;

import com.qdc.lims.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling report-related requests.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService service;

    /**
     * Constructs a ReportController with the specified ReportService.
     *
     * @param service the ReportService to handle report generation
     */
    public ReportController(ReportService service) {
        this.service = service;
    }

    /**
     * Downloads the PDF report for the specified order ID.
     *
     * @param orderId the ID of the order for which to generate the report
     * @return ResponseEntity containing the PDF bytes as an attachment
     */
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