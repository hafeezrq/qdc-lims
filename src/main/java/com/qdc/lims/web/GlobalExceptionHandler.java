package com.qdc.lims.web;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Catch specific business logic errors (like we threw in Service)
    @ExceptionHandler(RuntimeException.class)
    public String handleLogicError(RuntimeException ex, Model model) {
        model.addAttribute("errorTitle", "Action Failed");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error-custom"; // We will create this HTML
    }

    // Catch unexpected system crashes (NullPointer, Database down, etc.)
    @ExceptionHandler(Exception.class)
    public String handleSystemError(Exception ex, Model model) {
        ex.printStackTrace(); // Print to console for the developer to see
        
        model.addAttribute("errorTitle", "System Error");
        model.addAttribute("errorMessage", "Something went wrong internally. Please contact support.");
        // We don't show the real technical error to the user for security reasons
        
        return "error-custom";
    }
}