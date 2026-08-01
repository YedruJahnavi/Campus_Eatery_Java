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
        // User is signed in, mount user button
        window.Clerk.mountUserButton(authContainer);
        
        // Update mock vendor if the user happens to be testing the vendor dashboard
        if (window.location.pathname.includes('dashboard.html')) {
            localStorage.setItem('mockVendorId', window.Clerk.user.id);
        }
    } else {
        // User is not signed in, mount sign in button
        const signInBtn = document.createElement('button');
        signInBtn.className = 'btn btn-primary';
        signInBtn.innerText = 'Sign In';
        signInBtn.onclick = () => window.Clerk.openSignIn();
        authContainer.appendChild(signInBtn);
    }
});
document.head.appendChild(script);
