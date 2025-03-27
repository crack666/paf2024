import './assets/styles/main.css'
import './assets/styles/dashboard.css'
import './assets/styles/forms.css'
import './assets/styles/feedback.css'
import { createPinia } from 'pinia';
import router from './router';
import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import axios from 'axios';

// Polyfill for global in browser environment (needed for SockJS)
window.global = window;

// Load environment configuration if available
const loadConfig = () => {
  // Default configuration (fallback)
  const config = {
    apiUrl: 'http://localhost:8080/api',
    wsUrl: 'http://localhost:8080/api/ws'
  };

  // Check if window.VUE_APP_API_URL is available (set by env.js)
  if (window.VUE_APP_API_URL) {
    config.apiUrl = window.VUE_APP_API_URL;
    config.wsUrl = window.VUE_APP_API_URL.replace(/\/api$/, '/api/ws');
  }

  return config;
};

const config = loadConfig();

// Create the app
const app = createApp(App);

// Create and mount Pinia
const pinia = createPinia();
app.use(pinia);

// Import stores after pinia is created
import { useAuthStore } from './stores/auth';

// Configure Axios
axios.defaults.baseURL = config.apiUrl;

// Provide config globally
app.provide('config', config);
app.config.globalProperties.$config = config;

// Use plugins
app.use(router);
app.use(ElementPlus);

// Mount the app
app.mount('#app');

// Try to restore user session
const authStore = useAuthStore();
authStore.checkAuth();