/**
 * Vendor Dashboard Logic
 */
const API_BASE_URL = '/api';
const VENDOR_ID = localStorage.getItem('mockVendorId') || 'vendor_test_123';

let currentVendorId = VENDOR_ID;
let stompClient = null;
let currentOrders = [];
let currentMenuItems = [];

document.addEventListener('DOMContentLoaded', async () => {
    try {
        const user = await window.api.getMe();
        if (user && user.id) {
            currentVendorId = user.id;
        }
    } catch (e) {
        console.warn('Using default vendor ID:', currentVendorId);
    }

    initDashboard();
    connectWebSocket();
});

function initDashboard() {
    const addBtn = document.getElementById('addBtn');
    const closeModalBtn = document.getElementById('closeModalBtn');
    const modalOverlay = document.getElementById('itemModalOverlay');
    const itemForm = document.getElementById('itemForm');
    const refreshOrdersBtn = document.getElementById('refreshOrdersBtn');

    if (addBtn) addBtn.addEventListener('click', () => openModal());
    if (closeModalBtn) closeModalBtn.addEventListener('click', closeModal);
    if (modalOverlay) modalOverlay.addEventListener('click', closeModal);
    if (itemForm) itemForm.addEventListener('submit', handleFormSubmit);
    if (refreshOrdersBtn) refreshOrdersBtn.addEventListener('click', loadVendorOrders);

    loadMenuItems();
    loadVendorOrders();
}

// -------------------------------------------------------------
// 1. 📊 Real-Time Key Metrics & ⚡ Live Order Processing Engine
// -------------------------------------------------------------

function connectWebSocket() {
    const wsStatus = document.getElementById('wsStatus');
    try {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.debug = null; // Disable debug logs

        stompClient.connect({}, function () {
            if (wsStatus) {
                wsStatus.innerText = '● Live WebSocket Connected';
                wsStatus.style.color = '#34d399';
            }

            stompClient.subscribe('/topic/orders/' + currentVendorId, function (message) {
                const newOrder = JSON.parse(message.body);
                handleIncomingLiveOrder(newOrder);
            });
        }, function () {
            if (wsStatus) {
                wsStatus.innerText = 'WebSocket Disconnected (Retrying...)';
                wsStatus.style.color = '#f87171';
            }
            setTimeout(connectWebSocket, 5000);
        });
    } catch (err) {
        console.warn('WebSocket connection error:', err);
    }
}

async function loadVendorOrders() {
    try {
        const headers = await window.api.getAuthHeaders();
        const res = await fetch(`${API_BASE_URL}/orders/vendor`, { headers });
        if (res.ok) {
            currentOrders = await res.json();
            renderOrders(currentOrders);
            updateMetrics();
        } else {
            renderDemoOrdersFallback();
        }
    } catch (e) {
        renderDemoOrdersFallback();
    }
}

function renderDemoOrdersFallback() {
    currentOrders = [
        {
            id: 'ord_101982',
            deliveryCode: '4821',
            status: 'PLACED',
            grandTotal: 34000,
            items: [
                { name: 'Paneer Butter Masala Combo', quantity: 1, pricePaise: 18000 },
                { name: 'Cold Coffee with Ice Cream', quantity: 2, pricePaise: 16000 }
            ],
            createdAt: new Date().toISOString()
        },
        {
            id: 'ord_101981',
            deliveryCode: '9124',
            status: 'PREPARING',
            grandTotal: 15000,
            items: [
                { name: 'Paneer Tikka Bowl', quantity: 1, pricePaise: 15000 }
            ],
            createdAt: new Date().toISOString()
        }
    ];
    renderOrders(currentOrders);
    updateMetrics();
}

function handleIncomingLiveOrder(newOrder) {
    const existingIndex = currentOrders.findIndex(o => o.id === newOrder.id);
    if (existingIndex >= 0) {
        currentOrders[existingIndex] = newOrder;
    } else {
        currentOrders.unshift(newOrder);
    }
    renderOrders(currentOrders);
    updateMetrics();
}

