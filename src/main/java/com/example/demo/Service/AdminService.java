package com.example.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.ui.Model;
import org.springframework.stereotype.Service;
import com.example.demo.respository.AdminRepository;
import com.example.demo.respository.DoctorRepository;

import java.util.List;
import com.example.demo.Models.Patient;
import com.example.demo.Models.Doctor;
import com.example.demo.Models.Admin;
import java.util.Comparator;

@Service
public class AdminService {

    @Autowired
    private PatientService patientService;
    
    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;

    // Patient related methods
    public Page<Patient> getPatientsPage(String searchQuery, PageRequest pageRequest) {
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            return patientService.searchPatients(searchQuery.trim(), pageRequest);
        }
        return patientService.getAllPatients(pageRequest);
    }

    public void assignPatientToAdmin(Patient patient) {
        List<Admin> admins = adminRepository.findAllByOrderByUserIdAsc();
        if (!admins.isEmpty()) {
            Admin leastLoadedAdmin = admins.stream()
                .min(Comparator.comparingInt(a -> a.getManagedPatients().size()))
                .orElseThrow();
            leastLoadedAdmin.getManagedPatients().add(patient);
            adminRepository.save(leastLoadedAdmin);
        }
    }

    public void populateModelAttributes(Model model, Page<Patient> patientsPage, String searchQuery) {
        model.addAttribute("patients", patientsPage.getContent());
        model.addAttribute("currentPage", patientsPage.getNumber() + 1);
        model.addAttribute("totalPages", patientsPage.getTotalPages());
        model.addAttribute("totalItems", patientsPage.getTotalElements());
        
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            model.addAttribute("searchQuery", searchQuery.trim());
        }
    }

    // Doctor related methods
    public Page<Doctor> getDoctorsPage(String searchQuery, PageRequest pageRequest) {
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            return doctorService.searchDoctors(searchQuery, pageRequest);
        }
        return doctorRepository.findAll(pageRequest);
    }

    public void assignDoctorToAdmin(Doctor doctor) {
        List<Admin> admins = adminRepository.findAllByOrderByUserIdAsc();
        if (!admins.isEmpty()) {
            Admin leastLoadedAdmin = admins.stream()
                .min(Comparator.comparingInt(a -> a.getManagedDoctors().size()))
                .orElseThrow();
            leastLoadedAdmin.getManagedDoctors().add(doctor);
            adminRepository.save(leastLoadedAdmin);
        }
    }

    public void populateDoctorModelAttributes(Model model, Page<Doctor> doctorsPage, String searchQuery) {
        model.addAttribute("doctors", doctorsPage.getContent());
        model.addAttribute("currentPage", doctorsPage.getNumber() + 1);
        model.addAttribute("totalPages", doctorsPage.getTotalPages());
        model.addAttribute("totalItems", doctorsPage.getTotalElements());
        
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            model.addAttribute("searchQuery", searchQuery.trim());
        }
    }
}