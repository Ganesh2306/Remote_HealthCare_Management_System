
    // Global variables to track current appointment
    var currentAppointmentId = null;
    var currentSource = 'requested'; // Default source
    
    $(document).ready(function() {
        // Initialize form submission handler
        $('#cancelForm').submit(function(e) {
            e.preventDefault();
            handleCancelFormSubmit();
        });
        
        // Close modal when clicking outside
        $(document).on('click', function(e) {
            if ($(e.target).hasClass('cancel-modal')) {
                closeCancelModal();
            }
        });
        
        // Close modal with escape key
        $(document).on('keydown', function(e) {
            if (e.key === 'Escape' && $('#cancelModal').is(':visible')) {
                closeCancelModal();
            }
        });
    });
    
    /* Modal Functions */
    function openCancelModal(appointmentId, source) {
        currentAppointmentId = appointmentId;
        currentSource = source || 'requested';
        $('#appointmentId').val(appointmentId);
        $('#cancellationReason').val('');
        $('#cancelModal').css('display', 'flex');
    }
    
    function closeCancelModal() {
        $('#cancelModal').hide();
        $('#cancellationReason').val('');
        currentAppointmentId = null;
    }
    
    /* Form Handling */
    function handleCancelFormSubmit() {
        const reason = $('#cancellationReason').val().trim();
        
        if (!validateCancellationForm(reason)) {
            return;
        }
        
        const $submitBtn = $('#cancelForm').find('button[type="submit"]');
        const originalText = $submitBtn.html();
        $submitBtn.prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Processing...');
        
        submitCancellationRequest(reason, $submitBtn, originalText);
    }
    
    function validateCancellationForm(reason) {
        if (!reason) {
            showToast('Please provide a cancellation reason', 'error');
            $('#cancellationReason').focus();
            return false;
        }
        
        if (!currentAppointmentId) {
            showToast('No appointment selected for cancellation', 'error');
            return false;
        }
        
        return true;
    }
    
    /* API Request Handling */
    function submitCancellationRequest(reason, $submitBtn, originalText) {
        const csrfToken = $("meta[name='_csrf']").attr("content");
        const csrfHeader = $("meta[name='_csrf_header']").attr("content");
        
        $.ajax({
            url: '/patient/dashboard/appointment/' + currentAppointmentId + '/cancel',
            method: 'POST',
            data: {
                reason: reason,
                source: currentSource
            },
            headers: {
                [csrfHeader]: csrfToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            success: function(data, status, xhr) {
                handleCancellationSuccess(xhr);
            },
            error: function(xhr) {
                handleCancellationError(xhr);
            },
            complete: function() {
                $submitBtn.prop('disabled', false).html(originalText);
            }
        });
    }
    
    function handleCancellationSuccess(xhr) {
        // Check for redirect in response headers (Spring redirect)
        const redirectUrl = xhr.getResponseHeader('Location');
        if (redirectUrl) {
            window.location.href = redirectUrl;
            return;
        }
        
        // Handle success case
        showToast('Appointment cancelled successfully', 'success');
        closeCancelModal();
        
        // Refresh after delay to ensure backend processed the request
        setTimeout(() => {
            window.location.reload();
        }, 1500);
    }
    
    function handleCancellationError(xhr) {
        let errorMsg = 'Failed to cancel appointment. ';
        
        // Handle specific HTTP status codes
        switch(xhr.status) {
            case 400: // BadRequestException
                errorMsg = extractErrorMessage(xhr) || 'Invalid request. Please check your input.';
                break;
            case 404: // ResourceNotFoundException
                errorMsg = 'Appointment not found. It may have been already cancelled.';
                // Refresh page since appointment doesn't exist
                setTimeout(() => window.location.reload(), 3000);
                break;
            case 500: // Other server errors
                errorMsg = extractErrorMessage(xhr) || 'Server error. Please try again later.';
                break;
            default:
                errorMsg += extractErrorMessage(xhr) || 'Please try again later.';
        }
        
        showToast(errorMsg, 'error');
    }
    
    function extractErrorMessage(xhr) {
        try {
            // Try to parse JSON error response
            if (xhr.responseJSON) {
                return xhr.responseJSON.error || xhr.responseJSON.message;
            }
            
            // Try to parse response text as JSON
            if (xhr.responseText) {
                const response = JSON.parse(xhr.responseText);
                return response.error || response.message;
            }
            
            // Return raw response text if not JSON
            return xhr.responseText;
        } catch (e) {
            console.error('Error parsing error response:', e);
            return null;
        }
    }
    
    /* Toast Notification */
    function showToast(message, type) {
        const toastContainer = $('#toastContainer');
        const toast = $(`
            <div class="toast toast-${type}">
                <i class="fas ${getToastIcon(type)} toast-icon"></i>
                <div class="toast-message">${message}</div>
                <button class="toast-close">&times;</button>
            </div>
        `);
        
        toastContainer.append(toast);
        
        // Animate toast in
        setTimeout(() => toast.addClass('show'), 10);
        
        // Auto-remove toast after delay
        setTimeout(() => {
            toast.removeClass('show');
            setTimeout(() => toast.remove(), 300);
        }, 5000);
        
        // Manual close handler
        toast.find('.toast-close').click(() => {
            toast.removeClass('show');
            setTimeout(() => toast.remove(), 300);
        });
    }
    
    function getToastIcon(type) {
        const icons = {
            success: 'fa-check-circle',
            error: 'fa-exclamation-circle',
            warning: 'fa-exclamation-triangle',
            info: 'fa-info-circle'
        };
        return icons[type] || 'fa-info-circle';
    }

    setTimeout(function () {
        const flashMessages = document.querySelectorAll('.flash-messages .alert');
        flashMessages.forEach(msg => {
            msg.style.transition = 'opacity 0.5s ease';
            msg.style.opacity = '0';
            setTimeout(() => msg.remove(), 500); // Remove element from DOM after fade out
        });
    }, 4000); // 4
   