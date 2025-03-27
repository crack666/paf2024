<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { userService } from '@/services/api';
import { validateRegister } from '@/composables/useValidation';

const username = ref('');
const email = ref('');
const password = ref('');
const confirmPassword = ref('');
const errorMessage = ref('');
const successMessage = ref('');
const isLoading = ref(false);
const router = useRouter();

async function handleRegister() {
  errorMessage.value = '';
  successMessage.value = '';
  
  const error = validateRegister({
    username: username.value,
    email: email.value,
    password: password.value,
    confirmPassword: confirmPassword.value,
  });

  if (error) {
    errorMessage.value = error;
    return;
  }
  
  isLoading.value = true;
  
  try {
    // Create user with the API
    const userData = {
      name: username.value,
      email: email.value,
      tasks: [],
      notifications: []
    };
    
    const response = await userService.createUser(userData);
    
    successMessage.value = `Registration successful! Your user ID is ${response.data.id}. Redirecting to login...`;
    
    // Redirect to login page after registration
    setTimeout(() => {
      router.push('/login');
    }, 2000);
  } catch (error) {
    console.error('Registration error:', error);
    errorMessage.value = 'Registration failed. Please try again.';
  } finally {
    isLoading.value = false;
  }
}

function redirectToLogin() {
  router.push('/login');
}
</script>

<template>
  <div class="form-container">
    <h1>Register</h1>
    <p v-if="errorMessage" class="feedback error">{{ errorMessage }}</p>
    <p v-if="successMessage" class="feedback success">{{ successMessage }}</p>
    <form @submit.prevent="handleRegister" novalidate>
      <div class="form-group">
        <label for="username">Name</label>
        <input
          id="username"
          type="text"
          v-model="username"
          placeholder="Enter your full name"
        />
      </div>
      <div class="form-group">
        <label for="email">Email</label>
        <input
          id="email"
          type="email"
          v-model="email"
          placeholder="Enter your email"
        />
      </div>
      <div class="form-group">
        <label for="password">Password</label>
        <input
          id="password"
          type="password"
          v-model="password"
          placeholder="Enter your password"
        />
        <small>Password must be at least 6 characters with at least one uppercase letter and one number.</small>
      </div>
      <div class="form-group">
        <label for="confirmPassword">Confirm Password</label>
        <input
          id="confirmPassword"
          type="password"
          v-model="confirmPassword"
          placeholder="Confirm your password"
        />
      </div>
      <button type="submit" class="glowing-button" :disabled="isLoading">
        {{ isLoading ? 'Registering...' : 'Register' }}
      </button>
      <div class="form-footer">
        <p>Already have an account? <a @click="redirectToLogin" class="link">Login</a></p>
      </div>
    </form>
  </div>
</template>