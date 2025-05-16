package com.example.demo.Exceptions;

public class CSVProcessingException extends RuntimeException {
    public CSVProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}