package com.example.demo.Controllers;

import com.example.demo.Exceptions.BadRequestException;
import com.example.demo.Exceptions.ResourceNotFoundException;
import com.example.demo.Models.Admin;
import com.example.demo.Models.Appointment;
import com.example.demo.Models.Doctor;
import com.example.demo.Models.Patient;
import com.example.demo.Models.SystemLog;
import com.example.demo.Service.AppointmentService;
import com.example.demo.Service.DoctorService;
import com.example.demo.Service.SystemLogService;
import com.example.demo.Service.UserService;
import com.example.demo.dto.AppointmentRequestDTO;
import com.example.demo.Utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/patient/dashboard/appointment")
public class PatientAppointmentController {

    private static final Logger log = LoggerFactory.getLogger(PatientAppointmentController.class);

    private final UserService userService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final SystemLogService logService;
    private final DateUtils dateUtils;

    public PatientAppointmentController(
            UserService userService,
            DoctorService doctorService,
            AppointmentService appointmentService,
            SystemLogService logService,
            DateUtils dateUtils) {
        this.userService = userService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.logService = logService;
        this.dateUtils = dateUtils;
    }

    @GetMapping("/book")
    public String showBookingForm(Model model,Authentication authentication) {
        String email = authentication.getName();
            Patient patient = (Patient) userService.findByEmail(email);
            model.addAttribute("patient", patient);
        model.addAttribute("appointmentRequest", new AppointmentRequestDTO());
        model.addAttribute("doctors", doctorService.getAllAvailableDoctors());
        model.addAttribute("minDateTime", dateUtils.getTodayDateTime());
        return "patient/AppointmentBook";
    }




@PostMapping("/book")
public String bookAppointment(
        @ModelAttribute("appointmentRequest") AppointmentRequestDTO appointmentRequest,
        BindingResult bindingResult,
        Authentication authentication,
        RedirectAttributes redirectAttributes) {

            String email = authentication.getName();
        Patient patient = (Patient) userService.findByEmail(email);
        Optional<Admin> admin = userService.findAdminForUser(patient);
        
    
    System.out.println("Received booking request for: " + appointmentRequest.getDateTime());

    if (bindingResult.hasErrors()) {
        System.out.println("Validation errors: " + bindingResult.getAllErrors());
        redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
        return "redirect:/patient/dashboard/appointment/book";

    }
    
    try {
        
        Doctor doctor = doctorService.findByUserId(appointmentRequest.getDoctorId());
        
        appointmentService.bookAppointment(patient, doctor, appointmentRequest);
        System.out.println("Booking successful!");
        
        redirectAttributes.addFlashAttribute("success", "Appointment booked successfully!");
        logService.createLog(admin.get(), patient.getUserId()+"scheduled appointment", SystemLog.Severity.INFO);
        return "redirect:/patient/dashboard/appointment/book";
        
    } catch (IllegalArgumentException e) {
         logService.createLog(admin.get(), patient.getUserId()+"failed to schedule appointment", SystemLog.Severity.ERROR);
        bindingResult.rejectValue("dateTime", "invalid.time", e.getMessage());
    } catch (Exception e) {
        log.error("Error booking appointment", e);
        bindingResult.reject("appointment.error", "Error booking appointment: " + e.getMessage());
        logService.createLog(admin.get(), patient.getUserId()+"failed to schedule appointment", SystemLog.Severity.ERROR);
    }
    
    redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
    return "redirect:/patient/dashboard/appointment/book";
}

