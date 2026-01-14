package com.qdc.lims.web;

import com.qdc.lims.entity.*;
import com.qdc.lims.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing test recipes and their ingredients.
 */
@Controller
public class RecipeController {

    private final TestDefinitionRepository testRepo;
    private final InventoryItemRepository inventoryRepo;
    private final TestConsumptionRepository consumptionRepo;

    /**
     * Constructs a RecipeController with the required repositories.
     *
     * @param testRepo repository for test definitions
     * @param inventoryRepo repository for inventory items
     * @param consumptionRepo repository for test consumptions
     */
    public RecipeController(TestDefinitionRepository testRepo, InventoryItemRepository inventoryRepo,
            TestConsumptionRepository consumptionRepo) {
        this.testRepo = testRepo;
        this.inventoryRepo = inventoryRepo;
        this.consumptionRepo = consumptionRepo;
    }

    /**
     * Displays the recipe manager page for a specific test.
     *
     * @param testId the ID of the test
     * @param model the model to pass data to the view
     * @return the view name for the recipe manager
     */
    @GetMapping("/tests/{testId}/recipe")
    public String showRecipe(@PathVariable Long testId, Model model) {
        TestDefinition test = testRepo.findById(testId).orElseThrow();

        model.addAttribute("test", test);
        // Load the existing ingredients for this test
        model.addAttribute("ingredients", consumptionRepo.findByTest(test));
        // Load all available stock items for the dropdown
        model.addAttribute("allItems", inventoryRepo.findAll());

        return "recipe-manager";
    }

    /**
     * Adds an ingredient to the recipe for a test.
     *
     * @param testId the ID of the test
     * @param itemId the ID of the inventory item
     * @param quantity the quantity of the item to add
     * @return redirect to the recipe manager view
     */
    @PostMapping("/tests/{testId}/recipe")
    public String addIngredient(@PathVariable Long testId,
            @RequestParam Long itemId,
            @RequestParam Double quantity) {

        TestDefinition test = testRepo.findById(testId).orElseThrow();
        InventoryItem item = inventoryRepo.findById(itemId).orElseThrow();

        TestConsumption consumption = new TestConsumption();
        consumption.setTest(test);
        consumption.setItem(item);
        consumption.setQuantity(quantity);

        consumptionRepo.save(consumption);

        return "redirect:/tests/" + testId + "/recipe";
    }

    /**
     * Removes an ingredient from the recipe for a test.
     *
     * @param testId the ID of the test
     * @param consumptionId the ID of the test consumption to remove
     * @return redirect to the recipe manager view
     */
    @GetMapping("/tests/{testId}/recipe/delete/{consumptionId}")
    public String removeIngredient(@PathVariable Long testId, @PathVariable Long consumptionId) {
        consumptionRepo.deleteById(consumptionId);
        return "redirect:/tests/" + testId + "/recipe";
    }
}