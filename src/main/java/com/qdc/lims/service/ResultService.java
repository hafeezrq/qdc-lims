package com.qdc.lims.service;

import com.qdc.lims.dto.ResultEntryRequest;
import com.qdc.lims.entity.LabResult;
import com.qdc.lims.entity.TestDefinition;
import com.qdc.lims.repository.LabResultRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResultService {

    private final LabResultRepository repository;
    @Autowired
    private com.qdc.lims.repository.LabOrderRepository orderRepo;

    public ResultService(LabResultRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public LabResult enterResult(ResultEntryRequest request) {
        // 1. Fetch the specific result row
        LabResult result = repository.findById(request.resultId())
                .orElseThrow(() -> new RuntimeException("Result ID not found"));

        TestDefinition test = result.getTestDefinition();

        // 2. Save the value
        result.setResultValue(request.value());

        // 3. Auto-Validation Logic
        try {
            // Try to convert string "150" to number 150.0
            double val = Double.parseDouble(request.value());

            if (test.getMinRange() != null && test.getMaxRange() != null) {
                if (val < test.getMinRange()) {
                    result.setAbnormal(true);
                    result.setRemarks("LOW");
                } else if (val > test.getMaxRange()) {
                    result.setAbnormal(true);
                    result.setRemarks("HIGH");
                } else {
                    result.setAbnormal(false);
                    result.setRemarks("Normal");
                }
            }
        } catch (NumberFormatException e) {
            // If the result is text (e.g., "Positive"), we can't check ranges
            result.setAbnormal(false);
        }

        return repository.save(result);
    }

    @Transactional
    public void saveResultsFromForm(com.qdc.lims.entity.LabOrder orderForm) {

        // Loop through the results submitted from the screen
        for (com.qdc.lims.entity.LabResult resultFromForm : orderForm.getResults()) {

            // Fetch the real result from DB to ensure we don't lose links
            com.qdc.lims.entity.LabResult dbResult = repository.findById(resultFromForm.getId()).orElseThrow();

            // Update the value
            String val = resultFromForm.getResultValue();
            dbResult.setResultValue(val);

            // Apply High/Low Logic
            com.qdc.lims.entity.TestDefinition test = dbResult.getTestDefinition();
            try {
                if (val != null && !val.isEmpty()) {
                    double numVal = Double.parseDouble(val);

                    if (test.getMinRange() != null && test.getMaxRange() != null) {
                        if (numVal < test.getMinRange()) {
                            dbResult.setAbnormal(true);
                            dbResult.setRemarks("LOW");
                        } else if (numVal > test.getMaxRange()) {
                            dbResult.setAbnormal(true);
                            dbResult.setRemarks("HIGH");
                        } else {
                            dbResult.setAbnormal(false);
                            dbResult.setRemarks("Normal");
                        }
                    }
                }
            } catch (NumberFormatException e) {
                // Not a number (e.g. "Positive"), ignore range check
                dbResult.setAbnormal(false);
                dbResult.setRemarks("");
            }

            repository.save(dbResult);
        }

        // --- NEW LOGIC: Update Order Status ---
        // We assume if the tech clicked "Save", the order is done.
        com.qdc.lims.entity.LabOrder dbOrder = orderRepo.findById(orderForm.getId()).orElseThrow();
        dbOrder.setStatus("COMPLETED");
        orderRepo.save(dbOrder);

    }

}