    @GetMapping("/reschedule/{appointmentId}")
    public String showRescheduleForm(@PathVariable Long appointmentId, 
                                   Authentication authentication,
                                   Model model,   RedirectAttributes redirectAttributes) {
                                      String email = authentication.getName();
            Patient patient = (Patient) userService.findByEmail(email);
        try {
            log.info("finding appointment");
            Appointment appointment = appointmentService.getAppointmentByIdAndPatient(
                appointmentId, authentication.getName());
                log.info("checking can be rescheduled");
            
            if (!appointmentService.canBeRescheduled(appointment)) {
                log.info("can't be rescheduled");
                throw new BadRequestException("This appointment cannot be rescheduled");
            }

            log.info("can be rescheduled");
                model.addAttribute("patient", patient);
            model.addAttribute("appointment", appointment);
            model.addAttribute("minDateTime", dateUtils.getTodayDateTime());
            log.info("rendering page");
            return "patient/RescheduleAppointment";
            
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (BadRequestException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            log.info(e.getMessage());
            return "redirect:/patient/dashboard/appointment/requested";
        } catch(Exception e ){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            log.info(e.getMessage());
            return "redirect:/patient/dashboard/appointment/requested";

        }
    }

    @PostMapping("/reschedule/{appointmentId}")
    public String rescheduleAppointment(
            @PathVariable Long appointmentId,
            @RequestParam("startTime") String startTime,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
                     String email = authentication.getName();
        Patient patient = (Patient) userService.findByEmail(email);

                Optional<Admin> admin = userService.findAdminForUser(patient);
        
        try {
            log.info("in controller....");
            LocalDateTime newDateTime = dateUtils.parseHtmlDateTime(startTime);
            Appointment existingAppointment = appointmentService.getAppointmentByIdAndPatient(
                appointmentId, authentication.getName());
            log.info("Rescheduling....");
            appointmentService.rescheduleAppointment(
                existingAppointment.getId(), 
                newDateTime,
                existingAppointment.getLocation()
            );

            logService.createLog(admin.get(), patient.getUserId()+"Rescheduled Appointment", SystemLog.Severity.INFO);
            
            redirectAttributes.addFlashAttribute("success", 
                "Appointment rescheduled successfully!");
            return "redirect:/patient/dashboard/appointment/requested";
            
        } catch (Exception e) {
            log.error("Error rescheduling appointment", e);

             logService.createLog(admin.get(), patient.getUserId()+"Failed to Reschedule Appointment", SystemLog.Severity.ERROR);
            redirectAttributes.addFlashAttribute("error", 
                "Error rescheduling appointment: " + e.getMessage());
        }
        
        return "redirect:/patient/dashboard/appointment/requested";
    }


    @GetMapping("/requested")
    public String viewAppointments(Authentication authentication, Model model) {
        String email = authentication.getName();
        Patient patient = (Patient) userService.findByEmail(email);
        Optional<Admin> admin = userService.findAdminForUser(patient);
        log.info("inside requested page");
            model.addAttribute("patient", patient);
        List<Appointment> appointments = appointmentService.getPatientAppointments(patient.getUserId());
        model.addAttribute("appointments", appointments);
        model.addAttribute("patient", patient);
        log.info("inside requested page");
         logService.createLog(admin.get(), patient.getUserId()+"Viewed Requested Appointment", SystemLog.Severity.INFO);
        return "patient/RequestedAppointment";
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Appointment> getAppointmentDetails(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

   @PostMapping("/{id}/cancel")
public String cancelAppointment(
        @PathVariable Long id,
        @RequestParam(value = "reason", required = false) String reason,
        @RequestParam(value = "source", required = false, defaultValue = "requested") String source,
        Authentication authentication,
        RedirectAttributes redirectAttributes) {
    
    log.info("Inside cancelAppointment controller");
    log.info("Appointment ID: " + id);
         String email = authentication.getName();
        Patient patient = (Patient) userService.findByEmail(email);
    Optional<Admin> admin = userService.findAdminForUser(patient);

    try {
        log.info("Cancelling appointment");
        appointmentService.cancelAppointment(id, reason);
         logService.createLog(admin.get(), patient.getUserId()+"Cancelled Appointment", SystemLog.Severity.INFO);

        log.info("Cancellation successful");


        redirectAttributes.addFlashAttribute("successMessage", "Appointment cancelled successfully.");
    } catch (ResourceNotFoundException e) {
         logService.createLog(admin.get(), patient.getUserId()+"Cancelled Appointment", SystemLog.Severity.INFO);
        redirectAttributes.addFlashAttribute("errorMessage", "Appointment not found.");
    } catch (BadRequestException e) {
        log.info("BadRequestException: " + e.getMessage());
         logService.createLog(admin.get(), patient.getUserId()+"Failed to cancel Appointment", SystemLog.Severity.ERROR);
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    } catch (Exception e) {
        log.error("Unexpected error while cancelling appointment", e);
         logService.createLog(admin.get(), patient.getUserId()+"Failed to cancel Appointment", SystemLog.Severity.ERROR);
        redirectAttributes.addFlashAttribute("errorMessage", "Server error. Please try again later.");
    }

    return "redirect:/patient/dashboard/appointment/request";
}


    

    @GetMapping("/inperson")
    public String getConfirmedInPersonAppointments(Model model, 
                                                 Authentication authentication, 
                                                 @RequestParam(required = false) String success) {
        String email = authentication.getName();
        Patient patient = (Patient) userService.findByEmail(email);
        Optional<Admin> admin = userService.findAdminForUser(patient);
        
        List<Appointment> confirmedAppointments = appointmentService
            .getConfirmedInPersonAppointmentsForPatient(patient.getUserId());
        
        model.addAttribute("patient", patient);
        model.addAttribute("confirmedAppointments", confirmedAppointments);

         logService.createLog(admin.get(), patient.getUserId()+"Viewed In-Person Appointment", SystemLog.Severity.INFO);
        
        if (success != null) {
            model.addAttribute("successMessage", "Appointment operation completed successfully");
        }
        
        return "patient/ConfirmedAppointment";
    }

    @GetMapping("/online")
    public String showOnlineAppointments(Authentication authentication, Model model) {
        String email = authentication.getName();
        Patient patient = (Patient) userService.findByEmail(email);
        Optional<Admin> admin = userService.findAdminForUser(patient);
            model.addAttribute("patient", patient);
        List<Appointment> onlineAppointments = appointmentService.getPatientOnlineAppointments(patient.getUserId());
        
        model.addAttribute("patient", patient);
        model.addAttribute("onlineAppointments", onlineAppointments);
         logService.createLog(admin.get(), patient.getUserId()+"Viewed Online Appointment", SystemLog.Severity.INFO);
        return "patient/OnlineAppointment";
    }
}