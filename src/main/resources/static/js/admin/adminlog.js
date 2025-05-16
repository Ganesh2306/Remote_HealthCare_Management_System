document.addEventListener('DOMContentLoaded', function() {
    initSystemLogsPage();
    
    // Close modal when clicking outside
    window.addEventListener('click', function(event) {
        const modal = document.getElementById('logDetailsModal');
        if (event.target === modal) {
            closeModal();
        }
    });
});

function initSystemLogsPage() {
    // Initialize table row click handlers
    document.querySelectorAll('.logs-table tbody tr').forEach(row => {
        row.addEventListener('click', function() {
            const logId = this.getAttribute('data-id');
            showLogDetails(logId, this);
        });
    });

    // Initialize filter controls
    const dateFrom = document.getElementById('dateFrom');
    const dateTo = document.getElementById('dateTo');
    const severityFilter = document.getElementById('severityFilter');
    const clearFiltersBtn = document.getElementById('clearFilters');
    const exportLogsBtn = document.getElementById('exportLogs');
    
    // Set default date range (last 7 days)
    const today = new Date();
    const sevenDaysAgo = new Date();
    sevenDaysAgo.setDate(today.getDate() - 7);
    
    // Format dates for input fields (YYYY-MM-DD)
    dateFrom.value = formatDateForInput(sevenDaysAgo);
    dateTo.value = formatDateForInput(today);
    
    // Add event listeners
    dateFrom.addEventListener('change', applyFilters);
    dateTo.addEventListener('change', applyFilters);
    severityFilter.addEventListener('change', applyFilters);
    clearFiltersBtn.addEventListener('click', clearFilters);
    if (exportLogsBtn) {
        exportLogsBtn.addEventListener('click', exportLogs);
    }

    // Apply filters initially
    applyFilters();
}

function formatDateForInput(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

function showLogDetails(logId, row) {
    const modal = document.getElementById('logDetailsModal');
    const cells = row.querySelectorAll('td');
    
    document.getElementById('detail-id').textContent = logId;
    document.getElementById('detail-timestamp').textContent = cells[0].textContent;
    
    const severityBadge = cells[1].querySelector('.severity-badge').cloneNode(true);
    document.getElementById('detail-severity').innerHTML = '';
    document.getElementById('detail-severity').appendChild(severityBadge);
    
    document.getElementById('detail-action').textContent = cells[2].textContent;
    
    modal.style.display = 'block';
}

function closeModal() {
    document.getElementById('logDetailsModal').style.display = 'none';
}

function applyFilters() {
    const dateFrom = document.getElementById('dateFrom').value;
    const dateTo = document.getElementById('dateTo').value;
    const severity = document.getElementById('severityFilter').value;
    
    const rows = document.querySelectorAll('.logs-table tbody tr');
    
    // Convert filter dates to Date objects at start/end of day
    const fromDate = dateFrom ? new Date(dateFrom + 'T00:00:00') : null;
    const toDate = dateTo ? new Date(dateTo + 'T23:59:59') : null;
    
    rows.forEach(row => {
        const rowDateText = row.cells[0].textContent;
        const rowDate = parseDateTime(rowDateText);
        const rowSeverity = row.cells[1].textContent.trim();
        
        // Check date filter
        let dateMatch = true;
        if (fromDate && rowDate < fromDate) dateMatch = false;
        if (toDate && rowDate > toDate) dateMatch = false;
        
        // Check severity filter
        let severityMatch = true;
        if (severity && severity !== '' && rowSeverity !== severity) {
            severityMatch = false;
        }
        
        // Show/hide row based on filters
        row.style.display = (dateMatch && severityMatch) ? '' : 'none';
    });
}

function parseDateTime(dateTimeString) {
    // Parse the format "yyyy-MM-dd HH:mm:ss"
    const [datePart, timePart] = dateTimeString.split(' ');
    const [year, month, day] = datePart.split('-').map(Number);
    const [hours, minutes, seconds] = timePart.split(':').map(Number);
    
    return new Date(year, month - 1, day, hours, minutes, seconds);
}

function clearFilters() {
    // Reset date filters to default (last 7 days)
    const today = new Date();
    const sevenDaysAgo = new Date();
    sevenDaysAgo.setDate(today.getDate() - 7);
    
    document.getElementById('dateFrom').value = formatDateForInput(sevenDaysAgo);
    document.getElementById('dateTo').value = formatDateForInput(today);
    
    // Reset severity filter
    document.getElementById('severityFilter').value = '';
    
    // Reapply filters
    applyFilters();
}

function exportLogs() {
    const dateFrom = document.getElementById('dateFrom').value;
    const dateTo = document.getElementById('dateTo').value;
    const severity = document.getElementById('severityFilter').value;
    
    // Get the current URL and add filter parameters
    const currentUrl = window.location.href.split('?')[0];
    const exportUrl = `/admin/dashboard/log/filter/download?from=${encodeURIComponent(dateFrom)}&to=${encodeURIComponent(dateTo)}&severity=${encodeURIComponent(severity)}`;
    
    // Open in new tab to trigger download
    window.open(exportUrl, '_blank');
}