package com.example.demo.Controllers;

import com.example.demo.Models.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.respository.MedicalRecordRepository;
import com.example.demo.respository.ReportRepository;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/download")
public class DownloadController {

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    
    @Autowired
    private ReportRepository reportRepository;

    @GetMapping("/patientreport/{userId}")
    public ResponseEntity<byte[]> downloadCompletePatientReport(@PathVariable String userId) throws DocumentException {
        MedicalRecord medicalRecord = medicalRecordRepository.findByPatientUserId(userId)
                .orElseThrow(() -> new RuntimeException("Medical record not found for user id: " + userId));
        
        List<Report> vitalSignsReports = reportRepository.findByVitalSignsPatientUserId(userId);
        List<Report> appointmentReports = reportRepository.findByAppointmentPatientUserId(userId);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        
        document.open();
        
        // Define fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font subsectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        
        // Cover page
        addCoverPage(document, medicalRecord, titleFont, normalFont);
        document.newPage();
        
        // Table of Contents
        addTableOfContents(document, sectionFont, normalFont, 
                !medicalRecord.getImmunizations().isEmpty() || !medicalRecord.getLabResults().isEmpty(),
                !vitalSignsReports.isEmpty(),
                !appointmentReports.isEmpty());
        document.newPage();
        
        // Patient Information Section
        addPatientInformationSection(document, medicalRecord, sectionFont, headerFont, normalFont);
        document.newPage();
        
        // Medical Record Section
        if (!medicalRecord.getImmunizations().isEmpty() || !medicalRecord.getLabResults().isEmpty()) {
            addMedicalRecordSection(document, medicalRecord, sectionFont, subsectionFont, headerFont, normalFont);
            document.newPage();
        }
        
        // Vital Signs Reports Section
        if (!vitalSignsReports.isEmpty()) {
            addVitalSignsSection(document, vitalSignsReports, sectionFont, subsectionFont, headerFont, normalFont, smallFont);
            document.newPage();
        }
        
        // Appointment Reports Section
        if (!appointmentReports.isEmpty()) {
            addAppointmentSection(document, appointmentReports, sectionFont, subsectionFont, headerFont, normalFont, smallFont);
        }
        
        document.close();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=patient_report_"+userId+".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(outputStream.toByteArray());
    }

    // ... (keep all existing helper methods like addCoverPage, addTableOfContents, etc.)

    private void addVitalSignsSection(Document document, List<Report> reports, 
                                    Font sectionFont, Font subsectionFont, Font headerFont, 
                                    Font normalFont, Font smallFont) throws DocumentException {
        Paragraph title = new Paragraph("3. VITAL SIGNS REPORTS", sectionFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        for (int i = 0; i < reports.size(); i++) {
            Report report = reports.get(i);
            VitalSigns vitalSigns = report.getVitalSigns();
            
            Paragraph reportTitle = new Paragraph("3." + (i+1) + " Report from " + 
                    vitalSigns.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), subsectionFont);
            reportTitle.setSpacingAfter(10);
            document.add(reportTitle);
            
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(60);
            table.setHorizontalAlignment(Element.ALIGN_LEFT);
            
            addVitalSignRow(table, "Body Temperature", vitalSigns.getBodyTemperature() + " °C", headerFont, normalFont);
            addVitalSignRow(table, "Pulse Rate", vitalSigns.getPulseRate() + " bpm", headerFont, normalFont);
            addVitalSignRow(table, "Respiratory Rate", vitalSigns.getRespiratoryRate() + " breaths/min", headerFont, normalFont);
            addVitalSignRow(table, "Blood Pressure", vitalSigns.getBloodPressure().getSystolic() + "/" + 
                    vitalSigns.getBloodPressure().getDiastolic() + " mmHg (" + 
                    vitalSigns.getBloodPressure().getCategory().getDescription() + ")", headerFont, normalFont);
            addVitalSignRow(table, "Oxygen Saturation", vitalSigns.getOxygenSaturation() + "%", headerFont, normalFont);
            
            if (vitalSigns.getHeight() != null) {
                addVitalSignRow(table, "Height", vitalSigns.getHeight() + " cm", headerFont, normalFont);
            }
            if (vitalSigns.getWeight() != null) {
                addVitalSignRow(table, "Weight", vitalSigns.getWeight() + " kg", headerFont, normalFont);
            }
            
            document.add(table);
            
            // Add prescription details if exists
            if (report.getPrescription() != null) {
                addPrescriptionDetails(document, report.getPrescription(), headerFont, normalFont);
            }
            
            // Add feedback details if exists
            if (report.getFeedback() != null) {
                addFeedbackDetails(document, report.getFeedback(), headerFont, normalFont);
            }
            
            document.add(Chunk.NEWLINE);
        }
    }

