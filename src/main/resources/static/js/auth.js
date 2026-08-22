const PUBLISHABLE_KEY = 'pk_test_cHJpbWFyeS1tYW1tYWwtMzMuY2xlcmsuYWNjb3VudHMuZGV2JA';

const startClerk = async () => {
  const Clerk = window.Clerk;

  try {
    // Load Clerk environment & session if available
    await Clerk.load();

    const loginBtn = document.getElementById('login-btn');
    const signupBtn = document.getElementById('signup-btn');
    const userButtonDiv = document.getElementById('user-button');

    // If the user is logged in, show the User Button and hide login/signup
    if (Clerk.user) {
      if (loginBtn) loginBtn.style.display = 'none';
      if (signupBtn) signupBtn.style.display = 'none';
      
      if (userButtonDiv) {
        Clerk.mountUserButton(userButtonDiv);
        userButtonDiv.style.display = 'block';
      }

      // If we're on the landing page and logged in, we might want to redirect to dashboard
      // uncomment this to auto-redirect:
      // if (window.location.pathname === '/' || window.location.pathname === '/index.html') {
      //   window.location.href = '/dashboard.html';
      // }
    } else {
      // User is not logged in
      if (loginBtn) {
        loginBtn.addEventListener('click', (e) => {
          e.preventDefault();
          Clerk.openSignIn({ afterSignInUrl: '/dashboard.html' });
        });
      }

      if (signupBtn) {
        signupBtn.addEventListener('click', (e) => {
          e.preventDefault();
          Clerk.openSignUp({ afterSignUpUrl: '/dashboard.html' });
        });
      }
    }
  } catch (err) {
    console.error('Error starting Clerk: ', err);
  }
};

// Initialize Clerk via script injection
(() => {
  const script = document.createElement('script');
  script.setAttribute('data-clerk-publishable-key', PUBLISHABLE_KEY);
  script.async = true;
  script.src = `https://cdn.jsdelivr.net/npm/@clerk/clerk-js@latest/dist/clerk.browser.js`;
  script.crossOrigin = 'anonymous';
  script.addEventListener('load', startClerk);
  script.addEventListener('error', () => {
    document.getElementById('no-frontend-api-warning').hidden = false;
  });
  document.body.appendChild(script);
})();
