<script setup>
import Header from '@/components/Header.vue';
import Footer from './components/Footer.vue';
import NotificationContainer from './components/NotificationContainer.vue';
import { useAuthStore } from '@/stores/auth';
import { useNotificationStore } from '@/stores/notification';
import { onMounted, watch } from 'vue';
import { wsState } from '@/services/websocket';

const authStore = useAuthStore();
const notificationStore = useNotificationStore();

// Function to explicitly fetch notifications
const fetchNotifications = async () => {
  if (authStore.isLoggedIn && authStore.user) {
    console.log('Explicitly fetching notifications after page visibility change');
    try {
      // Fetch notifications directly from the store
      await notificationStore.fetchUserNotifications(authStore.user.id);
    } catch (err) {
      console.error('Error fetching notifications on visibility change:', err);
    }
  }
};

// Add an event listener for visibility change to handle page refresh/tab switch
onMounted(() => {
  document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible') {
      // When page becomes visible (after refresh or tab switch)
      fetchNotifications();
    }
  });
  
  // Also fetch notifications on initial mount
  fetchNotifications();
});

// Watch for WebSocket connection changes and refetch notifications when connected
watch(() => wsState.connected.value, (isConnected) => {
  if (isConnected) {
    // Delay slightly to ensure WebSocket subscriptions are set up
    setTimeout(fetchNotifications, 500);
  }
});
</script>

<template>
  <div id="app">
    <Header />
    <router-view class='body-content'/>
    <Footer />
    <NotificationContainer v-if="authStore.isLoggedIn" />
  </div>
</template>

<style>
.body-content {
  min-height: calc(100vh - 120px);
  padding: 1rem;
}
</style>