    private void addAppointmentSection(Document document, List<Report> reports, 
                                      Font sectionFont, Font subsectionFont, Font headerFont, 
                                      Font normalFont, Font smallFont) throws DocumentException {
        Paragraph title = new Paragraph("4. APPOINTMENT REPORTS", sectionFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        for (int i = 0; i < reports.size(); i++) {
            Report report = reports.get(i);
            Appointment appointment = report.getAppointment();
            
            Paragraph reportTitle = new Paragraph("4." + (i+1) + " Appointment on " + 
                    appointment.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), subsectionFont);
            reportTitle.setSpacingAfter(10);
            document.add(reportTitle);
            
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(80);
            
            addAppointmentRow(table, "Doctor", appointment.getDoctor().getFullName(), headerFont, normalFont);
            addAppointmentRow(table, "Date/Time", appointment.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), headerFont, normalFont);
            addAppointmentRow(table, "Duration", appointment.getDuration().toMinutes() + " minutes", headerFont, normalFont);
            addAppointmentRow(table, "Reason", appointment.getReason(), headerFont, normalFont);
            addAppointmentRow(table, "Location", appointment.getLocation().toString(), headerFont, normalFont);
            addAppointmentRow(table, "Status", appointment.getStatus().toString(), headerFont, normalFont);
            
            document.add(table);
            
            // Add prescription details if exists
            if (report.getPrescription() != null) {
                addPrescriptionDetails(document, report.getPrescription(), headerFont, normalFont);
            }
            
            // Add feedback details if exists
            if (report.getFeedback() != null) {
                addFeedbackDetails(document, report.getFeedback(), headerFont, normalFont);
            }
            
            document.add(Chunk.NEWLINE);
        }
    }

    private void addPrescriptionDetails(Document document, Prescription prescription, 
                                      Font headerFont, Font normalFont) throws DocumentException {
        Paragraph prescriptionHeader = new Paragraph("Prescription Details:", headerFont);
        prescriptionHeader.setSpacingAfter(5);
        document.add(prescriptionHeader);
        
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(80);
        table.setSpacingBefore(5);
        table.setSpacingAfter(10);
        
        addPrescriptionRow(table, "Prescribing Doctor", prescription.getPrescribingDoctor().getFullName(), headerFont, normalFont);
        addPrescriptionRow(table, "Medication", prescription.getMedication(), headerFont, normalFont);
        addPrescriptionRow(table, "Date Prescribed", prescription.getDatePrescribed().toString(), headerFont, normalFont);
        addPrescriptionRow(table, "Start Date", prescription.getStartDate().toString(), headerFont, normalFont);
        
        if (prescription.getEndDate() != null) {
            addPrescriptionRow(table, "End Date", prescription.getEndDate().toString(), headerFont, normalFont);
        }
        
        // Dosage details
        Prescription.Dosage dosage = prescription.getDosage();
        if (dosage != null) {
            addPrescriptionRow(table, "Dosage", dosage.getAmount() + " " + dosage.getUnit(), headerFont, normalFont);
            addPrescriptionRow(table, "Frequency", dosage.getFrequency().getDescription(), headerFont, normalFont);
            addPrescriptionRow(table, "Route", dosage.getRoute(), headerFont, normalFont);
        }
        
        addPrescriptionRow(table, "Instructions", prescription.getInstructions(), headerFont, normalFont);
        addPrescriptionRow(table, "Status", prescription.getStatus().toString(), headerFont, normalFont);
        
        document.add(table);
    }

    private void addFeedbackDetails(Document document, Feedback feedback, 
                                  Font headerFont, Font normalFont) throws DocumentException {
        Paragraph feedbackHeader = new Paragraph("Doctor's Feedback:", headerFont);
        feedbackHeader.setSpacingAfter(5);
        document.add(feedbackHeader);
        
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(80);
        table.setSpacingBefore(5);
        table.setSpacingAfter(10);
        
        addFeedbackRow(table, "Doctor", feedback.getDoctor().getFullName(), headerFont, normalFont);
        addFeedbackRow(table, "Date", feedback.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), headerFont, normalFont);
        
        document.add(table);
        
        // Feedback comments (can be long, so add as separate paragraph)
        Paragraph comments = new Paragraph("Comments:\n" + feedback.getComments(), normalFont);
        comments.setSpacingBefore(10);
        comments.setSpacingAfter(20);
        document.add(comments);
    }

    private void addPrescriptionRow(PdfPTable table, String label, String value, Font headerFont, Font normalFont) {
        table.addCell(createCell(label, headerFont));
        table.addCell(createCell(value != null ? value : "N/A", normalFont));
    }

    private void addFeedbackRow(PdfPTable table, String label, String value, Font headerFont, Font normalFont) {
        table.addCell(createCell(label, headerFont));
        table.addCell(createCell(value != null ? value : "N/A", normalFont));
    }

    // ... (keep all other existing helper methods)

      private void addInfoRow(PdfPTable table, String label, String value, Font headerFont, Font normalFont) {
        table.addCell(createCell(label, headerFont));
        table.addCell(createCell(value != null ? value : "Not specified", normalFont));
    }

    private void addTableHeader(PdfPTable table, Font font, String... headers) {
        Stream.of(headers)
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(1);
                    header.setPhrase(new Phrase(columnTitle, font));
                    table.addCell(header);
                });
    }

    private PdfPCell createCell(String content, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setPadding(5);
        return cell;
    }

    private void addVitalSignRow(PdfPTable table, String label, String value, Font headerFont, Font normalFont) {
        table.addCell(createCell(label, headerFont));
        table.addCell(createCell(value, normalFont));
    }

    private void addAppointmentRow(PdfPTable table, String label, String value, Font headerFont, Font normalFont) {
        table.addCell(createCell(label, headerFont));
        table.addCell(createCell(value != null ? value : "N/A", normalFont));
    }

    private void addCoverPage(Document document, MedicalRecord medicalRecord, Font titleFont, Font normalFont) throws DocumentException {
        // Title
        Paragraph title = new Paragraph("PATIENT MEDICAL REPORT", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(50);
        document.add(title);
        
        // Patient info
        Paragraph patientInfo = new Paragraph();
        patientInfo.add(new Chunk("Patient: ", normalFont));
        patientInfo.add(new Chunk(medicalRecord.getPatient().getFullName() + "\n", normalFont));
        patientInfo.add(new Chunk("Date of Birth: ", normalFont));
        patientInfo.add(new Chunk(medicalRecord.getPatient().getDateOfBirth().toString() + "\n", normalFont));
        patientInfo.add(new Chunk("Report Date: ", normalFont));
        patientInfo.add(new Chunk(java.time.LocalDate.now().toString() + "\n", normalFont));
        patientInfo.setAlignment(Element.ALIGN_CENTER);
        patientInfo.setSpacingAfter(100);
        document.add(patientInfo);
        
        // Confidential notice
        Paragraph confidential = new Paragraph("CONFIDENTIAL MEDICAL RECORD", normalFont);
        confidential.setAlignment(Element.ALIGN_CENTER);
        confidential.setSpacingAfter(20);
        document.add(confidential);
        
        Paragraph notice = new Paragraph("This document contains sensitive patient information " +
                "and is intended only for authorized medical personnel.", normalFont);
        notice.setAlignment(Element.ALIGN_CENTER);
        document.add(notice);
    }
 private void addTableOfContents(Document document, Font sectionFont, Font normalFont, 
                              boolean hasMedicalData, boolean hasVitalSigns, boolean hasAppointments) throws DocumentException {
    Paragraph tocTitle = new Paragraph("TABLE OF CONTENTS", sectionFont);
    tocTitle.setAlignment(Element.ALIGN_CENTER);
    tocTitle.setSpacingAfter(30);
    document.add(tocTitle);
    
    PdfPTable tocTable = new PdfPTable(2);
    tocTable.setWidthPercentage(80);
    tocTable.setHorizontalAlignment(Element.ALIGN_CENTER);
    
    // Patient Information
    addTocEntry(tocTable, "1. Patient Information", "1", normalFont);
    
    // Medical Record
    if (hasMedicalData) {
        addTocEntry(tocTable, "2. Medical Record", "3", normalFont);
        addTocEntry(tocTable, "   2.1 Immunizations", "3", normalFont);
        addTocEntry(tocTable, "   2.2 Lab Results", "3", normalFont);
    }
    
    // Vital Signs
    if (hasVitalSigns) {
        addTocEntry(tocTable, "3. Vital Signs Reports", String.valueOf(hasMedicalData ? 4 : 3), normalFont);
    }
    
    // Appointments
    if (hasAppointments) {
        int page = 4;
        if (hasMedicalData) page++;
        if (hasVitalSigns) page++;
        addTocEntry(tocTable, "4. Appointment Reports", String.valueOf(page), normalFont);
    }
    
    document.add(tocTable);
}

 private void addPatientInformationSection(Document document, MedicalRecord medicalRecord, 
                                           Font sectionFont, Font headerFont, Font normalFont) throws DocumentException {
        // Section title
        Paragraph title = new Paragraph("1. PATIENT INFORMATION", sectionFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        Patient patient = medicalRecord.getPatient();
        
        // Basic info table
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10);
        infoTable.setSpacingAfter(20);
        
        addInfoRow(infoTable, "Full Name:", patient.getFullName(), headerFont, normalFont);
        addInfoRow(infoTable, "Date of Birth:", patient.getDateOfBirth().toString(), headerFont, normalFont);
        addInfoRow(infoTable, "Gender:", patient.getGender().toString(), headerFont, normalFont);
        addInfoRow(infoTable, "Blood Type:", patient.getBloodType().toString(), headerFont, normalFont);
       
        
        document.add(infoTable);
    }

    private void addMedicalRecordSection(Document document, MedicalRecord medicalRecord, 
                                       Font sectionFont, Font subsectionFont, Font headerFont, Font normalFont) throws DocumentException {
        // Section title
        Paragraph title = new Paragraph("2. MEDICAL RECORD", sectionFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        // Immunizations subsection
        if (!medicalRecord.getImmunizations().isEmpty()) {
            Paragraph immunizationsTitle = new Paragraph("2.1 Immunizations", subsectionFont);
            immunizationsTitle.setSpacingAfter(10);
            document.add(immunizationsTitle);
            
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            addTableHeader(table, headerFont, "Vaccine", "Date", "Notes");
            
            medicalRecord.getImmunizations().forEach(immunization -> {
                table.addCell(createCell(immunization.getVaccineName(), normalFont));
                table.addCell(createCell(immunization.getAdministrationDate().toString(), normalFont));
                table.addCell(createCell("", normalFont)); // Placeholder for notes
            });
            
            document.add(table);
            document.add(Chunk.NEWLINE);
        }
        
        // Lab Results subsection
        if (!medicalRecord.getLabResults().isEmpty()) {
            Paragraph labResultsTitle = new Paragraph("2.2 Lab Results", subsectionFont);
            labResultsTitle.setSpacingAfter(10);
            document.add(labResultsTitle);
            
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            addTableHeader(table, headerFont, "Test", "Date", "Result", "Notes");
            
            medicalRecord.getLabResults().forEach(labResult -> {
                table.addCell(createCell(labResult.getTestName(), normalFont));
                table.addCell(createCell(labResult.getTestDate().toString(), normalFont));
                table.addCell(createCell(labResult.getResult(), normalFont));
                table.addCell(createCell("", normalFont)); // Placeholder for notes
            });
            
            document.add(table);
        }
    }


    private void addTocEntry(PdfPTable table, String title, String page, Font font) {
    PdfPCell titleCell = new PdfPCell(new Phrase(title, font));
    titleCell.setBorder(Rectangle.NO_BORDER);
    titleCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    
    PdfPCell pageCell = new PdfPCell(new Phrase(page, font));
    pageCell.setBorder(Rectangle.NO_BORDER);
    pageCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
    
    table.addCell(titleCell);
    table.addCell(pageCell);
}


}
    

  