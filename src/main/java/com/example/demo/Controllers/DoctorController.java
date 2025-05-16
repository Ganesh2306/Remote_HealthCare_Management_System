package com.example.demo.Controllers;





import com.example.demo.Models.Admin;
import com.example.demo.Models.Alert;
import com.example.demo.Models.Doctor;
import com.example.demo.Models.Feedback;
import com.example.demo.Models.Prescription;
import org.springframework.data.domain.Page;
import com.example.demo.Models.SystemLog;
import com.example.demo.Models.VitalSigns;
import com.example.demo.Service.UserService;
import com.example.demo.Service.VitalSignsService;
import com.example.demo.Service.PrescriptionService;
import com.example.demo.Service.ReportService;
import com.example.demo.Service.FeedbackService;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Optional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;


import com.example.demo.Service.AlertService;

import com.example.demo.Service.SystemLogService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;





@Controller
@RequestMapping("/doctor/dashboard")
public class DoctorController {
    private final VitalSignsService vitalSignsService;
    private final UserService userService;
    private final AlertService alertService;
    private final PrescriptionService prescriptionService;
    private final FeedbackService feedbackService;
    private final ReportService reportService;

   

    private final SystemLogService logService;
    @Autowired
    public DoctorController(VitalSignsService vitalSignsService,
                              UserService userService, AlertService alertService,
                              SystemLogService logService,
                              PrescriptionService prescriptionService, FeedbackService feedbackService,
                              ReportService reportService) {
        this.vitalSignsService = vitalSignsService;
        this.userService=userService;
        this.alertService=alertService;
        this.logService=logService;
        this.feedbackService=feedbackService;
        this.prescriptionService=prescriptionService;
        this.reportService=reportService;
    }

@GetMapping("/vitals")
public String getRequestedVitals(Model model, 
                                Authentication authentication,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "4") int size) {
    
    String email = authentication.getName();
    Doctor doctor = (Doctor) userService.findByEmail(email);
    
    // Get paginated results
    Page<VitalSigns> requestedVitalsPage = vitalSignsService.getRequestedVitalsForDoctor(doctor, page, size);
    
    model.addAttribute("requestedVitals", requestedVitalsPage.getContent());
    model.addAttribute("doctor", doctor);
    
    // Add pagination attributes
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", requestedVitalsPage.getTotalPages());
    model.addAttribute("totalItems", requestedVitalsPage.getTotalElements());
    model.addAttribute("pageSize", size);
    
    // Calculate range for display
    int start = page * size + 1;
    int end =(int) Math.min((page + 1) * size, requestedVitalsPage.getTotalElements());
    model.addAttribute("start", start);
    model.addAttribute("end", end);
    
    // Calculate page numbers to display
    List<Integer> pageNumbers = new ArrayList<>();
    int totalPages = requestedVitalsPage.getTotalPages();
    int currentPage = page;
    
    // Always show first page
    pageNumbers.add(0);
    
    // Show pages around current page
    int startPage = Math.max(1, currentPage - 2);
    int endPage = Math.min(totalPages - 1, currentPage + 2);
    
    for (int i = startPage; i <= endPage; i++) {
        if (!pageNumbers.contains(i)) {
            pageNumbers.add(i);
        }
    }
    
    // Always show last page
    if (totalPages > 1 && !pageNumbers.contains(totalPages - 1)) {
        pageNumbers.add(totalPages - 1);
    }
    
    // Sort the page numbers
    Collections.sort(pageNumbers);
    model.addAttribute("pageNumbers", pageNumbers);
    
    return "doctor/Vitals";
}





      @PostMapping("/vitals/feedback")
    @ResponseBody
    public Map<String, Object> submitFeedback(
            @RequestParam Long vitalId,
            @RequestParam String patientId,
            @RequestParam String comments,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();
        try {
            String email = authentication.getName();
            Doctor doctor = (Doctor) userService.findByEmail(email);
            Feedback feedback= feedbackService.createFeedback(patientId, doctor, comments);
            vitalSignsService.UpdateFeedbackStatus(vitalId, feedback);

            response.put("success", true);
            response.put("message", "Feedback submitted successfully.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }







    @PostMapping("/vitals/{vitalId}/review")
    @ResponseBody
    public Map<String, Object> markAsReviewed(@PathVariable Long vitalId) {
        Map<String, Object> response = new HashMap<>();
        try {
           VitalSigns vitalSigns= vitalSignsService.markAsReviewed(vitalId);
            reportService.createReport(vitalSigns);
            
            response.put("success", true);
            response.put("message", "Vital signs marked as reviewed successfully.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/vitals/prescribe")
    @ResponseBody
    public Map<String, Object> createPrescription(
            @RequestParam Long vitalId,
            @RequestParam String patientId,
            @RequestParam String medication,
            @RequestParam double dosageAmount,
            @RequestParam String dosageUnit,
            @RequestParam String frequency,
            @RequestParam String route,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String instructions,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();
        try {
            String email = authentication.getName();
            Doctor doctor = (Doctor) userService.findByEmail(email);

            Prescription.Dosage dosage = new Prescription.Dosage(
                dosageAmount, dosageUnit,
                Prescription.Frequency.valueOf(frequency),
                route
            );

            Prescription prescription = prescriptionService.createPrescription(
                patientId, doctor, medication,
                dosage, startDate, endDate, instructions
            );
            vitalSignsService.UpdatePrescriptionStatus(vitalId, prescription);


            response.put("success", true);
            response.put("message", "Prescription created successfully.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create prescription: " + e.getMessage());
        }
        return response;
    }









    @GetMapping("/alerts")
    public String getAlerts(Model model, 
                         Authentication authentication) {
                             String email = authentication.getName();
        Doctor doctor = (Doctor) userService.findByEmail(email);
           Optional<Admin> admin = userService.findAdminForUser(doctor);
        List<Alert> alerts = alertService.getActiveAlertsForDoctor(email);
        model.addAttribute("doctor", doctor);
        model.addAttribute("alerts", alerts);
        logService.createLog(admin.get(), doctor.getUserId()+"viewed critical Alerts", SystemLog.Severity.INFO);
        return "doctor/Alerts";
    }




     @PostMapping("/alerts/feedback")
    @ResponseBody
    public Map<String, Object> submitAlertFeedback(
            @RequestParam Long alertId,
            @RequestParam String patientId,
            @RequestParam String comments,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();
        try {
            String email = authentication.getName();
            Doctor doctor = (Doctor) userService.findByEmail(email);
            Feedback feedback = feedbackService.createFeedback(patientId, doctor, comments);
            alertService.UpdateFeedbackStatus(alertId, feedback);

            response.put("success", true);
            response.put("message", "Feedback submitted successfully.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/alerts/{alertId}/acknowledge")
    @ResponseBody
    public Map<String, Object> acknowledgeAlert(
            @PathVariable Long alertId,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Alert alert = alertService.acknowledgeAlert(alertId);
            reportService.createReport(alert);
            
            response.put("success", true);
            response.put("message", "Alert acknowledged successfully.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/alerts/prescribe")
    @ResponseBody
    public Map<String, Object> createAlertPrescription(
            @RequestParam Long alertId,
            @RequestParam String patientId,
            @RequestParam String medication,
            @RequestParam double dosageAmount,
            @RequestParam String dosageUnit,
            @RequestParam String frequency,
            @RequestParam String route,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String instructions,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();
        try {
            String email = authentication.getName();
            Doctor doctor = (Doctor) userService.findByEmail(email);

            Prescription.Dosage dosage = new Prescription.Dosage(
                dosageAmount, dosageUnit,
                Prescription.Frequency.valueOf(frequency),
                route
            );

            Prescription prescription = prescriptionService.createPrescription(
                patientId, doctor, medication,
                dosage, startDate, endDate, instructions
            );
            alertService.UpdatePrescriptionStatus(alertId, prescription);

            response.put("success", true);
            response.put("message", "Prescription created successfully.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create prescription: " + e.getMessage());
        }
        return response;
    }




}
