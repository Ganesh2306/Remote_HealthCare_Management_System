   // Toggle sidebar
        document.getElementById('sidebarToggle').addEventListener('click', function() {
            const sidebar = document.getElementById('sidebar');
            const mainContent = document.getElementById('mainContent');
            
            sidebar.classList.toggle('sidebar-collapsed');
            mainContent.classList.toggle('main-content-expanded');
        });

        // Set today's date as default for prescription start date
        document.addEventListener('DOMContentLoaded', function() {
            const today = new Date().toISOString().split('T')[0];
            document.getElementById('startDate').value = today;
        });

        // Modal functions
        function openPrescriptionModal(button) {
            const appointmentId = button.getAttribute('data-appointment-id');
            const patientId = button.getAttribute('data-patient-id');
            const patientName = button.getAttribute('data-patient-name');
            
            document.getElementById('prescriptionAppointmentId').value = appointmentId;
            document.getElementById('prescriptionPatientId').value = patientId;
            document.getElementById('patientName').value = patientName;
            
            document.getElementById('prescriptionModal').style.display = 'block';
        }

        function openFeedbackModal(button) {
            const appointmentId = button.getAttribute('data-appointment-id');
            const patientId = button.getAttribute('data-patient-id');
            const patientName = button.getAttribute('data-patient-name');
            
            document.getElementById('feedbackAppointmentId').value = appointmentId;
            document.getElementById('feedbackPatientId').value = patientId;
            document.getElementById('feedbackPatientName').value = patientName;
            
            document.getElementById('feedbackModal').style.display = 'block';
        }

        function closeModal(modalId) {
            document.getElementById(modalId).style.display = 'none';
        }

        function markAsDone(button) {
            const appointmentId = button.getAttribute('data-appointment-id');
            document.getElementById('completeModal').style.display = 'block';
            
            document.getElementById('confirmCompleteBtn').onclick = function() {
                // Here you would typically make an AJAX call to mark the appointment as done
                // For now, we'll just close the modal
                closeModal('completeModal');
                // Show a success message
                alert('Appointment marked as completed successfully!');
            };
        }

        // Close modal when clicking outside of it
        window.onclick = function(event) {
            if (event.target.className === 'modal') {
                event.target.style.display = 'none';
            }
        }