<script setup>
import { ref, onMounted, computed, onBeforeUnmount } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useNotificationStore } from '@/stores/notification';
import { useRouter } from 'vue-router';
import { wsState, checkConnectionStatus } from '@/services/websocket';

const authStore = useAuthStore();
const notificationStore = useNotificationStore();
const router = useRouter();

const isLoading = ref(false);
const error = ref(null);
const activeTab = ref('unread');
const showNotificationDetails = ref(false);
const selectedNotification = ref(null);

// Redirect if not logged in
if (!authStore.isLoggedIn) {
  router.push('/login');
}

import { watch } from 'vue';

// Fetch notifications when component mounts
onMounted(() => {
  if (authStore.isLoggedIn && authStore.user) {
    // On mount, check if WS connection is established
    if (wsState.connected.value) {
      // If connected, load notifications immediately
      loadNotifications();
    } else {
      // If not connected yet, wait for connection
      console.log("WebSocket not connected yet, waiting for connection...");
    }
    
    // Set up WebSocket connection state watcher
    watch(
      () => wsState.connected.value,
      (isConnected) => {
        if (isConnected) {
          console.log("WebSocket connection established, loading notifications...");
          loadNotifications();
        }
      }
    );
    
    // Also watch for new notifications and refresh the list
    watch(
      [() => wsState.notifications.value, () => wsState.systemNotifications.value],
      () => {
        console.log("New notifications available, refreshing list...");
        loadNotifications();
      },
      { deep: true }
    );
  }
});

// Load notifications - using WebSockets but with improved handling of both system and user notifications
async function loadNotifications() {
  isLoading.value = true;
  error.value = null;
  
  try {
    if (authStore.isLoggedIn && authStore.user) {
      console.log("Loading notifications from WebSocket for userId:", authStore.user.id);
      
      // First, ensure our store has both user and system notifications from WebSockets
      notificationStore.reset();
      
      // Combine ALL notifications from both WebSocket arrays
      const allWsNotifications = [
        ...(wsState.notifications.value || []), 
        ...(wsState.systemNotifications.value || [])
      ];
      
      console.log(`Found ${allWsNotifications.length} total WebSocket notifications to process`);
      
      // Process all WebSocket notifications and add to store
      for (const notification of allWsNotifications) {
        // Deep copy to avoid reference issues
        const notificationCopy = JSON.parse(JSON.stringify(notification));
        
        // CRITICAL: Ensure system notifications have userId=0
        if (notificationCopy.type === 'SYSTEM' || notificationCopy.type === 'DEADLOCK_DETECTED') {
          notificationCopy.userId = 0;
        }
        
        // Ensure type and userId are present (for sorting and filtering)
        if (!notificationCopy.type) {
          notificationCopy.type = notificationCopy.userId === 0 ? 'SYSTEM' : 'USER';
        }
        
        // Add to notification store (no userId filtering)
        notificationStore.addWebSocketNotification(notificationCopy);
      }
      
      // Now retrieve filtered notifications from the store as needed
      await notificationStore.fetchUserNotifications(authStore.user.id);
      
      // Log what we've got
      console.log("Final notification state:", {
        all: (notificationStore.userNotifications?.value?.length || 0),
        unread: (notificationStore.unreadNotifications?.length || 0),
        read: (notificationStore.readNotifications?.length || 0),
        types: Array.from(new Set((notificationStore.userNotifications?.value || []).map(n => n.type)))
      });
      
    } else {
      console.error("Cannot load notifications: User not logged in");
      error.value = "Please log in to view notifications";
    }
  } catch (err) {
    console.error('Error loading notifications:', err);
    error.value = 'Failed to load notifications. Please try again.';
  } finally {
    isLoading.value = false;
  }
}

// Computed properties for filtered notifications
const unreadNotifications = computed(() => {
  // Sort by timestamp, newest first
  console.log("Computing unread notifications, count:", notificationStore.unreadNotifications.length);
  console.log("Unread notification types:", notificationStore.unreadNotifications.map(n => n.type).join(", "));
  return [...notificationStore.unreadNotifications].sort((a, b) => 
    new Date(b.timestamp) - new Date(a.timestamp)
  );
});

