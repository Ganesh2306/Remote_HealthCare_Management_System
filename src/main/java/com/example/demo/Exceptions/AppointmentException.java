package com.example.demo.Exceptions;


public class AppointmentException extends RuntimeException {
    public AppointmentException(String message) {
        super(message);
    }
}