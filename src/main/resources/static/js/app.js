/**
 * Main Application Logic
 */
document.addEventListener('DOMContentLoaded', () => {
    initApp();
});

async function initApp() {
    console.log('App Initialized');
    
    // Set a mock user if not exists
    if (!localStorage.getItem('mockUserId')) {
        localStorage.setItem('mockUserId', 'mock_student_123');
    }

    // Attach Event Listeners
    document.getElementById('exploreBtn').addEventListener('click', loadVendors);

    // Initial load
    await loadVendors();
}

async function loadVendors() {
    const vendorList = document.getElementById('vendorList');
    vendorList.innerHTML = '<div style="grid-column: 1/-1; text-align: center;"><p class="text-secondary">Loading culinary experiences...</p></div>';

    try {
        const data = await window.api.getVendors();
        const vendors = data.vendors || [];
        
        if (vendors.length === 0) {
            vendorList.innerHTML = '<div style="grid-column: 1/-1; text-align: center;"><p class="text-muted">No vendors available at the moment.</p></div>';
            return;
        }

        vendorList.innerHTML = vendors.map(vendor => `
            <div class="glass-panel vendor-card" onclick="viewVendor('${vendor.id}')">
                <div class="vendor-img-placeholder">
                    <ion-icon name="fast-food-outline" style="font-size: 3rem;"></ion-icon>
                </div>
                <div class="vendor-info">
                    <h3>${escapeHtml(vendor.name)}</h3>
                    <p>${escapeHtml(vendor.description || 'A delicious campus stall')}</p>
                    <div style="margin-top: 1rem; display: flex; justify-content: space-between; align-items: center;">
                        <span class="text-secondary" style="font-size: 0.85rem;">
                            <ion-icon name="location-outline"></ion-icon> ${escapeHtml(vendor.collegeLocation || 'Main Campus')}
                        </span>
                        <span style="color: var(--warning); display: flex; align-items: center; gap: 0.25rem;">
                            <ion-icon name="star"></ion-icon> ${vendor.rating || 'New'}
                        </span>
                    </div>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error(error);
        vendorList.innerHTML = '<div style="grid-column: 1/-1; text-align: center;"><p class="text-error">Failed to load vendors. Is the Spring Boot backend running?</p></div>';
    }
}

function viewVendor(id) {
    window.location.href = `vendor.html?id=${id}`;
}

// Utility to prevent XSS
function escapeHtml(unsafe) {
    if (!unsafe) return '';
    return unsafe
         .toString()
         .replace(/&/g, "&amp;")
         .replace(/</g, "&lt;")
         .replace(/>/g, "&gt;")
         .replace(/"/g, "&quot;")
         .replace(/'/g, "&#039;");
}
