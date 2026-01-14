package com.qdc.lims.web;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Controller advice for handling exceptions globally and displaying user-friendly error pages.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles business logic errors and displays a custom error page.
     *
     * @param ex the RuntimeException thrown
     * @param model the model to pass error details to the view
     * @return the view name for the custom error page
     */
    @ExceptionHandler(RuntimeException.class)
    public String handleLogicError(RuntimeException ex, Model model) {
        model.addAttribute("errorTitle", "Action Failed");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error-custom"; // We will create this HTML
    }

    /**
     * Handles unexpected system errors and displays a generic error page.
     *
     * @param ex the Exception thrown
     * @param model the model to pass error details to the view
     * @return the view name for the custom error page
     */
    @ExceptionHandler(Exception.class)
    public String handleSystemError(Exception ex, Model model) {
        ex.printStackTrace(); // Print to console for the developer to see
        
        model.addAttribute("errorTitle", "System Error");
        model.addAttribute("errorMessage", "Something went wrong internally. Please contact support.");
        // We don't show the real technical error to the user for security reasons
        
        return "error-custom";
    }
}