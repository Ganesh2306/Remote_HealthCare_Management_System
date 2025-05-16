// Configuration Constants
const CONFIG = {
    VITAL_RANGES: {
        heartRate: { min: 60, max: 100 },
        bloodPressure: {
            systolic: { normal: 120, elevated: 130, high1: 140 },
            diastolic: { normal: 80, high1: 90 }
        },
        oxygenLevel: { min: 95, max: 100 },
        temperature: { min: 36.1, max: 37.2 }
    },
    STATUS_CLASSES: {
        normal: 'status-normal',
        elevated: 'status-elevated',
        warning: 'status-warning',
        low: 'status-low'
    },
    STATUS_TEXTS: {
        heartRate: {
            normal: 'Normal',
            low: 'Low',
            high: 'High'
        },
        bloodPressure: {
            normal: 'Normal',
            elevated: 'Elevated',
            high1: 'High (Stage 1)',
            high2: 'High (Stage 2)',
            low: 'Low'
        },
        oxygenLevel: {
            normal: 'Normal',
            low: 'Low'
        },
        temperature: {
            normal: 'Normal',
            low: 'Low',
            high: 'High'
        }
    }
};

class Dashboard {
    constructor() {
        this.initialize = this.initialize.bind(this);
        this.updateVitalStatus = this.updateVitalStatus.bind(this);
        this.updateBloodPressureStatus = this.updateBloodPressureStatus.bind(this);
        this.showAlert = this.showAlert.bind(this);
    }

    initialize() {
        this.updateCurrentDate();
        this.setupEventListeners();
        
        if (typeof dashboardData !== 'undefined') {
            this.updateAllVitalStatuses();
            this.initCharts();
        }

        this.setupAlertCloseButton();
    }

    setupAlertCloseButton() {
        document.querySelectorAll('.alert-close').forEach(button => {
            button.addEventListener('click', (e) => {
                const alertContainer = e.target.closest('.alert-container');
                if (alertContainer) {
                    alertContainer.style.display = 'none';
                    
                    // Optional: Add fade-out animation
                    alertContainer.style.opacity = '1';
                    let opacity = 1;
                    const fadeOut = setInterval(() => {
                        opacity -= 0.1;
                        alertContainer.style.opacity = opacity;
                        if (opacity <= 0) {
                            clearInterval(fadeOut);
                            alertContainer.remove();
                        }
                    }, 50);
                }
            });
        });
    }

    updateCurrentDate() {
        const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
        document.getElementById('currentDate').textContent = 
            new Date().toLocaleDateString('en-US', options);
    }

    setupEventListeners() {
        document.getElementById('emergencyBtn')?.addEventListener('click', () => {
            $('#emergencyModal').fadeIn();
        });
    }

    updateAllVitalStatuses() {
        if (!dashboardData?.latestVitals) return;

        // Heart Rate
        this.updateVitalStatus(
            'heartRate',
            dashboardData.latestVitals.pulseRate,
            'heartRateStatus',
            CONFIG.VITAL_RANGES.heartRate
        );

        // Blood Pressure
        if (dashboardData.latestVitals.bloodPressure) {
            this.updateBloodPressureStatus();
        }

        // Oxygen Level
        this.updateVitalStatus(
            'oxygenLevel',
            dashboardData.latestVitals.oxygenSaturation,
            'oxygenStatus',
            CONFIG.VITAL_RANGES.oxygenLevel
        );

        // Temperature
        this.updateVitalStatus(
            'temperature',
            dashboardData.latestVitals.bodyTemperature,
            'temperatureStatus',
            CONFIG.VITAL_RANGES.temperature
        );
    }

    updateVitalStatus(type, value, elementId, ranges) {
        const element = document.getElementById(elementId);
        if (!element || value === null || value === undefined) {
            if (element) element.textContent = '--';
            return;
        }

        let statusClass, statusText;

        if (value < ranges.min) {
            statusClass = CONFIG.STATUS_CLASSES.low;
            statusText = CONFIG.STATUS_TEXTS[type]?.low || 'Low';
        } else if (value > ranges.max) {
            statusClass = CONFIG.STATUS_CLASSES.warning;
            statusText = CONFIG.STATUS_TEXTS[type]?.high || 'High';
        } else {
            statusClass = CONFIG.STATUS_CLASSES.normal;
            statusText = CONFIG.STATUS_TEXTS[type]?.normal || 'Normal';
        }

        element.className = `vital-status ${statusClass}`;
        element.textContent = statusText;
    }

