package com.example.demo.Controllers;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;

import com.example.demo.Service.AppointmentService;

import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.time.Duration;



@RestController
@RequestMapping("/api/doctors")
public class DoctorApiController {
    
    private final AppointmentService appointmentService;
    
    public DoctorApiController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }
   @GetMapping("/{doctorId}/availability")
public ResponseEntity<Map<String, Object>> getDoctorAvailability(
        @PathVariable String doctorId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(required = false) String duration,
        @RequestParam(required = false) String excludeAppointmentId) {

    if (date == null) {
        date = LocalDate.now();
    }

    Duration parsedDuration = Duration.ofMinutes(30);

    Map<String, Object> availability = appointmentService.getDoctorAvailability(
        doctorId, date, parsedDuration, excludeAppointmentId
    );

    return ResponseEntity.ok(availability);
}

}