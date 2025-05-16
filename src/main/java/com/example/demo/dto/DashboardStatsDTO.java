package com.example.demo.dto;


public class DashboardStatsDTO {
    private long totalPatients;
    private long totalDoctors;
    private long totalLogs;
    private long criticalLogs;

    // Constructor
    public DashboardStatsDTO(long totalPatients, long totalDoctors, 
                           long totalLogs, long criticalLogs) {
        this.totalPatients = totalPatients;
        this.totalDoctors = totalDoctors;
        this.totalLogs = totalLogs;
        this.criticalLogs = criticalLogs;
    }

    // Getters and Setters
    public long getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(long totalPatients) {
        this.totalPatients = totalPatients;
    }

    public long getTotalDoctors() {
        return totalDoctors;
    }

    public void setTotalDoctors(long totalDoctors) {
        this.totalDoctors = totalDoctors;
    }

    public long getTotalLogs() {
        return totalLogs;
    }

    public void setTotalLogs(long totalLogs) {
        this.totalLogs = totalLogs;
    }

    public long getCriticalLogs() {
        return criticalLogs;
    }

    public void setCriticalLogs(long criticalLogs) {
        this.criticalLogs = criticalLogs;
    }
}