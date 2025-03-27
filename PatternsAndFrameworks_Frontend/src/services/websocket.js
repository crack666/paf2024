import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { ref, inject } from 'vue';

/**
 * WebSocket service for real-time notifications and updates.
 * 
 * IMPORTANT: The backend is transitioning from using NotificationPayload (with notificationId/taskId) 
 * to standardized NotificationDTO (with id/relatedTaskId).
 * 
 * For backward compatibility, this file contains mapping code that converts between the formats.
 * Once the backend migration is complete, all field mapping can be removed.
 */

// Get config from inject or use default
let config;
try {
  config = inject('config', {
    wsUrl: 'http://localhost:8080/api/ws'
  });
} catch (error) {
  // If not in component context, use default
  config = {
    wsUrl: 'http://localhost:8080/api/ws'
  };
}

const SOCKET_URL = window.VUE_APP_API_URL ? 
  window.VUE_APP_API_URL.replace(/\/api$/, '/api/ws') : 
  'http://localhost:8080/api/ws';

// Reactive state to be used in components
export const wsState = {
  connected: ref(false),
  notifications: ref([]),
  systemNotifications: ref([]),
  taskUpdates: ref([]),
  queueUpdates: ref([])
};

// Create a STOMP client
let stompClient = null;
let wsSubscriptions = {};

export const initializeWebSocket = (userId) => {
  if (stompClient) {
    disconnectWebSocket();
  }

  // Create WebSocket connection
  const socket = new SockJS(SOCKET_URL);
  stompClient = new Client({
    webSocketFactory: () => socket,
    debug: function(str) {
      console.log('STOMP: ' + str);
    },
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
    onConnect: () => {
      wsState.connected.value = true;
      console.log('WebSocket connection established');
      
      // Set up a heartbeat check to detect connection loss even if onDisconnect doesn't fire
      if (window.heartbeatInterval) {
        clearInterval(window.heartbeatInterval);
      }
      window.heartbeatInterval = setInterval(() => {
        if (stompClient && (!stompClient.connected || !socket.readyState)) {
          console.log('Heartbeat detected WebSocket disconnection');
          wsState.connected.value = false;
          clearInterval(window.heartbeatInterval);
          window.heartbeatInterval = null;
        }
      }, 5000);
      
      // Subscribe to system-wide notifications - these are critical for seeing deadlock and other system messages
      console.log("Subscribing to system and broadcast notification topics");
      wsSubscriptions.systemTopic = stompClient.subscribe('/topic/system', onSystemNotification);
      wsSubscriptions.broadcastTopic = stompClient.subscribe('/topic/notifications', onBroadcastNotification);
      
      // Subscribe to task updates
      wsSubscriptions.taskStatusTopic = stompClient.subscribe('/topic/tasks/status', onTaskStatusUpdate);
      wsSubscriptions.taskResultsTopic = stompClient.subscribe('/topic/tasks/results', onTaskResultUpdate);
      
      // Subscribe to queue updates
      wsSubscriptions.queuesTopic = stompClient.subscribe('/topic/queues', onQueueUpdate);
      
      // Subscribe to user-specific notifications
      if (userId) {
        wsSubscriptions.userQueue = stompClient.subscribe(`/user/queue/notifications`, onUserNotification);
        
        // Clear existing notifications before fetching new ones to prevent duplicates
        wsState.systemNotifications.value = [];
        wsState.notifications.value = [];
        
        // IMPORTANT: First subscribe to system notifications (userId=0)
        // This ensures we get all DEADLOCK_DETECTED and other system notifications
        console.log("Requesting system notifications (userId=0)");
        stompClient.publish({
          destination: '/app/notifications.subscribe',
          body: JSON.stringify({ userId: 0 })
        });
        
        // Then send message to get existing unread notifications for the user
        console.log(`Requesting user notifications for userId=${userId}`);
        stompClient.publish({
          destination: '/app/notifications.subscribe',
          body: JSON.stringify({ userId })
        });
        
        // Subscribe to user-specific task updates
        wsSubscriptions.userTasksTopic = stompClient.subscribe(`/user/${userId}/tasks`, onUserTaskUpdate);
        wsSubscriptions.userTaskResultsTopic = stompClient.subscribe(`/user/${userId}/task-results`, onUserTaskResultUpdate);
        wsSubscriptions.taskProgressTopic = stompClient.subscribe('/topic/tasks/progress', onTaskProgressUpdate);
      }
    },
    onDisconnect: () => {
      wsState.connected.value = false;
      console.log('WebSocket connection closed');
    },
    onStompError: (frame) => {
      console.error('STOMP error', frame);
    }
  });

  stompClient.activate();
  return stompClient;
};

