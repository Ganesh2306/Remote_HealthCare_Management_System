package com.example.demo.Utils;

import org.springframework.stereotype.Component;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class DateUtils {
    
    // Common date/time formatters
    private static final DateTimeFormatter HTML_DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HTML_DATETIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter HTML_DATETIME_WITH_SECONDS = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter DISPLAY_DATETIME_FORMATTER = 
        DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    /**
     * Gets current date in HTML input format (yyyy-MM-dd)
     */
    public String getTodayDate() {
        return LocalDate.now().format(HTML_DATE_FORMATTER);
    }

    /**
     * Gets current datetime in HTML input format (yyyy-MM-dd'T'HH:mm)
     */
    public String getTodayDateTime() {
        return LocalDateTime.now().format(HTML_DATETIME_FORMATTER);
    }

    /**
     * Parses HTML datetime input (with seconds)
     */
    public LocalDateTime parseHtmlDateTime(String htmlDateTime) {
        try {
            return LocalDateTime.parse(htmlDateTime, HTML_DATETIME_WITH_SECONDS);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid datetime format", e);
        }
    }

    /**
     * Formats date for display (e.g. "May 12, 2025")
     */
    public String formatDisplayDate(LocalDate date) {
        return date.format(DISPLAY_DATE_FORMATTER);
    }

    /**
     * Formats datetime for display (e.g. "May 12, 2025 02:30 PM")
     */
    public String formatDisplayDateTime(LocalDateTime dateTime) {
        return dateTime.format(DISPLAY_DATETIME_FORMATTER);
    }

    /**
     * Checks if a date is in the future
     */
    public boolean isFutureDate(LocalDate date) {
        return date.isAfter(LocalDate.now());
    }

    /**
     * Checks if a datetime is in the future
     */
    public boolean isFutureDateTime(LocalDateTime dateTime) {
        return dateTime.isAfter(LocalDateTime.now());
    }

    /**
     * Gets minimum datetime for HTML input (now + 1 hour)
     */
    public String getMinimumBookingDateTime() {
        return LocalDateTime.now().plusHours(1).format(HTML_DATETIME_FORMATTER);
    }

    /**
     * Calculates duration between two datetimes in minutes
     */
    public long calculateDurationMinutes(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end).toMinutes();
    }

    /**
     * Converts LocalDate to LocalDateTime at start of day
     */
    public LocalDateTime atStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }
    /**
     * Converts LocalDate to LocalDateTime at end of day
     */
    public LocalDateTime atEndOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }
}