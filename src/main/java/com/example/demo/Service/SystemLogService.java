package com.example.demo.Service;


import com.example.demo.Models.Admin;
import com.example.demo.Models.SystemLog;
import com.example.demo.respository.SystemLogRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Transactional
public class SystemLogService{

    private final SystemLogRepository logRepository;

    public SystemLogService(SystemLogRepository logRepository) {
        this.logRepository = logRepository;
    }


    public SystemLog createLog(Admin admin, String action, SystemLog.Severity severity) {
        SystemLog log = new SystemLog(admin, action, severity);
        return logRepository.save(log);
    }

    
    public List<SystemLog> getRecentLogs(int limit) {
        return logRepository.findAllByOrderByTimestampDesc(Pageable.ofSize(limit)).getContent();
    }

    
    public Page<SystemLog> getLogsBySeverity(SystemLog.Severity severity, Pageable pageable) {
        return logRepository.findBySeverityOrderByTimestampDesc(severity, pageable);
    }

    
    public Map<String, Long> getLogStatistics() {
        List<SystemLog> allLogs = logRepository.findAll();
        
        Map<String, Long> stats = new HashMap<>();
        stats.put("TOTAL_LOGS", (long) allLogs.size());
        stats.put("CRITICAL_LOGS", allLogs.stream()
                .filter(log -> log.getSeverity() == SystemLog.Severity.CRITICAL)
                .count());
        stats.put("ERROR_LOGS", allLogs.stream()
                .filter(log -> log.getSeverity() == SystemLog.Severity.ERROR)
                .count());
        stats.put("WARNING_LOGS", allLogs.stream()
                .filter(log -> log.getSeverity() == SystemLog.Severity.WARNING)
                .count());
        stats.put("INFO_LOGS", allLogs.stream()
                .filter(log -> log.getSeverity() == SystemLog.Severity.INFO)
                .count());
        
        return stats;
    }

    public List<SystemLog> searchLogs(String searchTerm) {
        return logRepository.findByActionContainingIgnoreCase(searchTerm);
    }

    public List<SystemLog> getAllLogs(){
        return logRepository.findAll();
    }

    public List<SystemLog> getCriticalLogs() {
        return logRepository.findBySeverityOrderByTimestampDesc(SystemLog.Severity.CRITICAL);
    }

    public void logAction(Admin admin, String action, SystemLog.Severity severity) {
        SystemLog log = new SystemLog(admin, action, severity);
        logRepository.save(log);

}}