function renderOrders(orders) {
    const container = document.getElementById('liveOrdersContainer');
    if (!container) return;

    // Filter active orders (PLACED, PREPARING, READY_FOR_PICKUP)
    const activeOrders = orders.filter(o => o.status !== 'DELIVERED' && o.status !== 'CANCELLED' && o.status !== 'COMPLETED');

    if (activeOrders.length === 0) {
        container.innerHTML = `
            <div class="glass-panel text-center" style="padding: 2.5rem; grid-column: 1/-1; color: var(--text-secondary);">
                <ion-icon name="restaurant-outline" style="font-size: 3rem; margin-bottom: 0.5rem; color: rgba(255,255,255,0.2);"></ion-icon>
                <p>No active incoming orders at the moment.</p>
            </div>
        `;
        return;
    }

    container.innerHTML = activeOrders.map(order => {
        const shortId = order.id ? order.id.substring(0, 8) : 'ORD';
        const code = order.deliveryCode || order.pickupCode || '4291';
        const grandTotalRupees = (order.grandTotal ? order.grandTotal / 100 : (order.totalPricePaise ? order.totalPricePaise / 100 : 0)).toFixed(2);
        
        let statusBadgeColor = '#fbbf24';
        let actionBtnText = 'Accept & Start Prep';
        let nextStatus = 'PREPARING';

        if (order.status === 'PREPARING') {
            statusBadgeColor = '#60a5fa';
            actionBtnText = 'Mark Ready for Pickup';
            nextStatus = 'READY_FOR_PICKUP';
        } else if (order.status === 'READY_FOR_PICKUP') {
            statusBadgeColor = '#34d399';
            actionBtnText = 'Complete & Deliver Order';
            nextStatus = 'DELIVERED';
        }

        const itemsHtml = (order.items || []).map(i => `
            <div style="display: flex; justify-content: space-between; font-size: 0.9rem; margin-bottom: 0.3rem;">
                <span><strong>${i.quantity || 1}x</strong> ${escapeHtml(i.name || i.menuItemId)}</span>
                <span>₹${((i.pricePaise || 0) * (i.quantity || 1) / 100).toFixed(2)}</span>
            </div>
        `).join('');

        return `
            <div class="glass-panel" style="padding: 1.5rem; border-left: 4px solid ${statusBadgeColor}; display: flex; flex-direction: column; justify-space-between;">
                <div>
                    <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.8rem;">
                        <div>
                            <h3 style="font-size: 1.15rem; margin-bottom: 0.2rem;">Order #${escapeHtml(shortId)}</h3>
                            <span style="font-size: 0.78rem; color: #a1a1aa;">Code: <strong style="color: #60a5fa;">${escapeHtml(code)}</strong></span>
                        </div>
                        <span style="background: rgba(255,255,255,0.08); color: ${statusBadgeColor}; padding: 0.25rem 0.6rem; border-radius: 6px; font-size: 0.8rem; font-weight: 600;">
                            ${escapeHtml(order.status)}
                        </span>
                    </div>

                    <div style="background: rgba(0,0,0,0.25); padding: 0.8rem; border-radius: 8px; margin-bottom: 1rem;">
                        ${itemsHtml || '<p style="font-size: 0.85rem; color: #a1a1aa;">Standard Meal Combo</p>'}
                    </div>

                    <div style="display: flex; justify-content: space-between; align-items: center; font-weight: 600; margin-bottom: 1.2rem;">
                        <span style="color: var(--text-secondary); font-size: 0.9rem;">Grand Total</span>
                        <span style="font-size: 1.2rem; color: #34d399;">₹${grandTotalRupees}</span>
                    </div>
                </div>

                <button class="btn btn-primary" style="width: 100%; padding: 0.7rem; font-weight: 600; background: linear-gradient(135deg, ${statusBadgeColor}, #3b82f6); border: none; border-radius: 8px; cursor: pointer;" onclick="updateOrderStatus('${order.id}', '${nextStatus}')">
                    ${actionBtnText}
                </button>
            </div>
        `;
    }).join('');
}

async function updateOrderStatus(orderId, newStatus) {
    try {
        const headers = await window.api.getAuthHeaders();
        const res = await fetch(`${API_BASE_URL}/orders/${orderId}/status`, {
            method: 'PUT',
            headers,
            body: JSON.stringify({ status: newStatus })
        });

        if (res.ok) {
            const updated = await res.json();
            handleIncomingLiveOrder(updated);
        } else {
            // Local state update for smooth demo operation
            const idx = currentOrders.findIndex(o => o.id === orderId);
            if (idx >= 0) {
                currentOrders[idx].status = newStatus;
                renderOrders(currentOrders);
                updateMetrics();
            }
        }
    } catch (e) {
        const idx = currentOrders.findIndex(o => o.id === orderId);
        if (idx >= 0) {
            currentOrders[idx].status = newStatus;
            renderOrders(currentOrders);
            updateMetrics();
        }
    }
}

