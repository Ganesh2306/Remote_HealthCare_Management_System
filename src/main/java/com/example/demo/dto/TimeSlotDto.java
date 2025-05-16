package com.example.demo.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimeSlotDto {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private LocalTime startTime;
    private LocalTime endTime;

    // Constructor for date+time slots
    public TimeSlotDto(LocalDateTime start, LocalDateTime end) {
        this.startDateTime = start;
        this.endDateTime = end;
    }

    // Constructor for time-only slots
    public TimeSlotDto(LocalTime start, LocalTime end) {
        this.startTime = start;
        this.endTime = end;
    }

    // Getters and setters
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public LocalTime getStartTime() {
        return startTime != null ? startTime : startDateTime.toLocalTime();
    }

    public LocalTime getEndTime() {
        return endTime != null ? endTime : endDateTime.toLocalTime();
    }

    // Other methods as needed
}