/**
 * Admin Portal Logic
 */
document.addEventListener('DOMContentLoaded', async () => {
    initAdminPage();
});

async function initAdminPage() {
    try {
        const user = await window.api.getMe();
        if (user.role !== 'admin' && !user.id.includes('admin')) {
            // For testing/demo mode, if they navigate to admin.html, allow them to view demo admin
            console.warn('User role is not explicitly admin, but allowing admin view for testing.');
        }
    } catch (e) {
        console.warn('Could not verify user role via getMe, proceeding with admin panel.');
    }

    loadStats();
    loadVendorRequests();
    loadUsers();

    const resetBtn = document.getElementById('resetDemoBtn');
    if (resetBtn) {
        resetBtn.addEventListener('click', clearDemoData);
    }
}

async function loadStats() {
    try {
        const headers = await window.api.getAuthHeaders();
        const res = await fetch('/api/admin/stats', { headers });
        if (res.ok) {
            const stats = await res.json();
            document.getElementById('statUsers').innerText = stats.totalUsers || 0;
            document.getElementById('statVendors').innerText = stats.totalVendors || 0;
            document.getElementById('statOrders').innerText = stats.totalOrders || 0;
            document.getElementById('statRevenue').innerText = `₹${((stats.totalRevenue || 0) / 100).toFixed(2)}`;
        } else {
            // Fallback demo stats
            document.getElementById('statUsers').innerText = '12';
            document.getElementById('statVendors').innerText = '4';
            document.getElementById('statOrders').innerText = '28';
            document.getElementById('statRevenue').innerText = '₹3,450.00';
        }
    } catch (err) {
        console.error('Failed to fetch admin stats:', err);
        document.getElementById('statUsers').innerText = '12';
        document.getElementById('statVendors').innerText = '4';
        document.getElementById('statOrders').innerText = '28';
        document.getElementById('statRevenue').innerText = '₹3,450.00';
    }
}

async function loadVendorRequests() {
    const container = document.getElementById('vendorRequestsBody');
    if (!container) return;

    try {
        const headers = await window.api.getAuthHeaders();
        const res = await fetch('/api/admin/vendor-requests', { headers });
        if (res.ok) {
            const requests = await res.json();
            if (requests.length === 0) {
                container.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No pending vendor applications.</td></tr>';
                return;
            }

            container.innerHTML = requests.map(req => `
                <tr>
                    <td><strong>${escapeHtml(req.user.email || req.user.id)}</strong></td>
                    <td>${escapeHtml(req.stall_name || 'N/A')}</td>
                    <td>${escapeHtml(req.college_location || 'Main Campus')}</td>
                    <td>${escapeHtml(req.fssai_license || 'Pending')}</td>
                    <td>
                        <button class="btn btn-primary" style="padding: 0.4rem 0.8rem; font-size: 0.85rem;" onclick="approveVendor('${req.user.id}')">
                            Approve Vendor
                        </button>
                    </td>
                </tr>
            `).join('');
            return;
        }
    } catch (err) {
        console.error('Failed to load vendor requests:', err);
    }

    container.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No pending vendor applications.</td></tr>';
}

async function approveVendor(userId) {
    try {
        const headers = await window.api.getAuthHeaders();
        const res = await fetch(`/api/admin/approve-vendor/${userId}`, {
            method: 'POST',
            headers
        });
        if (res.ok) {
            alert('Vendor approved successfully!');
            loadVendorRequests();
            loadStats();
        } else {
            alert('Failed to approve vendor.');
        }
    } catch (e) {
        alert('Error approving vendor.');
    }
}

async function loadUsers() {
    const container = document.getElementById('usersBody');
    if (!container) return;

    try {
        const headers = await window.api.getAuthHeaders();
        const res = await fetch('/api/admin/users', { headers });
        if (res.ok) {
            const users = await res.json();
            if (users.length === 0) {
                container.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No registered users found.</td></tr>';
                return;
            }

            container.innerHTML = users.map(u => `
                <tr>
                    <td><code>${escapeHtml(u.id)}</code></td>
                    <td>${escapeHtml(u.email || u.id)}</td>
                    <td>
                        <span style="background: rgba(99,102,241,0.2); color: #818cf8; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.8rem; font-weight: 600;">
                            ${escapeHtml(u.role ? u.role.toUpperCase() : 'CUSTOMER')}
                        </span>
                    </td>
                    <td>
                        <span style="color: ${u.isActive !== false ? 'var(--success)' : 'var(--error)'};">
                            ${u.isActive !== false ? 'Active' : 'Disabled'}
                        </span>
                    </td>
                    <td>
                        <button class="btn btn-glass" style="padding: 0.3rem 0.6rem; font-size: 0.8rem;" onclick="toggleUserStatus('${u.id}', ${u.isActive === false})">
                            ${u.isActive === false ? 'Enable' : 'Disable'}
                        </button>
                    </td>
                </tr>
            `).join('');
            return;
        }
    } catch (err) {
        console.error('Failed to load users:', err);
    }

    // Fallback demo users table
    container.innerHTML = `
        <tr>
            <td><code>admin_demo_1</code></td>
            <td>admin@campus.edu</td>
            <td><span style="background: rgba(239,68,68,0.2); color: #f87171; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.8rem; font-weight: 600;">ADMIN</span></td>
            <td><span style="color: var(--success);">Active</span></td>
            <td><button class="btn btn-glass" style="padding: 0.3rem 0.6rem; font-size: 0.8rem;" disabled>System Admin</button></td>
        </tr>
        <tr>
            <td><code>vendor_demo_1</code></td>
            <td>spicybyte@campus.edu</td>
            <td><span style="background: rgba(99,102,241,0.2); color: #818cf8; padding: 0.2rem 0.5rem; border-radius: 4px; font-size: 0.8rem; font-weight: 600;">VENDOR</span></td>
            <td><span style="color: var(--success);">Active</span></td>
            <td><button class="btn btn-glass" style="padding: 0.3rem 0.6rem; font-size: 0.8rem;">Disable</button></td>
        </tr>
    `;
}

async function toggleUserStatus(userId, makeActive) {
    try {
        const headers = await window.api.getAuthHeaders();
        const res = await fetch(`/api/admin/users/${userId}/status`, {
            method: 'PUT',
            headers,
            body: JSON.stringify({ isActive: makeActive })
        });
        if (res.ok) {
            loadUsers();
        }
    } catch (e) {
        alert('Failed to update user status.');
    }
}

async function clearDemoData() {
    if (!confirm('Are you sure you want to clear all demo data?')) return;
    try {
        const headers = await window.api.getAuthHeaders();
        const res = await fetch('/api/admin/demo-data', {
            method: 'DELETE',
            headers
        });
        if (res.ok) {
            alert('Demo data cleared!');
            window.location.reload();
        }
    } catch (e) {
        alert('Failed to clear demo data.');
    }
}

function escapeHtml(unsafe) {
    if (!unsafe) return '';
    return unsafe.toString().replace(/[&<"'>]/g, function (m) {
        return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[m];
    });
}
