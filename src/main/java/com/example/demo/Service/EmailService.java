package com.example.demo.Service;

import com.example.demo.Models.VerificationToken;
import com.example.demo.dto.PatientRegistrationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.time.Duration;
import java.time.LocalDate;

import com.example.demo.Exceptions.EmailException;
import java.util.List;
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Main email dispatcher for all email types
     */
    public void sendEmail(PatientRegistrationRequest request, String token, VerificationToken.EmailType emailType) {
        switch (emailType) {
            case VERIFICATION:
                sendVerificationEmail(request.getEmail(), request.getFirstName(), token);
                break;
            case ACCEPTANCE:
                sendWelcomeEmail(request.getEmail(), request.getFirstName());
                break;
            case REJECTION:
                sendRejectionEmail(request.getEmail(), request.getFirstName());
                break;
            default:
                throw new IllegalArgumentException("Unsupported email type: " + emailType);
        }
    }

    /**
     * Send account verification email
     */
    public void sendVerificationEmail(String email, String name, String token) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("name", name);
            variables.put("verificationUrl", buildVerificationUrl(token));


            sendEmail(
                email,
                "Verify Your Account Registration",
                "email/verification_email",
                variables
            );
        } catch (Exception e) {
            throw new EmailException("Failed to send verification email to " + email, e);
        }
    }

    /**
     * Send welcome email after successful verification
     */
    public void sendWelcomeEmail(String email, String name) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("name", name);
            variables.put("loginUrl", baseUrl + "/login");

            sendEmail(
                email,
                "Welcome to Our Healthcare Platform",
                "email/account_approved",
                variables
            );
        } catch (Exception e) {
            throw new EmailException("Failed to send welcome email to " + email, e);
        }
    }

    /**
     * Send rejection email for expired tokens
     */
    public void sendRejectionEmail(String email, String name) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("name", name);

            sendEmail(
                email,
                "Verification Link Expired",
                "email/account_rejected",
                variables
            );
        } catch (Exception e) {
            throw new EmailException("Failed to send rejection email to " + email, e);
        }
    }

    /**
 * Send confirmation to patient that vitals were successfully uploaded
 */
public void sendVitalsUploadConfirmation(String patientEmail, int recordsCount) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("recordsCount", recordsCount);
        
        sendEmail(
            patientEmail,
            "Your Vital Signs Have Been Recorded",
            "email/vitals_upload_confirmation",
            variables
        );
    } catch (Exception e) {
        throw new EmailException("Failed to send vitals upload confirmation to " + patientEmail, e);
    }
}

/**
 * Notify doctor about new vital signs upload
 */
public void sendDoctorVitalsNotification(String doctorEmail, String patientName, int recordsCount) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("patientName", patientName);
        variables.put("recordsCount", recordsCount);
        variables.put("dashboardUrl", baseUrl + "/doctor/dashboard");
        
        sendEmail(
            doctorEmail,
            "New Vital Signs Uploaded for " + patientName,
            "email/doctor_vitals_confirmation",
            variables
        );
    } catch (Exception e) {
        throw new EmailException("Failed to send doctor vitals notification to " + doctorEmail, e);
    }
}

/**
 * Alert patient about critical conditions
 */
public void sendCriticalConditionAlert(String patientEmail, int criticalRecordsCount,List<List<String>> alerts) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("criticalRecordsCount", criticalRecordsCount);
        variables.put("emergencyContact", "Call 911 or your nearest emergency service");
        variables.put("supportContact", "support@healthcare.com");
        variables.put("alerts", alerts);
        
        sendEmail(
            patientEmail,
            "Urgent: Critical Vital Signs Detected",
            "email/critical_condition_alert",
            variables
        );
    } catch (Exception e) {
        throw new EmailException("Failed to send critical condition alert to " + patientEmail, e);
    }
}

/**
 * Priority alert to doctor about critical conditions
 */
public void sendCriticalConditionDoctorAlert(String doctorEmail, String patientName, int criticalRecordsCount, List<List<String>> alerts) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("patientName", patientName);
        variables.put("criticalRecordsCount", criticalRecordsCount);
        variables.put("patientRecordsUrl", baseUrl + "/doctor/patient-records");
        variables.put("priority", "HIGH PRIORITY");
        variables.put("alerts", alerts);
        
        sendEmail(
            doctorEmail,
            "URGENT: Critical Vital Signs for " + patientName,
            "email/critical_condition_doctor_alert",
            variables
        );
    } catch (Exception e) {
        throw new EmailException("Failed to send critical condition doctor alert to " + doctorEmail, e);
    }
}


