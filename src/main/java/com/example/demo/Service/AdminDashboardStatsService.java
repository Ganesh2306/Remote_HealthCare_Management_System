package com.example.demo.Service;


import com.example.demo.dto.DashboardStatsDTO;
import org.springframework.stereotype.Service;
import com.example.demo.respository.PatientRepository;


@Service
public class AdminDashboardStatsService {

    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final SystemLogService logService;

    public AdminDashboardStatsService(PatientRepository patientRepository,
                                   DoctorService doctorService,
                                   SystemLogService logService) {
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.logService = logService;
    }

    public DashboardStatsDTO getDashboardStatistics() {
        return new DashboardStatsDTO(
            patientRepository.findAll().size(),
            doctorService.findAllDoctors().size(),
            logService.getAllLogs().size(),
            logService.getCriticalLogs().size()
        );
    }
}