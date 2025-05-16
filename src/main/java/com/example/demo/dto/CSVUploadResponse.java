package com.example.demo.dto;

import java.util.List;

public class CSVUploadResponse {
    private boolean success;
    private int totalRecords;       // Total records in CSV (was recordsProcessed)
    private int successfulRecords;  // Successfully processed (was successCount)
    private int failedRecords;      // Failed to process (was errorCount)
    private String message;
    private List<String> errors;
    private String fileName;

    // Constructors
    public CSVUploadResponse() {
    }

    public CSVUploadResponse(boolean success, int totalRecords, int successfulRecords, 
                           int failedRecords, String message, List<String> errors, 
                           String fileName) {
        this.success = success;
        this.totalRecords = totalRecords;
        this.successfulRecords = successfulRecords;
        this.failedRecords = failedRecords;
        this.message = message;
        this.errors = errors;
        this.fileName = fileName;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getSuccessfulRecords() {
        return successfulRecords;
    }

    public void setSuccessfulRecords(int successfulRecords) {
        this.successfulRecords = successfulRecords;
    }

    public int getFailedRecords() {
        return failedRecords;
    }

    public void setFailedRecords(int failedRecords) {
        this.failedRecords = failedRecords;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    // Helper methods
    public static CSVUploadResponse success(int totalRecords, int successfulRecords, String fileName) {
        return new CSVUploadResponse(
            true,
            totalRecords,
            successfulRecords,
            totalRecords - successfulRecords,
            String.format("Processed %d/%d records successfully", successfulRecords, totalRecords),
            null,
            fileName
        );
    }

    public static CSVUploadResponse partialSuccess(int totalRecords, int successfulRecords, 
                                                 List<String> errors, String fileName) {
        return new CSVUploadResponse(
            false,
            totalRecords,
            successfulRecords,
            errors != null ? errors.size() : 0,
            String.format("Processed %d/%d records with %d errors", 
                successfulRecords, totalRecords, errors != null ? errors.size() : 0),
            errors,
            fileName
        );
    }

    public static CSVUploadResponse error(String message, List<String> errors) {
        return new CSVUploadResponse(
            false,
            0,
            0,
            errors != null ? errors.size() : 0,
            message,
            errors,
            null
        );
    }

    @Override
    public String toString() {
        return "CSVUploadResponse{" +
            "success=" + success +
            ", totalRecords=" + totalRecords +
            ", successfulRecords=" + successfulRecords +
            ", failedRecords=" + failedRecords +
            ", message='" + message + '\'' +
            ", errors=" + errors +
            ", fileName='" + fileName + '\'' +
            '}';
    }
}