package com.example.demo.respository;

import com.example.demo.Models.Doctor;
import com.example.demo.Models.VitalSigns;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface VitalSignsRepository extends JpaRepository<VitalSigns, Long> {

    // BASIC QUERIES
    
    /**
     * Count all vitals for a specific patient
     */
    int countByPatient_UserId(String patientId);

    
// For VitalSignsRepository
@Query("SELECT DISTINCT v.patient.userId FROM VitalSigns v " +
           "WHERE v.doctor.userId = :doctorId AND v.status = :status")
    Set<String> findDistinctPatientIdsByDoctorUserIdAndStatus(
            @Param("doctorId") String doctorId,
            @Param("status") VitalSigns.Status status);
    


int countDistinctPatientByDoctorUserIdAndStatus(String doctorId, VitalSigns.Status status);

    
    /**
     * Find all vitals for a specific patient (ordered by timestamp descending)
     */
List<VitalSigns> findByPatient_UserIdOrderByTimestampDescIdDesc(String patientId);


    // DASHBOARD-SPECIFIC QUERIES

    /**
     * Get the single most recent vital record for a patient
     */
    Optional<VitalSigns> findTopByPatient_UserIdOrderByTimestampDesc(String patientId);
    
    /**
     * Get the previous vital record before the specified timestamp
     */
@Query("SELECT v FROM VitalSigns v " +
       "WHERE v.patient.userId = :patientId " +
       "AND v.id != :currentId " +
       "ORDER BY v.timestamp DESC, v.id DESC")
List<VitalSigns> findPreviousVitals(
    @Param("patientId") String patientId,
    @Param("currentId") Long currentId,
    Pageable pageable
);

default Optional<VitalSigns> findPreviousVital(String patientId, Long currentId) {
    List<VitalSigns> results = findPreviousVitals(
        patientId,
        currentId,
        PageRequest.of(0, 1)
    );
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
}
    /**
     * Get trend data for the specified date range
     */
    @Query("SELECT v FROM VitalSigns v " +
           "WHERE v.patient.userId = :patientId " +
           "AND v.timestamp >= :startDate " +
           "ORDER BY v.timestamp ASC")
    List<VitalSigns> findTrendData(
        @Param("patientId") String patientId,
        @Param("startDate") LocalDateTime startDate
    );

    /**
     * Check if patient has any vitals recorded (more efficient than count > 0)
     */
    boolean existsByPatient_UserId(String patientId);

    // DOCTOR-SPECIFIC QUERIES
    
    /**
     * Count vitals by doctor and status
     */
    int countByDoctorUserIdAndStatus(String doctorId, VitalSigns.Status status);

    // ADDITIONAL QUERIES FOR ROBUSTNESS
    
    /**
     * Find previous vital excluding current record (by ID)
     */
 @Query("SELECT v FROM VitalSigns v " +
       "WHERE v.patient.userId = :patientId " +
       "ORDER BY v.timestamp DESC")
List<VitalSigns> findVitalsOrderedDesc(
    @Param("patientId") String patientId,
    Pageable pageable
);

// Convenience method to get the one before the latest
default Optional<VitalSigns> findPreviousVital(String patientId) {
    List<VitalSigns> results = findVitalsOrderedDesc(patientId, PageRequest.of(1, 1)); // skip 1, fetch 1
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
}

    
    /**
     * Find vitals in date range for reporting
     */
    @Query("SELECT v FROM VitalSigns v " +
           "WHERE v.patient.userId = :patientId " +
           "AND v.timestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY v.timestamp DESC")
    List<VitalSigns> findVitalsInDateRange(
        @Param("patientId") String patientId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );



     List<VitalSigns> findByStatusAndDoctorOrderByTimestampDesc(VitalSigns.Status status, Doctor doctor);


      Page<VitalSigns> findByDoctorAndStatus(Doctor doctor, VitalSigns.Status status, Pageable pageable);
}