const readNotifications = computed(() => {
  // Sort by timestamp, newest first
  console.log("Computing read notifications, count:", notificationStore.readNotifications.length);
  return [...notificationStore.readNotifications].sort((a, b) => 
    new Date(b.timestamp) - new Date(a.timestamp)
  );
});

const allNotifications = computed(() => {
  // Sort by timestamp, newest first
  console.log("Computing all notifications, count:", notificationStore.userNotifications.length);
  console.log("All notification types:", notificationStore.userNotifications.map(n => n.type).join(", "));
  return [...notificationStore.userNotifications].sort((a, b) => 
    new Date(b.timestamp) - new Date(a.timestamp)
  );
});

// Mark notification as read - always uses HTTP for reliability when possible
async function markAsRead(notification) {
  if (!notification.read) {
    try {
      // Use the notification's userId, not the current user's ID
      // This ensures system notifications (userId=0) are properly marked
      const userId = notification.userId || authStore.user.id;
      
      // Always update UI immediately for responsiveness
      notification.read = true;
      notification.status = 'READ';
      
      console.log(`Marking notification ${notification.id} as read for user ${userId}`);
      
      // Check if the ID is numeric - important for HTTP compatibility
      const isNumericId = typeof notification.id === 'number' || !isNaN(parseInt(notification.id));
      
      if (!isNumericId) {
        console.warn(`Notification has non-numeric ID: ${notification.id}`);
        console.warn('This may only update the UI, not the backend state (WebSocket will be tried)');
      }
      
      // Use the store's method which now checks for numeric IDs and falls back appropriately
      const result = await notificationStore.markNotificationAsRead(notification.id, userId);
      
      if (result === true || (result && typeof result === 'object')) {
        console.log(`Successfully marked notification ${notification.id} as read`);
      } else {
        console.warn(`Notification ${notification.id} marked as read in UI only`);
      }
    } catch (err) {
      console.error('Error marking notification as read:', err);
      // Keep the notification marked as read in the UI even if backend update fails
    }
  }
}

// View notification details
function viewNotificationDetails(notification) {
  selectedNotification.value = notification;
  showNotificationDetails.value = true;
  
  // Mark as read when viewed
  if (!notification.read) {
    markAsRead(notification);
  }
}

// Close notification details
function closeNotificationDetails() {
  showNotificationDetails.value = false;
  selectedNotification.value = null;
}

// Switch between tabs
function switchTab(tab) {
  activeTab.value = tab;
}

// Format date for display
function formatDate(dateString) {
  if (!dateString) return '';
  return new Date(dateString).toLocaleString();
}

// Get class based on urgency
function getUrgencyClass(urgency) {
  switch (urgency?.toUpperCase()) {
    case 'HIGH': return 'urgency-high';
    case 'NORMAL': return 'urgency-normal';
    case 'LOW': return 'urgency-low';
    default: return 'urgency-normal';
  }
}

// Get class based on notification type
function getTypeClass(type) {
  switch (type?.toUpperCase()) {
    case 'TASK_CREATED': return 'type-created';
    case 'TASK_STARTED': return 'type-started';
    case 'TASK_COMPLETED': return 'type-completed';
    case 'TASK_ERROR': return 'type-error';
    case 'TASK_OVERDUE': return 'type-overdue';
    case 'DEADLOCK_DETECTED': return 'type-error'; // Style as error
    case 'SYSTEM': return 'type-system';
    default: return 'type-default';
  }
}

// Check if there is a related task
function hasRelatedTask(notification) {
  // Handle deadlock notification special case - they have relatedTaskId in the payload
  if (notification.type === 'DEADLOCK_DETECTED' && notification.payload) {
    try {
      // Try to parse payload if it's a string
      const payload = typeof notification.payload === 'string' 
        ? JSON.parse(notification.payload) 
        : notification.payload;
      return payload.taskIds && payload.taskIds.length > 0;
    } catch (e) {
      console.error('Error parsing deadlock payload', e);
      return false;
    }
  }
  
  // Regular case - direct relatedTaskId or taskId
  return (notification.relatedTaskId !== null && notification.relatedTaskId !== undefined) || 
         (notification.taskId !== null && notification.taskId !== undefined);
}