    updateBloodPressureStatus() {
        const element = document.getElementById('bloodPressureStatus');
        const bp = dashboardData.latestVitals.bloodPressure;
        
        if (!element || !bp || bp.systolic === null || bp.diastolic === null) {
            if (element) element.textContent = '--';
            return;
        }

        let statusClass, statusText;
        const { systolic, diastolic } = bp;
        const ranges = CONFIG.VITAL_RANGES.bloodPressure;

        if (systolic >= ranges.systolic.high1 || diastolic >= ranges.diastolic.high1) {
            statusClass = CONFIG.STATUS_CLASSES.warning;
            statusText = CONFIG.STATUS_TEXTS.bloodPressure.high2;
        } else if (systolic >= ranges.systolic.elevated || diastolic >= ranges.diastolic.normal) {
            statusClass = CONFIG.STATUS_CLASSES.elevated;
            statusText = CONFIG.STATUS_TEXTS.bloodPressure.high1;
        } else if (systolic >= ranges.systolic.normal) {
            statusClass = CONFIG.STATUS_CLASSES.elevated;
            statusText = CONFIG.STATUS_TEXTS.bloodPressure.elevated;
        } else if (systolic < 90 || diastolic < 60) {
            statusClass = CONFIG.STATUS_CLASSES.low;
            statusText = CONFIG.STATUS_TEXTS.bloodPressure.low;
        } else {
            statusClass = CONFIG.STATUS_CLASSES.normal;
            statusText = CONFIG.STATUS_TEXTS.bloodPressure.normal;
        }

        element.className = `vital-status ${statusClass}`;
        element.textContent = statusText;
    }

    initCharts() {
        if (!dashboardData?.trends || dashboardData.trends.timestamps.length === 0) {
            return;
        }

        // Heart Rate Chart
        if (dashboardData.trends.heartRates) {
            this.createChart('heartRateChart', {
                label: 'Heart Rate (bpm)',
                data: dashboardData.trends.heartRates,
                borderColor: '#e63946'
            });
        }

        // Blood Pressure Chart
        if (dashboardData.trends.systolicBP && dashboardData.trends.diastolicBP) {
            this.createBloodPressureChart();
        }

        // Oxygen Chart
        if (dashboardData.trends.oxygenLevels) {
            this.createChart('oxygenChart', {
                label: 'Oxygen Saturation (%)',
                data: dashboardData.trends.oxygenLevels,
                borderColor: '#2a9d8f'
            });
        }
    }

