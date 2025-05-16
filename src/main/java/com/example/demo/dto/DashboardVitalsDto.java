package com.example.demo.dto;


import com.example.demo.Models.VitalSigns;
import java.util.List;

public class DashboardVitalsDto {
    private VitalSigns latestVitals;
    private VitalSigns previousVitals; 
    private VitalTrendsDto trends;
    private List<String> alerts;
    private boolean hasData;
    private int vitalsCount;

    // Constructors
    public DashboardVitalsDto() {
    }

    public DashboardVitalsDto(VitalSigns latestVitals, 
                            VitalTrendsDto trends, 
                            List<String> alerts, 
                            boolean hasData, int vitalsCount) {
        this.latestVitals = latestVitals;
        this.trends = trends;
        this.alerts = alerts;
        this.hasData = hasData;
        this.vitalsCount=vitalsCount;
    }

    // Getters and Setters
    public VitalSigns getLatestVitals() {
        return latestVitals;
    }


    public void setLatestVitals(VitalSigns latestVitals) {
        this.latestVitals = latestVitals;
    }
    public VitalSigns getPreviousVitals() {
        return previousVitals;
    }
    
    public void setPreviousVitals(VitalSigns previousVitals) {
        this.previousVitals = previousVitals;
    }

    public VitalTrendsDto getTrends() {
        return trends;
    }

public int getVitalsCount() {
    return vitalsCount;
}


public void setVitalsCount(int count) {
    this.vitalsCount = count;
}
    public void setTrends(VitalTrendsDto trends) {
        this.trends = trends;
    }

    public List<String> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<String> alerts) {
        this.alerts = alerts;
    }

    public boolean isHasData() {
        return hasData;
    }

    public void setHasData(boolean hasData) {
        this.hasData = hasData;
    }

    // Utility Methods
    public boolean hasAlerts() {
        return alerts != null && !alerts.isEmpty();
    }
}