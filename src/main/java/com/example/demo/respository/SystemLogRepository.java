package com.example.demo.respository;


import com.example.demo.Models.Admin;
import com.example.demo.Models.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Map;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {

    
    List<SystemLog> findBySeverity(SystemLog.Severity severity);
    
    // Time-based queries
    List<SystemLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    // Combined queries
    List<SystemLog> findByAdminAndSeverity(Admin admin, SystemLog.Severity severity);
    List<SystemLog> findByAdminAndTimestampBetween(Admin admin, LocalDateTime start, LocalDateTime end);
    
    // Paginated queries
    Page<SystemLog> findAllByOrderByTimestampDesc(Pageable pageable);
    Page<SystemLog> findBySeverityOrderByTimestampDesc(SystemLog.Severity severity, Pageable pageable);
    
    // Count queries
    long countByAdmin(Admin admin);
    long countBySeverity(SystemLog.Severity severity);
    
    // Custom query for dashboard stats
    @Query("SELECT new map(l.severity as severity, COUNT(l) as count) FROM SystemLog l GROUP BY l.severity")
    List<Map<String, Object>> countLogsBySeverity();
    
    // Search logs by action containing text
    List<SystemLog> findByActionContainingIgnoreCase(String searchTerm);
    
    // Get most recent logs (alternative to pagination)
    @Query("SELECT l FROM SystemLog l ORDER BY l.timestamp DESC LIMIT :limit")
    List<SystemLog> findRecentLogs(int limit);

    List<SystemLog> findBySeverityOrderByTimestampDesc(SystemLog.Severity severity);

    List<SystemLog> findAllByOrderByTimestampDesc();

    Page<SystemLog> findByAdmin(Admin admin, Pageable pageable);
    
    Page<SystemLog> findByAdminAndSeverity(Admin admin, SystemLog.Severity severity, Pageable pageable);
    
    Page<SystemLog> findByAdminAndTimestampBetween(
            Admin admin, 
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            Pageable pageable);
            
    Page<SystemLog> findByAdminAndTimestampBetweenAndSeverity(
            Admin admin, 
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            SystemLog.Severity severity, 
            Pageable pageable);
            
    @Query("SELECT l FROM SystemLog l WHERE l.admin = :admin AND " +
           "(:searchTerm IS NULL OR l.action LIKE %:searchTerm%)")
    Page<SystemLog> searchByAdminAndAction(
            @Param("admin") Admin admin,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);


          @Query("SELECT l FROM SystemLog l WHERE " +
           "(:from IS NULL OR l.timestamp >= :from) AND " +
           "(:to IS NULL OR l.timestamp <= :to) AND " +
           "(:severity IS NULL OR l.severity = :severity) " +
           "ORDER BY l.timestamp DESC")
    List<SystemLog> findFilteredLogs(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to,
        @Param("severity") String severity
    );

}