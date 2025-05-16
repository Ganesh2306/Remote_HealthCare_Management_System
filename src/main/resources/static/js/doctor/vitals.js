class DoctorVitals {
    static currentVitalId = null;
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
        
        // Set up review button
        if (document.getElementById('confirmReviewBtn')) {
            document.getElementById('confirmReviewBtn').addEventListener('click', this.confirmReview.bind(this));
        }
    }
    
    static openPrescriptionModal(button) {
        this.currentVitalId = button.getAttribute('data-vital-id');
        this.currentPatientId = button.getAttribute('data-patient-id');
        this.currentPatientName = button.getAttribute('data-patient-name');
        
        document.getElementById('prescriptionVitalId').value = this.currentVitalId;
        document.getElementById('prescriptionPatientId').value = this.currentPatientId;
        document.getElementById('patientName').value = this.currentPatientName;
        
        this.showModal('prescriptionModal');
    }
    
    static openFeedbackModal(button) {
        this.currentVitalId = button.getAttribute('data-vital-id');
        this.currentPatientId = button.getAttribute('data-patient-id');
        this.currentPatientName = button.getAttribute('data-patient-name');
        
        document.getElementById('feedbackVitalId').value = this.currentVitalId;
        document.getElementById('feedbackPatientId').value = this.currentPatientId;
        document.getElementById('feedbackPatientName').value = this.currentPatientName;
        
        this.showModal('feedbackModal');
    }
    
    static markAsReviewed(button) {
        this.currentVitalId = button.getAttribute('data-vital-id');
        this.showModal('reviewModal');
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
    
    // Convert form data to URL-encoded format
    const formData = new URLSearchParams(new FormData(form));
    const token = document.querySelector('meta[name="_csrf"]').content;
    const header = document.querySelector('meta[name="_csrf_header"]').content;
    
    fetch(form.action, {
        method: 'POST',
        body: formData,
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            [header]: token
        }
    })
    .then(response => {
        if (response.redirected) {
            window.location.href = response.url;
            return;
        }
        if (!response.ok) {
            throw new Error(`Server returned ${response.status}`);
        }
        return response.text();
    })
    .then(() => {
        window.location.reload();
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
            },
            redirect: 'manual' // Important for handling redirects properly
        })
        .then(response => {
            if (response.redirected) {
                window.location.href = response.url;
                return;
            }
            if (!response.ok) {
                throw new Error('Failed to submit feedback');
            }
            return response.json();
        })
        .then(data => {
            if (data && data.success) {
                this.closeModal('feedbackModal');
                this.showAlert('success', data.message);
                window.location.reload(); // Force reload
            } else if (data) {
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

    static confirmReview() {
        const confirmBtn = document.getElementById('confirmReviewBtn');
        const submitText = confirmBtn.querySelector('.submit-text');
        const spinner = confirmBtn.querySelector('.spinner-border');
        
        // Show loading state
        submitText.style.display = 'none';
        spinner.style.display = 'inline-block';
        confirmBtn.disabled = true;
        
        const token = document.querySelector('meta[name="_csrf"]').content;
        const header = document.querySelector('meta[name="_csrf_header"]').content;
        
        fetch(`/doctor/dashboard/vitals/${this.currentVitalId}/review`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [header]: token,
                'Accept': 'application/json'
            },
            redirect: 'manual'
        })
        .then(response => {
            if (response.redirected) {
                window.location.href = response.url;
                return;
            }
            if (!response.ok) {
                throw new Error('Failed to mark vital as reviewed');
            }
            return response.json();
        })
        .then(data => {
            if (data && data.success) {
                this.closeModal('reviewModal');
                this.showAlert('success', data.message);
                window.location.reload(); // Force reload
            } else if (data) {
                throw new Error(data.message || 'Failed to mark vital as reviewed');
            }
        })
        .catch(error => {
            this.showAlert('error', error.message);
        })
        .finally(() => {
            submitText.style.display = 'inline-block';
            spinner.style.display = 'none';
            confirmBtn.disabled = false;
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
    DoctorVitals.openPrescriptionModal(button);
}

function openFeedbackModal(button) {
    DoctorVitals.openFeedbackModal(button);
}

function closeModal(modalId) {
    DoctorVitals.closeModal(modalId);
}

function markAsReviewed(button) {
    DoctorVitals.markAsReviewed(button);
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    DoctorVitals.init();
});