export const disconnectWebSocket = () => {
  // Clear heartbeat interval if it exists
  if (window.heartbeatInterval) {
    clearInterval(window.heartbeatInterval);
    window.heartbeatInterval = null;
  }
  
  if (stompClient) {
    // Even attempt disconnection if not connected
    try {
      // Unsubscribe from all topics
      Object.values(wsSubscriptions).forEach(subscription => {
        if (subscription && subscription.unsubscribe) {
          subscription.unsubscribe();
        }
      });
      
      wsSubscriptions = {};
      stompClient.deactivate();
    } catch (e) {
      console.warn('Error during WebSocket disconnection:', e);
    } finally {
      // Always update state
      wsState.connected.value = false;
      console.log('WebSocket disconnected');
    }
  }
};

export const markNotificationAsRead = (notificationId, userId) => {
  // First update local state for immediate feedback
  // Find the notification in both notification arrays and update UI
  const userNotificationIndex = wsState.notifications.value.findIndex(n => n.id === notificationId);
  const userNotification = userNotificationIndex >= 0 ? wsState.notifications.value[userNotificationIndex] : null;
  if (userNotificationIndex >= 0) {
    wsState.notifications.value[userNotificationIndex].read = true;
    wsState.notifications.value[userNotificationIndex].status = 'READ';
  }
  
  const systemNotificationIndex = wsState.systemNotifications.value.findIndex(n => n.id === notificationId);
  const systemNotification = systemNotificationIndex >= 0 ? wsState.systemNotifications.value[systemNotificationIndex] : null;
  if (systemNotificationIndex >= 0) {
    wsState.systemNotifications.value[systemNotificationIndex].read = true;
    wsState.systemNotifications.value[systemNotificationIndex].status = 'READ';
  }
  
  // Get the notification object from either array
  const notification = userNotification || systemNotification;
  
  // For system notifications (userId=0), we need special handling
  const adjustedUserId = userId === 0 ? 0 : userId;
  
  console.log(`WebSocket: Marking notification ${notificationId} as read for user ${adjustedUserId}`);
  
  // Try WebSocket first if connected
  if (stompClient && stompClient.connected) {
    console.log(`Using WebSocket to mark notification ${notificationId} as read`);
    
    // IMPORTANT: WebSocket can handle any ID type, so no need to parse
    // Send through WebSocket
    stompClient.publish({
      destination: '/app/notifications.markRead',
      body: JSON.stringify({ notificationId, userId: adjustedUserId })
    });
    return true;
  } else {
    // If WebSocket is not connected, see if we can use HTTP with appropriate ID
    
    // Use the notification ID - that's what the backend expects
    let backendId = null;
    
    // Try to parse the notification ID - this should be the primary ID we use
    if (typeof notificationId === 'number' || !isNaN(parseInt(notificationId))) {
      backendId = parseInt(notificationId);
      console.log(`Using notification ID ${backendId} for HTTP request`);
    }
    
    // If we have a valid numeric ID, make HTTP request
    if (backendId !== null) {
      console.log(`WebSocket not connected, using HTTP for notification ${backendId}`);
      
      // Use fetch directly for better error handling
      fetch(`http://localhost:8080/api/notifications/${backendId}/read`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ userId: adjustedUserId }),
      })
      .then(response => {
        if (!response.ok) {
          // 404 just means the notification doesn't exist in the backend database
          if (response.status === 404) {
            console.warn(`Notification with ID ${backendId} not found in backend database.`);
            console.warn(`UI will show it as read, but backend state won't change.`);
            return { ok: true }; // Pretend it succeeded
          }
          
          throw new Error(`HTTP error ${response.status}`);
        }
        return response.json();
      })
      .then(data => {
        console.log('REST API mark as read response:', data);
      })
      .catch(error => {
        console.error('Error marking notification as read via REST:', error);
      });
      
      return true; // Return true for optimistic UI update
    } else {
      console.warn(`Cannot mark notification with non-numeric ID (${notificationId}) as read - WebSocket disconnected`);
      return true; // Still return true for UI consistency
    }
  }
};

