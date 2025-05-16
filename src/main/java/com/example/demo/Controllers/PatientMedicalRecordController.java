package com.example.demo.Controllers;


import com.example.demo.Models.MedicalRecord;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.Models.Patient;

import com.example.demo.Service.UserService;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Service.MedicalRecordService;





@Controller
@RequestMapping("/patient/dashboard/medicalrecord")
public class PatientMedicalRecordController {

    private final MedicalRecordService medicalRecordService;
    private final UserService userService;

    @Autowired
    public PatientMedicalRecordController(MedicalRecordService medicalRecordService,
                                 UserService userService) {
        this.medicalRecordService = medicalRecordService;

        this.userService=userService;
    }

    @GetMapping("/edit")
    public String showEditMedicalRecord(Authentication authentication, Model model) {
        String email = authentication.getName();
        Patient patient =(Patient) userService.findByEmail(email);
        MedicalRecord medicalRecord = medicalRecordService.findByPatientUserId(patient.getUserId());
        
        model.addAttribute("patient", patient);
        model.addAttribute("medicalRecord", medicalRecord);
        return "patient/MedicalRecord";
    }

    @PostMapping("/immunization/add")
    public String addImmunization(@ModelAttribute("immunization") MedicalRecord.Immunization immunization,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
         String email = authentication.getName();
        Patient patient =(Patient) userService.findByEmail(email);
        medicalRecordService.addImmunization(patient.getUserId(), immunization);
        
        redirectAttributes.addFlashAttribute("success", "Immunization added successfully");
        return "redirect:/patient/dashboard/medicalrecord/edit";
    }

    @PostMapping("/immunization/edit/{id}")
    public String updateImmunization(@PathVariable Long id,
                                   @ModelAttribute("immunization") MedicalRecord.Immunization immunization,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
       String email = authentication.getName();
        Patient patient =(Patient) userService.findByEmail(email);
        medicalRecordService.updateImmunization(patient.getUserId(), id, immunization);
        
        redirectAttributes.addFlashAttribute("success", "Immunization updated successfully");
        return "redirect:/patient/dashboard/medicalrecord/edit";
    }

    @PostMapping("/immunization/delete/{id}")
    public String deleteImmunization(@PathVariable Long id,
                                   Authentication authentication ,
                                   RedirectAttributes redirectAttributes) {
         String email = authentication.getName();
        Patient patient =(Patient) userService.findByEmail(email);
        medicalRecordService.deleteImmunization(patient.getUserId(), id);
        
        redirectAttributes.addFlashAttribute("success", "Immunization deleted successfully");
        return "redirect:/patient/dashboard/medicalrecord/edit";
    }

    @PostMapping("/labresult/add")
    public String addLabResult(@ModelAttribute("labResult") MedicalRecord.LabResult labResult,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
       String email = authentication.getName();
        Patient patient =(Patient) userService.findByEmail(email);
        medicalRecordService.addLabResult(patient.getUserId(), labResult);
        
        redirectAttributes.addFlashAttribute("success", "Lab result added successfully");
        return "redirect:/patient/dashboard/medicalrecord/edit";
    }

    @PostMapping("/labresult/edit/{id}")
    public String updateLabResult(@PathVariable Long id,
                                @ModelAttribute("labResult") MedicalRecord.LabResult labResult,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        String email = authentication.getName();
        Patient patient =(Patient) userService.findByEmail(email);
        medicalRecordService.updateLabResult(patient.getUserId(), id, labResult);
        
        redirectAttributes.addFlashAttribute("success", "Lab result updated successfully");
        return "redirect:/patient/dashboard/medicalrecord/edit";
    }

    @PostMapping("/labresult/delete/{id}")
    public String deleteLabResult(@PathVariable Long id,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        String email = authentication.getName();
        Patient patient =(Patient) userService.findByEmail(email);
        medicalRecordService.deleteLabResult(patient.getUserId(), id);
        
        redirectAttributes.addFlashAttribute("success", "Lab result deleted successfully");
        return "redirect:/patient/dashboard/medicalrecord/edit";
    }

   
}