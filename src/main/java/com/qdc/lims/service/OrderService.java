package com.qdc.lims.service;

import com.qdc.lims.dto.OrderRequest;
import com.qdc.lims.entity.*;
import com.qdc.lims.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final LabOrderRepository orderRepo;
    private final PatientRepository patientRepo;
    private final TestDefinitionRepository testRepo;
    private final DoctorRepository doctorRepo;
    private final CommissionLedgerRepository commissionRepo;
    private final TestConsumptionRepository consumptionRepo;
    private final InventoryItemRepository inventoryRepo;

    // Constructor Injection (Spring auto-wires these)
    public OrderService(LabOrderRepository orderRepo, PatientRepository patientRepo,
            TestDefinitionRepository testRepo, DoctorRepository doctorRepo,
            CommissionLedgerRepository commissionRepo, TestConsumptionRepository consumptionRepo,
            InventoryItemRepository inventoryRepo) {
        this.orderRepo = orderRepo;
        this.patientRepo = patientRepo;
        this.testRepo = testRepo;
        this.doctorRepo = doctorRepo;
        this.commissionRepo = commissionRepo;
        this.consumptionRepo = consumptionRepo;
        this.inventoryRepo = inventoryRepo;
    }

    @Transactional
    public LabOrder createOrder(OrderRequest request) {
        // 1. Find Patient
        Patient patient = patientRepo.findById(request.patientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // 2. Find Doctor (Handle Null for Self-Patients)
        Doctor doctor = null;
        if (request.doctorId() != null) {
            doctor = doctorRepo.findById(request.doctorId()).orElse(null);
        }

        // 3. Setup Order
        LabOrder order = new LabOrder();
        order.setPatient(patient);
        order.setReferringDoctor(doctor);

        double totalAmount = 0.0;
        List<TestDefinition> tests = testRepo.findAllById(request.testIds());

        for (TestDefinition test : tests) {
            // A. Create Empty Result Slot
            LabResult result = new LabResult();
            result.setLabOrder(order);
            result.setTestDefinition(test);
            result.setResultValue(""); // Waiting for Lab Tech
            order.getResults().add(result);

            // B. Add Price to Bill
            totalAmount += test.getPrice();

            // C. INVENTORY LOGIC (Automatic Deduction)
            List<TestConsumption> recipe = consumptionRepo.findByTest(test);
            for (TestConsumption ingredient : recipe) {
                InventoryItem item = ingredient.getItem();

                // Subtract Stock
                double newStock = item.getCurrentStock() - ingredient.getQuantity();
                if (newStock < 0) {
                    throw new RuntimeException(
                            "❌ OUT OF STOCK: Not enough " + item.getItemName() + " to book this test.");
                }

                item.setCurrentStock(newStock);

                // Save updated stock
                inventoryRepo.save(item);
            }
        }

        // --- NEW FINANCE LOGIC ---
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(request.discount() != null ? request.discount() : 0.0);
        order.setPaidAmount(request.cashPaid() != null ? request.cashPaid() : 0.0);

        // Auto-calculate balance (Total - Discount - Paid)
        order.calculateBalance();
        // -------------------------
        LabOrder savedOrder = orderRepo.save(order);

        // 4. COMMISSION LOGIC (Secret Table)
        if (doctor != null && doctor.getCommissionPercentage() > 0) {
            CommissionLedger ledger = new CommissionLedger();
            ledger.setLabOrder(savedOrder);
            ledger.setDoctor(doctor);
            ledger.setTotalBillAmount(totalAmount);
            ledger.setCommissionPercentage(doctor.getCommissionPercentage());

            // Calc: 2000 * 10 / 100 = 200
            double commAmount = totalAmount * (doctor.getCommissionPercentage() / 100.0);
            ledger.setCalculatedAmount(commAmount);

            commissionRepo.save(ledger);
        }

        return savedOrder;
    }
}