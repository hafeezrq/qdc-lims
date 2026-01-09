package com.qdc.lims.web;

import com.qdc.lims.entity.LabInfo;
import com.qdc.lims.repository.LabInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private LabInfoRepository labInfoRepo;

    // This method runs for EVERY request.
    // It adds "labInfo" to the Model automatically.
    @ModelAttribute("labInfo")
    public LabInfo populateLabInfo() {
        return labInfoRepo.findById(1L).orElse(new LabInfo());
    }
}