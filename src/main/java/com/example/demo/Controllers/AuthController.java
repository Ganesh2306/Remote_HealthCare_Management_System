package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/")
    public String showMainPage(){
        return "login";
    }

    @GetMapping("/login")
    public String showLoginForm(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "expired", required = false) String expired,
            @RequestParam(value="disabled", required=false) String disabled,
            @RequestParam(value="bad", required=false) String bad,
            @RequestParam(value="notfound" ,required=false) String notfound,
            Model model) {

        if (bad != null) {
            model.addAttribute("bad", "Invalid email or password");
        }

        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }

        if (disabled != null) {
            model.addAttribute("disabled", "Your account is disabled right now or in progress of registration.");
        }
        if (error!= null) {
            model.addAttribute("error", "Some error occured");
        }
        if (notfound!= null) {
            model.addAttribute("notfound", "User Not Registered");
        }


        return "login"; //  login.html Thymeleaf template
    }



}