export const broadcastNotification = (message, type = 'BROADCAST', urgency = 'NORMAL') => {
  if (stompClient && stompClient.connected) {
    stompClient.publish({
      destination: '/app/notifications.broadcast',
      body: JSON.stringify({ message, type, urgency })
    });
    return true;
  }
  return false;
};

// Notification handlers
const onSystemNotification = (message) => {
  try {
    const notificationData = JSON.parse(message.body);
    
    // Create a copy to avoid modifying the original
    const notification = { ...notificationData };
    
    // BACKWARDS COMPATIBILITY: Handle old NotificationPayload format if it comes through
    // This can be removed once the backend is fully migrated to NotificationDTO
    if (notification.notificationId !== undefined && notification.id === undefined) {
      notification.id = notification.notificationId;
      console.log(`Legacy format detected: mapped notificationId to id`);
    }
    
    if (notification.taskId !== undefined && notification.relatedTaskId === undefined) {
      notification.relatedTaskId = notification.taskId;
      console.log(`Legacy format detected: mapped taskId to relatedTaskId`);
    }
    
    // Ensure userId=0 is set for system notifications
    if (!notification.userId) {
      notification.userId = 0;
    }
    
    // Ensure timestamp exists
    if (!notification.timestamp) {
      notification.timestamp = new Date().toISOString();
    }
    
    // Make sure read status is properly set as a boolean
    if (notification.status === 'READ' || notification.status === 'ARCHIVED') {
      notification.read = true;
    } else {
      notification.read = !!notification.read; // Convert to boolean
    }
    
    // For deadlock notifications, ensure HIGH urgency
    if (notification.type === 'DEADLOCK_DETECTED' && (!notification.urgency || notification.urgency !== 'HIGH')) {
      notification.urgency = 'HIGH';
    }
    
    // Check if we already have this notification
    const existingIndex = wsState.systemNotifications.value.findIndex(n => n.id === notification.id);
    
    if (existingIndex >= 0) {
      // Update existing notification
      wsState.systemNotifications.value[existingIndex] = notification;
    } else {
      // Add new notification
      wsState.systemNotifications.value.unshift(notification);
    }
    
    // Also update the notification store directly if available
    try {
      const { useNotificationStore } = require('@/stores/notification');
      const notificationStore = useNotificationStore();
      if (notificationStore) {
        notificationStore.addWebSocketNotification(notification);
      }
    } catch (storeError) {
      // Store might not be available in this context, which is fine
    }
    
    console.log('System notification received', notification);
  } catch (error) {
    console.error('Error parsing system notification', error);
  }
};

const onBroadcastNotification = (message) => {
  try {
    const notificationData = JSON.parse(message.body);
    
    // Create a copy to avoid modifying the original
    const notification = { ...notificationData };
    
    // BACKWARDS COMPATIBILITY: Handle old NotificationPayload format if it comes through
    // This can be removed once the backend is fully migrated to NotificationDTO
    if (notification.notificationId !== undefined && notification.id === undefined) {
      notification.id = notification.notificationId;
      console.log(`Legacy format detected: mapped notificationId to id`);
    }
    
    if (notification.taskId !== undefined && notification.relatedTaskId === undefined) {
      notification.relatedTaskId = notification.taskId;
      console.log(`Legacy format detected: mapped taskId to relatedTaskId`);
    }
    
    // Ensure timestamp exists
    if (!notification.timestamp) {
      notification.timestamp = new Date().toISOString();
    }
    
    // Make sure read status is properly set as a boolean
    if (notification.status === 'READ' || notification.status === 'ARCHIVED') {
      notification.read = true;
    } else {
      notification.read = !!notification.read; // Convert to boolean
    }
    
    // Check if we already have this notification
    const existingIndex = wsState.notifications.value.findIndex(n => n.id === notification.id);
    
    if (existingIndex >= 0) {
      // Update existing notification
      wsState.notifications.value[existingIndex] = notification;
    } else {
      // Add new notification
      wsState.notifications.value.unshift(notification);
    }
    
    // Also update the notification store directly if available
    try {
      const { useNotificationStore } = require('@/stores/notification');
      const notificationStore = useNotificationStore();
      if (notificationStore) {
        notificationStore.addWebSocketNotification(notification);
      }
    } catch (storeError) {
      // Store might not be available in this context, which is fine
    }
    
    console.log('Broadcast notification received', notification);
  } catch (error) {
    console.error('Error parsing broadcast notification', error);
  }
};