function updateMetrics() {
    const activeCount = currentOrders.filter(o => o.status !== 'DELIVERED' && o.status !== 'CANCELLED' && o.status !== 'COMPLETED').length;
    const todayRevenuePaise = currentOrders
        .filter(o => o.status === 'DELIVERED' || o.status === 'COMPLETED')
        .reduce((sum, o) => sum + (o.grandTotal || o.totalPricePaise || 0), 0);

    const activeEl = document.getElementById('metricActiveOrders');
    const revenueEl = document.getElementById('metricTodayRevenue');
    const totalMenuEl = document.getElementById('metricTotalMenuItems');

    if (activeEl) activeEl.innerText = activeCount;
    if (revenueEl) revenueEl.innerText = `₹${(todayRevenuePaise / 100).toFixed(2)}`;
    if (totalMenuEl) totalMenuEl.innerText = currentMenuItems.length;
}

// -------------------------------------------------------------
// 🍔 3. Digital Menu Management & Instant Availability Toggle
// -------------------------------------------------------------

async function loadMenuItems() {
    const tbody = document.getElementById('vendorMenuBody');
    if (!tbody) return;

    try {
        const headers = await window.api.getAuthHeaders();
        const res = await fetch(`${API_BASE_URL}/vendors/menu`, { headers });
        if (res.ok) {
            currentMenuItems = await res.json();
            renderMenuItems(currentMenuItems);
            updateMetrics();
            return;
        }
    } catch (e) {
        console.warn('Could not load menu items from server:', e);
    }

    // Fallback demo menu items
    currentMenuItems = [
        { id: 'm1', name: 'Paneer Butter Masala Combo', category: 'North Indian', pricePaise: 18000, description: 'Rich tomato gravy paneer served with 2 Butter Naans.', isAvailable: true },
        { id: 'm2', name: 'Cheese Grilled Sandwich', category: 'Snacks', pricePaise: 9000, description: 'Triple-layered loaded cheese sandwich toasted to perfection.', isAvailable: true },
        { id: 'm3', name: 'Cold Coffee with Ice Cream', category: 'Beverages', pricePaise: 8000, description: 'Thick blended cold coffee topped with vanilla scoop.', isAvailable: false }
    ];
    renderMenuItems(currentMenuItems);
    updateMetrics();
}

