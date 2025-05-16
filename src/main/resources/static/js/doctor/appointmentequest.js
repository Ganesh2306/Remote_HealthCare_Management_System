class AppointmentRequests {
    static init() {
        console.log("Appointment requests page initialized");
        
        // Add CSRF token to all AJAX requests
        const token = document.querySelector('meta[name="_csrf"]').content;
        const header = document.querySelector('meta[name="_csrf_header"]').content;
        
        if (token && header) {
            $.ajaxSetup({
                beforeSend: function(xhr) {
                    xhr.setRequestHeader(header, token);
                }
            });
        }
    }
    
    static switchTab(tabName, event) {
        event.preventDefault();
        
        // Hide all tab contents
        document.querySelectorAll('.tab-content').forEach(tab => {
            tab.classList.remove('active');
        });
        
        // Deactivate all tabs
        document.querySelectorAll('.tab').forEach(tab => {
            tab.classList.remove('active');
        });
        
        // Activate selected tab
        document.getElementById(tabName + '-tab').classList.add('active');
        event.currentTarget.classList.add('active');
    }
    
    static openCancelModal(button) {
        const appointmentId = button.getAttribute('data-appointment-id');
        document.getElementById('cancelAppointmentId').value = appointmentId;
        document.getElementById('cancelModal').style.display = 'block';
    }
    
    static openConfirmOnlineModal(button) {
        const appointmentId = button.getAttribute('data-appointment-id');
        document.getElementById('confirmAppointmentId').value = appointmentId;
        document.getElementById('confirmOnlineModal').style.display = 'block';
    }
    
    static closeModal(modalId) {
        document.getElementById(modalId).style.display = 'none';
    }
    
    static confirmAppointment(button) {
        const appointmentId = button.getAttribute('data-appointment-id');
        if (confirm('Are you sure you want to confirm this appointment?')) {
            const form = document.createElement('form');
            form.method = 'post';
            form.action = '/doctor/dashboard/appointment/confirm';
            
            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = '_csrf';
            csrfInput.value = document.querySelector('meta[name="_csrf"]').content;
            form.appendChild(csrfInput);
            
            const idInput = document.createElement('input');
            idInput.type = 'hidden';
            idInput.name = 'appointmentId';
            idInput.value = appointmentId;
            form.appendChild(idInput);
            
            document.body.appendChild(form);
            form.submit();
        }
    }
    
    static rescheduleAppointment(button) {
        const appointmentId = button.getAttribute('data-appointment-id');
        window.location.href = '/doctor/appointments/reschedule/' + appointmentId;
    }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    AppointmentRequests.init();
    
    // Close modal when clicking outside
    window.onclick = function(event) {
        if (event.target.className === 'modal') {
            event.target.style.display = 'none';
        }
    }
});