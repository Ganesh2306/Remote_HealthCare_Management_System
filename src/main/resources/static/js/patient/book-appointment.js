/**
     * Enhanced Appointment Booking System with Detailed Logging
     */
    class AppointmentBooking {
        constructor() {
            console.log('AppointmentBooking class initialized');
            this.config = {
                workingHours: { start: 9, end: 17 },
                lunchBreak: { start: 13, end: 14 },
                slotInterval: 30,
                minDate: new Date().toISOString().split('T')[0]
            };

            this.state = {
                selectedDoctor: null,
                selectedDate: null,
                selectedTime: null,
                doctorAvailability: []
            };

            this.init();
        }

        init() {
            console.log('Initializing AppointmentBooking');
            this.cacheElements();
            this.setupEventListeners();
            this.setInitialDate();
            this.disableManualTimeInput();
            console.log('Initialization complete');
        }

        cacheElements() {
            console.log('Caching DOM elements');
            this.elements = {
                form: document.getElementById('appointmentForm'),
                formContainer: document.getElementById('appointmentFormContainer'),
                doctorSelect: document.getElementById('doctor'),
                dateInput: document.getElementById('appointmentDate'),
                timeInput: document.getElementById('appointmentTime'),
                locationSelect: document.getElementById('location'),
                reasonInput: document.getElementById('reason'),
                availabilityContainer: document.getElementById('availability-container'),
                occupiedSlots: document.getElementById('occupied-slots'),
                availableSlots: document.getElementById('available-slots'),
                timeError: document.getElementById('timeError'),
                dateTimeField: document.getElementById('dateTime'),
                csrfToken: document.getElementById('csrfToken'),
                successMessage: document.getElementById('successMessage'),
                successText: document.getElementById('successText'),
                errorContainer: document.getElementById('errorContainer'),
                errorList: document.getElementById('errorList'),
                submitButton: document.getElementById('submitButton')
            };
            console.log('Elements cached:', Object.keys(this.elements));
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

        disableManualTimeInput() {
            console.log('Disabling manual time input');
            if (this.elements.timeInput) {
                this.elements.timeInput.readOnly = true;
                this.elements.timeInput.placeholder = "Select from available slots below";
                this.elements.timeInput.style.backgroundColor = "#f5f5f5";
                this.elements.timeInput.style.cursor = "not-allowed";
            }
        }

        setupEventListeners() {
            console.log('Setting up event listeners');
            
            // Doctor selection
            if (this.elements.doctorSelect) {
                this.elements.doctorSelect.addEventListener('change', (e) => {
                    console.log('Doctor selected:', e.target.value);
                    this.state.selectedDoctor = e.target.value;
                    this.loadDoctorAvailability();
                });
            }

            // Date selection
            if (this.elements.dateInput) {
                this.elements.dateInput.addEventListener('change', (e) => {
                    console.log('Date selected:', e.target.value);
                    this.state.selectedDate = e.target.value;
                    this.updateTimeSlots();
                });
            }

            // Submit button
            if (this.elements.submitButton) {
                this.elements.submitButton.addEventListener('click', (e) => {
                    console.log('Submit button clicked');
                    this.handleFormSubmission();
                });
            }
            
            console.log('Event listeners set up');
        }

        async handleFormSubmission() {
            console.log('Handling form submission');
            this.clearErrors();
            
            if (!this.validateForm()) {
                console.log('Form validation failed');
                return;
            }

            console.log('Form validated successfully');
            this.setFormData();
            await this.submitForm();
        }

        async loadDoctorAvailability() {
            console.log('Loading doctor availability for doctor:', this.state.selectedDoctor, 'and date:', this.state.selectedDate);
            
            if (!this.state.selectedDoctor) {
                console.log('No doctor selected, hiding availability');
                this.hideAvailability();
                return;
            }

            try {
                const date = this.state.selectedDate || this.config.minDate;
                console.log('Fetching availability for date:', date);
                
                const response = await fetch(`/api/doctors/${this.state.selectedDoctor}/availability?date=${date}`);
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

            const lunchStart = "13:00";
            const lunchEnd = "14:00";

            if (data?.occupiedSlots) {
                console.log('Processing occupied slots:', data.occupiedSlots);
                data.occupiedSlots
                    .filter(slot => {
                        const start = this.cleanTimeFormat(slot.startTime);
                        const end = this.cleanTimeFormat(slot.endTime);
                        return !(start === lunchStart && end === lunchEnd);
                    })
                    .forEach(slot => {
                        const startTime = this.cleanTimeFormat(slot.startTime);
                        const endTime = this.cleanTimeFormat(slot.endTime);

                        const slotElement = document.createElement('div');
                        slotElement.className = 'occupied-slot';
                        slotElement.textContent = `${this.formatTime(startTime)} - ${this.formatTime(endTime)}`;
                        this.elements.occupiedSlots.appendChild(slotElement);
                    });
            }

            this.elements.occupiedSlots.innerHTML += `
                <div class="occupied-slot lunch-break">1:00 PM - 2:00 PM</div>
            `;

            if (data?.availableSlots) {
                console.log('Processing available slots:', data.availableSlots);
                data.availableSlots.forEach(slot => {
                    const startTime = this.cleanTimeFormat(slot.startTime);
                    const endTime = this.cleanTimeFormat(slot.endTime);

                    const slotElement = document.createElement('div');
                    slotElement.className = 'available-slot';
                    slotElement.textContent = `${this.formatTime(startTime)} - ${this.formatTime(endTime)}`;
                    slotElement.dataset.time = startTime;
                    slotElement.addEventListener('click', () => {
                        console.log('Time slot selected:', startTime);
                        this.selectTimeSlot(startTime);
                    });
                    this.elements.availableSlots.appendChild(slotElement);
                });
            }

            this.showAvailability();
            console.log('Availability display updated');
        }

        selectTimeSlot(startTime) {
            console.log('Selecting time slot:', startTime);
            const timeValue = this.cleanTimeFormat(startTime);
            
            document.querySelectorAll('.available-slot.selected').forEach(el => {
                el.classList.remove('selected');
            });
            
            this.state.selectedTime = timeValue;
            this.elements.timeInput.value = timeValue;
            
            const selectedSlot = document.querySelector(`.available-slot[data-time="${timeValue}"]`);
            if (selectedSlot) {
                selectedSlot.classList.add('selected');
            }
            
            this.validateTime();
            console.log('Time slot selected:', timeValue);
        }

        updateTimeSlots() {
            console.log('Updating time slots for current selection');
            if (this.state.selectedDoctor) {
                this.loadDoctorAvailability();
            }
        }

        validateTime() {
            console.log('Validating time selection:', this.state.selectedTime);
            if (!this.state.selectedTime) {
                console.log('No time selected - validation failed');
                this.showError('Please select an available time slot', this.elements.timeInput);
                return false;
            }
            
            const [hours] = this.state.selectedTime.split(':');
            if (parseInt(hours) === 13) {
                console.log('Lunch time selected - validation failed');
                this.showError('Cannot book during lunch break (1 PM - 2 PM)', this.elements.timeInput);
                return false;
            }
            
            this.clearErrors(this.elements.timeInput);
            console.log('Time validation passed');
            return true;
        }

        validateForm() {
            console.log('Validating entire form');
            let isValid = true;

            // Validate doctor
            if (!this.state.selectedDoctor) {
                console.log('Doctor not selected - validation failed');
                this.showError('Please select a doctor', this.elements.doctorSelect);
                isValid = false;
            }

            // Validate date
            if (!this.state.selectedDate) {
                console.log('Date not selected - validation failed');
                this.showError('Please select a date', this.elements.dateInput);
                isValid = false;
            }

            // Validate time
            if (!this.validateTime()) {
                isValid = false;
            }

            // Validate location
            if (!this.elements.locationSelect.value) {
                console.log('Location not selected - validation failed');
                this.showError('Please select appointment type', this.elements.locationSelect);
                isValid = false;
            }

            // Validate reason
            if (!this.elements.reasonInput.value.trim()) {
                console.log('Reason not provided - validation failed');
                this.showError('Please enter a reason for your visit', this.elements.reasonInput);
                isValid = false;
            }

            console.log('Form validation result:', isValid);
            return isValid;
        }

        setFormData() {
            console.log('Setting form data for submission');
            if (this.state.selectedDate && this.state.selectedTime) {
                const cleanedTime = this.cleanTimeFormat(this.state.selectedTime);
                const formattedTime = cleanedTime.length === 5 ? `${cleanedTime}:00` : cleanedTime;
                this.elements.dateTimeField.value = `${this.state.selectedDate}T${formattedTime}`;
                console.log('Formatted dateTime:', this.elements.dateTimeField.value);
            }
        }

    
 async submitForm() {
        console.log('Starting form submission');
        
        // Store original state
        const originalButtonHTML = this.elements.submitButton.innerHTML;
        const originalButtonDisabled = this.elements.submitButton.disabled;
        
        // Set loading state
        this.elements.submitButton.disabled = true;
        this.elements.submitButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processing...';
        
        try {
            // Force UI update
            await new Promise(resolve => {
                requestAnimationFrame(() => {
                    requestAnimationFrame(() => {
                        // Additional check for slow devices
                        setTimeout(resolve, 50);
                    });
                });
            });
            
            // Submit form normally
            this.elements.form.submit();
            
            // Fallback with more precise detection
            const submissionTimeout = setTimeout(() => {
                if (document.readyState === 'loading') { // Page hasn't reloaded
                    this.elements.submitButton.disabled = originalButtonDisabled;
                    this.elements.submitButton.innerHTML = originalButtonHTML;
                    this.showError('Submission taking too long. Please check your connection.');
                }
            }, 10000);
            
            // Clean up if page unloads
            window.addEventListener('beforeunload', () => {
                clearTimeout(submissionTimeout);
            });
            
        } catch (error) {
            console.error('Form submission error:', error);
            this.elements.submitButton.disabled = originalButtonDisabled;
            this.elements.submitButton.innerHTML = originalButtonHTML;
            this.showError('Failed to submit form. Please try again.');
        }
    
    
    // Submit the form
    this.elements.form.submit();
    
    // Fallback - restore button if submission fails (unlikely with native submit)
    setTimeout(() => {
        if (this.elements.submitButton.disabled) {
            this.elements.submitButton.disabled = originalButtonDisabled;
            this.elements.submitButton.innerHTML = originalButtonHTML;
        }
    }, 5000); // 5 second timeout
}
        clearErrors() {
            console.log('Clearing all errors');
            document.querySelectorAll('.text-danger').forEach(el => {
                el.textContent = '';
                el.style.display = 'none';
            });
            
            document.querySelectorAll('.is-invalid').forEach(el => {
                el.classList.remove('is-invalid');
            });
            
            this.elements.errorList.innerHTML = '';
            this.elements.errorContainer.style.display = 'none';
        }

        handleErrors(result) {
            console.log('Handling submission errors:', result);
            this.clearErrors();
            
            if (result.errors) {
                console.log('Processing field errors:', result.errors);
                for (const [field, message] of Object.entries(result.errors)) {
                    console.log(`Error in field ${field}:`, message);
                    const element = document.getElementById(field) || 
                                    document.querySelector(`[name="${field}"]`);
                    if (element) {
                        this.showError(message, element);
                    } else {
                        this.showGeneralError(message);
                    }
                }
            } else if (result.message) {
                console.log('General error:', result.message);
                this.showGeneralError(result.message);
            }
        }

        showSuccess(message) {
            console.log('Showing success message:', message);
            this.elements.successText.textContent = message;
            this.elements.successMessage.style.display = 'block';
            setTimeout(() => {
                this.elements.successMessage.style.display = 'none';
            }, 5000);
        }

        showError(message, element = null) {
            console.log('Showing error:', message, 'for element:', element);
            if (element) {
                const errorElement = document.getElementById(`${element.id}Error`) || 
                                      element.nextElementSibling;
                if (errorElement) {
                    errorElement.textContent = message;
                    errorElement.style.display = 'block';
                    element.classList.add('is-invalid');
                }
            } else {
                this.showGeneralError(message);
            }
        }

        showGeneralError(message) {
            console.log('Showing general error:', message);
            const li = document.createElement('li');
            li.textContent = message;
            this.elements.errorList.appendChild(li);
            this.elements.errorContainer.style.display = 'block';
        }


        resetForm() {
            console.log('Resetting form');
            this.elements.form.reset();
            this.state = {
                selectedDoctor: null,
                selectedDate: this.config.minDate,
                selectedTime: null,
                doctorAvailability: []
            };
            this.clearSlots();
            this.hideAvailability();
            this.setInitialDate();
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
            this.elements.timeInput.value = '';
        }
    }

    // Initialize when DOM is ready
   // Single DOMContentLoaded handler
// Replace your current flash message handling with this:
document.addEventListener('DOMContentLoaded', () => {
    console.log('DOM fully loaded and parsed');
    
    // Initialize appointment booking
    new AppointmentBooking();
    
    // Handle flash messages
    const initFlashMessages = () => {
        const showMessage = (element) => {
            if (element && (element.textContent.trim() || element.querySelector('li'))) {
                // Ensure proper initial state
                element.style.display = 'flex';
                element.style.opacity = '0';
                element.style.transform = 'translateY(-20px)';
                element.style.maxHeight = '0';
                
                // Trigger reflow before animating
                void element.offsetHeight;
                
                // Animate in
                element.style.opacity = '1';
                element.style.transform = 'translateY(0)';
                element.style.maxHeight = '200px'; // Adjust based on content
                
                // Close button functionality
                const closeBtn = element.querySelector('.close');
                if (closeBtn) {
                    closeBtn.addEventListener('click', () => {
                        element.style.opacity = '0';
                        element.style.transform = 'translateY(-20px)';
                        element.style.maxHeight = '0';
                        setTimeout(() => {
                            element.remove();
                        }, 300);
                    });
                }
                
                // Auto-hide after 5 seconds
                setTimeout(() => {
                    if (element.parentNode) { // Check if still in DOM
                        element.style.opacity = '0';
                        element.style.transform = 'translateY(-20px)';
                        element.style.maxHeight = '0';
                        setTimeout(() => {
                            element.remove();
                        }, 300);
                    }
                }, 5000);
            }
        };
        
        // Initialize existing messages
        document.querySelectorAll('.alert').forEach(showMessage);
    };
    
    initFlashMessages();
});
   