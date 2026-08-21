/**
 * Cart Logic
 */
document.addEventListener('DOMContentLoaded', () => {
    initCart();
});

const elements = {
    cartToggleBtn: document.getElementById('cartToggleBtn'),
    closeCartBtn: document.getElementById('closeCartBtn'),
    cartSidebar: document.getElementById('cartSidebar'),
    sidebarOverlay: document.getElementById('sidebarOverlay'),
    cartItemsContainer: document.getElementById('cartItemsContainer'),
    cartTotalValue: document.getElementById('cartTotalValue'),
    checkoutBtn: document.getElementById('checkoutBtn')
};

function initCart() {
    if (!elements.cartToggleBtn) return; // not on a page with cart
    
    elements.cartToggleBtn.addEventListener('click', (e) => {
        e.preventDefault();
        openCart();
    });

    elements.closeCartBtn.addEventListener('click', closeCart);
    elements.sidebarOverlay.addEventListener('click', closeCart);
    elements.checkoutBtn.addEventListener('click', handleCheckout);
}

function openCart() {
    elements.cartSidebar.classList.add('open');
    elements.sidebarOverlay.classList.add('active');
    loadCart();
}

function closeCart() {
    elements.cartSidebar.classList.remove('open');
    elements.sidebarOverlay.classList.remove('active');
}

async function loadCart() {
    elements.cartItemsContainer.innerHTML = '<p class="text-secondary text-center" style="margin-top:2rem;">Loading...</p>';
    try {
        const cart = await window.api.getCart();
        renderCart(cart);
    } catch (err) {
        console.error('Failed to load cart', err);
        elements.cartItemsContainer.innerHTML = '<p class="text-error text-center" style="margin-top:2rem;">Failed to load cart.</p>';
    }
}

function renderCart(cart) {
    if (!cart || !cart.items || cart.items.length === 0) {
        elements.cartItemsContainer.innerHTML = '<p class="text-secondary text-center" style="margin-top:2rem;">Your cart is empty.</p>';
        elements.cartTotalValue.innerText = '₹0.00';
        return;
    }

    elements.cartItemsContainer.innerHTML = cart.items.map(item => `
        <div class="cart-item">
            <div class="cart-item-info">
                <h4>${escapeHtml(item.name)}</h4>
                <div class="cart-item-price">₹${(item.price / 100).toFixed(2)}</div>
            </div>
            <div class="cart-item-actions">
                <button class="qty-btn" onclick="updateCartItem('${item.menuItemId}', 'decrement')"><ion-icon name="remove-outline"></ion-icon></button>
                <span>${item.quantity}</span>
                <button class="qty-btn" onclick="updateCartItem('${item.menuItemId}', 'increment')"><ion-icon name="add-outline"></ion-icon></button>
            </div>
        </div>
    `).join('');

    elements.cartTotalValue.innerText = `₹${(cart.total / 100).toFixed(2)}`;
}

async function updateCartItem(itemId, action) {
    try {
        const res = await fetch(`${API_BASE_URL}/cart/${itemId}`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify({ action })
        });
        if (!res.ok) throw new Error('Update failed');
        const updatedCart = await res.json();
        renderCart(updatedCart);
    } catch (err) {
        console.error(err);
        alert('Failed to update cart');
    }
}

window.addToCart = async function(menuItemId) {
    try {
        await window.api.addToCart(menuItemId, 1);
        openCart(); // Show cart after adding
    } catch (err) {
        console.error(err);
        alert('Failed to add to cart: ' + err.message);
    }
};

function handleCheckout() {
    window.location.href = 'checkout.html';
}

function escapeHtml(unsafe) {
    if (!unsafe) return '';
    return unsafe.toString().replace(/[&<"'>]/g, function (m) {
        return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[m];
    });
}
