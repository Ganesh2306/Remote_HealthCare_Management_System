package com.example.demo.dto;



import java.time.LocalDateTime;
import com.example.demo.Models.Appointment;

public class AppointmentRequestDTO {
    private String doctorId;
    private LocalDateTime dateTime;
    private Appointment.Location location;
    private String reason;

    // Getters and Setters
    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }



    public Appointment.Location getLocation() {
        return location;
    }

    public void setLocation(Appointment.Location location) {
        this.location = location;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}