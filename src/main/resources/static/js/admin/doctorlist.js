// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initDoctorList();
    setupModalEventListeners();
});

function initDoctorList() {
    // Add event listeners for search functionality
    const searchInput = document.getElementById('doctorSearch');
    if (searchInput) {
        searchInput.addEventListener('keyup', searchDoctors);
    }
}

function setupModalEventListeners() {
    // Delete modal handlers
    const deleteReason = document.getElementById('deleteReason');
    if (deleteReason) {
        deleteReason.addEventListener('change', function() {
            handleReasonChange('delete');
        });
    }

    const deleteOtherReason = document.getElementById('deleteOtherReason');
    if (deleteOtherReason) {
        deleteOtherReason.addEventListener('input', function() {
            handleOtherReasonInput('delete');
        });
    }

    const deleteForm = document.getElementById('deleteForm');
    if (deleteForm) {
        deleteForm.addEventListener('submit', function(e) {
            validateForm('delete', e);
        });
    }

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

// Action confirmation functions
function confirmDelete(button) {
    const doctorId = button.getAttribute('data-id');
    const doctorName = button.getAttribute('data-name');
    
    document.getElementById('deleteDoctorName').textContent = doctorName;
    document.getElementById('deleteForm').action = `/admin/dashboard/manage/doctors/delete/${doctorId}`;
    
    // Reset form
    document.getElementById('deleteReason').value = '';
    document.getElementById('deleteOtherReason').value = '';
    document.getElementById('deleteOtherReasonContainer').style.display = 'none';
    document.getElementById('deleteReasonError').style.display = 'none';
    
    showModal('deleteModal');
}

function confirmDeactivate(button) {
    const doctorId = button.getAttribute('data-id');
    const doctorName = button.getAttribute('data-name');
    
    document.getElementById('deactivateDoctorName').textContent = doctorName;
    document.getElementById('deactivateForm').action = `/admin/dashboard/manage/doctors/deactivate/${doctorId}`;
    
    // Reset form
    document.getElementById('deactivateReason').value = '';
    document.getElementById('deactivateOtherReason').value = '';
    document.getElementById('deactivateOtherReasonContainer').style.display = 'none';
    document.getElementById('deactivateReasonError').style.display = 'none';
    
    showModal('deactivateModal');
}

function confirmReactivate(button) {
    const doctorId = button.getAttribute('data-id');
    const doctorName = button.getAttribute('data-name');
    
    document.getElementById('reactivateDoctorName').textContent = doctorName;
    document.getElementById('reactivateForm').action = `/admin/dashboard/manage/doctors/reactivate/${doctorId}`;
    
    // Reset form
    document.getElementById('reactivateReason').value = '';
    document.getElementById('reactivateOtherReason').value = '';
    document.getElementById('reactivateOtherReasonContainer').style.display = 'none';
    document.getElementById('reactivateReasonError').style.display = 'none';
    
    showModal('reactivateModal');
}

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

// Search functionality
function searchDoctors() {
    const input = document.getElementById('doctorSearch');
    const filter = input.value.toUpperCase();
    const table = document.querySelector('.doctors-table');
    
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

// Helper function
function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}