package com.example.demo.Controllers;


import com.example.demo.Models.Admin;
import com.example.demo.Models.SystemLog;
import com.example.demo.respository.SystemLogRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.Authentication;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.demo.Service.SystemLogService;
import com.example.demo.Service.UserService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class LogReportController {
    @Autowired
private SystemLogService logService;

    @Autowired
    private SystemLogRepository systemLogRepository;
    
    @Autowired
    private UserService userService;

    @GetMapping("/admin/dashboard/Log/download/report")
    public ResponseEntity<InputStreamResource> downloadSystemLogsReport(Authentication authentication) throws Exception {
         String email = authentication.getName();
        Admin admin = (Admin) userService.findByEmail(email);
      
        // Fetch all logs from database
        List<SystemLog> logs = systemLogRepository.findAllByOrderByTimestampDesc();
        
        // Generate PDF
        ByteArrayInputStream bis = generatePdfReport(logs);
        
        // Set response headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=system_logs_report.pdf");

        logService.createLog(admin, "Report Downloaded", SystemLog.Severity.INFO);
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    private ByteArrayInputStream generatePdfReport(List<SystemLog> logs) throws DocumentException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate()); // Landscape orientation
        
        PdfWriter.getInstance(document, out);
        document.open();
        
        // Add title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("System Logs Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20f);
        document.add(title);
        
        // Add generation date
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Paragraph generationDate = new Paragraph(
            "Generated on: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), 
            smallFont
        );
        generationDate.setAlignment(Element.ALIGN_RIGHT);
        generationDate.setSpacingAfter(20f);
        document.add(generationDate);
        
        // Create table
        PdfPTable table = new PdfPTable(4); // 4 columns
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        
        // Table headers
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        addTableHeader(table, "Timestamp", headerFont);
        addTableHeader(table, "Admin", headerFont);
        addTableHeader(table, "Action", headerFont);
        addTableHeader(table, "Severity", headerFont);
        
        // Table data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        
        for (SystemLog log : logs) {
            addTableCell(table, log.getTimestamp().format(formatter), cellFont);
            addTableCell(table, log.getAdmin().getUserId(), cellFont);
            addTableCell(table, log.getAction(), cellFont);
            
            // Color code severity
            PdfPCell severityCell = new PdfPCell(new Phrase(log.getSeverity().toString(), cellFont));
            switch(log.getSeverity()) {
                case INFO:
                    severityCell.setBackgroundColor(BaseColor.GREEN);
                    break;
                case WARNING:
                    severityCell.setBackgroundColor(BaseColor.YELLOW);
                    break;
                case ERROR:
                    severityCell.setBackgroundColor(BaseColor.ORANGE);
                    break;
                case CRITICAL:
                    severityCell.setBackgroundColor(BaseColor.RED);
                    break;
            }
            table.addCell(severityCell);
        }
        
        document.add(table);
        document.close();
        
        return new ByteArrayInputStream(out.toByteArray());
    }
    
    private void addTableHeader(PdfPTable table, String text, Font font) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
        header.setBorderWidth(2);
        header.setPhrase(new Phrase(text, font));
        table.addCell(header);
    }
    
    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        table.addCell(cell);
    }
}