    createChart(canvasId, { label, data, borderColor }) {
        const ctx = document.getElementById(canvasId);
        if (!ctx) {
            console.error(`Canvas element ${canvasId} not found`);
            return;
        }
        
        try {
            new Chart(ctx, {
                type: 'line',
                data: {
                    labels: dashboardData.trends.timestamps.map(dateStr => 
                        new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })),
                    datasets: [{
                        label: label,
                        data: data,
                        borderColor: borderColor,
                        backgroundColor: this.hexToRgba(borderColor, 0.1),
                        tension: 0.4,
                        fill: true,
                        borderWidth: 2
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { position: 'top' }
                    },
                    scales: {
                        x: { grid: { display: false } },
                        y: { beginAtZero: false }
                    }
                }
            });
        } catch (error) {
            console.error(`Error creating chart ${canvasId}:`, error);
        }
    }

    createBloodPressureChart() {
        const ctx = document.getElementById('bloodPressureChart');
        if (!ctx) return;

        new Chart(ctx, {
            type: 'line',
            data: {
                labels: dashboardData.trends.timestamps.map(dateStr => 
                    new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })),
                datasets: [
                    {
                        label: 'Systolic (mmHg)',
                        data: dashboardData.trends.systolicBP,
                        borderColor: '#1d3557',
                        backgroundColor: this.hexToRgba('#1d3557', 0.1)
                    },
                    {
                        label: 'Diastolic (mmHg)',
                        data: dashboardData.trends.diastolicBP,
                        borderColor: '#457b9d',
                        backgroundColor: this.hexToRgba('#457b9d', 0.1)
                    }
                ]
            },
            options: this.getChartOptions()
        });
    }

    getChartOptions() {
        return {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'top' },
                tooltip: { mode: 'index', intersect: false }
            },
            scales: {
                x: { grid: { display: false } },
                y: { beginAtZero: false }
            }
        };
    }

    hexToRgba(hex, alpha) {
        const r = parseInt(hex.slice(1, 3), 16);
        const g = parseInt(hex.slice(3, 5), 16);
        const b = parseInt(hex.slice(5, 7), 16);
        return `rgba(${r}, ${g}, ${b}, ${alpha})`;
    }

   showAlert(message, type = 'success') {
    const toastContainer = document.getElementById('toastContainer');
    if (!toastContainer) {
        console.error('Toast container not found');
        return;
    }
    
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`; // This will apply the correct color class
    
    // Map type to icon
    const icons = {
        success: 'fa-check-circle',
        error: 'fa-exclamation-circle',
        warning: 'fa-exclamation-triangle',
        info: 'fa-info-circle'
    };
    
    toast.innerHTML = `
        <i class="fas ${icons[type] || 'fa-info-circle'} toast-icon"></i>
        <div class="toast-message">${message}</div>
        <button class="toast-close">&times;</button>
    `;
    
    toastContainer.appendChild(toast);
    
    // Force reflow to enable CSS transition
    void toast.offsetWidth;
    
    // Add show class
    toast.classList.add('show');
    
    // Auto-remove after 5 seconds
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    }, 5000);
    
    // Manual close
    toast.querySelector('.toast-close').addEventListener('click', () => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 300);
    });
}
}

// Initialize dashboard instance globally
window.dashboard = new Dashboard();

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    setTimeout(() => window.dashboard.initialize(), 100);
});

// Emergency Alert Functionality
$(document).ready(function() {
    // Show modal when emergency button is clicked
    $('#emergencyBtn').click(function() {
        $('#emergencyModal').fadeIn();
    });

    // Close modal when X or Cancel is clicked
    $('#closeEmergencyModal, #cancelEmergencyAlert').click(function() {
        $('#emergencyModal').fadeOut();
        $('#emergencyAlertForm')[0].reset();
    });

    // Handle form submission
    $('#emergencyAlertForm').submit(function(e) {
        e.preventDefault();
        
        const doctorId = $('#doctorId').val();
        const category = $('#alertCategory').val();
        const message = $('#alertMessage').val();
        const patientId = $('#patientId').val();
        const csrfToken = $("meta[name='_csrf']").attr("content");
        const csrfHeader = $("meta[name='_csrf_header']").attr("content");

        if (!doctorId || !category || !message) {
            window.dashboard.showAlert('Please fill in all fields', 'error');
            return;
        }

        const $form = $(this);
        const $submitBtn = $form.find('button[type="submit"]');
        const originalText = $submitBtn.html();
        
        // Set loading state
        $submitBtn.prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Sending...');

        $.ajax({
            url: '/patient/dashboard/alert/emergency',
            method: 'POST',
            contentType: 'application/json',
            headers: {
                [csrfHeader]: csrfToken
            },
            data: JSON.stringify({
                patientId: patientId,
                doctorId: doctorId,
                category: category,
                message: message
            }),
            timeout: 30000, // 30 second timeout
            success: function(response) {
                $('#emergencyModal').fadeOut('fast', function() {
                    $form[0].reset();
                    window.dashboard.showAlert(
                        'Emergency alert sent successfully! Your doctor has been notified.', 
                        'success'
                    );
                });
            },
            error: function(xhr, status, error) {
                let errorMsg = 'Failed to send emergency alert. ';
                
                if (status === "timeout") {
                    errorMsg = 'Request timed out. Please try again.';
                } else if (xhr.status === 0) {
                    errorMsg += 'Network error - please check your connection.';
                } else if (xhr.status === 403) {
                    errorMsg += 'Session expired or unauthorized. Please refresh the page.';
                } else if (xhr.responseJSON && xhr.responseJSON.error) {
                    errorMsg += xhr.responseJSON.error;
                } else {
                    errorMsg += 'Server error occurred.';
                }
                
                window.dashboard.showAlert(errorMsg, 'error');
            },
            complete: function() {
                $submitBtn.prop('disabled', false).html(originalText);
            }
        });
    });
});