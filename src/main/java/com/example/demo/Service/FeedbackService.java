package com.example.demo.Service;


import com.example.demo.Models.*;
import com.example.demo.respository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserService userService;

    @Autowired
    public FeedbackService(FeedbackRepository feedbackRepository,
                          UserService userService) {
        this.feedbackRepository = feedbackRepository;
        this.userService=userService;
    }

    public List<Feedback> getPatientFeedback(Patient patient, Doctor doctor) {
        return feedbackRepository.findByPatientAndDoctorOrderByCreatedAtDesc(patient, doctor);
    }

    @Transactional
    public Feedback createFeedback( String patientId, 
                                 Doctor doctor, String comments){
        Patient patient=(Patient)userService.findByUserId(patientId);

        Feedback feedback = new Feedback();
        feedback.setDoctor(doctor);
        feedback.setPatient(patient);
        feedback.setComments(comments);
        feedback.setCreatedAt(LocalDateTime.now());

        return feedbackRepository.save(feedback);
    }

    
}