package com.example.demo.Service;

import com.example.demo.Models.SystemLog;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;
import com.example.demo.Models.Admin;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class LogExportService {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ByteArrayInputStream exportLogsToPdf(List<SystemLog> logs,
                                          String dateFrom,
                                          String dateTo,
                                          String severityFilter,Admin admin) throws DocumentException {
    
    Objects.requireNonNull(logs, "Logs list cannot be null");
    
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Document document = new Document();
    
    try {
        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();

        // Add content to document
        addTitle(document, admin.getFullName());
        addFilters(document, dateFrom, dateTo, severityFilter);
        addLogTable(document, logs);
        addFooter(document);

        document.close();
        writer.close();
        
        return new ByteArrayInputStream(out.toByteArray());
        
    } catch (Exception e) {
        if (document.isOpen()) {
            document.close();
        }
        throw new DocumentException("Failed to generate PDF", e);
    } finally {
        try {
            out.close();
        } catch (IOException e) {
            // Log error if needed
        }
    }
}

    private void addTitle(Document document, String adminName) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("HealthTrack Pro - Activity Logs Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(5f);
        document.add(title);

        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 14);
        Paragraph subtitle = new Paragraph("For: " + adminName, subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(15f);
        document.add(subtitle);
    }

    private void addFilters(Document document, String dateFrom, String dateTo, String severityFilter) 
            throws DocumentException {
        
        Font filterFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Paragraph filters = new Paragraph();
        
        filters.add(new Chunk("Date Range: ", filterFont));
        filters.add(new Chunk(
            (dateFrom == null ? "Start" : dateFrom) + " to " + 
            (dateTo == null ? "End" : dateTo), 
            filterFont
        ));
        filters.add(Chunk.NEWLINE);
        filters.add(new Chunk("Severity: ", filterFont));
        filters.add(new Chunk(
            severityFilter == null || severityFilter.isEmpty() ? "All Levels" : severityFilter, 
            filterFont
        ));
        filters.setSpacingAfter(15f);
        document.add(filters);
    }

    private void addLogTable(Document document, List<SystemLog> logs) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(20f);

        // Table headers
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        Stream.of("Timestamp", "Severity", "Action")
              .forEach(header -> {
                  PdfPCell cell = new PdfPCell();
                  cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                  cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                  cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                  cell.setPhrase(new Phrase(header, headerFont));
                  table.addCell(cell);
              });

        // Table rows
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA);
        for (SystemLog log : logs) {
            if (log == null) continue;
            
            // Timestamp cell
            String timestamp = log.getTimestamp() != null 
                ? TIMESTAMP_FORMATTER.format(log.getTimestamp()) 
                : "N/A";
            table.addCell(createCell(timestamp, cellFont, BaseColor.WHITE));
            
            // Severity cell
            BaseColor severityColor = getSeverityColor(log.getSeverity());
            PdfPCell severityCell = createCell(
                log.getSeverity() != null ? log.getSeverity().name() : "UNKNOWN", 
                cellFont, 
                severityColor
            );
            severityCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(severityCell);

            // Action cell
            table.addCell(createCell(
                log.getAction() != null ? log.getAction() : "N/A", 
                cellFont, 
                BaseColor.WHITE
            ));
        }

        document.add(table);
    }

    private PdfPCell createCell(String content, Font font, BaseColor backgroundColor) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setBackgroundColor(backgroundColor);
        cell.setPadding(5);
        return cell;
    }

    private BaseColor getSeverityColor(SystemLog.Severity severity) {
        if (severity == null) return BaseColor.WHITE;
        
        switch (severity) {
            case INFO:
                return new BaseColor(200, 230, 255); // Light blue
            case WARNING:
                return new BaseColor(255, 230, 150); // Light orange
            case ERROR:
            case CRITICAL:
                return new BaseColor(255, 200, 200); // Light red
            default:
                return BaseColor.WHITE;
        }
    }

    private void addFooter(Document document) throws DocumentException {
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10);
        Paragraph footer = new Paragraph(
            "Generated on: " + TIMESTAMP_FORMATTER.format(LocalDateTime.now()), 
            footerFont
        );
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);
    }
}