package com.example.demo.Controllers;


import com.example.demo.dto.DashboardStatsDTO;
import com.example.demo.dto.DoctorRegistrationRequest;
import com.example.demo.Service.AdminService;
import jakarta.validation.Valid;


import com.example.demo.Models.Admin;
import com.example.demo.Models.Patient;
import com.example.demo.Models.Doctor;
import com.example.demo.Models.SystemLog;
import com.example.demo.Service.AdminDashboardStatsService;
import com.example.demo.Service.PatientService;
import com.example.demo.Service.SystemLogService;
import com.example.demo.Service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.demo.Service.DoctorService;


import java.util.List;

@Controller
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardStatsService statsService;
    private final SystemLogService logService;
    private final UserService userService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AdminService adminService;

    public AdminDashboardController(AdminDashboardStatsService statsService,
                                  SystemLogService logService,
                                  UserService userService,
                                  PatientService patientService,
                                  DoctorService doctorService,
                                  AdminService adminService) {
        this.statsService = statsService;
        this.logService = logService;
        this.userService = userService;
        this.patientService = patientService;
        this.doctorService=doctorService;
        this.adminService=adminService;
    }

    @GetMapping
    public String showDashboard(Model model, Authentication authentication) {
        String email = authentication.getName();
        Admin admin = (Admin) userService.findByEmail(email);

        // Add admin details to model
        model.addAttribute("admin", admin);
        
        // Get and add dashboard statistics
        DashboardStatsDTO stats = statsService.getDashboardStatistics();
        model.addAttribute("stats", stats);
        
        // Add recent activities (logs)
        List<SystemLog> recentActivities = logService.getRecentLogs(10);
        model.addAttribute("recentActivities", recentActivities);
        
        return "admin/dashboard";
    }

    @GetMapping("/manage/patients")
    public String viewPatientList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "search", required = false) String searchQuery,
            Model model,Authentication authentication) {
                String email = authentication.getName();
                Admin admin = (Admin) userService.findByEmail(email);
        
                // Add admin details to model
                model.addAttribute("admin", admin);
        
        // Validate pagination parameters
        page = Math.max(1, page);
        size = size > 0 && size <= 50 ? size : DEFAULT_PAGE_SIZE;
        
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Page<Patient> patientsPage = adminService.getPatientsPage(searchQuery, pageRequest);
        
        adminService.populateModelAttributes(model, patientsPage, searchQuery);
        
        logService.createLog(admin, "Viewed patient list", SystemLog.Severity.INFO);
        
        return "admin/patientlist";
    }

   @RequestMapping(value = "/manage/patients/deactivate/{patientId}", 
                   method = {RequestMethod.PUT, RequestMethod.POST})
    @PreAuthorize("hasRole('ADMIN')")
    public String deactivatePatient(
            @PathVariable String patientId,
            @RequestParam String deactivateReason,
            RedirectAttributes redirectAttributes, Authentication authentication) {
                  String email = authentication.getName();
                Admin admin = (Admin) userService.findByEmail(email);
        
        try {
          
            validateReason(deactivateReason, "Deactivation");
            
            patientService.deactivatePatient(patientId, admin, deactivateReason);
            
            redirectAttributes.addFlashAttribute("successMessage",
                "Patient deactivated successfully. Reason: " + deactivateReason);
                
            logService.createLog(admin, 
                "Patient deactivated: " + patientId, 
                SystemLog.Severity.INFO);
                
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            logService.createLog(admin, 
                "Deactivation validation failed: " + e.getMessage(), 
                SystemLog.Severity.WARNING);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Failed to deactivate patient: " + e.getMessage());
                
            logService.createLog(admin, 
                "Deactivation failed for patient: " + patientId, 
                SystemLog.Severity.ERROR);
        }
        
        return "redirect:/admin/dashboard/manage/patients";
    }

    @RequestMapping(value = "/manage/patients/reactivate/{patientId}", 
                   method = {RequestMethod.PUT, RequestMethod.POST})
    @PreAuthorize("hasRole('ADMIN')")
    public String reactivatePatient(
            @PathVariable String patientId,
            @RequestParam String reactivateReason,
            RedirectAttributes redirectAttributes, Authentication authentication) {
                  String email = authentication.getName();
                Admin admin = (Admin) userService.findByEmail(email);
        
        try {
            validateReason(reactivateReason, "Reactivation");
            
            patientService.reactivatePatient(patientId, admin, reactivateReason);
            
            redirectAttributes.addFlashAttribute("successMessage",
                "Patient reactivated successfully. Reason: " + reactivateReason);
                
            logService.createLog(admin, 
                "Patient reactivated: " + patientId, 
                SystemLog.Severity.INFO);
                
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            logService.createLog(admin, 
                "Reactivation validation failed: " + e.getMessage(), 
                SystemLog.Severity.WARNING);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Failed to reactivate patient: " + e.getMessage());
                
            logService.createLog(admin, 
                "Reactivation failed for patient: " + patientId, 
                SystemLog.Severity.ERROR);
        }
        
        return "redirect:/admin/dashboard/manage/patients";
    }


 private void validateReason(String reason, String actionType) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException(actionType + " reason is required");
        }
    }

    // Helper methods
    private static final int DEFAULT_PAGE_SIZE = 10;




   @GetMapping("/manage/doctors")
