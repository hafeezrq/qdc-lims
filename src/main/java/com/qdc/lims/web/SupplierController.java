package com.qdc.lims.web;

import com.qdc.lims.entity.Supplier;
import com.qdc.lims.repository.SupplierRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Controller for managing suppliers, purchases, and supplier ledger operations.
 */
@Controller
public class SupplierController {

    private final SupplierRepository supplierRepo;

    @Autowired
    private com.qdc.lims.repository.InventoryItemRepository inventoryRepo;
    @Autowired
    private com.qdc.lims.service.PurchaseService purchaseService;
    @Autowired
    private com.qdc.lims.repository.SupplierLedgerRepository ledgerRepo;

    /**
     * Constructs a SupplierController with the specified SupplierRepository.
     *
     * @param supplierRepo repository for suppliers
     */
    public SupplierController(SupplierRepository supplierRepo) {
        this.supplierRepo = supplierRepo;
    }

    /**
     * Displays the add supplier form.
     *
     * @param model the model to pass data to the view
     * @return the view name for the add supplier form
     */
    // 1. Show Add Form (Default)
    @GetMapping("/admin/suppliers")
    public String addSupplierPage(Model model) {
        model.addAttribute("newSupplier", new Supplier());
        return "suppliers-form";
    }

    /**
     * Saves a supplier entity.
     *
     * @param supplier the supplier to save
     * @return redirect to the add supplier page with success flag
     */
    // 2. Save Supplier
    @PostMapping("/admin/suppliers")
    public String saveSupplier(@ModelAttribute Supplier supplier) {
        supplierRepo.save(supplier);
        return "redirect:/admin/suppliers?success=true";
    }

    /**
     * Displays the list of suppliers.
     *
     * @param model the model to pass data to the view
     * @return the view name for the supplier list
     */
    // 3. Show List
    @GetMapping("/admin/suppliers/list")
    public String listSuppliers(Model model) {
        model.addAttribute("suppliers", supplierRepo.findAll());
        return "suppliers-list";
    }

    /**
     * Displays the purchase entry screen for suppliers.
     *
     * @param model the model to pass data to the view
     * @return the view name for purchase entry
     */
    // 4. Show Purchase Entry Screen
    @GetMapping("/admin/suppliers/purchase")
    public String purchasePage(Model model) {
        model.addAttribute("suppliers", supplierRepo.findAll());
        model.addAttribute("items", inventoryRepo.findAll());
        return "purchase-entry";
    }

    /**
     * API endpoint to save a purchase request for a supplier.
     *
     * @param request the purchase request data
     * @return ResponseEntity with success or error message
     */
    // 5. API to Save Purchase (Called via JavaScript/Axios)
    @org.springframework.web.bind.annotation.ResponseBody
    @PostMapping("/api/suppliers/purchase")
    public org.springframework.http.ResponseEntity<?> savePurchase(
            @org.springframework.web.bind.annotation.RequestBody com.qdc.lims.dto.PurchaseRequest request) {
        try {
            purchaseService.processPurchase(request);
            return org.springframework.http.ResponseEntity.ok("Saved");
        } catch (RuntimeException e) {
            // Send the specific error message (e.g. "Duplicate Invoice") back to the
            // browser
            return org.springframework.http.ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    /**
     * Displays the supplier ledger (statement) for a specific supplier.
     *
     * @param id the ID of the supplier
     * @param model the model to pass data to the view
     * @return the view name for the supplier ledger
     */
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