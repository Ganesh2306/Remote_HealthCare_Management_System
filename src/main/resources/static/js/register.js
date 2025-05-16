/**
 * Registration page specific JavaScript
 */
document.addEventListener('DOMContentLoaded', function() {
    // Password strength indicator
    const passwordInput = document.getElementById('password');
    if (passwordInput) {
        passwordInput.addEventListener('input', updatePasswordStrength);
    }
    
    // Form submission handling
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', handleFormSubmission);
        
        // Real-time validation on blur
        registerForm.querySelectorAll('input, select, textarea').forEach(input => {
            input.addEventListener('blur', validateField);
        });
    }
});

// Update password strength meter
function updatePasswordStrength() {
    const strengthBar = document.getElementById('passwordStrength');
    if (!strengthBar) return;
    
    const password = this.value;
    let strength = 0;
    
    // Length check
    if (password.length >= 8) strength++;
    // Uppercase check
    if (/[A-Z]/.test(password)) strength++;
    // Number check
    if (/\d/.test(password)) strength++;
    // Special char check
    if (/[@$!%*#?&]/.test(password)) strength++;
    
    // Update strength meter
    strengthBar.style.width = (strength * 25) + '%';
    strengthBar.className = 'password-strength-bar strength-' + strength;
}

// Validate individual field
function validateField() {
    if (this.checkValidity()) {
        this.classList.remove('is-invalid');
    } else {
        this.classList.add('is-invalid');
    }
    
    // Special case for password confirmation
    if (this.id === 'password' || this.id === 'confirmPassword') {
        const password = document.getElementById('password')?.value;
        const confirmPassword = document.getElementById('confirmPassword')?.value;
        
        if (password && confirmPassword && password !== confirmPassword) {
            document.getElementById('confirmPassword').classList.add('is-invalid');
        } else if (this.id === 'confirmPassword') {
            this.classList.remove('is-invalid');
        }
    }
}

// Handle form submission
function handleFormSubmission(e) {
    // Validate form
    if (!validateForm(this)) {
        e.preventDefault();
        
        // Scroll to first error
        const firstError = this.querySelector('.is-invalid');
        if (firstError) {
            firstError.scrollIntoView({
                behavior: 'smooth',
                block: 'center'
            });
        }
    }
}