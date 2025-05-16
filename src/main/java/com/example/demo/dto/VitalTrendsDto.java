package com.example.demo.dto;



import java.util.ArrayList;
import java.util.List;

public class VitalTrendsDto {
    private List<String> timestamps = new ArrayList<>();
    private List<Integer> heartRates = new ArrayList<>();
    private List<Integer> systolicBP = new ArrayList<>();
    private List<Integer> diastolicBP = new ArrayList<>();
    private List<Double> oxygenLevels = new ArrayList<>();
    private List<Double> temperatures = new ArrayList<>();
    private List<Integer> respiratoryRates = new ArrayList<>();

    // Getters
    public List<String> getTimestamps() {
        return timestamps;
    }

    public List<Integer> getHeartRates() {
        return heartRates;
    }

    public List<Integer> getSystolicBP() {
        return systolicBP;
    }

    public List<Integer> getDiastolicBP() {
        return diastolicBP;
    }

    public List<Double> getOxygenLevels() {
        return oxygenLevels;
    }

    public List<Double> getTemperatures() {
        return temperatures;
    }

    public List<Integer> getRespiratoryRates() {
        return respiratoryRates;
    }

    // Utility Methods
    public boolean isEmpty() {
        return timestamps.isEmpty();
    }

    public void addDataPoint(String timestamp,
                           Integer heartRate,
                           Integer systolic,
                           Integer diastolic,
                           Double oxygen,
                           Double temperature,
                           Integer respiratoryRate) {
        this.timestamps.add(timestamp);
        this.heartRates.add(heartRate);
        this.systolicBP.add(systolic);
        this.diastolicBP.add(diastolic);
        this.oxygenLevels.add(oxygen);
        this.temperatures.add(temperature);
        this.respiratoryRates.add(respiratoryRate);
    }
}