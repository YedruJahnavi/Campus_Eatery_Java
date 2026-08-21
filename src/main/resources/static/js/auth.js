const version = "@latest";

async function initializeAuth() {
    try {
        const configRes = await fetch('/api/config');
        if (!configRes.ok) throw new Error("Could not load config");
        const config = await configRes.json();
        
        const clerkPublishableKey = config.clerkPublishableKey;
        const frontendApi = config.frontendApi;

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

                // Unconditionally inject Admin Panel link, but hide it from the public home page
                const isHomePage = window.location.pathname.endsWith('index.html') || window.location.pathname === '/' || window.location.pathname === '';
                if (!isHomePage && !document.getElementById('navAdminLink')) {
                    const adminLink = document.createElement('a');
                    adminLink.id = 'navAdminLink';
                    adminLink.href = 'admin.html';
                    adminLink.className = 'nav-link';
                    adminLink.style.color = '#f87171';
                    adminLink.style.fontWeight = 'bold';
                    adminLink.innerHTML = '<ion-icon name="shield-checkmark-outline"></ion-icon> Admin Panel';
                    navLinks.insertBefore(adminLink, navLinks.firstChild);
                }
            }



            if (window.Clerk.user) {
                // User is signed in, mount user button in place of the login btn
                const existingLoginBtn = document.getElementById('loginBtn');
                if (existingLoginBtn) {
                    existingLoginBtn.style.display = 'none';
                }
                
                window.Clerk.mountUserButton(authContainer);
                
                if (window.location.pathname.includes('dashboard.html')) {
                    localStorage.setItem('mockVendorId', window.Clerk.user.id);
                }

                await checkUserRoleAndRedirect(window.Clerk.user, false);
            } else {
                // Not signed in
                const existingLoginBtn = document.getElementById('loginBtn');
                if (existingLoginBtn) {
                    existingLoginBtn.onclick = () => window.Clerk.openSignIn();
                }

                // If trying to access vendor dashboard without Clerk session
                if (!localStorage.getItem('mockVendorId') && window.location.pathname.includes('dashboard.html')) {
                    window.location.replace('index.html');
                }
            }

            // Listen for authentication state changes (e.g. right after a user logs in via the modal)
            window.Clerk.addListener(async ({ user }) => {
                if (user) {
                    await checkUserRoleAndRedirect(user, true);
                }
            });
        });

        document.head.appendChild(script);
    } catch (err) {
        console.error("Failed to initialize auth:", err);
    }
}

async function checkUserRoleAndRedirect(user, isPostLoginEvent = false) {
    let role = 'customer';
    let approvalStatus = 'approved';
    const email = user.primaryEmailAddress ? user.primaryEmailAddress.emailAddress : '';
    const navLinks = document.querySelector('.nav-links');

    try {
        const token = window.Clerk.session ? await window.Clerk.session.getToken() : null;
        const headers = { 'X-User-Id': user.id };
        if (token) headers['Authorization'] = `Bearer ${token}`;

        const res = await fetch('/api/users/me', { headers });
        if (res.ok) {
            const dbUser = await res.json();
            role = dbUser.role || role;
            approvalStatus = dbUser.approvalStatus || approvalStatus;
        }
    } catch (e) {
        console.warn("Could not fetch user profile from API:", e);
    }

    // Fallback checks via email or Clerk metadata
    if (email.toLowerCase().includes('admin') || (user.publicMetadata && user.publicMetadata.role === 'admin')) {
        role = 'admin';
    } else if (email.toLowerCase().includes('vendor') || (user.publicMetadata && user.publicMetadata.role === 'vendor')) {
        role = 'vendor';
    }

    // Inject Become a Vendor link if customer hasn't applied yet
    if (navLinks && role !== 'vendor' && role !== 'admin' && approvalStatus !== 'pending_approval' && !document.getElementById('navBecomeVendorLink')) {
        const becomeVendorLink = document.createElement('a');
        becomeVendorLink.id = 'navBecomeVendorLink';
        becomeVendorLink.href = '#';
        becomeVendorLink.className = 'nav-link';
        becomeVendorLink.style.color = '#818cf8';
        becomeVendorLink.style.fontWeight = 'bold';
        becomeVendorLink.innerHTML = '<ion-icon name="storefront-outline"></ion-icon> Become a Vendor';
        becomeVendorLink.onclick = (e) => {
            e.preventDefault();
            if (typeof openVendorModal === 'function') openVendorModal();
        };
        navLinks.insertBefore(becomeVendorLink, navLinks.firstChild);
    }



    // Handle redirects
    const isHomePage = window.location.pathname.endsWith('index.html') || window.location.pathname === '/' || window.location.pathname === '';

    if (role === 'admin') {
        if (isPostLoginEvent || isHomePage) {
            window.location.replace('admin.html');
        }
    } else if (role === 'vendor' && approvalStatus === 'approved') {
        if (isPostLoginEvent || isHomePage) {
            window.location.replace('dashboard.html');
        }
    }
}

initializeAuth();
