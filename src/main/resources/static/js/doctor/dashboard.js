// Dashboard Module
const DoctorDashboard = (function() {
    // Private variables
    let meetingTimer;
    let meetingSeconds = 0;
    
    // Initialize dashboard
    function initDashboard() {
        updateCurrentDate();
        setupDashboardCards();
        setupEmergencyAlerts();
        setupScheduleTimeline();
        checkAppointmentReminders();
        setInterval(checkAppointmentReminders, 60000); // Check every minute
    }
    
    // Update the current date display
    function updateCurrentDate() {
        const options = { 
            weekday: 'long', 
            year: 'numeric', 
            month: 'long', 
            day: 'numeric' 
        };
        document.getElementById('currentDate').textContent = 
            new Date().toLocaleDateString('en-US', options);
    }
    
    // Add click animation to dashboard cards
    function setupDashboardCards() {
        document.querySelectorAll('.dashboard-card').forEach(card => {
            card.addEventListener('click', function() {
                this.style.transform = 'scale(0.95)';
                setTimeout(() => {
                    this.style.transform = '';
                }, 200);
            });
        });
    }
    
    // Check for appointments that are about to start
    function checkAppointmentReminders() {
        const now = new Date();
        const currentHour = now.getHours();
        const currentMinutes = now.getMinutes();
        
        // Find appointments starting in the next 15 minutes
        const upcomingAppointments = appointmentsData.filter(appt => {
            const [hours, minutes] = appt.startTime.split(':').map(Number);
            const timeDiff = (hours - currentHour) * 60 + (minutes - currentMinutes);
            return timeDiff > 0 && timeDiff <= 15;
        });
        
        // Show reminders for each upcoming appointment
        upcomingAppointments.forEach(appt => {
            showAppointmentReminder(
                appt.patientName, 
                appt.startTime, 
                appt.isOnline, 
                appt.zoomLink
            );
        });
    }
    
    // Show appointment reminder
    function showAppointmentReminder(patientName, time, isOnline, zoomLink) {
        const reminder = document.querySelector('.appointment-reminder');
        if (reminder) {
            reminder.querySelector('span').textContent = 
                `You have an appointment with ${patientName} at ${time}`;
            reminder.style.display = 'flex';
            
            const startBtn = reminder.querySelector('.start-meeting-btn');
            if (isOnline && zoomLink) {
                startBtn.style.display = 'inline-block';
                startBtn.onclick = function() { 
                    startZoomMeeting(zoomLink); 
                };
            } else {
                startBtn.style.display = 'none';
            }
        }
    }
    
    // Setup schedule timeline interactions
    function setupScheduleTimeline() {
        document.querySelectorAll('.appointment-block').forEach(block => {
            block.addEventListener('click', function() {
                const appointmentId = this.dataset.appointmentId;
                viewAppointmentDetails(appointmentId);
            });
        });
        
        document.querySelectorAll('.start-meeting-btn').forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.stopPropagation();
                const zoomLink = this.dataset.zoomLink;
                startZoomMeeting(zoomLink);
            });
        });
    }
    
    // View appointment details
    function viewAppointmentDetails(appointmentId) {
        window.location.href = `/doctor/appointments/${appointmentId}`;
    }
    
    // Setup emergency alert dismissal using event delegation
    function setupEmergencyAlerts() {
        document.addEventListener('click', function(e) {
            // Handle dismiss button click
            if (e.target.classList.contains('dismiss-alert-btn') || 
                e.target.closest('.dismiss-alert-btn')) {
                
                const btn = e.target.classList.contains('dismiss-alert-btn') ? 
                    e.target : e.target.closest('.dismiss-alert-btn');
                
                const alertId = btn.dataset.alertId;
                if (alertId) {
                    dismissAlert(alertId, btn);
                }
            }
        });
    }
    
    // Dismiss alert with improved error handling
    function dismissAlert(alertId, buttonElement = null) {
        if (buttonElement) {
            buttonElement.disabled = true;
            buttonElement.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Dismissing...';
        }
        
        const token = document.querySelector('meta[name="_csrf"]').content;
        const header = document.querySelector('meta[name="_csrf_header"]').content;
        
        fetch(`/api/alerts/${alertId}/dismiss`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [header]: token
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to dismiss alert');
            }
            // Hide the alert on success
            const alertElement = buttonElement ? 
                buttonElement.closest('.flash-message') : 
                document.querySelector(`.flash-message [data-alert-id="${alertId}"]`)?.closest('.flash-message');
            
            if (alertElement) {
                alertElement.style.display = 'none';
            }
        })
        .catch(error => {
            console.error('Error dismissing alert:', error);
            if (buttonElement) {
                buttonElement.disabled = false;
                buttonElement.textContent = 'Dismiss';
            }
        });
    }
    
    // Start Zoom meeting
    function startZoomMeeting(zoomLink) {
        if (zoomLink) {
            window.open(zoomLink, '_blank', 'noopener,noreferrer');
        } else {
            alert('No Zoom meeting link available for this appointment');
        }
    }
    
    // View patient record
    function viewPatient(patientId) {
        window.location.href = `/doctor/patients/${patientId}`;
    }
    
    // Public API
    return {
        initDashboard,
        startZoomMeeting,
        viewPatient,
        dismissAlert,
        checkAppointmentReminders
    };
})();

// Initialize when DOM is fully loaded
document.addEventListener('DOMContentLoaded', function() {
    DoctorDashboard.initDashboard();
});