package com.qdc.lims.web;

import com.qdc.lims.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepo;

    @GetMapping("/login")
    public String loginPage() {
        // If no users exist, force redirect to setup
        if (userRepo.count() == 0) {
            return "redirect:/setup";
        }
        return "login";
    }
}