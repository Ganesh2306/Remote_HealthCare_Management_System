class AppointmentRescheduler {
    constructor() {
        console.log('AppointmentRescheduler initialized');
        this.config = {
            workingHours: { start: 9, end: 17 },
            lunchBreak: { start: 13, end: 14 },
            slotInterval: 30,
            minDate: new Date().toISOString().split('T')[0]
        };

        this.state = {
            selectedDate: null,
            selectedTime: null,
            doctorAvailability: [],
            currentAppointmentId: currentAppointmentId,
            doctorId: doctorId,
            currentAppointmentTime: document.querySelector('.current-appointment div:nth-child(2) span:nth-child(2)')?.textContent
        };

        this.init();
    }

    init() {
        console.log('Initializing AppointmentRescheduler');
        this.cacheElements();
        this.setupEventListeners();
        this.setInitialDate();
        this.loadDoctorAvailability();
        console.log('Initialization complete');
    }

    cacheElements() {
        console.log('Caching DOM elements');
        this.elements = {
            form: document.getElementById('appointmentForm'),
            dateInput: document.getElementById('appointmentDate'),
            timeInput: document.getElementById('appointmentTime'),
            availabilityContainer: document.getElementById('availability-container'),
            occupiedSlots: document.getElementById('occupied-slots'),
            availableSlots: document.getElementById('available-slots'),
            timeError: document.getElementById('timeError'),
            startTimeField: document.getElementById('startTime'),
            submitButton: document.querySelector('button[type="submit"]')
        };
        console.log('Elements cached:', Object.keys(this.elements));
    }

    setupEventListeners() {
        console.log('Setting up event listeners');
        
        // Date selection
        if (this.elements.dateInput) {
            this.elements.dateInput.addEventListener('change', (e) => {
                console.log('Date selected:', e.target.value);
                this.state.selectedDate = e.target.value;
                this.updateTimeSlots();
            });
        }

        // Form submission
        if (this.elements.form) {
            this.elements.form.addEventListener('submit', (e) => {
                this.handleFormSubmission(e);
            });
        }
        
        console.log('Event listeners set up');
    }

    async handleFormSubmission(e) {
        e.preventDefault();
        console.log('Handling form submission');
        
        if (!this.validateForm()) {
            console.log('Form validation failed');
            return;
        }

        console.log('Form validated successfully');
        await this.submitForm();
    }

    setInitialDate() {
        console.log('Setting initial date');
        if (this.elements.dateInput) {
            this.elements.dateInput.min = this.config.minDate;
            this.elements.dateInput.value = this.config.minDate;
            this.state.selectedDate = this.config.minDate;
            console.log('Date set to:', this.config.minDate);
        }
    }

    async loadDoctorAvailability() {
        console.log('Loading doctor availability for doctor:', this.state.doctorId, 
                   'excluding appointment:', this.state.currentAppointmentId);
        
        if (!this.state.doctorId) {
            console.log('No doctor ID found, hiding availability');
            this.hideAvailability();
            return;
        }

        try {
            const date = this.state.selectedDate || this.config.minDate;
            
            console.log('Fetching availability for date:', date);
            
            const url = `/api/doctors/${this.state.doctorId}/availability?date=${date}&excludeAppointmentId=${this.state.currentAppointmentId}`;
            const response = await fetch(url);
            console.log('Response received:', response.status);
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const data = await response.json();
            console.log('Availability data received:', data);
            
            this.state.doctorAvailability = data;
            this.displayAvailability(data);
            
        } catch (error) {
            console.error('Error loading doctor availability:', error);
            this.showError('Failed to load availability. Please try again.');
            this.hideAvailability();
        }
    }

    displayAvailability(data) {
        console.log('Displaying availability data:', data);
        this.clearSlots();

        // Show occupied slots (excluding current appointment)
        if (data?.occupiedSlots) {
            console.log('Processing occupied slots:', data.occupiedSlots);
            data.occupiedSlots.forEach(slot => {
                const startTime = this.cleanTimeFormat(slot.startTime);
                const endTime = this.cleanTimeFormat(slot.endTime);

                const slotElement = document.createElement('div');
                slotElement.className = 'occupied-slot';
                slotElement.textContent = `${this.formatTime(startTime)} - ${this.formatTime(endTime)}`;
                this.elements.occupiedSlots.appendChild(slotElement);
            });
        }

        // Add lunch break
        this.elements.occupiedSlots.innerHTML += `
            <div class="occupied-slot lunch-break">1:00 PM - 2:00 PM</div>
        `;

        // Show available slots
        if (data?.availableSlots) {
            console.log('Processing available slots:', data.availableSlots);
            data.availableSlots.forEach(slot => {
                const startTime = this.cleanTimeFormat(slot.startTime);
                const endTime = this.cleanTimeFormat(slot.endTime);

                const slotElement = document.createElement('div');
                slotElement.className = 'available-slot';
                slotElement.textContent = `${this.formatTime(startTime)} - ${this.formatTime(endTime)}`;
                slotElement.dataset.startTime = startTime;
                slotElement.dataset.endTime = endTime;
                slotElement.addEventListener('click', () => {
                    console.log('Time slot selected:', startTime);
                    this.selectTimeSlot(startTime, endTime);
                });
                this.elements.availableSlots.appendChild(slotElement);
            });
        }

        this.showAvailability();
        console.log('Availability display updated');
    }

    selectTimeSlot(startTime, endTime) {
        console.log('Selecting time slot:', startTime);
        const timeValue = this.cleanTimeFormat(startTime);
        
        // Remove selection from all slots
        document.querySelectorAll('.available-slot.selected').forEach(el => {
            el.classList.remove('selected');
        });
        
        // Add selection to clicked slot
        const selectedSlot = document.querySelector(`.available-slot[data-start-time="${startTime}"]`);
        if (selectedSlot) {
            selectedSlot.classList.add('selected');
        }
        
        // Update form fields
        this.state.selectedTime = timeValue;
        this.elements.timeInput.value = timeValue;
        this.elements.startTimeField.value = `${this.state.selectedDate}T${timeValue}:00`;
        
        this.clearErrors();
        console.log('Time slot selected:', timeValue);
    }

    updateTimeSlots() {
        console.log('Updating time slots for current selection');
        this.clearSlots();
        this.loadDoctorAvailability();
    }

    validateForm() {
        console.log('Validating entire form');
        let isValid = true;

        // Validate date
        if (!this.state.selectedDate) {
            console.log('Date not selected - validation failed');
            this.showError('Please select a date', this.elements.dateInput);
            isValid = false;
        }

        // Validate time
        if (!this.state.selectedTime) {
            console.log('Time not selected - validation failed');
            this.showError('Please select an available time slot', this.elements.timeInput);
            isValid = false;
        }

        console.log('Form validation result:', isValid);
        return isValid;
    }

    cleanTimeFormat(timeStr) {
        if (!timeStr) return '';
        const timeMatch = timeStr.match(/(\d{2}:\d{2})/);
        return timeMatch?.[1] || timeStr.substring(0, 5);
    }

    formatTime(timeString) {
        const cleanedTime = this.cleanTimeFormat(timeString);
        const [hours, minutes] = cleanedTime.split(':');
        const hourNum = parseInt(hours);
        const ampm = hourNum >= 12 ? 'PM' : 'AM';
        const displayHour = hourNum % 12 || 12;
        return `${displayHour}:${minutes} ${ampm}`;
    }

   async submitForm() {
    console.log('Starting form submission');
    
    const originalButtonHTML = this.elements.submitButton.innerHTML;
    const originalButtonDisabled = this.elements.submitButton.disabled;
    
    this.elements.submitButton.disabled = true;
    this.elements.submitButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';
    
    try {
        const formData = new FormData(this.elements.form);
        
        const response = await fetch(this.elements.form.action, {
            method: 'POST',
            body: formData,
            headers: {
                // Tell the server this is an AJAX request, but don't expect JSON back
                redirect: 'follow',
                'X-Requested-With': 'XMLHttpRequest'
            },
            redirect: 'follow' // Allow automatic redirect
        });

        if (!response.ok) {
            // Read response text (usually HTML) to inspect or log it
            const errorText = await response.text();
            throw new Error('Failed to reschedule appointment');
        }

        // Success — redirect the user
        window.location.href = response.url;

    } catch (error) {
        console.error('Form submission error:', error);
        this.elements.submitButton.disabled = originalButtonDisabled;
        this.elements.submitButton.innerHTML = originalButtonHTML;
        this.showFlashMessage(error.message || 'Failed to reschedule appointment. Please try again.', 'error');
    }
}

    showAvailability() {
        console.log('Showing availability container');
        if (this.elements.availabilityContainer) {
            this.elements.availabilityContainer.style.display = 'block';
        }
    }

    hideAvailability() {
        console.log('Hiding availability container');
        if (this.elements.availabilityContainer) {
            this.elements.availabilityContainer.style.display = 'none';
        }
    }

    clearSlots() {
        console.log('Clearing time slots');
        if (this.elements.occupiedSlots) this.elements.occupiedSlots.innerHTML = '';
        if (this.elements.availableSlots) this.elements.availableSlots.innerHTML = '';
        this.state.selectedTime = null;
        if (this.elements.timeInput) this.elements.timeInput.value = '';
        if (this.elements.startTimeField) this.elements.startTimeField.value = '';
    }

   showError(message, element = null) {
    console.log('Showing error:', message, 'for element:', element);
    
    if (element) {
        // Field-specific error (keep your existing implementation)
        const errorElement = element.nextElementSibling;
        if (errorElement && errorElement.classList.contains('error-message')) {
            errorElement.textContent = message;
            errorElement.style.display = 'block';
            element.classList.add('is-invalid');
        }
    } else {
        // General error - show in flash message container
        this.showFlashMessage(message, 'error');
    }
}

// Add this new method to handle flash messages
showFlashMessage(message, type = 'error') {
    const container = document.getElementById('flash-message-container');
    if (!container) return;
    
    const messageElement = document.createElement('div');
    messageElement.className = `flash-message ${type}`;
    messageElement.innerHTML = `
        <span>${message}</span>
        <span class="close-btn">&times;</span>
    `;
    
    // Add click handler for close button
    messageElement.querySelector('.close-btn').addEventListener('click', () => {
        messageElement.remove();
    });
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
        messageElement.remove();
    }, 5000);
    
    container.appendChild(messageElement);
}

    clearErrors() {
        console.log('Clearing all errors');
        if (this.elements.timeError) {
            this.elements.timeError.style.display = 'none';
        }
        document.querySelectorAll('.is-invalid').forEach(el => {
            el.classList.remove('is-invalid');
        });
    }
}

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    console.log('DOM fully loaded and parsed');
    new AppointmentRescheduler();
});