function renderMenuItems(items) {
    const tbody = document.getElementById('vendorMenuBody');
    if (!tbody) return;

    if (items.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-secondary">No menu items added yet. Click "Add New Item" to create one.</td></tr>';
        return;
    }

    tbody.innerHTML = items.map(item => `
        <tr>
            <td>
                <strong>${escapeHtml(item.name)}</strong>
                <p style="font-size: 0.8rem; color: #a1a1aa; margin-top: 0.2rem;">${escapeHtml(item.description)}</p>
            </td>
            <td>
                <span style="background: rgba(99,102,241,0.2); color: #818cf8; padding: 0.25rem 0.6rem; border-radius: 6px; font-size: 0.8rem; font-weight: 600;">
                    ${escapeHtml(item.category)}
                </span>
            </td>
            <td style="color: #34d399; font-weight: 600; font-size: 1.05rem;">
                ₹${((item.pricePaise || 0) / 100).toFixed(2)}
            </td>
            <td>
                <label style="display: flex; align-items: center; gap: 0.5rem; cursor: pointer;">
                    <input type="checkbox" ${item.isAvailable ? 'checked' : ''} style="width: 18px; height: 18px; accent-color: #34d399; cursor: pointer;" onchange="toggleItemAvailability('${item.id}', this.checked)">
                    <span style="color: ${item.isAvailable ? '#34d399' : '#f87171'}; font-weight: 600; font-size: 0.85rem;">
                        ${item.isAvailable ? 'In Stock' : 'Out of Stock'}
                    </span>
                </label>
            </td>
            <td>
                <div style="display: flex; gap: 0.5rem;">
                    <button class="btn btn-glass" style="padding: 0.35rem 0.6rem; font-size: 0.85rem;" onclick='openModal(${JSON.stringify(item).replace(/'/g, "&apos;")})'>
                        <ion-icon name="create-outline"></ion-icon> Edit
                    </button>
                    <button class="btn btn-glass" style="padding: 0.35rem 0.6rem; font-size: 0.85rem; color: #f87171; border-color: rgba(239,68,68,0.3);" onclick="deleteMenuItem('${item.id}')">
                        <ion-icon name="trash-outline"></ion-icon> Delete
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

async function toggleItemAvailability(itemId, isAvailable) {
    const item = currentMenuItems.find(i => i.id === itemId);
    if (!item) return;

    item.isAvailable = isAvailable;
    updateMetrics();

    try {
        const headers = await window.api.getAuthHeaders();
        await fetch(`${API_BASE_URL}/vendors/menu/${itemId}`, {
            method: 'PUT',
            headers,
            body: JSON.stringify({
                name: item.name,
                description: item.description,
                pricePaise: item.pricePaise,
                category: item.category,
                isAvailable
            })
        });
    } catch (e) {
        console.warn('Local availability toggled:', e);
    }
}

function openModal(item = null) {
    const modal = document.getElementById('itemModal');
    const overlay = document.getElementById('itemModalOverlay');
    const modalTitle = document.getElementById('modalTitle');
    
    if (modal) modal.style.display = 'block';
    if (overlay) overlay.style.display = 'block';

    if (item) {
        if (modalTitle) modalTitle.innerText = 'Edit Menu Item';
        document.getElementById('itemId').value = item.id;
        document.getElementById('itemName').value = item.name;
        document.getElementById('itemDesc').value = item.description;
        document.getElementById('itemPriceRupees').value = (item.pricePaise / 100);
        document.getElementById('itemCategory').value = item.category;
        document.getElementById('itemImageUrl').value = item.imageUrl || '';
        document.getElementById('itemAvailable').checked = item.isAvailable;
    } else {
        if (modalTitle) modalTitle.innerText = 'Add Menu Item';
        document.getElementById('itemForm').reset();
        document.getElementById('itemId').value = '';
        document.getElementById('itemAvailable').checked = true;
    }
}

function closeModal() {
    const modal = document.getElementById('itemModal');
    const overlay = document.getElementById('itemModalOverlay');
    if (modal) modal.style.display = 'none';
    if (overlay) overlay.style.display = 'none';
}

async function handleFormSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('itemId').value;
    const isEdit = !!id;
    
    const priceRupees = parseFloat(document.getElementById('itemPriceRupees').value);
    const payload = {
        name: document.getElementById('itemName').value.trim(),
        description: document.getElementById('itemDesc').value.trim(),
        pricePaise: Math.round(priceRupees * 100),
        category: document.getElementById('itemCategory').value.trim(),
        imageUrl: document.getElementById('itemImageUrl').value.trim(),
        isAvailable: document.getElementById('itemAvailable').checked
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

        if (res.ok) {
            closeModal();
            loadMenuItems();
        } else {
            // Local state update for smooth demo execution
            if (isEdit) {
                const idx = currentMenuItems.findIndex(i => i.id === id);
                if (idx >= 0) currentMenuItems[idx] = { ...currentMenuItems[idx], ...payload };
            } else {
                currentMenuItems.push({ id: 'item_' + Date.now(), ...payload });
            }
            closeModal();
            renderMenuItems(currentMenuItems);
            updateMetrics();
        }
    } catch (err) {
        closeModal();
    }
}

async function deleteMenuItem(id) {
    if (!confirm('Are you sure you want to delete this menu item?')) return;
    
    try {
        const headers = await window.api.getAuthHeaders();
        await fetch(`${API_BASE_URL}/vendors/menu/${id}`, {
            method: 'DELETE',
            headers
        });
    } catch (e) {}

    currentMenuItems = currentMenuItems.filter(i => i.id !== id);
    renderMenuItems(currentMenuItems);
    updateMetrics();
}

function escapeHtml(unsafe) {
    if (!unsafe) return '';
    return unsafe.toString().replace(/[&<"'>]/g, function (m) {
        return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[m];
    });
}
