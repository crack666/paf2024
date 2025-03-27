<script setup>
import { ref, watch, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useNotificationStore } from '@/stores/notification';
import { wsState } from '@/services/websocket';
import NotificationToast from './NotificationToast.vue';

const authStore = useAuthStore();
const notificationStore = useNotificationStore();

const notificationQueue = ref([]);
const maxToasts = 5; // Maximum number of visible toasts at once

// Combined function to process notifications from both sources
function processNewNotifications(newNotifications, oldNotifications, isSystem = false) {
  if ((!authStore.isLoggedIn && !isSystem) || !newNotifications || !oldNotifications) {
    return;
  }
  
  if (newNotifications.length > oldNotifications.length) {
    // Only show notification toast for the newest notification
    const newNotification = newNotifications[0];
    
    // Skip if already in queue
    const isInQueue = notificationQueue.value.some(n => 
      (n.id && n.id === newNotification.id) || 
      (!n.id && n.message === newNotification.message && n.type === newNotification.type)
    );
    
    if (!isInQueue) {
      // Create a unique temporary ID if none exists
      if (!newNotification.id) {
        newNotification.id = isSystem 
          ? `system-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`
          : `notification-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`;
      }
      
      console.log(`Processing ${isSystem ? 'system' : 'user'} notification:`, newNotification);
      
      addToNotificationQueue({
        ...newNotification,
        type: newNotification.type || (isSystem ? 'SYSTEM' : undefined),
        // Don't override userId if it exists already - important for correctly displaying
        // both system (userId=0) and user-specific notifications
        userId: newNotification.userId !== undefined ? newNotification.userId : 
                isSystem ? 0 : authStore.user?.id
      });
    }
  }
}

// Watch for new notifications coming from WebSocket - user notifications
watch(() => wsState.notifications.value, (newNotifications, oldNotifications) => {
  processNewNotifications(newNotifications, oldNotifications, false);
}, { deep: true });

// Watch also for system notifications
watch(() => wsState.systemNotifications.value, (newNotifications, oldNotifications) => {
  processNewNotifications(newNotifications, oldNotifications, true);
}, { deep: true });

// Add a notification to the queue
function addToNotificationQueue(notification) {
  // Create a copy to avoid reference issues
  const notificationCopy = {...notification};
  
  // Ensure deadlock notifications have HIGH urgency
  if (notificationCopy.type === 'DEADLOCK_DETECTED') {
    notificationCopy.urgency = 'HIGH';
    
    // Try to parse payload if it's a string
    if (notificationCopy.payload && typeof notificationCopy.payload === 'string') {
      try {
        notificationCopy.payload = JSON.parse(notificationCopy.payload);
      } catch (e) {
        console.error('Error parsing deadlock notification payload', e);
      }
    }
    
    // Increase visibility time for deadlock notifications (10 seconds)
    notificationCopy.duration = 10000;
    
    // Ensure system notifications are marked as userId=0
    notificationCopy.userId = 0;
  }
  
  // Ensure system notifications have userId=0
  if (notificationCopy.type === 'SYSTEM') {
    notificationCopy.userId = 0;
  }
  
  console.log(`Adding notification to queue: ${notificationCopy.type}, userId: ${notificationCopy.userId}`);
  
  // Add to store for history - without userId filtering
  notificationStore.addWebSocketNotification(notificationCopy);
  
  // Add to toast queue
  notificationQueue.value.unshift(notificationCopy);
  
  // Limit the number of visible toasts
  if (notificationQueue.value.length > maxToasts) {
    notificationQueue.value = notificationQueue.value.slice(0, maxToasts);
  }
}

// Remove notification from the queue
function removeFromQueue(notification) {
  const index = notificationQueue.value.findIndex(n => n.id === notification.id);
  if (index !== -1) {
    notificationQueue.value.splice(index, 1);
  }
}

// Get toasts in reverse order (oldest first)
const orderedToasts = computed(() => {
  return [...notificationQueue.value].reverse();
});
</script>

<template>
  <div class="notification-container">
    <notification-toast
      v-for="notification in orderedToasts"
      :key="notification.id"
      :notification="notification"
      @close="removeFromQueue(notification)"
    />
  </div>
</template>

