package com.qdc.lims.web;

import com.qdc.lims.entity.Doctor;
import com.qdc.lims.entity.LabInfo;
import com.qdc.lims.entity.LabOrder;
import com.qdc.lims.entity.Patient;
import com.qdc.lims.repository.*;
import com.qdc.lims.service.PatientService;
import com.qdc.lims.util.QrCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class MainWebController {

    @Autowired
    private PatientRepository patientRepo;
    @Autowired
    private DoctorRepository doctorRepo;
    @Autowired
    private TestDefinitionRepository testRepo;
    @Autowired
    private InventoryItemRepository inventoryRepo;
    @Autowired
    private LabOrderRepository orderRepo;
    @Autowired
    private LabInfoRepository labInfoRepo; // Settings Repo
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SupplierRepository supplierRepo; // Supplier Repo

    @Autowired
    private PatientService patientService;

    // ================= HOME & SETUP =================

    @GetMapping("/")
    public String home(Model model) {
        // First Run Check: If no settings exist, force setup
        if (labInfoRepo.count() == 0) {
            return "redirect:/setup";
        }
        return "index"; // The Portal Selection Screen
    }

    @GetMapping("/setup")
    public String setupPage(Model model) {
        if (labInfoRepo.count() > 0) {
            return "redirect:/";
        }
        model.addAttribute("labInfo", new LabInfo());
        model.addAttribute("isFirstRun", true); // Flag for HTML
        return "settings";
    }

    @PostMapping("/setup")
    public String saveInitialSetup(@ModelAttribute LabInfo labInfo, @RequestParam String adminUsername,
            @RequestParam String adminPassword) {

        // 1. Save Lab Info
        labInfo.setId(1L);
        labInfoRepo.save(labInfo);

        // 2. Create the Super Admin User
        if (userRepo.count() == 0) {
            com.qdc.lims.entity.User admin = new com.qdc.lims.entity.User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword)); // Encrypt!
            admin.setRole("ROLE_ADMIN");
            admin.setFullName("System Administrator");
            userRepo.save(admin);
        }

        return "redirect:/login";
    }

    // ================= SETTINGS (EDIT MODE) =================

    @GetMapping("/settings")
    public String settingsPage(Model model) {
        LabInfo info = labInfoRepo.findById(1L).orElse(new LabInfo());
        model.addAttribute("labInfo", info);
        model.addAttribute("isFirstRun", false); // Flag for HTML
        return "settings";
    }

    // THIS IS THE METHOD YOU WERE LIKELY MISSING OR WAS BROKEN
    @PostMapping("/settings")
    public String saveSettings(@ModelAttribute LabInfo labInfo) {
        labInfo.setId(1L); // Force Update ID 1
        labInfoRepo.save(labInfo);
        return "redirect:/";
    }

    // ================= DASHBOARDS =================

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin-dashboard";
    }

    // ================= RECEPTION MODULE =================

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("patient", new Patient());
        return "register-patient";
    }

    @PostMapping("/register")
    public String registerPatient(@ModelAttribute Patient patient, Model model) {
        try {
            Patient saved = patientService.registerPatient(patient);
            // Redirect to Booking
            return "redirect:/book-test?patientId=" + saved.getId();
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register-patient";
        }
    }

    @GetMapping("/search")
    public String searchPage(@RequestParam(value = "query", required = false) String query, Model model) {
        if (query != null && !query.isEmpty()) {
            List<Patient> results = patientRepo.searchPatients(query);
            model.addAttribute("patients", results);
            model.addAttribute("query", query);
        }
        return "search-patient";
    }

    @GetMapping("/patient/history/{id}")
    public String patientHistoryPage(@PathVariable Long id, Model model) {
        Patient patient = patientRepo.findById(id).orElseThrow();
        List<LabOrder> orders = orderRepo.findByPatientIdOrderByIdDesc(id);
        model.addAttribute("patient", patient);
        model.addAttribute("orders", orders);
        return "patient-history";
    }

    @PostMapping("/orders/pay-balance")
    public String payBalance(@RequestParam Long orderId, @RequestParam Double amount) {
        LabOrder order = orderRepo.findById(orderId).orElseThrow();
        double newPaid = order.getPaidAmount() + amount;
        order.setPaidAmount(newPaid);
        // order.calculateBalance() is called automatically by Entity @PreUpdate
        orderRepo.save(order);
        return "redirect:/patient/history/" + order.getPatient().getId() + "?paymentSuccess=true";
    }

    // ================= BOOKING & ORDERS =================

    @GetMapping("/book-test")
    public String bookTestPage(@RequestParam Long patientId, Model model) {
        Patient patient = patientRepo.findById(patientId).orElseThrow();
        model.addAttribute("doctors", doctorRepo.findAll());

        // Group Tests by Department
        List<com.qdc.lims.entity.TestDefinition> allTests = testRepo.findAll();
        Map<String, List<com.qdc.lims.entity.TestDefinition>> testsByDept = allTests.stream().collect(
                Collectors.groupingBy(test -> test.getDepartment() == null ? "General" : test.getDepartment()));

        model.addAttribute("testsByDept", testsByDept);
        model.addAttribute("patient", patient);
        return "book-test";
    }

    @GetMapping("/orders/receipt/{orderId}")
    public String showReceipt(@PathVariable Long orderId, Model model) {
        LabOrder order = orderRepo.findById(orderId).orElseThrow();
        // Load Lab Info for the Header
        LabInfo info = labInfoRepo.findById(1L).orElse(new LabInfo());
        model.addAttribute("info", info);
        model.addAttribute("order", order);
        return "receipt";
    }

    @GetMapping("/orders/report/{orderId}")
    public String showReport(@PathVariable Long orderId, Model model) {
        LabOrder order = orderRepo.findById(orderId).orElseThrow();
        LabInfo info = labInfoRepo.findById(1L).orElse(new LabInfo());

        model.addAttribute("order", order);
        model.addAttribute("info", info);

        // QR Code Generation
        String qrContent = "Verified Report | ID: " + order.getId() + " | Patient: " + order.getPatient().getFullName();
        String qrImageBase64 = QrCodeUtil.generateBase64Qr(qrContent, 150, 150);
        model.addAttribute("qrImage", qrImageBase64);

        return "report";
    }

    // ================= DOCTORS =================

    @GetMapping("/doctors")
    public String doctorAddPage(Model model) {
        model.addAttribute("newDoctor", new Doctor());
        return "doctors-form";
    }

    @GetMapping("/doctors/list")
    public String doctorListPage(Model model) {
        model.addAttribute("doctors", doctorRepo.findAll());
        return "doctors-list";
    }

    @PostMapping("/doctors")
    public String saveDoctor(@ModelAttribute Doctor doctor) {
        doctorRepo.save(doctor);
        return "redirect:/doctors?success=true";
    }

    // ================= TESTS =================

    @GetMapping("/tests")
    public String testsAddPage(Model model) {
        model.addAttribute("newTest", new com.qdc.lims.entity.TestDefinition());
        return "tests-form";
    }

    @GetMapping("/tests/list")
    public String testsListPage(Model model) {
        model.addAttribute("tests", testRepo.findAll());
        return "tests-list";
    }

    @PostMapping("/tests")
    public String saveTest(@ModelAttribute com.qdc.lims.entity.TestDefinition test) {
        testRepo.save(test);
        return "redirect:/tests?success=true";
    }

    // ================= INVENTORY =================

    @GetMapping("/inventory")
    public String inventoryPage(Model model) {
        model.addAttribute("items", inventoryRepo.findAll());
        model.addAttribute("newItem", new com.qdc.lims.entity.InventoryItem());
        return "inventory";
    }

    @PostMapping("/inventory")
    public String saveInventory(@ModelAttribute com.qdc.lims.entity.InventoryItem item) {
        inventoryRepo.save(item);
        return "redirect:/inventory";
    }

    // ================= USER MANAGEMENT =================

    // 19. Show User List & Add Form
    @GetMapping("/admin/users")
    public String usersPage(Model model) {
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("newUser", new com.qdc.lims.entity.User());
        return "users-list";
    }

    // 20. Save New User
    @PostMapping("/admin/users")
    public String saveUser(@ModelAttribute com.qdc.lims.entity.User user) {
        // Encrypt the password before saving!
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true); // Default to active
        userRepo.save(user);
        return "redirect:/admin/users?success=true";
    }

    // 21. Friendly Access Denied Page
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied"; 
    }
}