document.addEventListener('DOMContentLoaded', () => {
    initVendorPage();
});

async function initVendorPage() {
    const urlParams = new URLSearchParams(window.location.search);
    const vendorId = urlParams.get('id');

    if (!vendorId) {
        document.getElementById('appRoot').innerHTML = '<h2 class="text-error text-center">Vendor ID missing</h2><a href="index.html" class="btn btn-glass" style="margin:2rem auto; display:block; width:max-content;">Go Back</a>';
        return;
    }

    try {
        const vendor = await window.api.getVendorById(vendorId);
        
        document.getElementById('vendorName').innerText = vendor.name;
        document.getElementById('vendorDesc').innerText = vendor.description || 'Delicious campus food';
        document.getElementById('vendorLocation').innerHTML = `<ion-icon name="location-outline"></ion-icon> ${escapeHtml(vendor.collegeLocation)}`;
        document.getElementById('vendorRating').innerHTML = `<ion-icon name="star"></ion-icon> ${vendor.rating || 'New'}`;

        const menuList = document.getElementById('menuList');
        if (!vendor.menuItems || vendor.menuItems.length === 0) {
            menuList.innerHTML = '<p class="text-muted">No items available.</p>';
            return;
        }

        menuList.innerHTML = vendor.menuItems.map(item => `
            <div class="glass-panel menu-card">
                <div class="menu-img">
                    <ion-icon name="fast-food-outline" style="font-size: 3rem; color: rgba(255,255,255,0.2); width:100%; height:100%; display:flex; align-items:center; justify-content:center;"></ion-icon>
                </div>
                <h3 class="menu-title">${escapeHtml(item.name)}</h3>
                <p class="menu-desc">${escapeHtml(item.description)}</p>
                <div class="menu-footer">
                    <span class="menu-price">₹${(item.price / 100).toFixed(2)}</span>
                    <button class="btn btn-primary" style="padding: 0.5rem 1rem; font-size: 0.85rem;" onclick="addToCart('${item.id}')">
                        <ion-icon name="add-outline"></ion-icon> Add
                    </button>
                </div>
            </div>
        `).join('');

    } catch (err) {
        console.error(err);
        document.getElementById('appRoot').innerHTML = '<h2 class="text-error text-center">Failed to load vendor</h2>';
    }
}

function escapeHtml(unsafe) {
    if (!unsafe) return '';
    return unsafe.toString().replace(/[&<"'>]/g, function (m) {
        return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[m];
    });
}
