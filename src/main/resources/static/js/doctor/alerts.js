class DoctorAlerts {
    static currentAlertId = null;
    static currentPatientId = null;
    static currentPatientName = null;

    static init() {
        // Set current date for prescription start date
        const today = new Date().toISOString().split('T')[0];
        if (document.getElementById('startDate')) {
            document.getElementById('startDate').value = today;
            document.getElementById('endDate').value = '';
        }
        
        // Set up form submissions
        if (document.getElementById('prescriptionForm')) {
            document.getElementById('prescriptionForm').addEventListener('submit', this.handlePrescriptionSubmit.bind(this));
        }
        
        if (document.getElementById('feedbackForm')) {
            document.getElementById('feedbackForm').addEventListener('submit', this.handleFeedbackSubmit.bind(this));
        }
        
        // Set up acknowledge button
        if (document.getElementById('confirmAcknowledgeBtn')) {
            document.getElementById('confirmAcknowledgeBtn').addEventListener('click', this.confirmAcknowledge.bind(this));
        }
    }
    
    static openPrescriptionModal(button) {
        this.currentAlertId = button.getAttribute('data-alert-id');
        this.currentPatientId = button.getAttribute('data-patient-id');
        this.currentPatientName = button.getAttribute('data-patient-name');
        
        document.getElementById('prescriptionAlertId').value = this.currentAlertId;
        document.getElementById('prescriptionPatientId').value = this.currentPatientId;
        document.getElementById('patientName').value = this.currentPatientName;
        
        this.showModal('prescriptionModal');
    }
    
    static openFeedbackModal(button) {
        this.currentAlertId = button.getAttribute('data-alert-id');
        this.currentPatientId = button.getAttribute('data-patient-id');
        this.currentPatientName = button.getAttribute('data-patient-name');
        
        document.getElementById('feedbackAlertId').value = this.currentAlertId;
        document.getElementById('feedbackPatientId').value = this.currentPatientId;
        document.getElementById('feedbackPatientName').value = this.currentPatientName;
        
        this.showModal('feedbackModal');
    }
    
    static markAsAcknowledged(button) {
        this.currentAlertId = button.getAttribute('data-alert-id');
        this.showModal('acknowledgeModal');
    }
    
    static showModal(modalId) {
        document.getElementById(modalId).style.display = 'block';
        document.body.style.overflow = 'hidden';
    }
    
    static closeModal(modalId) {
        document.getElementById(modalId).style.display = 'none';
        document.body.style.overflow = 'auto';
    }
    
    static handlePrescriptionSubmit(e) {
        e.preventDefault();
        const form = e.target;
        const submitBtn = form.querySelector('button[type="submit"]');
        const submitText = submitBtn.querySelector('.submit-text');
        const spinner = submitBtn.querySelector('.spinner-border');
        
        // Show loading state
        submitText.style.display = 'none';
        spinner.style.display = 'inline-block';
        submitBtn.disabled = true;
        
        const formData = new FormData(form);
        const token = document.querySelector('meta[name="_csrf"]').content;
        const header = document.querySelector('meta[name="_csrf_header"]').content;
        
        fetch(form.action, {
            method: 'POST',
            body: formData,
            headers: {
                [header]: token,
                'Accept': 'application/json'
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to save prescription');
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                this.closeModal('prescriptionModal');
                this.showAlert('success', data.message || 'Prescription saved successfully');
                setTimeout(() => window.location.reload(), 1500);
            } else {
                throw new Error(data.message || 'Failed to save prescription');
            }
        })
        .catch(error => {
            this.showAlert('error', error.message);
        })
        .finally(() => {
            submitText.style.display = 'inline-block';
            spinner.style.display = 'none';
            submitBtn.disabled = false;
        });
    }
    
    static handleFeedbackSubmit(e) {
        e.preventDefault();
        const form = e.target;
        const submitBtn = form.querySelector('button[type="submit"]');
        const submitText = submitBtn.querySelector('.submit-text');
        const spinner = submitBtn.querySelector('.spinner-border');
        
        // Show loading state
        submitText.style.display = 'none';
        spinner.style.display = 'inline-block';
        submitBtn.disabled = true;
        
        const formData = new FormData(form);
        const token = document.querySelector('meta[name="_csrf"]').content;
        const header = document.querySelector('meta[name="_csrf_header"]').content;
        
        fetch(form.action, {
            method: 'POST',
            body: formData,
            headers: {
                [header]: token,
                'Accept': 'application/json'
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to submit feedback');
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                this.closeModal('feedbackModal');
                this.showAlert('success', data.message || 'Feedback submitted successfully');
                setTimeout(() => window.location.reload(), 1500);
            } else {
                throw new Error(data.message || 'Failed to submit feedback');
            }
        })
        .catch(error => {
            this.showAlert('error', error.message);
        })
        .finally(() => {
            submitText.style.display = 'inline-block';
            spinner.style.display = 'none';
            submitBtn.disabled = false;
        });
    }
    
    static confirmAcknowledge() {
        const confirmBtn = document.getElementById('confirmAcknowledgeBtn');
        const submitText = confirmBtn.querySelector('.submit-text');
        const spinner = confirmBtn.querySelector('.spinner-border');
        
        // Disable button and show spinner without shrinking
        confirmBtn.disabled = true;
        spinner.style.display = 'inline-block';
        submitText.style.marginRight = '8px'; // Maintain spacing
        
        const token = document.querySelector('meta[name="_csrf"]').content;
        const header = document.querySelector('meta[name="_csrf_header"]').content;
        
        fetch(`/doctor/dashboard/alerts/${this.currentAlertId}/acknowledge`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [header]: token,
                'Accept': 'application/json'
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to acknowledge alert');
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                this.closeModal('acknowledgeModal');
                this.showAlert('success', data.message || 'Alert acknowledged successfully');
                setTimeout(() => window.location.reload(), 1500);
            } else {
                throw new Error(data.message || 'Failed to acknowledge alert');
            }
        })
        .catch(error => {
            this.showAlert('error', error.message);
            // Reset button state on error
            confirmBtn.disabled = false;
            spinner.style.display = 'none';
            submitText.style.marginRight = '0';
        });
        // Note: We don't reset on success as page will reload
    }
    
    static markAllAsRead() {
        const token = document.querySelector('meta[name="_csrf"]').content;
        const header = document.querySelector('meta[name="_csrf_header"]').content;
        
        fetch('/doctor/dashboard/alerts/acknowledge-all', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [header]: token,
                'Accept': 'application/json'
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to acknowledge all alerts');
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                this.showAlert('success', data.message || 'All alerts marked as acknowledged');
                setTimeout(() => window.location.reload(), 1500);
            } else {
                throw new Error(data.message || 'Failed to acknowledge all alerts');
            }
        })
        .catch(error => {
            this.showAlert('error', error.message);
        });
    }
    
    static showAlert(type, message) {
        const alertEl = type === 'success' 
            ? document.getElementById('successAlert') 
            : document.getElementById('errorAlert');
        
        const messageEl = type === 'success'
            ? document.getElementById('successMessage')
            : document.getElementById('errorMessage');
        
        messageEl.textContent = message;
        alertEl.style.display = 'flex';
        
        setTimeout(() => {
            alertEl.style.display = 'none';
        }, 5000);
    }
}

// Global functions for button onclick attributes
function openPrescriptionModal(button) {
    DoctorAlerts.openPrescriptionModal(button);
}

function openFeedbackModal(button) {
    DoctorAlerts.openFeedbackModal(button);
}

function closeModal(modalId) {
    DoctorAlerts.closeModal(modalId);
}

function markAsAcknowledged(button) {
    DoctorAlerts.markAsAcknowledged(button);
}

function markAllAsRead() {
    DoctorAlerts.markAllAsRead();
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    DoctorAlerts.init();
});