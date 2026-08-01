/**
 * Vendor Dashboard Logic
 */
const API_BASE_URL = '/api';

// For this frontend demo, we will pretend the current user is a vendor who owns a specific stall.
// In reality, this vendor ID would come from their authenticated session.
// We will use one of the vendor IDs from your database if available. Let's assume 'vendor_test_123'.
const VENDOR_ID = localStorage.getItem('mockVendorId') || 'vendor_test_123';

const elements = {
    tbody: document.getElementById('vendorMenuBody'),
    addBtn: document.getElementById('addBtn'),
    modal: document.getElementById('itemModal'),
    closeModalBtn: document.getElementById('closeModalBtn'),
    itemForm: document.getElementById('itemForm'),
    modalTitle: document.getElementById('modalTitle'),
    
    // Form fields
    itemId: document.getElementById('itemId'),
    itemName: document.getElementById('itemName'),
    itemDesc: document.getElementById('itemDesc'),
    itemPrice: document.getElementById('itemPrice'),
    itemCategory: document.getElementById('itemCategory'),
    itemAvailable: document.getElementById('itemAvailable')
};

let stompClient = null;
let currentVendorId = null;

document.addEventListener('DOMContentLoaded', async () => {
    // Authenticate and get vendor ID
    try {
        const user = await window.api.getMe();
        if (user.role !== 'vendor') {
            alert('Access Denied. You must be an approved vendor to view this page.');
            window.location.replace('index.html');
            return;
        }
        currentVendorId = user.id;
        initDashboard();
        connectWebSocket();
    } catch (e) {
        window.location.replace('index.html');
    }
});

function connectWebSocket() {
    const wsStatus = document.getElementById('wsStatus');
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null; // Disable spammy logs
    
    stompClient.connect({}, function (frame) {
        wsStatus.innerText = '(Connected Live)';
        wsStatus.style.color = 'var(--success)';
        
        stompClient.subscribe('/topic/orders/' + currentVendorId, function (message) {
            const orderData = JSON.parse(message.body);
            displayNewOrder(orderData);
        });
    }, function(error) {
        wsStatus.innerText = '(Disconnected - Retrying...)';
        wsStatus.style.color = 'var(--error)';
        setTimeout(connectWebSocket, 5000);
    });
}

function displayNewOrder(order) {
    const container = document.getElementById('liveOrdersContainer');
    
    // Remove the "No active orders" message if it exists
    if (container.querySelector('p.text-secondary')) {
        container.innerHTML = '';
    }
    
    const card = document.createElement('div');
    card.className = 'glass-panel';
    card.style.padding = '1rem';
    card.style.borderLeft = '4px solid var(--warning)';
    card.style.animation = 'pulse 2s infinite';
    
    setTimeout(() => { card.style.animation = 'none'; }, 10000);
    
    card.innerHTML = `
        <h3 style="margin-bottom: 0.5rem;">New Order #${order.id.substring(0,6)}</h3>
        <p class="text-secondary" style="font-size: 0.9rem; margin-bottom: 0.5rem;">Status: <strong style="color:var(--warning)">${order.status}</strong></p>
        <p style="font-weight: 600; margin-bottom: 1rem;">Total: ₹${(order.totalPricePaise / 100).toFixed(2)}</p>
        <button class="btn btn-primary" style="width:100%; padding: 0.5rem;" onclick="alert('Order Accepted!')">Accept Order</button>
    `;
    
    container.prepend(card);
}

function initDashboard() {
    loadMenuItems();

    elements.addBtn.addEventListener('click', () => openModal());
    elements.closeModalBtn.addEventListener('click', closeModal);
    
    elements.itemForm.addEventListener('submit', handleFormSubmit);
}

