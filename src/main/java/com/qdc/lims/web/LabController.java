package com.qdc.lims.web;

import com.qdc.lims.entity.LabOrder;
import com.qdc.lims.repository.LabOrderRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.util.List;

@Controller
public class LabController {

    private final LabOrderRepository orderRepo;

    @Autowired
    private com.qdc.lims.service.ResultService resultService; // Inject Service

    public LabController(LabOrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    // 1. Show Pending Worklist
    @GetMapping("/lab/worklist")
    public String showWorklist(Model model) {
        // Fetch all orders, newest first
        // In a real app, you might filter by status="PENDING"
        List<LabOrder> orders = orderRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));

        model.addAttribute("orders", orders);
        return "lab-worklist";
    }

    // 2. Show the "Enter Results" Form
    @GetMapping("/lab/enter-results/{id}")
    public String enterResultsPage(@PathVariable Long id, Model model) {
        LabOrder order = orderRepo.findById(id).orElseThrow();
        model.addAttribute("order", order);
        return "lab-entry";
    }

    // 3. Save the Data
    @PostMapping("/lab/save-results")
    public String saveResults(@ModelAttribute LabOrder order) {
        // The 'order' object here only contains the Results list from the form
        resultService.saveResultsFromForm(order);

        // Redirect back to worklist
        return "redirect:/lab/worklist?saved=true";
    }

}