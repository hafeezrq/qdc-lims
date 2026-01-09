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

}