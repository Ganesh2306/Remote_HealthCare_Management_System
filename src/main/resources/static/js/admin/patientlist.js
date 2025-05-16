// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initPatientList();
    setupModalEventListeners();
});

function initPatientList() {
    // Add event listeners for search functionality
    const searchInput = document.getElementById('patientSearch');
    if (searchInput) {
        searchInput.addEventListener('keyup', searchPatients);
    }
}

function setupModalEventListeners() {
    // Deactivate modal handlers
    const deactivateReason = document.getElementById('deactivateReason');
    if (deactivateReason) {
        deactivateReason.addEventListener('change', function() {
            handleReasonChange('deactivate');
        });
    }

    const deactivateOtherReason = document.getElementById('deactivateOtherReason');
    if (deactivateOtherReason) {
        deactivateOtherReason.addEventListener('input', function() {
            handleOtherReasonInput('deactivate');
        });
    }

    const deactivateForm = document.getElementById('deactivateForm');
    if (deactivateForm) {
        deactivateForm.addEventListener('submit', function(e) {
            validateForm('deactivate', e);
        });
    }

    // Reactivate modal handlers
    const reactivateReason = document.getElementById('reactivateReason');
    if (reactivateReason) {
        reactivateReason.addEventListener('change', function() {
            handleReasonChange('reactivate');
        });
    }

    const reactivateOtherReason = document.getElementById('reactivateOtherReason');
    if (reactivateOtherReason) {
        reactivateOtherReason.addEventListener('input', function() {
            handleOtherReasonInput('reactivate');
        });
    }

    const reactivateForm = document.getElementById('reactivateForm');
    if (reactivateForm) {
        reactivateForm.addEventListener('submit', function(e) {
            validateForm('reactivate', e);
        });
    }

    // Close modals when clicking outside
    window.addEventListener('click', function(event) {
        if (event.target.classList.contains('modal')) {
            closeModal(event.target.id);
        }
    });
}

// Generic modal functions
function showModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden'; // Prevent scrolling
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = ''; // Restore scrolling
    }
}

// Deactivate/Reactivate confirmation
function confirmDeactivate(button) {
    const patientId = button.getAttribute('data-id');
    const form = document.getElementById('deactivateForm');
    form.action = `/admin/dashboard/manage/patients/deactivate/${patientId}`;
    showStatusChangeModal(button, 'deactivate');
}

function confirmReactivate(button) {
    showStatusChangeModal(button, 'reactivate');
}

// All your existing JavaScript remains the same
// Only update the showStatusChangeModal function:

function showStatusChangeModal(button, actionType) {
    const patientId = button.getAttribute('data-id');
    const patientName = button.getAttribute('data-name');
    const modalId = `${actionType}Modal`;
    
    document.getElementById(`${actionType}PatientName`).textContent = patientName;
    
    // Set form action with proper context path
    const form = document.getElementById(`${actionType}Form`);
    form.action = `${window.location.origin}/admin/dashboard/manage/patients/${actionType}/${patientId}`;
    
    // Reset form
    document.getElementById(`${actionType}Reason`).value = '';
    document.getElementById(`${actionType}OtherReason`).value = '';
    document.getElementById(`${actionType}OtherReasonContainer`).style.display = 'none';
    document.getElementById(`${actionType}ReasonError`).style.display = 'none';
    
    showModal(modalId);
}

// Rest of your JavaScript remains the same
// Generic handlers for reason selection
function handleReasonChange(actionType) {
    const reason = document.getElementById(`${actionType}Reason`).value;
    const otherContainer = document.getElementById(`${actionType}OtherReasonContainer`);
    
    if (reason === 'Other') {
        otherContainer.style.display = 'block';
    } else {
        otherContainer.style.display = 'none';
        document.getElementById(`final${capitalizeFirstLetter(actionType)}Reason`).value = reason;
    }
}

function handleOtherReasonInput(actionType) {
    const otherReason = document.getElementById(`${actionType}OtherReason`).value.trim();
    const reasonSelect = document.getElementById(`${actionType}Reason`);
    
    if (reasonSelect.value === 'Other' && otherReason.length > 0) {
        document.getElementById(`final${capitalizeFirstLetter(actionType)}Reason`).value = otherReason;
    }
}

function validateForm(actionType, e) {
    const reason = document.getElementById(`${actionType}Reason`).value;
    const otherReason = document.getElementById(`${actionType}OtherReason`).value.trim();
    const errorElement = document.getElementById(`${actionType}ReasonError`);
    
    if (!reason || (reason === 'Other' && !otherReason)) {
        e.preventDefault();
        errorElement.style.display = 'block';
        return false;
    }
    
    // Set final reason
    const finalReason = reason === 'Other' ? otherReason : reason;
    document.getElementById(`final${capitalizeFirstLetter(actionType)}Reason`).value = finalReason;
    return true;
}

// Helper function
function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

// Search functionality
function searchPatients() {
    const input = document.getElementById('patientSearch');
    const filter = input.value.toUpperCase();
    const table = document.querySelector('.patients-table');
    
    if (!table) return;
    
    const rows = table.querySelectorAll('tbody tr');
    
    rows.forEach(row => {
        const nameCell = row.querySelector('td:nth-child(2)'); // Name column
        if (nameCell) {
            const txtValue = nameCell.textContent || nameCell.innerText;
            row.style.display = txtValue.toUpperCase().includes(filter) ? '' : 'none';
        }
    });
}