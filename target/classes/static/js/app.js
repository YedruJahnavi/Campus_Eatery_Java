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

    // Modal listeners
    const modal = document.getElementById('vendorRequestModal');
    const overlay = document.getElementById('vendorModalOverlay');
    const closeBtn = document.getElementById('closeVendorModalBtn');
    const form = document.getElementById('vendorApplicationForm');

    const closeModal = () => {
        if (modal) modal.style.display = 'none';
        if (overlay) overlay.style.display = 'none';
    };

    if (closeBtn) closeBtn.addEventListener('click', closeModal);
    if (overlay) overlay.addEventListener('click', closeModal);

    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const stallName = document.getElementById('reqStallName').value.trim();
            const fssaiLicense = document.getElementById('reqFssaiLicense').value.trim();
            const collegeLocation = document.getElementById('reqCollegeLocation').value.trim();
            const whatTheySell = document.getElementById('reqWhatTheySell').value.trim();
            const mobileNumber = document.getElementById('reqMobileNumber').value.trim();
            const errDiv = document.getElementById('vendorModalError');

            if (errDiv) errDiv.style.display = 'none';

            if (!/^\d{14}$/.test(fssaiLicense)) {
                if (errDiv) {
                    errDiv.innerText = "FSSAI License Code must be exactly 14 digits.";
                    errDiv.style.display = 'block';
                }
                return;
            }

            try {
                await window.api.applyForVendor({
                    stallName,
                    fssaiLicense,
                    collegeLocation,
                    whatTheySell,
                    mobileNumber
                });
                closeModal();
                alert("Vendor application submitted successfully! Your request is now pending admin approval.");
                window.location.reload();
            } catch (e) {
                if (errDiv) {
                    errDiv.innerText = e.message || "Failed to submit vendor application.";
                    errDiv.style.display = 'block';
                }
            }
        });
    }

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
                vendorBtn.addEventListener('click', openVendorModal);
            }
        }
    } catch (e) {
        console.warn('Could not fetch user profile (might not be logged in yet).');
        const vendorBtn = document.getElementById('dynamicVendorBtn');
        if(vendorBtn) {
            vendorBtn.style.display = 'inline-block';
            vendorBtn.addEventListener('click', openVendorModal);
        }
    }

    // Initial load
    await loadVendors();
}

function openVendorModal() {
    const modal = document.getElementById('vendorRequestModal');
    const overlay = document.getElementById('vendorModalOverlay');
    if (modal) modal.style.display = 'block';
    if (overlay) overlay.style.display = 'block';
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
        <div class="vendor-card" onclick="viewVendor('${vendor.id}')">
            <div class="vendor-img-placeholder">
                <ion-icon name="restaurant"></ion-icon>
            </div>
            <div class="vendor-info">
                <h3>${escapeHtml(vendor.name)}</h3>
                <p class="vendor-desc">${escapeHtml(vendor.description || 'Delicious campus food')}</p>
                <div class="vendor-meta">
                    <span class="loc">
                        <ion-icon name="location-outline"></ion-icon> ${escapeHtml(vendor.collegeLocation || 'Main Campus')}
                    </span>
                    <span class="rating">
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
