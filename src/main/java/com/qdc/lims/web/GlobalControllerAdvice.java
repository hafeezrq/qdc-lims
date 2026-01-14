package com.qdc.lims.web;

import com.qdc.lims.entity.LabInfo;
import com.qdc.lims.repository.LabInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Controller advice to add global model attributes for all requests.
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private LabInfoRepository labInfoRepo;

    // This method runs for EVERY request.
    // It adds "labInfo" to the Model automatically.
    /**
     * Adds the LabInfo entity to the model for every request.
     *
     * @return the LabInfo entity (defaults to new LabInfo if not found)
     */
    @ModelAttribute("labInfo")
    public LabInfo populateLabInfo() {
        return labInfoRepo.findById(1L).orElse(new LabInfo());
    }
}