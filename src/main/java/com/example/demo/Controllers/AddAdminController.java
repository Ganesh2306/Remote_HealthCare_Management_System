package com.example.demo.Controllers;

import com.example.demo.dto.AdminRegistrationRequest;

import jakarta.validation.Valid;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.demo.Service.AdminRegistrationService;


@Controller
@RequestMapping("/register/admin")
public class AddAdminController {
    
        private final AdminRegistrationService registrationService;
    
        public AddAdminController(AdminRegistrationService registrationService) {
            this.registrationService = registrationService;
        }
    
        @GetMapping
        public String showRegistrationForm(Model model) {
            model.addAttribute("admin", new AdminRegistrationRequest());
            return "admin/Add";
        }
    
        @PostMapping
        public String registerAdmin(
                @Valid @ModelAttribute("admin") AdminRegistrationRequest request,
                BindingResult bindingResult,
                Model model,
                RedirectAttributes redirectAttributes) {
    
            if (bindingResult.hasErrors()) {
                return "admin/Add";
            }
    
              try {
            registrationService.registerAdmin(request);
            redirectAttributes.addFlashAttribute("success", "Registration successfull!");
            
            return "redirect:/login"; 
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("admin", request);
            return "admin/Add";
        }
    }}