const onUserNotification = (message) => {
  try {
    const notificationData = JSON.parse(message.body);
    let updatedNotifications = [];
    
    // Also update the notification store if available
    let notificationStore = null;
    try {
      const { useNotificationStore } = require('@/stores/notification');
      notificationStore = useNotificationStore();
    } catch (storeError) {
      // Store might not be available in this context, which is fine
    }
    
    if (Array.isArray(notificationData)) {
      // Handle initial notifications list
      notificationData.forEach(itemData => {
        // Create a copy to avoid modifying the original
        const item = { ...itemData };
        
        // BACKWARDS COMPATIBILITY: Handle old NotificationPayload format if it comes through
        // This can be removed once the backend is fully migrated to NotificationDTO
        if (item.notificationId !== undefined && item.id === undefined) {
          item.id = item.notificationId;
          console.log(`Legacy format detected: mapped notificationId to id`);
        }
        
        if (item.taskId !== undefined && item.relatedTaskId === undefined) {
          item.relatedTaskId = item.taskId;
          console.log(`Legacy format detected: mapped taskId to relatedTaskId`);
        }
        
        // Ensure timestamps exist
        if (!item.timestamp) {
          item.timestamp = new Date().toISOString();
        }
        
        // Make sure read status is properly set as a boolean
        if (item.status === 'READ' || item.status === 'ARCHIVED') {
          item.read = true;
        } else {
          item.read = !!item.read; // Convert to boolean
        }
        
        // Keep track of processed notifications
        updatedNotifications.push(item);
        
        // Add to notification store directly if available
        if (notificationStore) {
          notificationStore.addWebSocketNotification(item);
        }
        
        // Add ALL notifications to the main notifications array
        const existingIndex = wsState.notifications.value.findIndex(n => n.id === item.id);
        if (existingIndex >= 0) {
          // Update existing notification
          wsState.notifications.value[existingIndex] = item;
        } else {
          // Add new notification
          wsState.notifications.value.unshift(item);
        }
        
        // Also add to system notifications array if it's a system notification
        if (item.userId === 0 || item.type === 'SYSTEM' || item.type === 'DEADLOCK_DETECTED') {
          const systemExistingIndex = wsState.systemNotifications.value.findIndex(n => n.id === item.id);
          if (systemExistingIndex >= 0) {
            // Update existing system notification
            wsState.systemNotifications.value[systemExistingIndex] = item;
          } else {
            // Add new system notification
            wsState.systemNotifications.value.unshift(item);
          }
        }
      });
      console.log('User notifications received', updatedNotifications.length);
    } else {
      // Handle single notification
      // Create a copy to avoid modifying the original
      const singleNotification = { ...notificationData };
      
      // BACKWARDS COMPATIBILITY: Handle old NotificationPayload format if it comes through
      // This can be removed once the backend is fully migrated to NotificationDTO
      if (singleNotification.notificationId !== undefined && singleNotification.id === undefined) {
        singleNotification.id = singleNotification.notificationId;
        console.log(`Legacy format detected: mapped notificationId to id`);
      }
      
      if (singleNotification.taskId !== undefined && singleNotification.relatedTaskId === undefined) {
        singleNotification.relatedTaskId = singleNotification.taskId;
        console.log(`Legacy format detected: mapped taskId to relatedTaskId`);
      }
      
      // Ensure timestamp exists
      if (!singleNotification.timestamp) {
        singleNotification.timestamp = new Date().toISOString();
      }
      
      // Make sure read status is properly set as a boolean
      if (singleNotification.status === 'READ' || singleNotification.status === 'ARCHIVED') {
        singleNotification.read = true;
      } else {
        singleNotification.read = !!singleNotification.read; // Convert to boolean
      }
      
      // Add to notification store directly if available
      if (notificationStore) {
        notificationStore.addWebSocketNotification(singleNotification);
      }
      
            // IMPORTANT: Add ALL notifications to both arrays to ensure visibility
      // First add to primary notifications array
      const existingIndex = wsState.notifications.value.findIndex(n => n.id === singleNotification.id);
      if (existingIndex >= 0) {
        // Update existing notification
        wsState.notifications.value[existingIndex] = singleNotification;
      } else {
        // Add new notification
        wsState.notifications.value.unshift(singleNotification);
      }
      
      // Also add to system notifications array if it's a system notification
      if (singleNotification.userId === 0 || singleNotification.type === 'SYSTEM' || singleNotification.type === 'DEADLOCK_DETECTED') {
        const systemExistingIndex = wsState.systemNotifications.value.findIndex(n => n.id === singleNotification.id);
        if (systemExistingIndex >= 0) {
          // Update existing system notification
          wsState.systemNotifications.value[systemExistingIndex] = singleNotification;
        } else {
          // Add new system notification
          wsState.systemNotifications.value.unshift(singleNotification);
        }
      }
      
      console.log('User notification received', singleNotification);
    }
  } catch (error) {
    console.error('Error parsing user notification', error);
  }
};

