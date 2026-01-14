package com.qdc.lims.web;

import com.qdc.lims.entity.LabOrder;
import com.qdc.lims.repository.LabOrderRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Controller for reception operations including dashboard and report delivery actions.
 */
@Controller
public class ReceptionController {

    private final LabOrderRepository orderRepo;

    /**
     * Constructs a ReceptionController with the specified LabOrderRepository.
     *
     * @param orderRepo repository for lab orders
     */
    public ReceptionController(LabOrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    // 1. The Dashboard (Today's Activity)
    /**
     * Displays the reception dashboard with today's activity.
     *
     * @param model the model to pass data to the view
     * @return the view name for the dashboard
     */
    @GetMapping("/reception/dashboard")
    public String dashboard(Model model) {
        // Define "Today"
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        // A. Not Ready (In Lab)
        List<LabOrder> processing = orderRepo.findByStatusAndOrderDateBetween("PENDING", startOfDay, endOfDay);

        // B. Ready (Waiting for Patient)
        List<LabOrder> ready = orderRepo.findByStatusAndIsReportDeliveredFalseAndOrderDateBetween("COMPLETED",
                startOfDay, endOfDay);

        // C. Collected (Done)
        List<LabOrder> collected = orderRepo.findByIsReportDeliveredTrueAndDeliveryDateBetween(startOfDay, endOfDay);

        model.addAttribute("processing", processing);
        model.addAttribute("ready", ready);
        model.addAttribute("collected", collected);

        return "reception-dashboard";
    }

    // 2. Action: Mark as Collected
    /**
     * Marks a lab order as delivered if payment is complete, and redirects to the report.
     *
     * @param orderId the ID of the lab order to mark as delivered
     * @return redirect to the report or patient history if payment is required
     */
    @PostMapping("/orders/mark-delivered")
    public String markDelivered(@RequestParam Long orderId) {
        LabOrder order = orderRepo.findById(orderId).orElseThrow();

        // Validation: Cannot deliver if money is owed!
        if (order.getBalanceDue() > 0) {
            return "redirect:/patient/history/" + order.getPatient().getId() + "?error=paymentRequired";
        }

        order.setReportDelivered(true);
        order.setDeliveryDate(LocalDateTime.now());
        orderRepo.save(order);

        // Redirect to print the report immediately
        return "redirect:/orders/report/" + orderId;
    }
}