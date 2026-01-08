package com.qdc.lims.web;

import com.qdc.lims.entity.*;
import com.qdc.lims.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RecipeController {

    private final TestDefinitionRepository testRepo;
    private final InventoryItemRepository inventoryRepo;
    private final TestConsumptionRepository consumptionRepo;

    public RecipeController(TestDefinitionRepository testRepo, InventoryItemRepository inventoryRepo,
            TestConsumptionRepository consumptionRepo) {
        this.testRepo = testRepo;
        this.inventoryRepo = inventoryRepo;
        this.consumptionRepo = consumptionRepo;
    }

    // 1. Show the Recipe Page for a specific Test
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

    // 2. Add an Ingredient to the Recipe
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

    // 3. Remove an Ingredient
    @GetMapping("/tests/{testId}/recipe/delete/{consumptionId}")
    public String removeIngredient(@PathVariable Long testId, @PathVariable Long consumptionId) {
        consumptionRepo.deleteById(consumptionId);
        return "redirect:/tests/" + testId + "/recipe";
    }
}