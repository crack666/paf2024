<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { userService } from '@/services/api';
import '@/assets/styles/forms.css';
import { validateLogin } from '@/composables/useValidation';

const usernameOrEmail = ref('');
const password = ref('Password123'); // Default password to make testing easier
const users = ref([]);
const errorMessage = ref('');
const successMessage = ref('');
const isLoading = ref(false);
const isLoadingUsers = ref(false);
const router = useRouter();
const authStore = useAuthStore();

// Load users on component mount for the dropdown
onMounted(async () => {
  isLoadingUsers.value = true;
  try {
    const response = await userService.getAllUsers();
    users.value = response.data;
  } catch (error) {
    console.error('Failed to load users:', error);
    errorMessage.value = 'Failed to load users. Please try again later.';
  } finally {
    isLoadingUsers.value = false;
  }
});

async function handleLogin() {
  errorMessage.value = '';
  successMessage.value = '';
  
  const error = validateLogin({ username: usernameOrEmail.value, password: password.value });
  if (error) {
    errorMessage.value = error;
    return;
  }
  
  isLoading.value = true;
  
  try {
    // Login with username/email
    await authStore.login(usernameOrEmail.value);
    successMessage.value = 'Login successful! Redirecting...';
    
    // Redirect to dashboard
    setTimeout(() => {
      router.push('/dashboard');
    }, 1000);
  } catch (error) {
    console.error('Login error:', error);
    errorMessage.value = 'Login failed. Please check your username/email and try again.';
  } finally {
    isLoading.value = false;
  }
}

function selectUser(userId) {
  if (!userId) return;
  
  const selectedUser = users.value.find(user => user.id == userId);
  if (selectedUser) {
    usernameOrEmail.value = selectedUser.email || selectedUser.name;
  }
}

function redirectToRegister() {
  router.push('/register');
}
</script>

<template>
  <div class="form-container">
    <h1>Login</h1>
    <p v-if="errorMessage" class="feedback error">{{ errorMessage }}</p>
    <p v-if="successMessage" class="feedback success">{{ successMessage }}</p>
    <form @submit.prevent="handleLogin" novalidate>
      <div class="form-group">
        <label for="usernameOrEmail">Username or Email</label>
        <input
          id="usernameOrEmail"
          type="text"
          v-model="usernameOrEmail"
          placeholder="Enter your username or email"
        />
      </div>
      
      <!-- User selection dropdown is always shown -->
      <div class="form-group">
        <label>Quick Login (Select Existing User)</label>
        <select 
          class="form-select"
          @change="selectUser($event.target.value)"
          :disabled="isLoadingUsers"
        >
          <option disabled selected value="">-- Select a user --</option>
          <option 
            v-for="user in users" 
            :key="user.id" 
            :value="user.id"
          >
            {{ user.name }} ({{ user.email || 'No email' }})
          </option>
        </select>
        <small v-if="isLoadingUsers">Loading users...</small>
        <small v-else-if="users.length === 0">No users found. Please register a new user first.</small>
        <small v-else>Select a user to auto-fill the login field</small>
      </div>
      
      <div class="form-group">
        <label for="password">Password</label>
        <input
          id="password"
          type="password"
          v-model="password"
          placeholder="Enter your password"
        />
        <small>Note: No real authentication is implemented. Use any valid password format.</small>
      </div>
      <button type="submit" class="glowing-button" :disabled="isLoading">
        {{ isLoading ? 'Logging in...' : 'Login' }}
      </button>
      <div class="form-footer">
        <p>Don't have an account? <a @click="redirectToRegister" class="link">Register</a></p>
      </div>
    </form>
  </div>
</template>