document.addEventListener('DOMContentLoaded', function() {
    // Get elements from the page
    const emailDisplay = document.getElementById('email-display');
    const usernameDisplay = document.getElementById('username-display');
    const verifyButton = document.getElementById('verify-button');
    const verificationStatus = document.getElementById('verification-status');
    const statusIcon = document.getElementById('status-icon');
    const statusMessage = document.getElementById('status-message');

    // Extract parameters from URL
    const urlParams = new URLSearchParams(window.location.search);
    const emailFromUrl = urlParams.get('email');
    const usernameFromUrl = urlParams.get('username');

    // Update displayed email/username if from URL parameters
    if (emailFromUrl) {
        emailDisplay.textContent = decodeURIComponent(emailFromUrl);
    }
    if (usernameFromUrl) {
        usernameDisplay.textContent = decodeURIComponent(usernameFromUrl);
    }

    // Check if this is a redirect from verification
    const verified = urlParams.get('verified');
    if (verified === 'true') {
        showVerificationStatus(true, "Email verified successfully!");
    } else if (verified === 'false') {
        showVerificationStatus(false, "Verification failed. Please try again.");
    }


    // Verify button handler (for manual verification)
    verifyButton.addEventListener('click', function() {
        const email = emailFromUrl || emailDisplay.textContent;
        checkVerificationStatus(email);
    });

    // Auto-check verification status every 5 seconds
    setInterval(function() {
        const email = emailFromUrl || emailDisplay.textContent;
        checkVerificationStatus(email);
    }, 5000);

    function checkVerificationStatus(email) {
        fetch('/api/check-verification?email=' + encodeURIComponent(email))
            .then(response => response.json())
            .then(data => {
                if (data.verified) {
                    showVerificationStatus(true, "Email verified successfully!");
                    verifyButton.style.display = 'none';
                    // Optional: Redirect after successful verification
                    setTimeout(() => {
                        window.location.href = '/dashboard?verified=true';
                    }, 2000);
                } else {
                    verifyButton.style.display = 'inline-block';
                }
            });
    }

    function showVerificationStatus(isSuccess, message) {
        verificationStatus.style.display = 'block';
        statusMessage.textContent = message;
        
        if (isSuccess) {
            verificationStatus.className = 'alert alert-success';
            statusIcon.className = 'fas fa-check-circle';
        } else {
            verificationStatus.className = 'alert alert-error';
            statusIcon.className = 'fas fa-exclamation-circle';
        }
    }
});