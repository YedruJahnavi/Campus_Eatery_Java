/**
 * Main Application Logic
 */
const DEFAULT_DEMO_VENDORS = [
    {
        id: 'stall_1',
        name: 'The Spicy Byte Cafe',
        description: 'Authentic Indian street food, thalis, and hot beverages.',
        collegeLocation: 'South Block Canteen',
        rating: 4.8,
        prepTimeMinutes: 15
    },
    {
        id: 'stall_2',
        name: 'Campus Bowl & Roll',
        description: 'Kathi rolls, shawarmas, and refreshing rice bowls.',
        collegeLocation: 'Student Activity Center (SAC)',
        rating: 4.6,
        prepTimeMinutes: 10
    },
    {
        id: 'stall_3',
        name: 'Grill & Chill Diner',
        description: 'Handcrafted burgers, peri peri fries, and thick milkshakes.',
        collegeLocation: 'Library Square',
        rating: 4.9,
        prepTimeMinutes: 12
    },
    {
        id: 'stall_4',
        name: 'Night Owl Canteen',
        description: 'Midnight Maggi, cheese toast, egg bhurji, and hot chai.',
        collegeLocation: 'Hostel Block 4',
        rating: 4.7,
        prepTimeMinutes: 8
    }
];

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
    const exploreBtn = document.getElementById('exploreBtn');
    if (exploreBtn) exploreBtn.addEventListener('click', loadVendors);
    
    // Check User Identity
    try {
        const user = await window.api.getMe();
        
        // Vendor Redirection Logic
        if (user.role === 'vendor' && user.approvalStatus === 'approved') {
            window.location.replace('dashboard.html');
            return;
        }

        // Show banner and hide button if pending
        const vendorBtn = document.getElementById('dynamicVendorBtn');
        const banner = document.getElementById('vendorStatusBanner');
        
        if (user.approvalStatus === 'pending_approval') {
            if(banner) banner.style.display = 'block';
            if(vendorBtn) vendorBtn.style.display = 'none';
        } else {
            // They are a normal customer, allow them to apply
            if(vendorBtn) {
                vendorBtn.style.display = 'inline-block';
                vendorBtn.addEventListener('click', applyToBeVendor);
            }
        }
    } catch (e) {
        console.warn('Could not fetch user profile (might not be logged in yet).');
    }

    // Initial load
    await loadVendors();
}

async function applyToBeVendor() {
    const stallName = prompt("What is the name of your stall?");
    if (!stallName) return;
    const whatTheySell = prompt("What do you sell?");
    
    try {
        await window.api.applyForVendor({
            stallName,
            whatTheySell,
            mobileNumber: "1234567890",
            collegeLocation: "Main Campus"
        });
        alert("Application submitted! We will review it shortly.");
        window.location.reload();
    } catch (e) {
        alert("Failed to submit application.");
    }
}

async function loadVendors() {
    const vendorList = document.getElementById('vendorList');
    if (!vendorList) return;

    let vendors = [];

    try {
        const data = await window.api.getVendors();
        vendors = data.vendors || [];
    } catch (error) {
        console.warn('Backend fetch failed, rendering demo vendors fallback.', error);
    }

    // Fallback to demo vendors if DB is empty or backend is offline
    if (vendors.length === 0) {
        vendors = DEFAULT_DEMO_VENDORS;
    }

    renderVendors(vendors);
}

function renderVendors(vendors) {
    const vendorList = document.getElementById('vendorList');
    if (!vendorList) return;

    vendorList.innerHTML = vendors.map(vendor => `
        <div class="glass-panel vendor-card" onclick="viewVendor('${vendor.id}')" style="cursor: pointer; transition: transform 0.2s, box-shadow 0.2s;">
            <div class="vendor-img-placeholder" style="background: linear-gradient(135deg, rgba(99,102,241,0.2), rgba(168,85,247,0.1)); border-radius: 12px; height: 140px; display: flex; align-items: center; justify-content: center;">
                <ion-icon name="restaurant" style="font-size: 3.5rem; color: var(--accent-primary, #818cf8);"></ion-icon>
            </div>
            <div class="vendor-info" style="padding: 1rem 0.5rem 0.5rem 0.5rem;">
                <h3 style="margin-bottom: 0.4rem; font-size: 1.25rem;">${escapeHtml(vendor.name)}</h3>
                <p style="color: var(--text-secondary, #94a3b8); font-size: 0.9rem; line-height: 1.4; margin-bottom: 1rem;">${escapeHtml(vendor.description || 'Delicious campus food')}</p>
                <div style="display: flex; justify-content: space-between; align-items: center; font-size: 0.85rem;">
                    <span style="color: var(--text-secondary, #94a3b8); display: flex; align-items: center; gap: 0.3rem;">
                        <ion-icon name="location-outline"></ion-icon> ${escapeHtml(vendor.collegeLocation || 'Main Campus')}
                    </span>
                    <span style="color: #fbbf24; font-weight: 600; display: flex; align-items: center; gap: 0.25rem; background: rgba(251,191,36,0.1); padding: 0.2rem 0.5rem; border-radius: 6px;">
                        <ion-icon name="star"></ion-icon> ${vendor.rating || '4.8'}
                    </span>
                </div>
            </div>
        </div>
    `).join('');
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
