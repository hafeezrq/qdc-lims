package com.qdc.lims.web;

import com.qdc.lims.entity.Supplier;
import com.qdc.lims.repository.SupplierRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SupplierController {

    private final SupplierRepository supplierRepo;

    @Autowired
    private com.qdc.lims.repository.InventoryItemRepository inventoryRepo;
    @Autowired
    private com.qdc.lims.service.PurchaseService purchaseService;
    @Autowired
    private com.qdc.lims.repository.SupplierLedgerRepository ledgerRepo;

    public SupplierController(SupplierRepository supplierRepo) {
        this.supplierRepo = supplierRepo;
    }

    // 1. Show Add Form (Default)
    @GetMapping("/admin/suppliers")
    public String addSupplierPage(Model model) {
        model.addAttribute("newSupplier", new Supplier());
        return "suppliers-form";
    }

    // 2. Save Supplier
    @PostMapping("/admin/suppliers")
    public String saveSupplier(@ModelAttribute Supplier supplier) {
        supplierRepo.save(supplier);
        return "redirect:/admin/suppliers?success=true";
    }

    // 3. Show List
    @GetMapping("/admin/suppliers/list")
    public String listSuppliers(Model model) {
        model.addAttribute("suppliers", supplierRepo.findAll());
        return "suppliers-list";
    }

    // 4. Show Purchase Entry Screen
    @GetMapping("/admin/suppliers/purchase")
    public String purchasePage(Model model) {
        model.addAttribute("suppliers", supplierRepo.findAll());
        model.addAttribute("items", inventoryRepo.findAll());
        return "purchase-entry";
    }

    // 5. API to Save Purchase (Called via JavaScript/Axios)
    @org.springframework.web.bind.annotation.ResponseBody // Important for JSON
    @PostMapping("/api/suppliers/purchase")
    public org.springframework.http.ResponseEntity<String> savePurchase(
            @org.springframework.web.bind.annotation.RequestBody com.qdc.lims.dto.PurchaseRequest request) {
        purchaseService.processPurchase(request);
        return org.springframework.http.ResponseEntity.ok("Saved");
    }

    // 6. View Supplier Ledger (Statement)
    @GetMapping("/admin/suppliers/ledger/{id}")
    public String viewLedger(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        // 1. Get Supplier
        Supplier supplier = supplierRepo.findById(id).orElseThrow();

        // 2. Get History (Newest first)
        java.util.List<com.qdc.lims.entity.SupplierLedger> transactions = ledgerRepo
                .findBySupplierIdOrderByTransactionDateDesc(id);

        // 3. Calculate Totals
        double totalBilled = transactions.stream().mapToDouble(l -> l.getBillAmount()).sum();
        double totalPaid = transactions.stream().mapToDouble(l -> l.getPaidAmount()).sum();
        double balance = totalBilled - totalPaid;

        model.addAttribute("supplier", supplier);
        model.addAttribute("transactions", transactions);
        model.addAttribute("totalBilled", totalBilled);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("balance", balance);

        return "supplier-ledger";
    }

}