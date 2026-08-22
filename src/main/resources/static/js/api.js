/**
 * Centralized API client for interacting with the Spring Boot backend.
 * Automatically injects the Clerk JWT token into the Authorization header.
 */

const API_BASE = '/api';

async function fetchWithAuth(endpoint, options = {}) {
    if (!window.Clerk || !window.Clerk.session) {
        throw new Error('User is not authenticated');
    }

    const token = await window.Clerk.session.getToken();
    
    const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
        ...options.headers
    };

    const response = await fetch(`${API_BASE}${endpoint}`, {
        ...options,
        headers
    });

    if (!response.ok) {
        let errorMsg = `API Error: ${response.status} ${response.statusText}`;
        try {
            const errorData = await response.json();
            if (errorData.detail) errorMsg = errorData.detail;
        } catch (e) {
            // ignore
        }
        throw new Error(errorMsg);
    }
    
    return response.json();
}

async function fetchPublic(endpoint, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    const response = await fetch(`${API_BASE}${endpoint}`, {
        ...options,
        headers
    });

    if (!response.ok) {
        throw new Error(`API Error: ${response.status}`);
    }
    return response.json();
}

// Exported API Methods
window.api = {
    // Orders
    getStudentOrders: () => fetchWithAuth('/orders'),
    checkout: () => fetchWithAuth('/orders/checkout', { method: 'POST' }),
    
    // Vendor APIs
    getVendorOrders: () => fetchWithAuth('/orders/vendor'),
    updateOrderStatus: (orderId, status) => fetchWithAuth(`/orders/${orderId}/status`, {
        method: 'PUT',
        body: JSON.stringify({ status })
    }),

    // Cart APIs
    getCart: () => fetchWithAuth('/cart'),
    addCartItem: (menuItemId, quantity) => fetchWithAuth('/cart/add', {
        method: 'POST',
        body: JSON.stringify({ menuItemId, quantity })
    }),
    updateCartItem: (menuItemId, action) => fetchWithAuth(`/cart/${menuItemId}`, {
        method: 'PUT',
        body: JSON.stringify({ action }) // 'increase' or 'decrease'
    }),
    removeCartItem: (menuItemId) => fetchWithAuth(`/cart/${menuItemId}`, { method: 'DELETE' }),

    // Review APIs
    submitReview: (stallId, rating, comment) => fetchWithAuth('/reviews', {
        method: 'POST',
        body: JSON.stringify({ stallId, rating, comment })
    }),
    getStallReviews: (stallId) => fetchPublic(`/reviews/${stallId}`),

    // Public APIs
    getVendors: (search = '') => fetchPublic(search ? `/vendors?search=${encodeURIComponent(search)}` : '/vendors'),
    getVendorById: (id) => fetchPublic(`/vendors/${id}`),
    getRecommendations: () => fetchPublic('/vendors/recommendations'),

    // Delivery APIs
    getAvailableDeliveries: () => fetchWithAuth('/delivery/available'),
    getMyDeliveries: () => fetchWithAuth('/delivery/my-deliveries'),
    acceptDelivery: (orderId) => fetchWithAuth(`/delivery/${orderId}/accept`, { method: 'POST' }),
    completeDelivery: (orderId, deliveryCode) => fetchWithAuth(`/delivery/${orderId}/complete`, {
        method: 'POST',
        body: JSON.stringify({ deliveryCode })
    }),

    // Admin APIs
    getAdminStats: () => fetchWithAuth('/admin/stats'),
    getAdminUsers: () => fetchWithAuth('/admin/users'),
    updateUserStatus: (userId, isActive) => fetchWithAuth(`/admin/users/${userId}/status`, {
        method: 'PUT',
        body: JSON.stringify({ isActive })
    })
};
