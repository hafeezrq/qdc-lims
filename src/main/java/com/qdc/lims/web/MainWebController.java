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

/**
 * Main web controller for handling all portal, dashboard, patient, doctor, test, inventory, and user management operations.
 */
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

    // @Autowired
    // private SupplierRepository supplierRepo; // Supplier Repo

    @Autowired
    private PatientService patientService;

    // ================= HOME & SETUP =================

    /**
     * Displays the home page or redirects to setup if no settings exist.
     */
    @GetMapping("/")
    public String home(Model model) {
        // First Run Check: If no settings exist, force setup
        if (labInfoRepo.count() == 0) {
            return "redirect:/setup";
        }
        return "index"; // The Portal Selection Screen
    }

    /**
     * Displays the initial setup page if settings do not exist.
     */
    @GetMapping("/setup")
    public String setupPage(Model model) {
        if (labInfoRepo.count() > 0) {
            return "redirect:/";
        }
        model.addAttribute("labInfo", new LabInfo());
        model.addAttribute("isFirstRun", true); // Flag for HTML
        return "settings";
    }

    /**
     * Saves initial setup information and creates the first admin user.
     */
    @PostMapping("/setup")
    public String saveInitialSetup(@ModelAttribute LabInfo labInfo,
            @RequestParam String adminUsername,
            @RequestParam String adminPassword,
            Model model) {

        // --- VALIDATION ---
        if (adminPassword.length() < 8 || !adminPassword.matches(".*\\d.*")) {
            model.addAttribute("isFirstRun", true);
            model.addAttribute("errorMessage", "Password too weak. Min 8 chars + 1 number.");
            return "settings"; // Go back
        }
        // ------------------

        labInfo.setId(1L);
        labInfoRepo.save(labInfo);

        if (userRepo.count() == 0) {
            com.qdc.lims.entity.User admin = new com.qdc.lims.entity.User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole("ROLE_ADMIN");
            admin.setFullName("System Administrator");
            userRepo.save(admin);
        }

        return "redirect:/login?setupSuccess=true";
    }

    // ================= SETTINGS (EDIT MODE) =================

    /**
     * Displays the settings edit page.
     */
    @GetMapping("/settings")
    public String settingsPage(Model model) {
        LabInfo info = labInfoRepo.findById(1L).orElse(new LabInfo());
        model.addAttribute("labInfo", info);
        model.addAttribute("isFirstRun", false); // Flag for HTML
        return "settings";
    }

    /**
     * Saves updated settings.
     */
    @PostMapping("/settings")
    public String saveSettings(@ModelAttribute LabInfo labInfo) {
        labInfo.setId(1L); // Force Update ID 1
        labInfoRepo.save(labInfo);
        return "redirect:/";
    }

    // ================= DASHBOARDS =================

    /**
     * Displays the admin dashboard.
     */
    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin-dashboard";
    }

    // ================= RECEPTION MODULE =================

    /**
     * Displays the patient registration form.
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("patient", new Patient());
        return "register-patient";
    }

    /**
     * Registers a new patient and redirects to booking, or shows error.
     */
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

    /**
     * Displays the patient search page and results.
     */
    @GetMapping("/search")
    public String searchPage(@RequestParam(value = "query", required = false) String query, Model model) {
        if (query != null && !query.isEmpty()) {
            List<Patient> results = patientRepo.searchPatients(query);
            model.addAttribute("patients", results);
            model.addAttribute("query", query);
        }
        return "search-patient";
    }

    /**
     * Displays the patient history page.
     */
    @GetMapping("/patient/history/{id}")
    public String patientHistoryPage(@PathVariable Long id, Model model) {
        Patient patient = patientRepo.findById(id).orElseThrow();
        List<LabOrder> orders = orderRepo.findByPatientIdOrderByIdDesc(id);
        model.addAttribute("patient", patient);
        model.addAttribute("orders", orders);
        return "patient-history";
    }

    /**
     * Pays the balance for a lab order.
     */
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

    /**
     * Displays the booking page for a patient.
     */
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

    /**
     * Displays the receipt for a lab order.
     */
    @GetMapping("/orders/receipt/{orderId}")
    public String showReceipt(@PathVariable Long orderId, Model model) {
        LabOrder order = orderRepo.findById(orderId).orElseThrow();
        // Load Lab Info for the Header
        LabInfo info = labInfoRepo.findById(1L).orElse(new LabInfo());
        model.addAttribute("info", info);
        model.addAttribute("order", order);
        return "receipt";
    }

    // 13a. Show Thermal Receipt (80mm)
    /**
     * Displays the thermal receipt for a lab order.
     */
    @GetMapping("/orders/receipt/thermal/{orderId}")
    public String showThermalReceipt(@PathVariable Long orderId, Model model) {
        LabOrder order = orderRepo.findById(orderId).orElseThrow();
        LabInfo info = labInfoRepo.findById(1L).orElse(new LabInfo());

        model.addAttribute("order", order);
        model.addAttribute("info", info);
        return "receipt-thermal";
    }

    /**
     * Displays the report for a lab order.
     */
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

    /**
     * Displays the doctor add form.
     */
    @GetMapping("/doctors")
    public String doctorAddPage(Model model) {
        model.addAttribute("newDoctor", new Doctor());
        return "doctors-form";
    }

    /**
     * Displays the doctor list page.
     */
    @GetMapping("/doctors/list")
    public String doctorListPage(Model model) {
        model.addAttribute("doctors", doctorRepo.findAll());
        return "doctors-list";
    }

    /**
     * Saves a doctor entity.
     */
    @PostMapping("/doctors")
    public String saveDoctor(@ModelAttribute Doctor doctor) {
        doctorRepo.save(doctor);
        return "redirect:/doctors?success=true";
    }

    // ================= TESTS =================

    /**
     * Displays the test add form.
     */
    @GetMapping("/tests")
    public String testsAddPage(Model model) {
        model.addAttribute("newTest", new com.qdc.lims.entity.TestDefinition());
        return "tests-form";
    }

    /**
     * Displays the test list page.
     */
    @GetMapping("/tests/list")
    public String testsListPage(Model model) {
        model.addAttribute("tests", testRepo.findAll());
        return "tests-list";
    }

    /**
     * Saves a test definition entity.
     */
    @PostMapping("/tests")
    public String saveTest(@ModelAttribute com.qdc.lims.entity.TestDefinition test) {
        testRepo.save(test);
        return "redirect:/tests?success=true";
    }

    // ================= INVENTORY =================

    /**
     * Displays the inventory page.
     */
    @GetMapping("/inventory")
    public String inventoryPage(Model model) {
        model.addAttribute("items", inventoryRepo.findAll());
        model.addAttribute("newItem", new com.qdc.lims.entity.InventoryItem());
        return "inventory";
    }

    /**
     * Saves an inventory item entity.
     */
    @PostMapping("/inventory")
    public String saveInventory(@ModelAttribute com.qdc.lims.entity.InventoryItem item) {
        inventoryRepo.save(item);
        return "redirect:/inventory";
    }

    // ================= USER MANAGEMENT =================

    /**
     * Displays the user list and add form.
     */
    // 19. Show User List & Add Form
    @GetMapping("/admin/users")
    public String usersPage(Model model) {
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("newUser", new com.qdc.lims.entity.User());
        return "users-list";
    }

    /**
     * Saves a new or updated user entity.
     */
    // 20. Save New User
    @PostMapping("/admin/users")
    public String saveUser(@ModelAttribute com.qdc.lims.entity.User user, Model model) {
        
        // Check if this is an Update (ID exists) or Create (ID null)
        boolean isUpdate = (user.getId() != null);

        // --- PASSWORD LOGIC ---
        if (isUpdate) {
            // Fetch existing from DB to get the old password
            com.qdc.lims.entity.User existing = userRepo.findById(user.getId()).orElseThrow();
            
            if (user.getPassword().isEmpty()) {
                // Admin left password blank -> Keep old password
                user.setPassword(existing.getPassword());
            } else {
                // Admin typed a new password -> Validate & Encrypt
                if (user.getPassword().length() < 8) {
                    return returnUserError(model, user, "Error: Password must be 8+ chars.");
                }
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        } else {
            // Creating New User -> Password is Mandatory
            if (user.getPassword().isEmpty() || user.getPassword().length() < 8) {
                return returnUserError(model, user, "Error: Password is required (8+ chars).");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // --- DUPLICATE USERNAME CHECK ---
        // Only check if username changed (for updates) or is new
        com.qdc.lims.entity.User existingUser = userRepo.findByUsername(user.getUsername()).orElse(null);
        if (existingUser != null && !existingUser.getId().equals(user.getId())) {
             return returnUserError(model, user, "Error: Username already exists.");
        }

        user.setActive(true); // Ensure active
        userRepo.save(user);
        return "redirect:/admin/users?success=true";
    }

    /**
     * Deletes (deactivates) a user by ID.
     */
    // 21. Delete (Deactivate) User
    @GetMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        com.qdc.lims.entity.User user = userRepo.findById(id).orElseThrow();
        // Soft Delete: Just turn them off so they can't login
        user.setActive(false);
        userRepo.save(user);
        return "redirect:/admin/users";
    }

    /**
     * Loads a user for editing by ID.
     */
    // 22. Load User for Editing
    @GetMapping("/admin/users/edit/{id}")
    public String editUserPage(@PathVariable Long id, Model model) {
        // Load the specific user
        com.qdc.lims.entity.User user = userRepo.findById(id).orElseThrow();

        // Load the list (so the right side of screen is still full)
        model.addAttribute("users", userRepo.findAll());

        // Put the user in the form
        model.addAttribute("newUser", user);

        return "users-list";
    }

    /**
     * Displays the access denied page.
     */
    // 23. Friendly Access Denied Page
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    /**
     * Helper method to reload the user page with an error message.
     */
    private String returnUserError(Model model, com.qdc.lims.entity.User user, String msg) {
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("newUser", user);
        model.addAttribute("errorMessage", msg);
        return "users-list";
    }
}