// Task and Queue update handlers
const onTaskStatusUpdate = (message) => {
  try {
    const taskUpdate = JSON.parse(message.body);
    wsState.taskUpdates.value.unshift({
      type: 'STATUS',
      timestamp: new Date(),
      data: taskUpdate
    });
    console.log('Task status update received', taskUpdate);
    
    // Trigger a custom event that components can listen to
    document.dispatchEvent(new CustomEvent('task-status-update', { 
      detail: taskUpdate 
    }));
  } catch (error) {
    console.error('Error parsing task status update', error);
  }
};

const onTaskResultUpdate = (message) => {
  try {
    const resultUpdate = JSON.parse(message.body);
    wsState.taskUpdates.value.unshift({
      type: 'RESULT',
      timestamp: new Date(),
      data: resultUpdate
    });
    console.log('Task result update received', resultUpdate);
    
    // Trigger a custom event that components can listen to
    document.dispatchEvent(new CustomEvent('task-result-update', { 
      detail: resultUpdate 
    }));
  } catch (error) {
    console.error('Error parsing task result update', error);
  }
};

const onQueueUpdate = (message) => {
  try {
    const queueUpdate = JSON.parse(message.body);
    wsState.queueUpdates.value.unshift({
      timestamp: new Date(),
      data: queueUpdate
    });
    console.log('Queue update received', queueUpdate);
    
    // Trigger a custom event that components can listen to
    document.dispatchEvent(new CustomEvent('queue-update', { 
      detail: queueUpdate 
    }));
  } catch (error) {
    console.error('Error parsing queue update', error);
  }
};

const onUserTaskUpdate = (message) => {
  try {
    const taskUpdate = JSON.parse(message.body);
    console.log('User task update received', taskUpdate);
    
    // Trigger a custom event that components can listen to
    document.dispatchEvent(new CustomEvent('user-task-update', { 
      detail: taskUpdate 
    }));
  } catch (error) {
    console.error('Error parsing user task update', error);
  }
};

const onUserTaskResultUpdate = (message) => {
  try {
    const resultUpdate = JSON.parse(message.body);
    console.log('User task result update received', resultUpdate);
    
    // Trigger a custom event that components can listen to
    document.dispatchEvent(new CustomEvent('user-task-result-update', { 
      detail: resultUpdate 
    }));
  } catch (error) {
    console.error('Error parsing user task result update', error);
  }
};

const onTaskProgressUpdate = (message) => {
  try {
    const progressUpdate = JSON.parse(message.body);
    // Aktualisiere den Zustand; z.B. könntest Du den Fortschritt in wsState.taskProgress speichern
    console.log('Task progress update received', progressUpdate);
    // Trigger ggf. ein CustomEvent, falls Komponenten darauf hören sollen:
    document.dispatchEvent(new CustomEvent('task-progress-update', { detail: progressUpdate }));
  } catch (error) {
    console.error('Error parsing task progress update', error);
  }
};

// Function to check actual connection status
export const checkConnectionStatus = () => {
  const isConnected = stompClient && stompClient.connected && stompClient.webSocket && stompClient.webSocket.readyState === 1;
  
  // If our state doesn't match reality, update it
  if (wsState.connected.value !== isConnected) {
    console.log(`Fixing incorrect connection status: was ${wsState.connected.value}, actual ${isConnected}`);
    wsState.connected.value = isConnected;
  }
  
  return isConnected;
};

export default {
  initializeWebSocket,
  disconnectWebSocket,
  markNotificationAsRead,
  broadcastNotification,
  checkConnectionStatus,
  wsState
};