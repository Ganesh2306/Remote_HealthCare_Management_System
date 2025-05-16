document.addEventListener('DOMContentLoaded', function() {
    // Form elements
    const form = document.getElementById('vitalCSVForm');
    const fileInput = document.getElementById('csvFile');
    const fileNameSpan = document.getElementById('fileName');
    const fileError = document.getElementById('fileError');
    
    // Status elements
    const processingStatus = document.getElementById('processingStatus');
    const resultsSummary = document.getElementById('resultsSummary');
    const successCount = document.getElementById('successCount');
    const errorCount = document.getElementById('errorCount');
    const errorDetails = document.getElementById('errorDetails');
    const dashboardActions = document.getElementById('dashboardActions');
    const uploadAnotherBtn = document.getElementById('uploadAnotherBtn');

    // Update file name display when file is selected
    fileInput.addEventListener('change', function() {
        if (this.files.length > 0) {
            fileNameSpan.textContent = this.files[0].name;
            fileError.textContent = '';
            
            // Validate file extension
            const fileName = this.files[0].name.toLowerCase();
            if (!fileName.endsWith('.csv')) {
                fileError.textContent = 'Only CSV files are allowed';
                this.value = ''; // Clear the file input
                fileNameSpan.textContent = 'Choose a CSV file';
            }
        } else {
            fileNameSpan.textContent = 'Choose a CSV file';
        }
    });

    // Upload another file handler
    uploadAnotherBtn?.addEventListener('click', function() {
        resetForm();
        fileInput.click(); // Reopen file dialog
    });

    // Form submission handler
    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        // Clear previous errors
        fileError.textContent = '';
        
        // Basic validation
        if (fileInput.files.length === 0) {
            fileError.textContent = 'Please select a CSV file to upload';
            fileInput.focus();
            return;
        }
        
        // Check file size (5MB limit)
        if (fileInput.files[0].size > 5 * 1024 * 1024) {
            fileError.textContent = 'File size exceeds 5MB limit';
            return;
        }
        
        // Reset UI state
        resetUploadUI();
        
        try {
            // Show processing status
            processingStatus.style.display = 'flex';
            resultsSummary.style.display = 'none';
            
            // Prepare and send form data
            const formData = new FormData(form);
            const response = await fetch(form.action, {
                method: 'POST',
                body: formData,
                headers: {
                    'Accept': 'application/json'
                }
            });
            
            const result = await response.json();
            
            if (!response.ok) {
                // Handle API-level errors (HTTP errors)
                throw new Error(result.message || `Server returned ${response.status} status`);
            }
            
            displayUploadResults(result);
            
        } catch (error) {
            console.error('Upload failed:', error);
            handleUploadError(error);
        } finally {
            processingStatus.style.display = 'none';
        }
    });
    
    // Helper functions
    
    function resetUploadUI() {
        resultsSummary.style.display = 'none';
        errorDetails.innerHTML = '';
        dashboardActions.style.display = 'none';
    }
    
    function resetForm() {
        form.reset();
        fileNameSpan.textContent = 'Choose a CSV file';
        resetUploadUI();
    }
    
    function displayUploadResults(result) {
        // Update counts - handles both new and old property names
        const successful = result.successfulRecords ?? result.successCount ?? 0;
        const failed = result.failedRecords ?? result.errorCount ?? 0;
        const total = result.totalRecords ?? (successful + failed) ?? 0;
        
        successCount.textContent = successful;
        errorCount.textContent = failed;
        
        // Show appropriate message
        const statusMessage = result.message ?? 
            (result.success ? 'CSV processed successfully' : 'Error processing CSV file');
        
        showToast(result.success ? 'success' : 'error', statusMessage);
        
        // Show dashboard actions if successful
        if (result.success) {
            dashboardActions.style.display = 'block';
        }
        
        // Display errors if available
        if (result.errors && result.errors.length > 0) {
            renderErrors(result.errors);
        }
        
        // Show results section
        resultsSummary.style.display = 'block';
    }
    
    function renderErrors(errors) {
        errorDetails.innerHTML = ''; // Clear previous errors
        
        const errorList = document.createElement('ul');
        errorList.className = 'error-list';
        
        errors.forEach((error, index) => {
            const li = document.createElement('li');
            
            if (typeof error === 'string') {
                // Simple string error
                li.textContent = error;
            } else if (error.errorMessage) {
                // Structured error object
                li.innerHTML = `
                    <strong>${error.recordNumber ? 'Row ' + error.recordNumber : 'Error #' + (index + 1)}</strong>:
                    ${error.field ? `<span class="error-field">${error.field}</span> - ` : ''}
                    <span class="error-message">${error.errorMessage}</span>
                `;
            } else {
                // Fallback for any other error format
                li.textContent = JSON.stringify(error);
            }
            
            errorList.appendChild(li);
        });
        
        errorDetails.appendChild(errorList);
    }
    
    function handleUploadError(error) {
        const errorMessage = error.message || 'An unexpected error occurred during upload';
        showToast('error', errorMessage);
        
        // Create a simple error display
        errorDetails.innerHTML = `
            <div class="error-message">
                <i class="fas fa-exclamation-circle"></i>
                ${errorMessage}
                ${error.stack ? `<div class="error-stack">${error.stack}</div>` : ''}
            </div>
        `;
        
        resultsSummary.style.display = 'block';
    }
  function showToast(type, message) {
    // Ensure message exists
    if (!message) {
        console.error('Toast message is empty!');
        message = type === 'success' ? 'Operation completed' : 'An error occurred';
    }

    // Create or get container
    let toastContainer = document.getElementById('toastContainer');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'toastContainer';
        toastContainer.className = 'toast-container';
        document.body.appendChild(toastContainer);
    }

    // Create toast element
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    // Use simple icons as fallback
    const icon = type === 'success' ? '✓' : '⚠';
    toast.innerHTML = `
        <div class="toast-icon">${icon}</div>
        <div class="toast-message">${message}</div>
        <button class="toast-close">×</button>
    `;

    // Add close handler
    toast.querySelector('.toast-close').addEventListener('click', () => {
        toast.classList.add('slide-out');
        toast.addEventListener('animationend', () => toast.remove());
    });

    // Add to DOM
    toastContainer.appendChild(toast);

    // Force reflow and animate in
    setTimeout(() => {
        toast.style.transform = 'translateX(0)';
        toast.style.opacity = '1';
    }, 10);

    // Auto-remove after 5 seconds
    setTimeout(() => {
        toast.classList.add('slide-out');
        toast.addEventListener('animationend', () => toast.remove());
    }, 5000);
}

function createToastContainer() {
    const container = document.createElement('div');
    container.id = 'toastContainer';
    container.className = 'toast-container';
    document.body.appendChild(container);
    return container;
} 
  
});