public void sendPatientAppointmentConfirmation(String patientEmail, String patientName, 
                                             String doctorName, LocalDateTime appointmentTime, 
                                             Duration duration, String location) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("patientName", patientName);
        variables.put("doctorName", doctorName);
        variables.put("appointmentDate", appointmentTime.toLocalDate());
        variables.put("appointmentTime", appointmentTime.toLocalTime());
        variables.put("duration", duration.toMinutes());
        variables.put("location", location);
        variables.put("dashboardUrl", baseUrl + "/patient/dashboard");

        sendEmail(
            patientEmail,
            "Your Appointment Confirmation",
            "email/patient_appointment_confirmation",
            variables
        );
    } catch (Exception e) {
        throw new EmailException("Failed to send appointment confirmation to " + patientEmail, e);
    }
}

/**
 * Send appointment notification to doctor
 */
public void sendDoctorAppointmentNotification(String doctorEmail, String doctorName,
                                           String patientName, LocalDateTime appointmentTime,
                                           Duration duration, String location, String reason) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("doctorName", doctorName);
        variables.put("patientName", patientName);
        variables.put("appointmentDate", appointmentTime.toLocalDate());
        variables.put("appointmentTime", appointmentTime.toLocalTime());
        variables.put("duration", duration.toMinutes());
        variables.put("location", location);
        variables.put("reason", reason);
        variables.put("dashboardUrl", baseUrl + "/doctor/dashboard");

        sendEmail(
            doctorEmail,
            "New Appointment Scheduled with " + patientName,
            "email/doctor-appointment-notification",
            variables
        );
    } catch (Exception e) {
        throw new EmailException("Failed to send appointment notification to " + doctorEmail, e);
    }
}
    /**
     * Core email sending method
     */
    private void sendEmail(String to, String subject, String templateName, 
                         Map<String, Object> variables) 
        throws MessagingException, UnsupportedEncodingException {
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        Context context = new Context();
        context.setVariables(variables);
        String htmlContent = templateEngine.process(templateName, context);

        helper.setFrom(fromEmail, "Healthcare Platform");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private String buildVerificationUrl(String token) {
        return baseUrl + "/register/verify?token=" + token;
    }

   /**
 * Send registration successful email with credentials
 */
public void sendRegistrationSuccessEmail(String email, String name, String adminId, String password) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", name);
        variables.put("patientId", adminId);
        variables.put("password", password);
        variables.put("loginUrl", baseUrl + "/login");

        sendEmail(
            email,
            "Your Healthcare Account Has Been Activated",
            "email/registration_success",
            variables
        );
    } catch (Exception e) {
        throw new EmailException("Failed to send registration success email to " + email, e);
    }
}
public void sendAppointmentCancellationNotification(String patientEmail, String patientName, 
                                                  String doctorName, LocalDateTime appointmentTime, 
                                                  String reason) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("patientName", patientName);
        variables.put("doctorName", doctorName);
        variables.put("appointmentDate", appointmentTime.toLocalDate());
        variables.put("appointmentTime", appointmentTime.toLocalTime());
        variables.put("reason", reason);
        variables.put("rescheduleUrl", baseUrl + "/appointments/new");
        variables.put("supportContact", "support@healthcare.com");

        sendEmail(
            patientEmail,
            "Appointment Cancellation Notice",
            "email/appointment_cancellation",
            variables
        );
    } catch (Exception e) {
        throw new EmailException("Failed to send appointment cancellation to " + patientEmail, e);
    }
}

/**
 * Notify doctor that their account has been deleted
 */
public void sendDoctorAccountDeactivationEmail(String email, String name, String reason) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", name);
        variables.put("supportContact", "support@healthcare.com");
        variables.put("reason", reason);

        sendEmail(
            email,
            "Your Account Has Been Removed",
            "email/doctor_account_deletion",
            variables
        );
    } catch (Exception e) {
        throw new EmailException("Failed to send doctor account deletion email to " + email, e);
    }
}


/**
 * Send account deletion notification email
 */
