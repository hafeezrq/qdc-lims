package com.qdc.lims.web;

import com.qdc.lims.entity.Supplier;
import com.qdc.lims.repository.SupplierRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SupplierController {

    private final SupplierRepository supplierRepo;

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
}