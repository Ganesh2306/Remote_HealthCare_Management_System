package com.example.demo.Service;


import com.example.demo.Models.Alert;
import com.example.demo.Models.Doctor;
import com.example.demo.Models.Feedback;
import com.example.demo.Models.Patient;
import com.example.demo.Models.Prescription;

import com.example.demo.respository.AlertRepository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserService userService;

    @Autowired
    public AlertService(AlertRepository alertRepository, UserService userService) {
        this.alertRepository = alertRepository;
        this.userService = userService;
    }


    public List<Alert> getAllAlertsforDoctor(String doctorId){
        return alertRepository.findAllAlertsByDoctorUserIdOrderedByTimestamp(doctorId);
    }



    public List<Alert> getRecentAlertsForDoctor(String doctorId, int limit) {
        return alertRepository.findAllAlertsByDoctorUserIdOrderedByTimestamp(doctorId)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // Additional useful methods
    public List<Alert> getAllUnacknowledgedAlerts(String doctorId) {
        return alertRepository.findUnacknowledgedAlertsByDoctorUserId(doctorId);
    }


    public Alert createAlert(Alert alert) {
        return alertRepository.save(alert);
    }

   
public List<Alert> getRequestedCriticalAlerts(String doctorId){
    return alertRepository.findByDoctorUserIdAndAcknowledgedFalse(doctorId);
}
   


public List<Alert> getActiveAlertsForDoctor(String doctorUsername) {
        Doctor doctor = (Doctor) userService.findByEmail(doctorUsername);
        
        return alertRepository.findByDoctorAndAcknowledgedFalseOrderByTimestampDesc(doctor);
    }

    public Alert acknowledgeAlert(Long alertId)  {
        Alert alert = alertRepository.findById(alertId).get();
        
        alert.setAcknowledged(true);
        return alertRepository.save(alert);
    }

 
    public int acknowledgeAllAlertsForDoctor(String doctorUsername) {
       
           Doctor doctor = (Doctor) userService.findByEmail(doctorUsername);
        List<Alert> alerts = alertRepository.findByDoctorAndAcknowledgedFalse(doctor);
        alerts.forEach(alert -> {
            alert.setAcknowledged(true);
        });
        
        alertRepository.saveAll(alerts);
        return alerts.size();
    }

   
    public Alert createAlert(Patient patient, Doctor doctor, String message, String category) {
        Alert alert = new Alert();
        alert.setPatient(patient);
        alert.setDoctor(doctor);
        alert.setMessage(message);
        alert.setCategory(category);
        alert.setTimestamp(LocalDateTime.now());
        alert.setAcknowledged(false);
        
        return alertRepository.save(alert);
    }
  



      public Alert UpdatePrescriptionStatus(Long id, Prescription prescription){

        Alert alert= alertRepository.findById(id).orElseThrow(()-> new IllegalArgumentException( "Not Found Vital for Id"));
         alert.setPrescription(prescription);
        return alertRepository.save(alert);
    }

    
    
    public Alert UpdateFeedbackStatus(Long id, Feedback feedback){

        Alert alert= alertRepository.findById(id).orElseThrow(()-> new IllegalArgumentException( "Not Found Vital for Id"));
        alert.setFeedback(feedback);
        return alertRepository.save(alert);
    }

}