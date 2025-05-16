package com.example.demo.Controllers;

import com.example.demo.Models.*;
import com.example.demo.Service.*;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequestMapping("/doctor/dashboard")
public class DoctorDashboardController {

    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final AlertService alertService;
    private final UserService userService;
    

    @Autowired
    public DoctorDashboardController(AppointmentService appointmentService,
                                   PatientService patientService,
                                   AlertService alertService,
                                   UserService userService) {
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.alertService = alertService;
        this.userService = userService;
    }

    @GetMapping
    @Transactional
    public String showDashboard(Authentication authentication, Model model) {
        String email = authentication.getName();
        Doctor doctor = (Doctor) userService.findByEmail(email);
        
        if (doctor == null) {
            return "redirect:/login";
        }

        // Add doctor information (initialize any lazy-loaded relationships)
        initializeDoctorRelationships(doctor);
        model.addAttribute("doctor", doctor);
        
        // Handle alerts
        List<Alert> criticalAlerts = alertService.getAllAlertsforDoctor(doctor.getUserId());
        if (!criticalAlerts.isEmpty()) {
            model.addAttribute("emergencyAlert", criticalAlerts.get(0));
        }

        // Handle upcoming appointment
        appointmentService.getNextAppointmentWithinHour(doctor.getUserId())
            .ifPresent(appt -> {
                initializeAppointmentRelationships(appt);
                model.addAttribute("upcomingAppointment", appt);
            });

        // Handle today's appointments
        List<Appointment> todaysAppointments = appointmentService.getTodaysAppointments(doctor.getUserId());
        for (Appointment appointment : todaysAppointments) {
    appointment.calculatePixelOffset();
}
        todaysAppointments.forEach(this::initializeAppointmentRelationships);
        model.addAttribute("todaysAppointments", todaysAppointments);
        
        // Add counts for dashboard cards
        model.addAttribute("vitalsRequestsCount", patientService.getPendingVitalsRequestsCount(doctor.getUserId()));
        model.addAttribute("appointmentRequestsCount", appointmentService.getPendingRequestsCount(doctor.getUserId()));
        model.addAttribute("upcomingAppointmentsCount", appointmentService.getUpcomingAppointmentsCount(doctor.getUserId()));
        model.addAttribute("telemedicineSessionsCount", appointmentService.getTodayTelemedicineSessionsCount(doctor.getUserId()));
        model.addAttribute("RequestedCriticalAlertCount", alertService.getRequestedCriticalAlerts(doctor.getUserId()).size());
        
        // Add stats
        model.addAttribute("stats", new DoctorStats(
            patientService.getPatientCountForDoctor(doctor.getUserId()),
            appointmentService.getTodaysAppointmentsCount(doctor.getUserId()),
            appointmentService.getAverageWaitTime(doctor.getUserId()),
            alertService.getAllAlertsforDoctor(doctor.getUserId()).size()
        ));
        
        // Add recent alerts
        List<Alert> recentAlerts = alertService.getRecentAlertsForDoctor(doctor.getUserId(), 5);
        recentAlerts.forEach(this::initializeAlertRelationships);
        model.addAttribute("recentAlerts", recentAlerts);
        
        return "doctor/dashboard";
    }

    private void initializeDoctorRelationships(Doctor doctor) {
        // Initialize any lazy-loaded relationships for Doctor
       
        if (doctor.getSpecialization() != null) {
            Hibernate.initialize(doctor.getSpecialization());
        }
    }

    private void initializeAppointmentRelationships(Appointment appointment) {
        if (appointment.getPatient() != null) {
            Hibernate.initialize(appointment.getPatient());
          
        }
        if (appointment.getDoctor() != null) {
            Hibernate.initialize(appointment.getDoctor());
        }
    }

    private void initializeAlertRelationships(Alert alert) {
        if (alert.getPatient() != null) {
            Hibernate.initialize(alert.getPatient());
        }
        if (alert.getDoctor() != null) {
            Hibernate.initialize(alert.getDoctor());
        }
    }

    // Inner class to hold the stats data
    public static class DoctorStats {
        private final int totalPatients;
        private final int appointmentsToday;
        private final int avgWaitTime;
        private final int emergencyAlertsCount;

        public DoctorStats(int totalPatients, int appointmentsToday, int avgWaitTime, int emergencyAlertsCount) {
            this.totalPatients = totalPatients;
            this.appointmentsToday = appointmentsToday;
            this.avgWaitTime = avgWaitTime;
            this.emergencyAlertsCount = emergencyAlertsCount;
        }

        // Getters
        public int getTotalPatients() { return totalPatients; }
        public int getAppointmentsToday() { return appointmentsToday; }
        public int getAvgWaitTime() { return avgWaitTime; }
        public int getEmergencyAlertsCount() { return emergencyAlertsCount; }
    }
}