/**
 * Shared authentication functions
 */

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Show any server-side messages
    const messages = document.querySelectorAll('.message');
    messages.forEach(message => {
        // Make message visible
        setTimeout(() => {
            message.style.opacity = '1';
            message.style.maxHeight = '500px'; // Allow for content expansion
        }, 100);
        
        // Auto-hide messages after 5 seconds
        setTimeout(() => {
            message.style.opacity = '0';
            message.style.maxHeight = '0';
            message.style.marginBottom = '0';
            message.style.padding = '0';
            setTimeout(() => {
                message.remove();
            }, 300);
        }, 5000);
    });

    // Initialize password toggles
    initPasswordToggles();
});

// Initialize all password visibility toggles
function initPasswordToggles() {
    document.querySelectorAll('.password-toggle').forEach(toggle => {
        const input = toggle.closest('.password-input')?.querySelector('input');
        if (input) {
            toggle.addEventListener('click', function() {
                const type = input.type === 'password' ? 'text' : 'password';
                input.type = type;
                this.classList.toggle('fa-eye-slash');
            });
        }
    });
}

// Show form validation errors
function showFormErrors(form, errors) {
    // Clear previous errors
    form.querySelectorAll('.is-invalid').forEach(el => {
        el.classList.remove('is-invalid');
    });
    
    // Add new errors
    Object.entries(errors).forEach(([field, message]) => {
        const input = form.querySelector(`[name="${field}"]`);
        if (input) {
            input.classList.add('is-invalid');
            const feedback = input.nextElementSibling;
            if (feedback && feedback.classList.contains('invalid-feedback')) {
                feedback.textContent = message;
            }
        }
    });
}

// Validate form before submission
function validateForm(form) {
    let isValid = true;
    
    // Check required fields
    form.querySelectorAll('[required]').forEach(input => {
        if (!input.value.trim()) {
            input.classList.add('is-invalid');
            isValid = false;
        }
    });
    
    // Check password match
    const password = form.querySelector('#password');
    const confirmPassword = form.querySelector('#confirmPassword');
    if (password && confirmPassword && password.value !== confirmPassword.value) {
        confirmPassword.classList.add('is-invalid');
        isValid = false;
    }
    
    return isValid;
}