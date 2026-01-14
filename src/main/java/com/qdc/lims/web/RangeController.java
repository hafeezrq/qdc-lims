package com.qdc.lims.web;

import com.qdc.lims.entity.ReferenceRange;
import com.qdc.lims.entity.TestDefinition;
import com.qdc.lims.repository.ReferenceRangeRepository;
import com.qdc.lims.repository.TestDefinitionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing reference ranges for lab tests.
 */
@Controller
public class RangeController {

    private final TestDefinitionRepository testRepo;
    private final ReferenceRangeRepository rangeRepo;

    /**
     * Constructs a RangeController with the required repositories.
     *
     * @param testRepo repository for test definitions
     * @param rangeRepo repository for reference ranges
     */
    public RangeController(TestDefinitionRepository testRepo, ReferenceRangeRepository rangeRepo) {
        this.testRepo = testRepo;
        this.rangeRepo = rangeRepo;
    }

    /**
     * Displays the range manager for a specific test.
     *
     * @param testId the ID of the test
     * @param model the model to pass data to the view
     * @return the view name for the range manager
     */
    @GetMapping("/tests/{testId}/ranges")
    public String manageRanges(@PathVariable Long testId, Model model) {
        TestDefinition test = testRepo.findById(testId).orElseThrow();
        
        model.addAttribute("test", test);
        model.addAttribute("ranges", test.getRanges()); // The list of existing rules
        model.addAttribute("newRange", new ReferenceRange()); // Empty form
        
        return "ranges-manager";
    }

    /**
     * Adds a new reference range rule to a test.
     *
     * @param testId the ID of the test
     * @param range the reference range to add
     * @return redirect to the range manager view
     */
    @PostMapping("/tests/{testId}/ranges")
    public String addRange(@PathVariable Long testId, @ModelAttribute ReferenceRange range) {
        TestDefinition test = testRepo.findById(testId).orElseThrow();
        
        range.setTest(test); // Link to parent
        rangeRepo.save(range);
        
        return "redirect:/tests/" + testId + "/ranges";
    }

    /**
     * Deletes a reference range rule from a test.
     *
     * @param testId the ID of the test
     * @param rangeId the ID of the reference range to delete
     * @return redirect to the range manager view
     */
    @GetMapping("/tests/{testId}/ranges/delete/{rangeId}")
    public String deleteRange(@PathVariable Long testId, @PathVariable Long rangeId) {
        rangeRepo.deleteById(rangeId);
        return "redirect:/tests/" + testId + "/ranges";
    }
}