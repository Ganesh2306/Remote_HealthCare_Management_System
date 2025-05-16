package com.example.demo.Exceptions;

public  class CSVParseException extends RuntimeException {
    public CSVParseException(String message) {
        super(message);
    }
    public CSVParseException(String message, Throwable cause) {
        super(message, cause);
    }
}