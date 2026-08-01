const clerkPublishableKey = "pk_test_cHJpbWFyeS1tYW1tYWwtMzMuY2xlcmsuYWNjb3VudHMuZGV2JA";
const frontendApi = "primary-mammal-33.clerk.accounts.dev";
const version = "@latest";

const script = document.createElement("script");
script.setAttribute("data-clerk-publishable-key", clerkPublishableKey);
script.async = true;
script.src = `https://${frontendApi}/npm/@clerk/clerk-js${version}/dist/clerk.browser.js`;
script.crossOrigin = "anonymous";
script.addEventListener("load", async function () {
    await window.Clerk.load();

    const authContainer = document.createElement('div');
    authContainer.id = 'clerk-auth-container';
    authContainer.style.display = 'flex';
    authContainer.style.alignItems = 'center';
    authContainer.style.marginLeft = 'auto';
    
    // Find navbar to inject auth UI
    const navLinks = document.querySelector('.nav-links');
    if (navLinks) {
        navLinks.appendChild(authContainer);
    }

    if (window.Clerk.user) {
        // User is signed in, mount user button in place of the login btn
        const existingLoginBtn = document.getElementById('loginBtn');
        if (existingLoginBtn) {
            existingLoginBtn.style.display = 'none';
        }
        
        window.Clerk.mountUserButton(authContainer);
        
        // Update mock vendor if the user happens to be testing the vendor dashboard
        if (window.location.pathname.includes('dashboard.html')) {
            localStorage.setItem('mockVendorId', window.Clerk.user.id);
        }
    } else {
        // Not signed in
        const existingLoginBtn = document.getElementById('loginBtn');
        if (existingLoginBtn) {
            existingLoginBtn.onclick = () => window.Clerk.openSignIn();
        }

        // If they try to access the dashboard while not logged in, redirect them back to home
        if (window.location.pathname.includes('dashboard.html')) {
            window.location.replace('index.html');
        }
    }

    // Listen for authentication state changes (e.g. right after a user logs in via the modal)
    window.Clerk.addListener(async ({ user }) => {
        if (user) {
            // If they are on the student homepage, check if they should be redirected
            if (window.location.pathname.endsWith('index.html') || window.location.pathname === '/' || window.location.pathname === '') {
                try {
                    const headers = { 'Authorization': `Bearer ${await window.Clerk.session.getToken()}` };
                    const res = await fetch('/api/users/me', { headers });
                    if (res.ok) {
                        const dbUser = await res.json();
                        // Redirect vendors to their dashboard!
                        if (dbUser.role === 'vendor' && dbUser.approvalStatus === 'approved') {
                            window.location.replace('dashboard.html');
                        }
                    }
                } catch (e) {
                    console.error("Failed to check user role after login", e);
                }
            }
        }
    });

});
document.head.appendChild(script);
