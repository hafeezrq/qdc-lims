package com.qdc.lims.web;

import com.qdc.lims.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for handling login page requests and setup redirection.
 */
@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepo;

    /**
     * Displays the login page, or redirects to setup if no users exist.
     *
     * @return the view name for login or redirect to setup
     */
    @GetMapping("/login")
    public String loginPage() {
        // If no users exist, force redirect to setup
        if (userRepo.count() == 0) {
            return "redirect:/setup";
        }
        return "login";
    }
}