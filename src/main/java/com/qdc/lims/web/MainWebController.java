package com.qdc.lims.web;

import com.qdc.lims.entity.Doctor;
import com.qdc.lims.entity.LabInfo;
import com.qdc.lims.entity.Patient;
import com.qdc.lims.service.PatientService;
import com.qdc.lims.repository.LabOrderRepository;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller // Note: NOT @RestController
public class MainWebController {

    private final PatientService patientService;

    @Autowired // Add this field for the Repo
    private com.qdc.lims.repository.PatientRepository patientRepo;

    @Autowired
    private com.qdc.lims.repository.DoctorRepository doctorRepo;

    @Autowired // Inject the Test Repository
    private com.qdc.lims.repository.TestDefinitionRepository testRepo;

    @Autowired
    private com.qdc.lims.repository.InventoryItemRepository inventoryRepo;

    @Autowired
    private com.qdc.lims.repository.DoctorRepository docRepo;

    @Autowired
    private LabOrderRepository orderRepo;

    @Autowired
    private com.qdc.lims.repository.LabInfoRepository labInfoRepo;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor referringDoctor; // Visible to Receptionist ("Ref By: Dr. Bilal")

    public MainWebController(PatientService patientService) {
        this.patientService = patientService;
    }

    // 1. The Home/Dashboard Page
    @GetMapping("/")
    public String home() {
        return "index"; // Looks for index.html in templates folder
    }

    // 2. The Patient Registration Form
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // Send an empty patient object to the form to be filled
        model.addAttribute("patient", new Patient());
        return "register-patient";
    }

    // 3. Handle the Form Submit
    @PostMapping("/register")
    public String registerPatient(@ModelAttribute Patient patient, Model model) {
        try {
            // Try to save
            Patient saved = patientService.registerPatient(patient);

            // Redirect directly to the Booking Page with the new ID
            return "redirect:/book-test?patientId=" + saved.getId();

        } catch (RuntimeException e) {
            // If error (Duplicate CNIC, etc.), stay on the same page and show error
            model.addAttribute("errorMessage", e.getMessage());
            return "register-patient"; // Reloads the form
        }
    }

    // 4. The Search Page
    @GetMapping("/search")
    public String searchPage(@RequestParam(value = "query", required = false) String query, Model model) {
        if (query != null && !query.isEmpty()) {
            // If user typed something, search DB
            List<Patient> results = patientRepo.searchPatients(query);
            model.addAttribute("patients", results);
            model.addAttribute("query", query); // Keep the text in the box
        }
        return "search-patient";
    }

    // 5. Show Doctor List & Add Form

    // ================= DOCTORS SECTION =================

    // 1. Show Add Form ONLY (Clean View)
    @GetMapping("/doctors")
    public String doctorAddPage(Model model) {
        model.addAttribute("newDoctor", new Doctor());
        return "doctors-form"; // New HTML file for Form
    }

    // 2. Show List ONLY (Table View)
    @GetMapping("/doctors/list")
    public String doctorListPage(Model model) {
        model.addAttribute("doctors", doctorRepo.findAll());
        return "doctors-list"; // New HTML file for List
    }

    // 3. Save Doctor -> Redirect back to Form (so you can add another)
    @PostMapping("/doctors")
    public String saveDoctor(@ModelAttribute Doctor doctor, Model model) {
        doctorRepo.save(doctor);
        // Add a success message param
        return "redirect:/doctors?success=true";
    }

    // 7. Show Test Menu & Add Form
    // ================= TESTS SECTION =================

    // 1. Show Add Form ONLY
    @GetMapping("/tests")
    public String testsAddPage(Model model) {
        model.addAttribute("newTest", new com.qdc.lims.entity.TestDefinition());

        // SEND INVENTORY ITEMS TO THE FORM
        // Removed: model.addAttribute("inventoryItems", inventoryRepo.findAll());

        return "tests-form";
    }

    // 2. Show List ONLY
    @GetMapping("/tests/list")
    public String testsListPage(Model model) {
        model.addAttribute("tests", testRepo.findAll());
        return "tests-list"; // New HTML file
    }

    // 3. Save Test -> Redirect back to Form (so you can add another)
    @PostMapping("/tests")
    public String saveTest(@ModelAttribute com.qdc.lims.entity.TestDefinition test) {
        testRepo.save(test);
        return "redirect:/tests?success=true";
    }

    // ================= INVENTORY SECTION =================

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

    // ==================================================== //

    // 11. Show Booking Page
    @GetMapping("/book-test")
    public String bookTestPage(@RequestParam Long patientId, Model model) {
        // 1. Load Patient
        com.qdc.lims.entity.Patient patient = patientRepo.findById(patientId).orElseThrow();

        // 2. Load Doctors
        model.addAttribute("doctors", docRepo.findAll());

        // 3. Load Tests and GROUP BY Department
        List<com.qdc.lims.entity.TestDefinition> allTests = testRepo.findAll();

        // This creates a Map: { "Biochemistry": [Glucose, Chol], "Hematology": [CBC,
        // ESR] }
        Map<String, List<com.qdc.lims.entity.TestDefinition>> testsByDept = allTests.stream().collect(
                Collectors.groupingBy(test -> test.getDepartment() == null ? "General" : test.getDepartment()));

        model.addAttribute("testsByDept", testsByDept);
        model.addAttribute("patient", patient);

        return "book-test";
    }

    // ==================================================== //

    // 12. Show Cash Receipt
    @GetMapping("/orders/receipt/{orderId}")
    public String showReceipt(@PathVariable Long orderId, Model model) {
        com.qdc.lims.entity.LabOrder order = orderRepo.findById(orderId).orElseThrow();
        LabInfo info = labInfoRepo.findById(1L).orElse(new LabInfo()); // Load Info

        model.addAttribute("order", order);
        model.addAttribute("info", info); // Send to HTML

        return "receipt";
    }

    // 13. Show Medical Report (The Final Result)
    @GetMapping("/orders/report/{orderId}")
    public String showReport(@PathVariable Long orderId, Model model) {
        com.qdc.lims.entity.LabOrder order = orderRepo.findById(orderId).orElseThrow();
        LabInfo info = labInfoRepo.findById(1L).orElse(new LabInfo()); // Load Info

        model.addAttribute("order", order);
        model.addAttribute("info", info); // Send to HTML

        return "report"; // Looks for report.html
    }

    // 14. Show Settings Page
    @GetMapping("/settings")
    public String settingsPage(Model model) {
        // Load ID 1 (The only record)
        LabInfo info = labInfoRepo.findById(1L).orElse(new LabInfo());
        model.addAttribute("labInfo", info);
        return "settings";
    }

    // 15. Save Settings
    @PostMapping("/settings")
    public String saveSettings(@ModelAttribute LabInfo labInfo) {
        labInfo.setId(1L); // Force ID 1 to update the existing row
        labInfoRepo.save(labInfo);
        return "redirect:/settings?success=true";
    }

}