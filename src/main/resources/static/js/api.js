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

    // Public APIs
    getVendors: () => fetchPublic('/vendors'),
    getVendorById: (id) => fetchPublic(`/vendors/${id}`)
};
