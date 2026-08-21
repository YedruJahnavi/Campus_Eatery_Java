document.addEventListener('DOMContentLoaded', () => {
    initCheckout();
});

const checkoutElements = {
    form: document.getElementById('addressForm'),
    summary: document.getElementById('checkoutSummary'),
    btn: document.getElementById('placeOrderBtn'),
    errorMsg: document.getElementById('checkoutError'),
    successScreen: document.getElementById('checkoutSuccess'),
    formContainer: document.getElementById('checkoutFormContainer'),
    successOrderId: document.getElementById('successOrderId'),

    line1: document.getElementById('addressLine1'),
    line2: document.getElementById('addressLine2'),
    city: document.getElementById('city'),
    pincode: document.getElementById('pincode')
};

async function initCheckout() {
    checkoutElements.form.addEventListener('submit', handleCheckoutSubmit);
    
    try {
        const cart = await window.api.getCart();
        
        if (!cart || !cart.items || cart.items.length === 0) {
            checkoutElements.summary.innerHTML = '<p class="text-error text-center">Your cart is empty.</p>';
            checkoutElements.btn.disabled = true;
            return;
        }

        let summaryHtml = '<div style="background: var(--surface-2); padding: 1rem; border-radius: 8px; border: 1px solid var(--border);">';
        cart.items.forEach(item => {
            summaryHtml += `
                <div style="display: flex; justify-content: space-between; margin-bottom: 0.5rem;">
                    <span>${item.quantity}x ${escapeHtml(item.name)}</span>
                    <span>₹${(item.price / 100).toFixed(2)}</span>
                </div>
            `;
        });
        
        const foodTotal = cart.total;
        const gst = Math.floor(foodTotal * 0.05);
        const grandTotal = foodTotal + gst;

        summaryHtml += `
            <div style="border-top: 1px solid var(--border); margin-top: 1rem; padding-top: 0.5rem; display: flex; justify-content: space-between;">
                <span class="text-secondary">Subtotal</span>
                <span>₹${(foodTotal / 100).toFixed(2)}</span>
            </div>
            <div style="display: flex; justify-content: space-between; margin-bottom: 0.5rem;">
                <span class="text-secondary">GST (5%)</span>
                <span>₹${(gst / 100).toFixed(2)}</span>
            </div>
            <div style="border-top: 1px solid var(--border); padding-top: 1rem; display: flex; justify-content: space-between; font-weight: 700; font-size: 1.2rem; color: var(--text);">
                <span>Grand Total</span>
                <span>₹${(grandTotal / 100).toFixed(2)}</span>
            </div>
        </div>`;

        checkoutElements.summary.innerHTML = summaryHtml;

    } catch (err) {
        checkoutElements.summary.innerHTML = '<p class="text-error text-center">Failed to load cart summary.</p>';
        console.error(err);
    }
}

async function handleCheckoutSubmit(e) {
    e.preventDefault();
    checkoutElements.btn.disabled = true;
    checkoutElements.btn.innerHTML = 'Processing...';
    checkoutElements.errorMsg.style.display = 'none';

    try {
        // 1. Update Address
        await window.api.updateAddress({
            addressLine1: checkoutElements.line1.value,
            addressLine2: checkoutElements.line2.value,
            city: checkoutElements.city.value,
            pincode: checkoutElements.pincode.value
        });

        // 2. Checkout
        const response = await window.api.checkout();

        // 3. Success
        checkoutElements.formContainer.style.display = 'none';
        checkoutElements.successScreen.style.display = 'block';
        checkoutElements.successOrderId.innerText = response.order_id;

    } catch (err) {
        console.error(err);
        checkoutElements.errorMsg.innerText = err.message;
        checkoutElements.errorMsg.style.display = 'block';
        checkoutElements.btn.disabled = false;
        checkoutElements.btn.innerHTML = '<ion-icon name="card-outline"></ion-icon> Place Order';
    }
}

function escapeHtml(unsafe) {
    if (!unsafe) return '';
    return unsafe.toString().replace(/[&<"'>]/g, function (m) {
        return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[m];
    });
}