public String listDoctors(
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "10") int size,
        @RequestParam(value = "search", required = false) String searchQuery,
        Model model,
        Authentication authentication) {
            
    String email = authentication.getName();
    Admin admin = (Admin) userService.findByEmail(email);

    // Add admin details to model
    model.addAttribute("admin", admin);

    // Validate pagination parameters
    page = Math.max(1, page);
    size = size > 0 && size <= 50 ? size : DEFAULT_PAGE_SIZE;
    
    PageRequest pageRequest = PageRequest.of(page - 1, size);
    Page<Doctor> doctorsPage = adminService.getDoctorsPage(searchQuery, pageRequest);
    
    adminService.populateDoctorModelAttributes(model, doctorsPage, searchQuery);
    
    logService.createLog(admin, "Viewed doctor list", SystemLog.Severity.INFO);
    
    return "admin/doctorlist";
}

@GetMapping("/manage/doctors/add")
public String showAddForm(Model model) {
       DoctorRegistrationRequest doctor = new DoctorRegistrationRequest(); // Will have one empty qualification
    model.addAttribute("doctor", doctor);
    model.addAttribute("specializations", Doctor.Specialization.values());
    return "admin/adddoctor";
}

@PostMapping("/manage/doctors/add")
public String addDoctor(
        @Valid @ModelAttribute("doctor") DoctorRegistrationRequest request,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes,
        Authentication authentication) {
    
    if (bindingResult.hasErrors()) {
        return "admin/adddoctor";
    }
    
    try {
        String email = authentication.getName();
        Admin admin = (Admin) userService.findByEmail(email);
        
        doctorService.registerDoctor(request, admin);
    
        
        redirectAttributes.addFlashAttribute("successMessage", "Doctor registered successfully!");
        logService.createLog(admin, "Added new doctor: " + request.getEmail(), SystemLog.Severity.WARNING);
        
        return "redirect:/admin/dashboard/manage/doctors"; 
    } catch (Exception e) {
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("doctor", request);
        return "admin/adddoctor";
    }
}

@RequestMapping(value = "/manage/doctors/deactivate/{doctorId}", 
               method = {RequestMethod.PUT, RequestMethod.POST})
@PreAuthorize("hasRole('ADMIN')")
public String deactivateDoctor(
        @PathVariable String doctorId,
        @RequestParam String deactivateReason,
        RedirectAttributes redirectAttributes, 
        Authentication authentication) {
    
    String email = authentication.getName();
    Admin admin = (Admin) userService.findByEmail(email);
    
    try {
        validateReason(deactivateReason, "Deactivation");
        
        doctorService.deactivateDoctor(doctorId,deactivateReason);
        
        redirectAttributes.addFlashAttribute("successMessage",
            "Doctor deactivated successfully. Reason: " + deactivateReason);
            
        logService.createLog(admin, 
            "Doctor deactivated: " + doctorId, 
            SystemLog.Severity.WARNING);
            
    } catch (IllegalArgumentException e) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        logService.createLog(admin, 
            "Deactivation validation failed: " + e.getMessage(), 
            SystemLog.Severity.WARNING);
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("errorMessage",
            "Failed to deactivate doctor: " + e.getMessage());
            
        logService.createLog(admin, 
            "Deactivation failed for doctor: " + doctorId, 
            SystemLog.Severity.ERROR);
    }
    
    return "redirect:/admin/dashboard/manage/doctors";
}

@RequestMapping(value = "/manage/doctors/reactivate/{doctorId}", 
               method = {RequestMethod.PUT, RequestMethod.POST})
@PreAuthorize("hasRole('ADMIN')")
public String reactivateDoctor(
        @PathVariable String doctorId,
        @RequestParam String reactivateReason,
        RedirectAttributes redirectAttributes, 
        Authentication authentication) {
    
    String email = authentication.getName();
    Admin admin = (Admin) userService.findByEmail(email);
    
    try {
        validateReason(reactivateReason, "Reactivation");
        
        doctorService.reactivateDoctor(doctorId, reactivateReason,admin);
        
        redirectAttributes.addFlashAttribute("successMessage",
            "Doctor reactivated successfully. Reason: " + reactivateReason);
            
        logService.createLog(admin, 
            "Doctor reactivated: " + doctorId, 
            SystemLog.Severity.WARNING);
            
    } catch (IllegalArgumentException e) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        logService.createLog(admin, 
            "Reactivation validation failed: " + e.getMessage(), 
            SystemLog.Severity.WARNING);
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("errorMessage",
            "Failed to reactivate doctor: " + e.getMessage());
            
        logService.createLog(admin, 
            "Reactivation failed for doctor: " + doctorId, 
            SystemLog.Severity.ERROR);
    }
    
    return "redirect:/admin/dashboard/manage/doctors";
}




}


