package com.qdc.lims.web;

import com.qdc.lims.entity.LabOrder;
import com.qdc.lims.repository.LabOrderRepository;

import org.springframework.beans.factory.annotation.Autowired;
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
 * Controller for finance-related operations, including commissions and daily closing.
 */
@Controller
public class FinanceController {

    private final LabOrderRepository orderRepo;

    @Autowired
    private com.qdc.lims.repository.CommissionLedgerRepository commissionRepo;
    @Autowired
    private com.qdc.lims.repository.DoctorRepository doctorRepo;

    /**
     * Constructs a FinanceController with the specified LabOrderRepository.
     *
     * @param orderRepo repository for lab orders
     */
    public FinanceController(LabOrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    /**
     * Displays the commission dashboard for all doctors.
     *
     * @param model the model to pass data to the view
     * @return the view name for the commission dashboard
     */
    // 1. Show Commission Dashboard
    @GetMapping("/admin/finance/commissions")
    public String commissionDashboard(Model model) {
        List<com.qdc.lims.entity.Doctor> doctors = doctorRepo.findAll();

        // We need to calculate "Total Unpaid" for each doctor manually
        // A simple Map to send to the view: Map<DoctorID, DoubleAmount>
        java.util.Map<Long, Double> balances = new java.util.HashMap<>();

        for (com.qdc.lims.entity.Doctor doc : doctors) {
            List<com.qdc.lims.entity.CommissionLedger> unpaid = commissionRepo.findByDoctorIdAndStatus(doc.getId(),
                    "UNPAID");

            double sum = unpaid.stream().mapToDouble(l -> l.getCalculatedAmount()).sum();
            balances.put(doc.getId(), sum);
        }

        model.addAttribute("doctors", doctors);
        model.addAttribute("balances", balances);

        return "finance-commissions";
    }

    /**
     * Pays a doctor by clearing all their unpaid commission dues.
     *
     * @param doctorId the ID of the doctor to pay
     * @return redirect to the commission dashboard with success flag
     */
    // 2. Pay a Doctor (Clear all their unpaid dues)
    @PostMapping("/admin/finance/pay-doctor")
    public String payDoctor(@RequestParam Long doctorId) {

        List<com.qdc.lims.entity.CommissionLedger> unpaid = commissionRepo.findByDoctorIdAndStatus(doctorId, "UNPAID");

        for (com.qdc.lims.entity.CommissionLedger ledger : unpaid) {
            ledger.setStatus("PAID");
            // You could also update a 'paymentDate' here if you added that field
            commissionRepo.save(ledger);
        }

        return "redirect:/admin/finance/commissions?success=true";
    }

    /**
     * Displays the daily closing summary for today's lab orders.
     *
     * @param model the model to pass data to the view
     * @return the view name for the daily closing summary
     */
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