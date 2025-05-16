package com.example.demo.Service;

import com.example.demo.respository.*;

import com.example.demo.Models.MedicalRecord;
import com.example.demo.Models.Patient;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;



import org.springframework.beans.factory.annotation.Autowired;

@Service
@Transactional
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final UserRepository userRepository;


    @Autowired
    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
             UserRepository userRepository ) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.userRepository=userRepository;
       
      
    }

    public MedicalRecord findByPatientUserId(String patientId) {
        Patient patient =(Patient)userRepository.findByUserId(patientId).get();
        Optional<MedicalRecord>medicalRecord= medicalRecordRepository.findByPatientUserId(patientId);
        if(medicalRecord.isPresent()){
            return medicalRecord.get();
        }else{
            MedicalRecord mr=new MedicalRecord();
            mr.setPatient(patient);
            return mr;
        }
    }

    public void addImmunization(String patientId, MedicalRecord.Immunization immunization) {
        MedicalRecord medicalRecord = findByPatientUserId(patientId);
        immunization.setMedicalRecord(medicalRecord);
        medicalRecord.getImmunizations().add(immunization);
        medicalRecordRepository.save(medicalRecord);
    }

    public void updateImmunization(String patientId, Long immunizationId, MedicalRecord.Immunization updatedImmunization) {
        MedicalRecord medicalRecord = findByPatientUserId(patientId);
        MedicalRecord.Immunization immunization = medicalRecord.getImmunizations().stream()
                .filter(i -> i.getId().equals(immunizationId))
                .findFirst().get();
        
        immunization.setVaccineName(updatedImmunization.getVaccineName());
        immunization.setAdministrationDate(updatedImmunization.getAdministrationDate());
        medicalRecordRepository.save(medicalRecord);
    }

    public void deleteImmunization(String patientId, Long immunizationId){
        MedicalRecord medicalRecord = findByPatientUserId(patientId);
        medicalRecord.getImmunizations().removeIf(i -> i.getId().equals(immunizationId));
        medicalRecordRepository.save(medicalRecord);
    }

    public void addLabResult(String patientId, MedicalRecord.LabResult labResult) {
        MedicalRecord medicalRecord = findByPatientUserId(patientId);
        labResult.setMedicalRecord(medicalRecord);
        medicalRecord.getLabResults().add(labResult);
        medicalRecordRepository.save(medicalRecord);
    }

    public void updateLabResult(String patientId, Long labResultId, MedicalRecord.LabResult updatedLabResult) {
        MedicalRecord medicalRecord = findByPatientUserId(patientId);
        MedicalRecord.LabResult labResult = medicalRecord.getLabResults().stream()
                .filter(l -> l.getId().equals(labResultId))
                .findFirst().get();
        
        labResult.setTestName(updatedLabResult.getTestName());
        labResult.setTestDate(updatedLabResult.getTestDate());
        labResult.setResult(updatedLabResult.getResult());
        medicalRecordRepository.save(medicalRecord);
    }

    public void deleteLabResult(String patientId, Long labResultId){
        MedicalRecord medicalRecord = findByPatientUserId(patientId);
         medicalRecord.getLabResults().removeIf(l -> l.getId().equals(labResultId));
        medicalRecordRepository.save(medicalRecord);
    }

  
}