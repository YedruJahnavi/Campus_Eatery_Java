/**
 * API Wrapper for Spring Boot Backend
 */
const API_BASE_URL = '/api';

const getAuthHeaders = async () => {
    let token = null;
    let userId = localStorage.getItem('mockUserId') || 'mock_student_123';

    if (window.Clerk && window.Clerk.session) {
        token = await window.Clerk.session.getToken();
        userId = window.Clerk.user.id;
    }

    const headers = {
        'Content-Type': 'application/json',
        'X-User-Id': userId
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    return headers;
};

const api = {
    getAuthHeaders,

    // Vendors
    getVendors: async (search = '') => {
        const url = search ? `${API_BASE_URL}/vendors?search=${encodeURIComponent(search)}` : `${API_BASE_URL}/vendors`;
        const headers = await getAuthHeaders();
        const res = await fetch(url, { headers });
        if (!res.ok) throw new Error('Failed to fetch vendors');
        return res.json();
    },

    getVendorById: async (id) => {
        const headers = await getAuthHeaders();
        const res = await fetch(`${API_BASE_URL}/vendors/${id}`, { headers });
        if (!res.ok) throw new Error('Failed to fetch vendor details');
        return res.json();
    },

    // Cart
    getCart: async () => {
        const headers = await getAuthHeaders();
        const res = await fetch(`${API_BASE_URL}/cart`, { headers });
        if (!res.ok) throw new Error('Failed to fetch cart');
        return res.json();
    },

    addToCart: async (menuItemId, quantity = 1) => {
        const headers = await getAuthHeaders();
        const res = await fetch(`${API_BASE_URL}/cart/add`, {
            method: 'POST',
            headers,
            body: JSON.stringify({ menuItemId, quantity })
        });
        if (!res.ok) throw new Error('Failed to add to cart');
        return res.json();
    },

    // Recommendations
    getRecommendations: async () => {
        const headers = await getAuthHeaders();
        const res = await fetch(`${API_BASE_URL}/recommendations`, { headers });
        if (!res.ok) throw new Error('Failed to fetch recommendations');
        return res.json();
    },

    // User Address
    updateAddress: async (addressData) => {
        const headers = await getAuthHeaders();
        const res = await fetch(`${API_BASE_URL}/users/address`, {
            method: 'PUT',
            headers,
            body: JSON.stringify(addressData)
        });
        if (!res.ok) throw new Error('Failed to update address');
        return res.json();
    },

    // Checkout
    checkout: async () => {
        const headers = await getAuthHeaders();
        const res = await fetch(`${API_BASE_URL}/orders/checkout`, {
            method: 'POST',
            headers
        });
        if (!res.ok) {
            const errData = await res.json();
            throw new Error(errData.detail || 'Checkout failed');
        }
        return res.json();
    },

    // Users & Identity
    getMe: async () => {
        const headers = await getAuthHeaders();
        const res = await fetch(`${API_BASE_URL}/users/me`, { headers });
        if (!res.ok) throw new Error('Failed to fetch user profile');
        return res.json();
    },

    updateProfile: async (profileData) => {
        const headers = await getAuthHeaders();
        const res = await fetch(`${API_BASE_URL}/users/profile`, {
            method: 'PUT',
            headers,
            body: JSON.stringify(profileData)
        });
        if (!res.ok) throw new Error('Failed to update profile');
        return res.json();
    },

    applyForVendor: async (vendorData) => {
        const headers = await getAuthHeaders();
        const res = await fetch(`${API_BASE_URL}/users/vendor-request`, {
            method: 'POST',
            headers,
            body: JSON.stringify(vendorData)
        });
        if (!res.ok) throw new Error('Failed to submit vendor application');
        return res.json();
    }
};

window.api = api;
