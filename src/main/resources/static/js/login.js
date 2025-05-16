/**
 * Login page specific JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    // Initialize password toggle
    togglePasswordVisibility('loginPassword', 'toggleLoginPassword');

    // Google sign-in button
    const googleBtn = document.getElementById('googleLoginBtn');
    if (googleBtn) {
        googleBtn.addEventListener('click', function() {
            window.location.href = '/auth/google';
        });
    }

    // Form submission handling
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            const submitBtn = this.querySelector('button[type="submit"]');
            submitBtn.classList.add('btn-loading');
            submitBtn.disabled = true;
            
            // Client-side validation
            const email = document.getElementById('loginEmail');
            const password = document.getElementById('loginPassword');
            let isValid = true;
            
            if (!email.value || !email.validity.valid) {
                email.classList.add('is-invalid');
                isValid = false;
            }
            
            if (!password.value || password.value.length < 8) {
                password.classList.add('is-invalid');
                isValid = false;
            }
            
            if (!isValid) {
                e.preventDefault();
                submitBtn.classList.remove('btn-loading');
                submitBtn.disabled = false;
                showToast('Please fill all fields correctly', 'error');
            }
        });
    }

    // Clear validation on input
    const inputs = document.querySelectorAll('.form-control');
    inputs.forEach(input => {
        input.addEventListener('input', function() {
            this.classList.remove('is-invalid');
        });
    });
});