async function loadMenuItems() {
    try {
        const headers = await window.api.getAuthHeaders();
        const res = await fetch(`${API_BASE_URL}/vendors/menu`, {
            headers
        });
        
        if (!res.ok) throw new Error('Failed to fetch menu items');
        const items = await res.json();
        
        if (items.length === 0) {
            elements.tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: var(--text-muted);">No items found. Click "Add New Item" to start.</td></tr>';
            return;
        }

        elements.tbody.innerHTML = items.map(item => `
            <tr>
                <td><strong>${escapeHtml(item.name)}</strong></td>
                <td><span style="background: rgba(255,255,255,0.1); padding: 0.25rem 0.5rem; border-radius: 4px; font-size: 0.8rem;">${escapeHtml(item.category)}</span></td>
                <td style="color: var(--accent-primary); font-weight: 600;">₹${(item.pricePaise / 100).toFixed(2)}</td>
                <td>
                    <span style="color: ${item.isAvailable ? 'var(--success)' : 'var(--error)'}">
                        ${item.isAvailable ? 'Yes' : 'No'}
                    </span>
                </td>
                <td>
                    <button class="btn btn-glass" style="padding: 0.4rem; margin-right: 0.5rem;" onclick='editItem(${JSON.stringify(item).replace(/'/g, "&apos;")})'>
                        <ion-icon name="create-outline"></ion-icon>
                    </button>
                    <button class="btn btn-glass" style="padding: 0.4rem; color: var(--error);" onclick="deleteItem('${item.id}')">
                        <ion-icon name="trash-outline"></ion-icon>
                    </button>
                </td>
            </tr>
        `).join('');

    } catch (err) {
        console.error(err);
        elements.tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: var(--error);">Error loading menu items. Is the backend running?</td></tr>';
    }
}

function openModal(item = null) {
    elements.modal.classList.add('active');
    
    if (item) {
        elements.modalTitle.innerText = 'Edit Menu Item';
        elements.itemId.value = item.id;
        elements.itemName.value = item.name;
        elements.itemDesc.value = item.description;
        elements.itemPrice.value = item.pricePaise;
        elements.itemCategory.value = item.category;
        elements.itemAvailable.checked = item.isAvailable;
    } else {
        elements.modalTitle.innerText = 'Add Menu Item';
        elements.itemForm.reset();
        elements.itemId.value = '';
        elements.itemAvailable.checked = true;
    }
}

function closeModal() {
    elements.modal.classList.remove('active');
}

async function handleFormSubmit(e) {
    e.preventDefault();
    
    const id = elements.itemId.value;
    const isEdit = !!id;
    
    const payload = {
        name: elements.itemName.value,
        description: elements.itemDesc.value,
        pricePaise: parseInt(elements.itemPrice.value, 10),
        category: elements.itemCategory.value,
        isAvailable: elements.itemAvailable.checked
    };

    try {
        const url = isEdit ? `${API_BASE_URL}/vendors/menu/${id}` : `${API_BASE_URL}/vendors/menu`;
        const method = isEdit ? 'PUT' : 'POST';
        
        const headers = await window.api.getAuthHeaders();
        const res = await fetch(url, {
            method,
            headers,
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const data = await res.json();
            throw new Error(data.detail || 'Failed to save item');
        }

        closeModal();
        loadMenuItems();
        
    } catch (err) {
        console.error(err);
        alert('Error: ' + err.message);
    }
}

window.editItem = function(item) {
    openModal(item);
};

window.deleteItem = async function(id) {
    if (!confirm('Are you sure you want to delete this menu item?')) return;
    
    try {
        const headers = await window.api.getAuthHeaders();
        const res = await fetch(`${API_BASE_URL}/vendors/menu/${id}`, {
            method: 'DELETE',
            headers
        });
        
        if (!res.ok) throw new Error('Failed to delete item');
        loadMenuItems();
    } catch (err) {
        console.error(err);
        alert('Error deleting item');
    }
};

function escapeHtml(unsafe) {
    if (!unsafe) return '';
    return unsafe.toString().replace(/[&<"'>]/g, function (m) {
        return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[m];
    });
}