// Navigate to related task
function goToRelatedTask(notification) {
  // Handle deadlock notification special case
  if (notification.type === 'DEADLOCK_DETECTED' && notification.payload) {
    try {
      // Parse payload if it's a string
      const payload = typeof notification.payload === 'string' 
        ? JSON.parse(notification.payload) 
        : notification.payload;
        
      // If there are multiple tasks in the deadlock, go to the first one
      if (payload.taskIds && payload.taskIds.length > 0) {
        router.push(`/tasks/${payload.taskIds[0]}`);
        return;
      }
    } catch (e) {
      console.error('Error parsing deadlock payload', e);
    }
  }
  
  // Regular case - direct relatedTaskId or taskId
  if (notification.relatedTaskId) {
    router.push(`/tasks/${notification.relatedTaskId}`);
  } else if (notification.taskId) {
    router.push(`/tasks/${notification.taskId}`);
  }
}

// Add CSS for connection indicator
const style = document.createElement('style');
style.textContent = `
  .connection-status {
    display: flex;
    align-items: center;
    padding: 5px 10px;
    border-radius: 4px;
    font-size: 14px;
    margin-left: 10px;
  }
  
  .connection-status.connected {
    background-color: rgba(0, 128, 0, 0.1);
    color: green;
  }
  
  .connection-status.disconnected {
    background-color: rgba(255, 0, 0, 0.1);
    color: red;
    animation: pulse 2s infinite;
  }
  
  .connection-indicator {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    margin-right: 8px;
    display: inline-block;
  }
  
  .connected .connection-indicator {
    background-color: green;
  }
  
  .disconnected .connection-indicator {
    background-color: red;
  }
  
  @keyframes pulse {
    0% { opacity: 1; }
    50% { opacity: 0.5; }
    100% { opacity: 1; }
  }
`;
document.head.appendChild(style);
</script>

