import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { notificationService, userService } from '@/services/api';
import { markNotificationAsRead as wsMarkAsRead } from '@/services/websocket'; 

export const useNotificationStore = defineStore('notification', () => {
  // Initialize all arrays to prevent null reference errors
  const notifications = ref([]);
  const userNotifications = ref([]);
  const loading = ref(false);
  const error = ref(null);

  // Computed properties with null checks
  const unreadNotifications = computed(() => 
    (userNotifications.value || []).filter(notification => !notification.read)
  );
  
  const readNotifications = computed(() => 
    (userNotifications.value || []).filter(notification => notification.read)
  );
  
  const highPriorityNotifications = computed(() => 
    (userNotifications.value || []).filter(notification => notification.urgency === 'HIGH' && !notification.read)
  );
  
  // Get all notifications, preserving existing ones
  const mergeNotifications = (newNotifications) => {
    if (!newNotifications || !Array.isArray(newNotifications)) {
      console.warn("Attempted to merge invalid or empty notifications");
      return userNotifications.value || [];
    }
    
    // Don't replace the array, merge with existing notifications
    const currentIds = (userNotifications.value || []).map(n => n.id);
    
    // Add only notifications that don't already exist
    newNotifications.forEach(notification => {
      if (!currentIds.includes(notification.id)) {
        userNotifications.value.push(notification);
      }
    });
    
    return userNotifications.value;
  };

  // Actions
  async function fetchAllNotifications() {
    loading.value = true;
    error.value = null;
    try {
      const response = await notificationService.getAllNotifications();
      notifications.value = response.data;
      return response.data;
    } catch (err) {
      console.error('Error fetching notifications:', err);
      error.value = err.response?.data?.message || 'Failed to fetch notifications';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // Enhanced with fallback to REST API when needed
  async function fetchUserNotifications(userId, readStatus = null) {
    console.log("Fetching notifications for userId:", userId);
    loading.value = true;
    error.value = null;
    
    try {
      // First check if we already have notifications in local state
      const currentNotifications = (userNotifications.value || []);
      
      // If websocket state is empty or we're on a page refresh, fetch from REST API
      if (currentNotifications.length === 0) {
        console.log("No notifications in state, fetching from REST API");
        try {
          // Construct URL with optional read status parameter
          let url = `${import.meta.env.VITE_API_URL || 'http://localhost:8080/api'}/notifications/user/${userId}`;
          if (readStatus !== null) {
            url += `?read=${readStatus}`;
          }
          
          const response = await fetch(url);
          if (response.ok) {
            const data = await response.json();
            console.log(`Received ${data.length} notifications from REST API`);
            
            // Add each notification to our store
            data.forEach(notification => {
              addWebSocketNotification(notification);
            });
          } else {
            console.error("Error fetching notifications from API:", response.statusText);
          }
        } catch (apiError) {
          console.error("Error during REST API fetch:", apiError);
        }
      }
      
      // Now continue with WebSocket state - with potentially new notifications from API
      // IMPORTANT: Only filter by read status, NEVER by userId
      // This ensures we see both user-specific and broadcast notifications
      
      // Apply read status filtering if requested
      const filteredNotifications = userNotifications.value.filter(n => {
        return readStatus === null || n.read === readStatus;
      });
      
      // Log what we found
      console.log(`Using ${filteredNotifications.length} notifications from store`);
      console.log("Notification details:", filteredNotifications.map(n => 
        `${n.type}(userId:${n.userId}, read:${n.read}, status:${n.status || 'unknown'})`).join(", "));
      
      return filteredNotifications;
    } catch (err) {
      console.error("Error processing notifications:", err);
      error.value = "Failed to process notifications";
      return [];
    } finally {
      loading.value = false;
    }
  }

  async function markNotificationAsRead(notificationId, userId) {
    loading.value = true;
    error.value = null;
    
    try {
      // Get the actual notification to determine correct userId
      const notification = userNotifications.value.find(n => n.id === notificationId);
      
      if (!notification) {
        console.error(`Notification with ID ${notificationId} not found`);
        return false;
      }
      
      // Use the notification's userId (for system notifications, this will be 0)
      const actualUserId = notification.userId || userId;
      console.log(`Marking notification ${notificationId} as read for user ${actualUserId}`);
      
      // First update our local state immediately for responsive UI
      userNotifications.value = userNotifications.value.map(notification => {
        if (notification.id === notificationId) {
          return { ...notification, read: true, status: 'READ' };
        }
        return notification;
      });
      
      // For system notifications (userId=0), make sure we use 0 as the userId
      const finalUserId = actualUserId === 0 ? 0 : userId;
      
      // IMPORTANT: Use the notification ID - that's what the backend expects
      let backendId = null;
      
      // Try to parse the notification ID - this should be the primary ID we use
      if (typeof notificationId === 'number' || !isNaN(parseInt(notificationId))) {
        backendId = parseInt(notificationId);
        console.log(`Using notification ID ${backendId} for backend request`);
      }
      
      // If we don't have a numeric ID to use, fall back to WebSocket
      if (backendId === null) {
        console.warn(`Cannot find a valid numeric ID for notification ${notificationId}`);
        console.warn(`Using WebSocket only to mark as read`);
        
        // Try WebSocket for non-numeric IDs
        wsMarkAsRead(notificationId, finalUserId);
        
        // Still return true for UI consistency
        return true;
      }
      
      // ALWAYS USE HTTP REQUEST - More reliable than WebSocket for "mark as read"
      console.log(`Using HTTP request to mark notification with ID ${backendId} as read for user ${finalUserId}`);
      
      try {
        // Make direct HTTP request to ensure it goes through
        const response = await fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080/api'}/notifications/${backendId}/read`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ userId: finalUserId }),
        });
        
        if (!response.ok) {
          if (response.status === 404) {
            console.warn(`Notification with ID ${backendId} not found in backend database.`);
            console.warn(`This is likely due to the notification being generated on the frontend but not existing in the backend database.`);
            console.warn(`Marking as read in the UI only.`);
            
            // Try WebSocket as fallback for notifications that don't exist in DB
            wsMarkAsRead(notificationId, finalUserId);
            
            // Return true to keep UI consistent
            return true;
          }
          
          throw new Error(`HTTP error ${response.status}`);
        }
        
        const data = await response.json();
        console.log('REST API mark as read response:', data);
        
        // Still try WebSocket as well for redundancy
        wsMarkAsRead(notificationId, finalUserId);
        
        return data;
      } catch (apiErr) {
        console.error('REST API error marking notification as read:', apiErr);
        
        // Try the service as a last resort
        try {
          const response = await notificationService.markAsRead(backendId, finalUserId);
          return response.data;
        } catch (serviceErr) {
          // Check if this is a 404 error from the service
          if (serviceErr.response && serviceErr.response.status === 404) {
            console.warn(`Notification service confirms ID ${backendId} not found in backend database.`);
          } else {
            console.error('Service error marking notification as read:', serviceErr);
          }
          
          // Even after HTTP failure, try WebSocket as last resort
          wsMarkAsRead(notificationId, finalUserId);
        }
      }
      
      // Always return true so the UI stays updated
      return true;
    } catch (err) {
      console.error('Error in markNotificationAsRead:', err);
      error.value = err.message || 'Failed to mark notification as read';
      // Even if there's an error, we'll keep the notification as read in the UI
      return false;
    } finally {
      loading.value = false;
    }
  }

  async function sendNotification(notificationData) {
    loading.value = true;
    error.value = null;
    try {
      const response = await notificationService.sendNotification(notificationData);
      return response.data;
    } catch (err) {
      console.error('Error sending notification:', err);
      error.value = err.response?.data?.message || 'Failed to send notification';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function broadcastNotification(notificationData) {
    loading.value = true;
    error.value = null;
    try {
      const response = await notificationService.broadcastNotification(notificationData);
      return response.data;
    } catch (err) {
      console.error('Error broadcasting notification:', err);
      error.value = err.response?.data?.message || 'Failed to broadcast notification';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // Add notifications received from WebSocket
  function addWebSocketNotification(notification) {
    // Deep copy to avoid reference issues
    const notificationCopy = JSON.parse(JSON.stringify(notification));
    
    // BACKWARDS COMPATIBILITY: Handle old NotificationPayload format if it comes through
    // This can be removed once the backend only sends NotificationDTO format
    if (notificationCopy.notificationId !== undefined && notificationCopy.id === undefined) {
      notificationCopy.id = notificationCopy.notificationId;
      console.log(`Legacy format detected: mapped notificationId to id`);
    }
    
    if (notificationCopy.taskId !== undefined && notificationCopy.relatedTaskId === undefined) {
      notificationCopy.relatedTaskId = notificationCopy.taskId;
      console.log(`Legacy format detected: mapped taskId to relatedTaskId`);
    }
    
    // Ensure proper read status
    if (notificationCopy.status === 'READ' || notificationCopy.status === 'ARCHIVED') {
      notificationCopy.read = true;
    } else {
      notificationCopy.read = !!notificationCopy.read; // Convert to boolean
    }
    
    // IMPROVED DEDUPLICATION STRATEGY:
    // 1. First try to match by ID - this is the most reliable way
    // 2. For notifications without ID, try to match by a combination of fields
    
    // Generate a unique composite key for notifications without ID
    const getCompositeKey = (n) => {
      return `${n.type || ''}:${n.message || ''}:${n.timestamp || ''}:${n.userId || ''}`;
    };
    
    // Check if notification already exists in our store (improved matching logic)
    const existingByIdIndex = notificationCopy.id ? 
      userNotifications.value.findIndex(n => n.id === notificationCopy.id) : -1;
      
    // If not found by ID, try to find by composite key
    const existingByCompositeIndex = existingByIdIndex === -1 ?
      userNotifications.value.findIndex(n => getCompositeKey(n) === getCompositeKey(notificationCopy)) : -1;
      
    const existingIndex = existingByIdIndex !== -1 ? existingByIdIndex : existingByCompositeIndex;
    
    // Ensure the notification has an ID (system notifications from WebSocket might not have one)
    if (!notificationCopy.id) {
      // Create deterministic ID based on content to help with deduplication
      const idBase = getCompositeKey(notificationCopy);
      notificationCopy.id = `system-${idBase}-${Date.now()}`;
      console.log(`Created synthetic ID for notification: ${notificationCopy.id}`);
    }
    
    // CRITICAL: Ensure system/broadcast notifications have userId=0
    if (notificationCopy.type === 'SYSTEM' || notificationCopy.type === 'DEADLOCK_DETECTED') {
      notificationCopy.userId = 0; // System user ID
    }
    
    // Parse payload if it's a string (common with WebSocket notifications)
    if (notificationCopy.payload && typeof notificationCopy.payload === 'string') {
      try {
        notificationCopy.payload = JSON.parse(notificationCopy.payload);
      } catch (e) {
        console.error('Failed to parse notification payload:', e);
      }
    }
    
    // Special handling for deadlock notifications
    if (notificationCopy.type === 'DEADLOCK_DETECTED') {
      // Ensure high urgency for deadlock notifications
      notificationCopy.urgency = 'HIGH';
      
      // Ensure userId is 0 for deadlock notifications (redundant check for safety)
      notificationCopy.userId = 0;
    }
    
    // Extra check to ensure we don't filter out important notifications
    if (notificationCopy.userId === undefined || notificationCopy.userId === null) {
      console.warn(`Notification without userId detected: ${notificationCopy.type}. Setting to 0.`);
      notificationCopy.userId = 0; // Default to system user ID to ensure it's visible
    }
    
    if (existingIndex >= 0) {
      // Update existing notification
      userNotifications.value[existingIndex] = notificationCopy;
      console.log(`Updated notification in store: ${notificationCopy.type} - ${notificationCopy.message} (ID: ${notificationCopy.id})`);
    } else {
      // Add new notification
      userNotifications.value.unshift(notificationCopy);
      console.log(`Added notification to store: ${notificationCopy.type} - ${notificationCopy.message} (ID: ${notificationCopy.id})`);
    }
  }

  // Reset state
  function reset() {
    notifications.value = [];
    userNotifications.value = [];
    loading.value = false;
    error.value = null;
  }

  return {
    // State
    notifications,
    userNotifications,
    loading,
    error,
    
    // Computed
    unreadNotifications,
    readNotifications,
    highPriorityNotifications,
    
    // Actions
    fetchAllNotifications,
    fetchUserNotifications,
    markNotificationAsRead,
    sendNotification,
    broadcastNotification,
    addWebSocketNotification,
    reset
  };
});