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

/**
 * Controller for lab operations including worklist display and result entry.
 */
@Controller
public class LabController {

    private final LabOrderRepository orderRepo;

    @Autowired
    private com.qdc.lims.service.ResultService resultService; // Inject Service

    /**
     * Constructs a LabController with the specified LabOrderRepository.
     *
     * @param orderRepo repository for lab orders
     */
    public LabController(LabOrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    /**
     * Displays the pending worklist of lab orders.
     *
     * @param model the model to pass data to the view
     * @return the view name for the worklist
     */
    // 1. Show Pending Worklist
    @GetMapping("/lab/worklist")
    public String showWorklist(Model model) {
        // Fetch all orders, newest first
        // In a real app, you might filter by status="PENDING"
        List<LabOrder> orders = orderRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));

        model.addAttribute("orders", orders);
        return "lab-worklist";
    }

    /**
     * Shows the form for entering results for a specific lab order.
     * Blocks access if the report has already been delivered.
     *
     * @param id the ID of the lab order
     * @param model the model to pass data to the view
     * @return the view name for result entry
     */
    // 2. Show the "Enter Results" Form
    @GetMapping("/lab/enter-results/{id}")
    public String enterResultsPage(@PathVariable Long id, Model model) {
        LabOrder order = orderRepo.findById(id).orElseThrow();
        // --- CHECK: Block access to the form if delivered ---
        if (order.isReportDelivered()) {
            model.addAttribute("errorMessage", "⛔ SECURITY LOCK: Report delivered. Editing is forbidden.");
            // We still send the 'order' object so the HTML doesn't crash,
            // but we will hide the form in the next step.
            model.addAttribute("order", order);
            return "lab-entry";
        }

        model.addAttribute("order", order);
        return "lab-entry";
    }

    /**
     * Saves the entered lab results from the form.
     * Handles errors and redirects appropriately.
     *
     * @param order the LabOrder containing results
     * @param model the model to pass data to the view
     * @return the view name or redirect URL
     */
    // 3. Save the Data
    @PostMapping("/lab/save-results")
    public String saveResults(@ModelAttribute LabOrder order, Model model) {
        try {
            resultService.saveResultsFromForm(order);
            return "redirect:/lab/worklist?saved=true";
        } catch (RuntimeException e) {
            // If locked, go back to the form and show the error in Red
            // We need to reload the order to show the form again
            LabOrder dbOrder = orderRepo.findById(order.getId()).orElseThrow();
            model.addAttribute("order", dbOrder);
            model.addAttribute("errorMessage", e.getMessage());
            return "lab-entry"; // Stay on the page
        }

    }

}