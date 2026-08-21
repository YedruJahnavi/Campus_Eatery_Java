const DEMO_VENDOR_DETAILS = {
    'stall_1': {
        id: 'stall_1',
        name: 'The Spicy Byte Cafe',
        description: 'Authentic Indian street food, thalis, and hot beverages.',
        collegeLocation: 'South Block Canteen',
        rating: 4.8,
        menuItems: [
            { id: 'm1', name: 'Paneer Butter Masala Combo', description: 'Rich tomato gravy paneer served with 2 Butter Naans.', pricePaise: 18000 },
            { id: 'm2', name: 'Cheese Grilled Sandwich', description: 'Triple-layered loaded cheese sandwich toasted to perfection.', pricePaise: 9000 },
            { id: 'm3', name: 'Cold Coffee with Ice Cream', description: 'Thick blended cold coffee topped with vanilla scoop.', pricePaise: 8000 }
        ]
    },
    'stall_2': {
        id: 'stall_2',
        name: 'Campus Bowl & Roll',
        description: 'Kathi rolls, shawarmas, and refreshing rice bowls.',
        collegeLocation: 'Student Activity Center (SAC)',
        rating: 4.6,
        menuItems: [
            { id: 'm4', name: 'Chicken Kathi Roll', description: 'Juicy spicy chicken wrapped in flaky paratha.', pricePaise: 12000 },
            { id: 'm5', name: 'Paneer Tikka Bowl', description: 'Basmati rice topped with grilled paneer tikka & mint chutney.', pricePaise: 15000 },
            { id: 'm6', name: 'Mango Lassi', description: 'Chilled sweet yogurt smoothie with fresh mango pulp.', pricePaise: 6000 }
        ]
    },
    'stall_3': {
        id: 'stall_3',
        name: 'Grill & Chill Diner',
        description: 'Handcrafted burgers, peri peri fries, and thick milkshakes.',
        collegeLocation: 'Library Square',
        rating: 4.9,
        menuItems: [
            { id: 'm7', name: 'Smoky BBQ Veggie Burger', description: 'Crispy vegetable patty with melted cheese & BBQ sauce.', pricePaise: 13000 },
            { id: 'm8', name: 'Peri Peri Loaded Fries', description: 'Golden french fries dusted with spicy peri peri seasoning & cheese dip.', pricePaise: 10000 }
        ]
    },
    'stall_4': {
        id: 'stall_4',
        name: 'Night Owl Canteen',
        description: 'Midnight Maggi, cheese toast, egg bhurji, and hot chai.',
        collegeLocation: 'Hostel Block 4',
        rating: 4.7,
        menuItems: [
            { id: 'm9', name: 'Special Masala Maggi', description: 'Double maggi cooked with fresh veggies and extra cheese.', pricePaise: 6000 },
            { id: 'm10', name: 'Kulhad Ginger Chai', description: 'Aromatic cardamom & ginger tea served in traditional clay pot.', pricePaise: 2500 }
        ]
    }
};

document.addEventListener('DOMContentLoaded', () => {
    initVendorPage();
});

async function initVendorPage() {
    const urlParams = new URLSearchParams(window.location.search);
    const vendorId = urlParams.get('id');

    if (!vendorId) {
        document.getElementById('appRoot').innerHTML = '<h2 class="text-error text-center">Vendor ID missing</h2><a href="index.html" class="btn btn-outline" style="margin:2rem auto; display:block; width:max-content;">Go Back</a>';
        return;
    }

    let vendor = null;

    try {
        vendor = await window.api.getVendorById(vendorId);
    } catch (err) {
        console.warn('Could not fetch vendor details from API, checking demo data.', err);
        vendor = DEMO_VENDOR_DETAILS[vendorId];
    }

    if (!vendor) {
        // Fallback default if not found
        vendor = DEMO_VENDOR_DETAILS['stall_1'];
    }

    const nameEl = document.getElementById('vendorName');
    if (nameEl) nameEl.innerText = vendor.name;
    
    const descEl = document.getElementById('vendorDesc');
    if (descEl) descEl.innerText = vendor.description || 'Delicious campus food';
    
    const locEl = document.getElementById('vendorLocation');
    if (locEl) locEl.innerHTML = `<ion-icon name="location-outline"></ion-icon> ${escapeHtml(vendor.collegeLocation || 'Main Campus')}`;
    
    const ratingEl = document.getElementById('vendorRating');
    if (ratingEl) ratingEl.innerHTML = `<ion-icon name="star"></ion-icon> ${vendor.rating || '4.8'}`;

    const menuList = document.getElementById('menuList');
    if (!menuList) return;

    if (!vendor.menuItems || vendor.menuItems.length === 0) {
        menuList.innerHTML = '<p class="text-muted">No items available.</p>';
        return;
    }

    menuList.innerHTML = vendor.menuItems.map(item => {
        const rawPrice = item.pricePaise !== undefined ? item.pricePaise : (item.price !== undefined ? item.price : 10000);
        const priceFormatted = (rawPrice / 100).toFixed(2);

        return `
            <div class="glass-panel menu-card">
                <div class="menu-img">
                    <ion-icon name="fast-food-outline" style="font-size: 3rem; color: var(--text-3); width:100%; height:100%; display:flex; align-items:center; justify-content:center;"></ion-icon>
                </div>
                <h3 class="menu-title">${escapeHtml(item.name)}</h3>
                <p class="menu-desc">${escapeHtml(item.description || 'Freshly prepared item')}</p>
                <div class="menu-footer">
                    <span class="menu-price">₹${priceFormatted}</span>
                    <button class="btn btn-dark btn-sm" onclick="addToCart('${item.id}')">
                        <ion-icon name="add-outline"></ion-icon> Add
                    </button>
                </div>
            </div>
        `;
    }).join('');
}

function escapeHtml(unsafe) {
    if (!unsafe) return '';
    return unsafe.toString().replace(/[&<"'>]/g, function (m) {
        return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[m];
    });
}
