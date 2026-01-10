package com.qdc.lims.web;

import com.qdc.lims.entity.ReferenceRange;
import com.qdc.lims.entity.TestDefinition;
import com.qdc.lims.repository.ReferenceRangeRepository;
import com.qdc.lims.repository.TestDefinitionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RangeController {

    private final TestDefinitionRepository testRepo;
    private final ReferenceRangeRepository rangeRepo;

    public RangeController(TestDefinitionRepository testRepo, ReferenceRangeRepository rangeRepo) {
        this.testRepo = testRepo;
        this.rangeRepo = rangeRepo;
    }

    // 1. Show Range Manager for a Test
    @GetMapping("/tests/{testId}/ranges")
    public String manageRanges(@PathVariable Long testId, Model model) {
        TestDefinition test = testRepo.findById(testId).orElseThrow();
        
        model.addAttribute("test", test);
        model.addAttribute("ranges", test.getRanges()); // The list of existing rules
        model.addAttribute("newRange", new ReferenceRange()); // Empty form
        
        return "ranges-manager";
    }

    // 2. Add a New Rule
    @PostMapping("/tests/{testId}/ranges")
    public String addRange(@PathVariable Long testId, @ModelAttribute ReferenceRange range) {
        TestDefinition test = testRepo.findById(testId).orElseThrow();
        
        range.setTest(test); // Link to parent
        rangeRepo.save(range);
        
        return "redirect:/tests/" + testId + "/ranges";
    }

    // 3. Delete a Rule
    @GetMapping("/tests/{testId}/ranges/delete/{rangeId}")
    public String deleteRange(@PathVariable Long testId, @PathVariable Long rangeId) {
        rangeRepo.deleteById(rangeId);
        return "redirect:/tests/" + testId + "/ranges";
    }
}