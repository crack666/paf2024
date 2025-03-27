import { defineStore } from 'pinia';
import { ref, onMounted } from 'vue';
import { userService } from '@/services/api';
import { initializeWebSocket, disconnectWebSocket } from '@/services/websocket';

export const useAuthStore = defineStore('auth', () => {
    const isLoggedIn = ref(false);
    const user = ref(null);
    const loading = ref(false);
    const error = ref(null);

    // Login by finding user by email or name
    async function login(usernameOrEmail) {
        loading.value = true;
        error.value = null;
        try {
            // Get all users and find the matching one
            const response = await userService.getAllUsers();
            const users = response.data;
            
            // Find user by email or name (case insensitive)
            const foundUser = users.find(u => 
                u.email?.toLowerCase() === usernameOrEmail.toLowerCase() ||
                u.name?.toLowerCase() === usernameOrEmail.toLowerCase()
            );
            
            if (!foundUser) {
                throw new Error('User not found');
            }
            
            // Store user data
            isLoggedIn.value = true;
            user.value = foundUser;
            
            // Initialize WebSocket connection for this user
            initializeWebSocket(foundUser.id);
            
            // Store user ID in localStorage for persistence
            localStorage.setItem('userId', foundUser.id);
            
            return foundUser;
        } catch (err) {
            console.error('Login error:', err);
            error.value = err.message || 'Failed to log in';
            throw err;
        } finally {
            loading.value = false;
        }
    }

    // Login directly by user ID (used for session restoration)
    async function loginById(userId) {
        loading.value = true;
        error.value = null;
        try {
            // Get user by ID
            const response = await userService.getUserById(userId);
            const userData = response.data;
            
            // Store user data
            isLoggedIn.value = true;
            user.value = userData;
            
            // Initialize WebSocket connection for this user
            initializeWebSocket(userData.id);
            
            return userData;
        } catch (err) {
            console.error('Login error:', err);
            error.value = err.response?.data?.message || 'Failed to log in';
            throw err;
        } finally {
            loading.value = false;
        }
    }

    function logout() {
        // Close WebSocket connection
        disconnectWebSocket();
        
        // Clear user data
        isLoggedIn.value = false;
        user.value = null;
        
        // Remove from localStorage
        localStorage.removeItem('userId');
    }
    
    // Function to check if user is already logged in (from localStorage)
    async function checkAuth() {
        const storedUserId = localStorage.getItem('userId');
        if (storedUserId) {
            try {
                await loginById(storedUserId);
                return true;
            } catch (err) {
                console.error('Failed to restore session:', err);
                return false;
            }
        }
        return false;
    }

    return { 
        isLoggedIn, 
        user, 
        loading,
        error,
        login,
        loginById,
        logout,
        checkAuth
    };
});