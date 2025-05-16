package com.example.demo.Security;

import com.example.demo.Models.Patient;
import com.example.demo.Models.SystemLog;
import com.example.demo.Service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.example.demo.Models.Doctor;
import com.example.demo.Models.Admin;
import java.util.Optional;
import com.example.demo.Service.SystemLogService;
@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

     private final UserService userService;
    private final SystemLogService systemLogService; // Changed from logService to systemLogService

    public CustomAuthenticationSuccessHandler(UserService userService, 
                                           SystemLogService systemLogService) {
        this.userService = userService;
        this.systemLogService = systemLogService; // Match parameter name
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {

                                         String email = authentication.getName();

        if(userService.findByEmail(email) instanceof Patient){
      Patient patient=(Patient) userService.findByEmail(email);
        
        // Find responsible admin and create log
        Optional<Admin> admin = userService.findAdminForUser(patient);
        if (admin.isPresent()) {
            systemLogService.createLog(
                admin.get(),
                "Patient_LOGIN "+patient.getUserId(),
                SystemLog.Severity.INFO
            );
        }}
        else if (userService.findByEmail(email) instanceof Doctor){
        // Find responsible admin and create log
         Doctor doctor=(Doctor) userService.findByEmail(email);
        Optional<Admin> admin = userService.findAdminForUser(doctor);
        if (admin.isPresent()) {
            systemLogService.createLog(
                admin.get(),
                "DOCTOR_LOGIN"+doctor.getUserId(),
                SystemLog.Severity.INFO
            );
        }}

        else{
            Admin admin=(Admin) userService.findByEmail(email);
            systemLogService.createLog(admin, "ADMIN_LOGIN"+admin.getUserId(), SystemLog.Severity.INFO);

        }
        userService.updateLastLogin(authentication.getName());
        getRedirectStrategy().sendRedirect(request, response, determineTargetUrl(authentication));
    }

    protected String determineTargetUrl(Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        return switch (role) {
            case "ROLE_ADMIN" -> "/admin/dashboard";
            case "ROLE_DOCTOR" -> "/doctor/dashboard";
            case "ROLE_PATIENT" -> "/patient/dashboard";
            default -> throw new IllegalStateException("Invalid role: " + role);
        };
    }
}