<template>
  <div class="notifications-container">
    <div class="notifications-header">
      <h1>Notifications</h1>
      <div class="notification-actions">
        <button @click="loadNotifications" class="refresh-button" title="Refresh notifications">
          ↻ Refresh
        </button>
      </div>
    </div>

    <!-- Loading and error states -->
    <div v-if="isLoading" class="loading-state">
      Loading notifications...
    </div>
    
    <div v-else-if="error" class="error-state">
      {{ error }}
    </div>
    
    <!-- Empty state -->
    <div v-else-if="allNotifications.length === 0" class="empty-state">
      <p>No notifications found.</p>
    </div>
    
    <!-- Notifications list -->
    <div v-else class="notifications-content">
      <!-- Tabs -->
      <div class="tabs">
        <button 
          @click="switchTab('all')" 
          :class="['tab-button', activeTab === 'all' ? 'active' : '']"
        >
          All
          <span v-if="allNotifications.length > 0" class="badge">{{ allNotifications.length }}</span>
        </button>
        <button 
          @click="switchTab('read')" 
          :class="['tab-button', activeTab === 'read' ? 'active' : '']"
        >
          Read
          <span v-if="readNotifications.length > 0" class="badge">{{ readNotifications.length }}</span>
        </button>
        <button
            @click="switchTab('unread')"
            :class="['tab-button', activeTab === 'unread' ? 'active' : '']"
        >
          Unread
          <span v-if="unreadNotifications.length > 0" class="badge">{{ unreadNotifications.length }}</span>
        </button>
      </div>
      
      <!-- Current tab content -->
      <div class="tab-content"  style="padding-bottom: 10rem">
        <div v-if="activeTab === 'unread' && unreadNotifications.length === 0" class="empty-tab">
          No unread notifications
        </div>
        
        <div v-else-if="activeTab === 'read' && readNotifications.length === 0" class="empty-tab">
          No read notifications
        </div>
        
        <div v-else class="notification-list">
          <div 
            v-for="notification in activeTab === 'unread' 
              ? unreadNotifications 
              : activeTab === 'read' 
                ? readNotifications 
                : allNotifications" 
            :key="notification.id"
            :class="['notification-item', notification.read ? 'read' : 'unread']"
            @click="viewNotificationDetails(notification)"
          >
            <div class="notification-content">
              <div class="notification-header">
                <span :class="['notification-type', getTypeClass(notification.type)]">
                  <span v-if="notification.type === 'DEADLOCK_DETECTED'" class="notification-icon">⚠️</span>
                  {{ notification.type }}
                </span>
                <span :class="['notification-urgency', getUrgencyClass(notification.urgency)]">
                  {{ notification.urgency }}
                </span>
              </div>
              <p class="notification-message">{{ notification.message }}</p>
              <div class="notification-footer">
                <span class="notification-time">{{ formatDate(notification.timestamp) }}</span>
                <button 
                  v-if="hasRelatedTask(notification)" 
                  @click.stop="goToRelatedTask(notification)"
                  class="related-task-button"
                  style="position: absolute; bottom: 10px; right: 130px;">
                  View Task
                </button>
              </div>
            </div>
            <button 
              v-if="!notification.read" 
              @click.stop="markAsRead(notification)"
              class="mark-read-button"
              style="position: absolute; bottom: 10px; right: 10px;">
              Mark as Read
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Notification Details Modal -->
    <div v-if="showNotificationDetails && selectedNotification" class="modal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>Notification Details</h2>
          <button @click="closeNotificationDetails" class="close-button">&times;</button>
        </div>
        <div class="modal-body">
          <div class="notification-details">
            <div class="detail-header">
              <div class="detail-header-content">
                <span :class="['notification-type', getTypeClass(selectedNotification.type)]">
                  <span v-if="selectedNotification.type === 'DEADLOCK_DETECTED'" class="notification-icon">⚠️</span>
                  {{ selectedNotification.type }}
                </span>
                <span :class="['notification-urgency', getUrgencyClass(selectedNotification.urgency)]">
                  {{ selectedNotification.urgency }}
                </span>
                <span v-if="selectedNotification.type === 'DEADLOCK_DETECTED' || selectedNotification.userId === 0" 
                      class="system-badge">
                  System
                </span>
              </div>
              <span class="notification-taskStatus">
                {{ selectedNotification.read ? 'Read' : 'Unread' }}
              </span>
            </div>
            
            <div class="detail-section">
              <h4>Message</h4>
              <p>{{ selectedNotification.message }}</p>
            </div>
            
            <div class="detail-section meta-details">
              <div class="detail-item">
                <h4>Notification ID</h4>
                <p>{{ selectedNotification.id }}</p>
              </div>
              
              <div class="detail-item">
                <h4>Received</h4>
                <p>{{ formatDate(selectedNotification.timestamp) }}</p>
              </div>
              
              <div class="detail-item">
                <h4>User ID</h4>
                <p>{{ selectedNotification.userId }}</p>
              </div>
              
              <div class="detail-item">
                <h4>Related Task</h4>
                <p v-if="hasRelatedTask(selectedNotification)">
                  <button @click="goToRelatedTask(selectedNotification)" class="link-button">
                    View Task #{{ 
                      selectedNotification.type === 'DEADLOCK_DETECTED' && selectedNotification.payload ? 
                        (typeof selectedNotification.payload === 'string' ? 
                          JSON.parse(selectedNotification.payload).taskIds[0] : 
                          selectedNotification.payload.taskIds[0])
                        : (selectedNotification.relatedTaskId || selectedNotification.taskId) 
                    }}
                  </button>
                </p>
                <p v-else>None</p>
              </div>
            </div>
            <div class="modal-actions">
              <button @click="closeNotificationDetails" class="cancel-button">Close</button>
              <button
                v-if="!selectedNotification.read"
                @click="markAsRead(selectedNotification)"
                class="action-button mark-read">
                Mark as Read
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>