public void sendAccountDeactivationEmail(String email, String name, String reason, LocalDateTime deletionTime) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", name);
        variables.put("deletionTime", deletionTime);
        variables.put("reason", reason);
        variables.put("supportEmail", "support@healthcare.com");
        variables.put("helpCenterUrl", baseUrl + "/help");
       

        sendEmail(
            email,
            "Your Account Has Been Deactivated",
            "email/account_deactivation",
            variables
        );
    } catch (Exception e) {
        throw new EmailException("Failed to send account deactivation email to " + email, e);
    }
}

/**
 * Send account reactivation notification email
 */
public void sendAccountReactivationEmail(String email, String name,String reason, LocalDateTime reactivationTime) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", name);
        variables.put("reactivationTime", reactivationTime);
        variables.put("loginUrl", baseUrl + "/login");
        variables.put("supportEmail", "support@healthcare.com");
        variables.put("reason", reason);

        sendEmail(
            email,
            "Your Account Has Been Reactivated",
            "email/account_reactivation",
            variables
        );
    } catch (Exception e) {
        throw new EmailException("Failed to send account reactivation email to " + email, e);
    }
}




/**
 * Send emergency alert to specific doctor
 */
public void sendEmergencyAlertToDoctor(String doctorEmail, 
                                     String patientName, 
                                     String message, 
                                     String category) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("patientName", patientName);
        variables.put("message", message);
        variables.put("category", category);
        variables.put("priority", "HIGH PRIORITY");
        variables.put("responseTime", "Please respond within 30 minutes");
        variables.put("contactInfo", "Patient can be reached at their registered contact");
        variables.put("dashboardUrl", baseUrl + "/doctor/dashboard");

        sendEmail(
            doctorEmail,
            "URGENT: Emergency Alert from " + patientName,
            "email/emergency_doctor_alert",
            variables
        );
    } catch (Exception e) {
        throw new EmailException("Failed to send emergency alert to doctor " + doctorEmail, e);
    }
}


/**
 * Send confirmation to patient that emergency alert was sent
 */
public void sendEmergencyAlertSentConfirmation(String patientEmail, 
                                             String doctorName, 
                                             String patientName, 
                                             String message, 
                                             String category) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("patientName", patientName);
        variables.put("doctorName", doctorName);
        variables.put("message", message);
        variables.put("category", category);
        variables.put("emergencyContact", "Call 911 or your nearest emergency service");
        variables.put("supportContact", "support@healthcare.com");
        variables.put("responseTime", "Doctor should respond within 30 minutes");

        sendEmail(
            patientEmail,
            "Emergency Alert Sent to Dr. " + doctorName,
            "email/emergency_patient_confirmation",
            variables
        );
    } catch (Exception e) {
        throw new EmailException("Failed to send emergency confirmation to " + patientEmail, e);
    }
}
   


/**
 * Send emergency alert to patient's emergency contact
 */
public void sendEmergencyContactAlert(String emergencyContactEmail, 
                                    String patientName, 
                                    String message, 
                                    String category) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("patientName", patientName);
        variables.put("message", message);
        variables.put("category", category);
        variables.put("emergencyInstructions", "Please check on the patient immediately");
        variables.put("supportContact", "support@healthcare.com");

        sendEmail(
            emergencyContactEmail,
            "URGENT: Health Alert for " + patientName,
            "email/emergency_contact_alert",
            variables
        );
    } catch (Exception e) {
        throw new EmailException("Failed to send emergency contact alert to " + emergencyContactEmail, e);
    }
}


/**
 * Send critical vital signs alert to patient's emergency contact
 * @param emergencyContactEmail The email of the emergency contact
 * @param emergencyContactName Name of the emergency contact
 * @param patientName Full name of the patient
 * @param criticalAlerts List of critical alerts detected
 * @param detectionTime When the critical vitals were detected
 */
public void sendCriticalVitalsEmergencyContactAlert(String emergencyContactEmail,
                                                  String emergencyContactName,
                                                  String patientName,
                                                  List<List<String>> criticalAlerts,
                                                  LocalDateTime detectionTime) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("emergencyContactName", emergencyContactName);
        variables.put("patientName", patientName);
        variables.put("criticalAlerts", criticalAlerts);
        variables.put("detectionTime", detectionTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")));
        variables.put("emergencyInstructions", "Please check on the patient immediately");
        variables.put("emergencyContact", "Call 911 if the patient is unresponsive");
        variables.put("supportContact", "support@healthcare.com");
        variables.put("healthcareFacility", "Nearest Medical Center");

        sendEmail(
            emergencyContactEmail,
            "URGENT: Critical Health Alert for " + patientName,
            "email/critical_vitals_emergency_contact",
            variables
        );
    } catch (Exception e) {
        throw new EmailException("Failed to send critical vitals alert to emergency contact " + emergencyContactEmail, e);
    }
}



