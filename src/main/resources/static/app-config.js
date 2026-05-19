// API Configuration - centralized, environment-aware
// Supports local development, live server, and production (Heroku)

(function() {
    const APP_CONFIG = {
        API_BASE: getApiBase()
    };

    function getApiBase() {
        // 1. Check if we're on Heroku (production)
        if (window.location.hostname.includes('herokuapp.com')) {
            return '/api'; // Same origin for Heroku deployments
        }

        // 2. Check if we're on localhost:8080 (local Spring Boot)
        if (window.location.hostname === 'localhost' && window.location.port === '8080') {
            return '/api';
        }

        // 3. Check if we're on 127.0.0.1:8080 (local Spring Boot)
        if (window.location.hostname === '127.0.0.1' && window.location.port === '8080') {
            return '/api';
        }

        // 4. For live server on port 5500 (127.0.0.1 or localhost), connect to localhost:8080
        if (window.location.port === '5500') {
            // Backend is on localhost:8080, accessible from live server
            return 'http://127.0.0.1:8080/api';
        }

        // 5. Default to relative /api path (works for same-origin requests)
        return '/api';
    }

    // Expose globally for all pages to use
    window.APP_CONFIG = APP_CONFIG;
})();
