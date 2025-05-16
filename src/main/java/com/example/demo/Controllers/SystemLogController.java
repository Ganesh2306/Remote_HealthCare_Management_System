package com.example.demo.Controllers;

import com.example.demo.Models.SystemLog;
import com.example.demo.Models.Admin;
import com.example.demo.respository.SystemLogRepository;
import com.example.demo.Service.UserService;
import com.example.demo.Service.LogExportService;
import com.example.demo.Service.SystemLogService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.InputStreamResource;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Controller
public class SystemLogController {

    private final SystemLogRepository systemLogRepository;
    private final UserService userService;
    private final LogExportService pdfExportService;
    private final SystemLogService systemLogService;

    public SystemLogController(SystemLogRepository systemLogRepository, 
                             UserService userService,
                             LogExportService pdfExportService, SystemLogService systemLogService) {
        this.systemLogRepository = systemLogRepository;
        this.userService = userService;
        this.pdfExportService = pdfExportService;
        this.systemLogService=systemLogService;
    }

    // Existing view endpoint
    @GetMapping("/admin/dashboard/Log/view")
    public String getMyLogs(
            Authentication authentication,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) SystemLog.Severity severity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        
        String email = authentication.getName();
        Admin admin = (Admin) userService.findByEmail(email);

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        LocalDateTime startDate = parseDate(dateFrom, true);
        LocalDateTime endDate = parseDate(dateTo, false);

        Page<SystemLog> logsPage = getFilteredLogs(admin, startDate, endDate, severity, pageable);

        model.addAttribute("logs", logsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", logsPage.getTotalPages());
        model.addAttribute("admin", admin);
        systemLogService.createLog(admin, "Log Activites reviewed", SystemLog.Severity.INFO);

        return "admin/adminlog";
    }

    // New PDF export endpoint
    @GetMapping("/admin/dashboard/log/filter/download")
    public ResponseEntity<InputStreamResource> exportLogsToPdf(
            Authentication authentication,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) SystemLog.Severity severity) {
        
        String email = authentication.getName();
        Admin admin = (Admin) userService.findByEmail(email);

        LocalDateTime startDate = parseDate(from, true);
        LocalDateTime endDate = parseDate(to, false);

        // Get all logs without pagination for export
        Pageable unlimited = Pageable.unpaged();
        Page<SystemLog> logsPage = getFilteredLogs(admin, startDate, endDate, severity, unlimited);

        try {
            ByteArrayInputStream bis = pdfExportService.exportLogsToPdf(
                logsPage.getContent(),
                from != null ? from : "All",
                to != null ? to : "All",
                severity != null ? severity.toString() : "All",
                admin
            );

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=activity_logs_report.pdf");
             systemLogService.createLog(admin, "Filtered log Downloaded reviewed", SystemLog.Severity.INFO);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(bis));
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private LocalDateTime parseDate(String dateString, boolean startOfDay) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        LocalDate date = LocalDate.parse(dateString);
        return startOfDay ? date.atStartOfDay() : date.atTime(LocalTime.MAX);
    }

    private Page<SystemLog> getFilteredLogs(Admin admin, 
                                          LocalDateTime startDate, 
                                          LocalDateTime endDate, 
                                          SystemLog.Severity severity, 
                                          Pageable pageable) {
        if (startDate != null && endDate != null && severity != null) {
            return systemLogRepository.findByAdminAndTimestampBetweenAndSeverity(
                    admin, startDate, endDate, severity, pageable);
        } else if (startDate != null && endDate != null) {
            return systemLogRepository.findByAdminAndTimestampBetween(
                    admin, startDate, endDate, pageable);
        } else if (severity != null) {
            return systemLogRepository.findByAdminAndSeverity(admin, severity, pageable);
        } else {
            return systemLogRepository.findByAdmin(admin, pageable);
        }
    }
}