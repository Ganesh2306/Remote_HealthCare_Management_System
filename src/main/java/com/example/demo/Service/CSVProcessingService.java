package com.example.demo.Service;

import org.apache.commons.csv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.example.demo.Models.VitalSigns;
import com.example.demo.Exceptions.CSVParseException;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class CSVProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(CSVProcessingService.class);
    private static final Set<String> REQUIRED_HEADERS = Set.of(
        "timestamp", "bodyTemperature", "pulseRate", "respiratoryRate",
        "systolicBP", "diastolicBP", "oxygenSaturation", "painLevel"
    );

    public List<VitalSigns> parseCSV(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             CSVParser parser = CSVParser.parse(reader, CSVFormat.DEFAULT
                 .withFirstRecordAsHeader()
                 .withIgnoreHeaderCase()
                 .withTrim()
                 .withIgnoreEmptyLines())) {
            
            validateHeaders(parser.getHeaderMap().keySet());
            List<VitalSigns> vitals = new ArrayList<>();
            int lineNumber = 1; // Header is line 1

            for (CSVRecord record : parser) {
                lineNumber++;
                try {
                    VitalSigns vital = parseRecord(record);
                    vitals.add(vital);
                } catch (Exception e) {
                    String errorMsg = String.format("Error in line %d: %s", lineNumber, e.getMessage());
                    logger.error(errorMsg);
                    throw new CSVParseException(errorMsg, e);
                }
            }
            
            if (vitals.isEmpty()) {
                throw new CSVParseException("CSV file contains no valid data");
            }
            
            return vitals;
        }
    }

    private VitalSigns parseRecord(CSVRecord record) {
        VitalSigns vital = new VitalSigns();
        
        try {
            vital.setTimestamp(parseTimestamp(getRequiredValue(record, "timestamp")));
            vital.setBodyTemperature(parseDouble(getRequiredValue(record, "bodyTemperature"), "bodyTemperature", 20.0, 45.0));
            vital.setPulseRate(parseInt(getRequiredValue(record, "pulseRate"), "pulseRate", 30, 200));
            vital.setRespiratoryRate(parseInt(getRequiredValue(record, "respiratoryRate"), "respiratoryRate", 5, 60));
            
            int systolic = parseInt(getRequiredValue(record, "systolicBP"), "systolicBP", 50, 250);
            int diastolic = parseInt(getRequiredValue(record, "diastolicBP"), "diastolicBP", 30, 150);
            vital.setBloodPressure(new VitalSigns.BloodPressure(systolic, diastolic));
            
            vital.setOxygenSaturation(parseDouble(getRequiredValue(record, "oxygenSaturation"), "oxygenSaturation", 70.0, 100.0));
            
            if (record.isSet("height") && !record.get("height").isBlank()) {
                vital.setHeight(parseDouble(record.get("height"), "height", 50.0, 250.0));  // height in cm
            }
            if (record.isSet("weight") && !record.get("weight").isBlank()) {
                vital.setWeight(parseDouble(record.get("weight"), "weight", 2.0, 300.0));  // weight in kg
            }
            
            vital.setPainLevel(parsePainLevel(getRequiredValue(record, "painLevel")));
            
            return vital;
        } catch (Exception e) {
            throw new CSVParseException(String.format("Error parsing record at line %d: %s", 
                record.getRecordNumber(), e.getMessage()), e);
        }
    }

    private String getRequiredValue(CSVRecord record, String field) {
        if (!record.isSet(field) || record.get(field).isBlank()) {
            throw new CSVParseException(String.format("Missing required field: %s", field));
        }
        return record.get(field);
    }

    private void validateHeaders(Set<String> headers) {
        Set<String> missingHeaders = new HashSet<>();
        for (String required : REQUIRED_HEADERS) {
            if (!headers.stream().anyMatch(h -> h.equalsIgnoreCase(required))) {
                missingHeaders.add(required);
            }
        }
        
        if (!missingHeaders.isEmpty()) {
            throw new CSVParseException("Missing required headers: " + missingHeaders);
        }
    }

    private LocalDateTime parseTimestamp(String value) {
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            throw new CSVParseException(String.format("Invalid timestamp format: %s (expected ISO-8601 format)", value), e);
        }
    }

    private int parseInt(String value, String fieldName, int min, int max) {
        try {
            int num = Integer.parseInt(value);
            if (num < min || num > max) {
                throw new CSVParseException(
                    String.format("%s value %s is out of range (%d-%d)", 
                    fieldName, value, min, max));
            }
            return num;
        } catch (NumberFormatException e) {
            throw new CSVParseException(
                String.format("Invalid %s value: %s (must be an integer)", 
                fieldName, value), e);
        }
    }

    private double parseDouble(String value, String fieldName, double min, double max) {
        try {
            double num = Double.parseDouble(value);
            if (num < min || num > max) {
                throw new CSVParseException(
                    String.format("%s value %s is out of range (%.1f-%.1f)", 
                    fieldName, value, min, max));
            }
            return num;
        } catch (NumberFormatException e) {
            throw new CSVParseException(
                String.format("Invalid %s value: %s (must be a number)", 
                fieldName, value), e);
        }
    }

    private VitalSigns.PainLevel parsePainLevel(String value) {
        try {
            return VitalSigns.PainLevel.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CSVParseException(String.format("Invalid pain level: %s. Valid values are: %s", 
                value, Arrays.toString(VitalSigns.PainLevel.values())));
        }
    }
}