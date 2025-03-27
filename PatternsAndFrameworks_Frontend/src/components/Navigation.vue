<script setup>
import { useAuthStore } from '@/stores/auth';
import { useNotificationStore } from '@/stores/notification';
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const notificationStore = useNotificationStore();
const router = useRouter();

const unreadCount = computed(() => {
  return notificationStore.unreadNotifications?.length || 0;
});

async function handleLogout() {
  authStore.logout();
  router.push('/');
}

// Check if user is already logged in on component mount
onMounted(async () => {
  await authStore.checkAuth();
  
  // If user is logged in, fetch their notifications
  if (authStore.isLoggedIn && authStore.user) {
    await notificationStore.fetchUserNotifications(authStore.user.id, false);
  }
});

// Watch for user login/logout to update notifications
watch(() => authStore.user, async (newUser) => {
  if (newUser) {
    await notificationStore.fetchUserNotifications(newUser.id, false);
  } else {
    notificationStore.reset();
  }
});
</script>

<template>
  <nav class="navbar">
    <ul class="nav-links">
      <li><router-link to="/">Home</router-link></li>
      
      <template v-if="authStore.isLoggedIn">
        <li>
          <div class="dropdown">
            <a>Tasks</a>
            <ul class="dropdown-menu">
              <li><router-link to="/tasks">View Tasks</router-link></li>
              <li><router-link to="/tasks/create">Create Task</router-link></li>
              <li><router-link to="/task-queues">Task Queues</router-link></li>
            </ul>
          </div>
        </li>
        <li><router-link to="/dashboard">Dashboard</router-link></li>
        
        <!-- Notification indicator -->
        <li class="notification-indicator">
          <router-link to="/notifications">
            Notifications
            <span v-if="unreadCount > 0" class="badge">{{ unreadCount }}</span>
          </router-link>
        </li>
        
        <li><a href="#" @click.prevent="handleLogout">Logout</a></li>
      </template>
      
      <template v-else>
        <li><router-link to="/login">Login</router-link></li>
        <li><router-link to="/register">Register</router-link></li>
      </template>
    </ul>
  </nav>
</template>

<style src="@/assets/styles/navigation.css"></style>