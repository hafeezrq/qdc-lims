package com.qdc.lims.web;

import com.qdc.lims.entity.LabOrder;
import com.qdc.lims.repository.LabOrderRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
public class FinanceController {

    private final LabOrderRepository orderRepo;

    public FinanceController(LabOrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    @GetMapping("/admin/finance/daily")
    public String dailyClosing(Model model) {
        // 1. Define Today's Time Range (00:00 to 23:59:59)
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        // 2. Fetch Data
        List<LabOrder> todaysOrders = orderRepo.findByOrderDateBetween(start, end);

        // 3. Calculate Totals (Using Java Math)
        double totalBilled = 0.0;
        double totalDiscount = 0.0;
        double cashCollected = 0.0;
        double pendingReceivables = 0.0;

        for (LabOrder ord : todaysOrders) {
            totalBilled += (ord.getTotalAmount() != null ? ord.getTotalAmount() : 0.0);
            totalDiscount += (ord.getDiscountAmount() != null ? ord.getDiscountAmount() : 0.0);
            cashCollected += (ord.getPaidAmount() != null ? ord.getPaidAmount() : 0.0);
            pendingReceivables += (ord.getBalanceDue() != null ? ord.getBalanceDue() : 0.0);
        }

        // 4. Send to HTML
        model.addAttribute("orders", todaysOrders);
        model.addAttribute("date", LocalDate.now());

        model.addAttribute("totalBilled", totalBilled);
        model.addAttribute("totalDiscount", totalDiscount);
        model.addAttribute("cashCollected", cashCollected);
        model.addAttribute("pending", pendingReceivables);

        return "finance-daily";
    }
}