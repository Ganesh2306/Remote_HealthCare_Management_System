package com.example.demo.Controllers;

import com.example.demo.dto.PatientRegistrationRequest;
import com.example.demo.Service.PatientRegistrationService;
import jakarta.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/register")
public class PatientRegistrationController {

    private final PatientRegistrationService registrationService;

    public PatientRegistrationController(PatientRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping
    public String showRegistrationForm(Model model) {
        model.addAttribute("patient", new PatientRegistrationRequest());
        return "register";
    }

    @PostMapping
    public String registerPatient(
            @Valid @ModelAttribute("patient") PatientRegistrationRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

          try {
        registrationService.registerPatient(request);
        redirectAttributes.addFlashAttribute("success", "Registration successful! Please check your email.");
        
        // Use this instead of string concatenation
        return "redirect:/register/checkEmail?username=" + 
               URLEncoder.encode(request.getFirstName(), StandardCharsets.UTF_8) + 
               "&email=" + URLEncoder.encode(request.getEmail(), StandardCharsets.UTF_8);
    } catch (Exception e) {
        model.addAttribute("error", e.getMessage());
        model.addAttribute("patient", request);
        return "register";
    }
    }

    @GetMapping("/checkEmail")
    public String showCheckEmailPage(
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "email", required = false) String email,
            Model model,
            @ModelAttribute("success") String successMessage) {
        
        model.addAttribute("username", username != null ? username : "User");
        model.addAttribute("email", email != null ? email : "user@example.com");
        
        if (successMessage != null) {
            model.addAttribute("success", successMessage);
        }
        
        return "checkemail";
    }
}