/**
 * Send appointment cancellation notification to patient
 */
public void sendAppointmentCancellation(String patientEmail, 
                                      String patientName,
                                      String doctorName,
                                      LocalDateTime appointmentTime,
                                      String location) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("patientName", patientName);
        variables.put("doctorName", doctorName);
        variables.put("appointmentDate", appointmentTime.toLocalDate());
        variables.put("appointmentTime", appointmentTime.toLocalTime());
        variables.put("location", location);
        variables.put("rescheduleUrl", baseUrl + "/appointments/new");
        variables.put("supportContact", "support@healthcare.com");
        variables.put("currentYear", LocalDate.now().getYear());

        sendEmail(
            patientEmail,
            "Your Appointment Has Been Cancelled",
            "email/appointment_cancellation",
            variables
        );

    
    } catch (Exception e) {
        throw new EmailException("Failed to send appointment cancellation to " + patientEmail, e);
    }
}


/**
 * Sends appointment cancellation notification to doctor
 * @param doctorEmail Doctor's email address
 * @param doctorName Doctor's full name
 * @param patientName Patient's full name
 * @param appointmentTime Original appointment time
 * @param cancellationReason Reason for cancellation
 * @param location Appointment location
 */
public void sendDoctorAppointmentCancellation(String doctorEmail,
                                            String doctorName,
                                            String patientName,
                                            LocalDateTime appointmentTime,
                                            String cancellationReason,
                                            String location) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("doctorName", doctorName);
        variables.put("patientName", patientName);
        variables.put("appointmentDate", appointmentTime.toLocalDate());
        variables.put("appointmentTime", appointmentTime.toLocalTime());
        variables.put("cancellationReason", cancellationReason);
        variables.put("location", location);
        variables.put("dashboardUrl", baseUrl + "/doctor/appointments");
        variables.put("currentYear", LocalDate.now().getYear());
        
        sendEmail(
            doctorEmail,
            "Appointment Cancellation: " + patientName,
            "email/doctor_appointment_cancellation",
            variables
        );
    } catch (Exception e) {
        throw new EmailException("Failed to send appointment cancellation to doctor " + doctorEmail, e);
    }
}


public void sendPatientAppointmentRescheduledConfirmation(String patientEmail, String patientName,
                                                           String doctorName, LocalDateTime newAppointmentTime,
                                                           Duration duration, String location) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("patientName", patientName);
        variables.put("doctorName", doctorName);
        variables.put("newAppointmentDate", newAppointmentTime.toLocalDate());
        variables.put("newAppointmentTime", newAppointmentTime.toLocalTime());
        variables.put("duration", duration.toMinutes());
        variables.put("location", location);
        variables.put("dashboardUrl", baseUrl + "/patient/dashboard");

        sendEmail(
            patientEmail,
            "Your Appointment Has Been Rescheduled",
            "email/patient_appointment_rescheduled_confirmation",
            variables
        );
    } catch (Exception e) {
        throw new EmailException("Failed to send rescheduled appointment confirmation to " + patientEmail, e);
    }
}



public void sendDoctorAppointmentRescheduledNotification(String doctorEmail, String doctorName,
                                                         String patientName, LocalDateTime newAppointmentTime,
                                                         Duration duration, String location, String reason) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("doctorName", doctorName);
        variables.put("patientName", patientName);
        variables.put("newAppointmentDate", newAppointmentTime.toLocalDate());
        variables.put("newAppointmentTime", newAppointmentTime.toLocalTime());
        variables.put("duration", duration.toMinutes());
        variables.put("location", location);
        variables.put("reason", reason);
        variables.put("dashboardUrl", baseUrl + "/doctor/dashboard");

        sendEmail(
            doctorEmail,
            "Appointment Rescheduled with " + patientName,
            "email/doctor_appointment_rescheduled_notification",
            variables
        );
    } catch (Exception e) {
        throw new EmailException("Failed to send rescheduled appointment notification to " + doctorEmail, e);
    }
}


/**
 * Send appointment confirmation to patient when doctor confirms
 */
public void sendAppointmentConfirmationByDoctor(String patientEmail, String patientName,
                                              String doctorName, LocalDateTime appointmentTime,
                                              Duration duration, String location, 
                                              String zoomLink) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("patientName", patientName);
        variables.put("doctorName", doctorName);
        variables.put("appointmentDate", appointmentTime.toLocalDate());
        variables.put("appointmentTime", appointmentTime.toLocalTime());
        variables.put("duration", duration.toMinutes());
        variables.put("location", location);
        variables.put("dashboardUrl", baseUrl + "/patient/dashboard");
        
        if (zoomLink != null && !zoomLink.isBlank()) {
            variables.put("zoomLink", zoomLink);
            variables.put("isOnline", true);
        } else {
            variables.put("isOnline", false);
        }

        sendEmail(
            patientEmail,
            "Your Appointment with Dr. " + doctorName + " Has Been Confirmed",
            "email/appointment_confirmed_by_doctor",
            variables
        );
    } catch (Exception e) {
        throw new EmailException("Failed to send appointment confirmation to " + patientEmail, e);
    }
}

/**
 * Send appointment cancellation notification to patient when doctor cancels
 */
public void sendAppointmentCancellationByDoctor(String patientEmail, String patientName,
                                              String doctorName, LocalDateTime appointmentTime,
                                              String location, String cancellationReason) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("patientName", patientName);
        variables.put("doctorName", doctorName);
        variables.put("appointmentDate", appointmentTime.toLocalDate());
        variables.put("appointmentTime", appointmentTime.toLocalTime());
        variables.put("location", location);
        variables.put("cancellationReason", cancellationReason);
        variables.put("rescheduleUrl", baseUrl + "/appointments/new");
        variables.put("supportContact", "support@healthcare.com");
        variables.put("currentYear", LocalDate.now().getYear());

        sendEmail(
            patientEmail,
            "Your Appointment with Dr. " + doctorName + " Has Been Cancelled",
            "email/appointment_cancelled_by_doctor",
            variables
        );
    } catch (Exception e) {
        throw new EmailException("Failed to send appointment cancellation to " + patientEmail, e);
    }
}



public void sendAppointmentReportReadyNotification(String patientEmail, String patientName,
                                                String doctorName, LocalDateTime appointmentTime) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("patientName", patientName);
        variables.put("doctorName", doctorName);
        variables.put("appointmentDate", appointmentTime.toLocalDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        variables.put("appointmentTime", appointmentTime.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
        variables.put("dashboardUrl", baseUrl + "/patient/dashboard");

        sendEmail(
            patientEmail,
            "Your Appointment Report is Ready",
            "email/appointment_report_ready_notification",
            variables
        );
    } catch (Exception e) {
        throw new EmailException("Failed to send report ready notification to " + patientEmail, e);
    }
}



/**
 * Notify patient that their vital signs report is ready for review
 */
public void sendVitalReportReadyNotification(String patientEmail, String patientName, String doctorName) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("patientName", patientName);
        variables.put("doctorName", doctorName);
        variables.put("dashboardUrl", baseUrl + "/patient/dashboard");
        variables.put("currentDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));

        sendEmail(
            patientEmail,
            "Your Vital Signs Report is Ready",
            "email/vital_report_ready",
            variables
        );
    } catch (Exception e) {
        throw new EmailException("Failed to send vital report notification to " + patientEmail, e);
    }
}




/**
 * Notify patient that their alert report is ready for review
 */
public void sendAlertReportReadyNotification(String patientEmail, 
                                           String patientName, 
                                           String doctorName) {
    try {
        Map<String, Object> variables = new HashMap<>();
        variables.put("patientName", patientName);
        variables.put("doctorName", doctorName);
        variables.put("dashboardUrl", baseUrl + "/patient/dashboard");
        variables.put("currentDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        variables.put("supportEmail", "support@healthcare.com");
        variables.put("clinicPhone", "1-800-HEALTH");

        sendEmail(
            patientEmail,
            "Your Health Alert Report is Ready",
            "email/alert_report_ready",
            variables
        );
    } catch (Exception e) {
        throw new EmailException("Failed to send alert report notification to " + patientEmail, e);
    }
}
}