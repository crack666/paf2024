import { initializeWebSocket, disconnectWebSocket, wsState, checkConnectionStatus } from './websocket';

/**
 * WebSocket Reconnection Service
 * 
 * This service provides functionality to automatically reconnect WebSocket connections
 * when the backend becomes available again, improving application resilience.
 */

// Configuration for reconnection behavior
const RECONNECT_CONFIG = {
  // Initial reconnection delay in milliseconds
  initialDelay: 1000,
  // Maximum reconnection delay in milliseconds (capped to prevent excessive delays)
  maxDelay: 30000,
  // Backoff multiplier - each failed reconnection attempt will increase the delay by this factor
  backoffMultiplier: 1.5,
  // Maximum number of reconnection attempts (0 = infinite attempts)
  maxAttempts: 0
};

// State tracking for reconnection
const reconnectState = {
  // Current reconnection attempt counter
  attempts: 0,
  // Current delay before next reconnection attempt
  currentDelay: RECONNECT_CONFIG.initialDelay,
  // Timer reference for reconnection attempts
  timer: null,
  // Flag indicating if reconnection is active
  isReconnecting: false,
  // Current user ID to restore WebSocket session
  userId: null
};

/**
 * Starts the reconnection process with exponential backoff
 * @param {number} userId The user ID to reconnect with
 */
export const startReconnection = (userId) => {
  // Save the userId for reconnection
  reconnectState.userId = userId;
  
  // If already reconnecting, don't start again
  if (reconnectState.isReconnecting) return;
  
  reconnectState.isReconnecting = true;
  reconnectState.attempts = 0;
  reconnectState.currentDelay = RECONNECT_CONFIG.initialDelay;
  
  // Make sure WebSocket is marked as disconnected
  wsState.connected.value = false;
  
  // Update reactive state for UI
  wsState.reconnecting.value = true;
  wsState.reconnectAttempt.value = 0;
  wsState.nextReconnectIn.value = Math.round(RECONNECT_CONFIG.initialDelay / 1000);
  
  // Schedule the first reconnection attempt
  scheduleReconnection();
  
  console.log(`WebSocket reconnection started with initial delay of ${reconnectState.currentDelay}ms`);
};

/**
 * Schedules the next reconnection attempt
 */
const scheduleReconnection = () => {
  // Clear any existing timer
  if (reconnectState.timer) {
    clearTimeout(reconnectState.timer);
  }
  
  // Check if we've exceeded max attempts (if max is not 0/unlimited)
  if (RECONNECT_CONFIG.maxAttempts > 0 && reconnectState.attempts >= RECONNECT_CONFIG.maxAttempts) {
    console.log(`Maximum reconnection attempts (${RECONNECT_CONFIG.maxAttempts}) reached. Giving up.`);
    stopReconnection();
    return;
  }
  
  // Update UI state
  wsState.reconnecting.value = true;
  wsState.reconnectAttempt.value = reconnectState.attempts;
  wsState.nextReconnectIn.value = Math.round(reconnectState.currentDelay / 1000);
  
  // Schedule the next attempt
  reconnectState.timer = setTimeout(attemptReconnection, reconnectState.currentDelay);
  
  console.log(`Next reconnection attempt in ${reconnectState.currentDelay}ms (attempt ${reconnectState.attempts + 1}${
    RECONNECT_CONFIG.maxAttempts > 0 ? ` of ${RECONNECT_CONFIG.maxAttempts}` : ''
  })`);
  
  // Set up countdown timer update
  const startTime = Date.now();
  const countdownInterval = setInterval(() => {
    if (!reconnectState.isReconnecting) {
      clearInterval(countdownInterval);
      return;
    }
    
    const elapsed = Date.now() - startTime;
    const remaining = Math.max(0, Math.round((reconnectState.currentDelay - elapsed) / 1000));
    wsState.nextReconnectIn.value = remaining;
    
    if (remaining <= 0) {
      clearInterval(countdownInterval);
    }
  }, 1000);
};

/**
 * Attempts to reconnect to the WebSocket server
 */
const attemptReconnection = async () => {
  reconnectState.attempts++;
  
  console.log(`Attempting WebSocket reconnection (attempt ${reconnectState.attempts})`);
  
  try {
    // First check if the server is reachable with a lightweight request
    const serverAvailable = await checkServerAvailability();
    
    if (serverAvailable) {
      // Try to reconnect WebSocket
      initializeWebSocket(reconnectState.userId);
      
      // Set up a check to verify the connection worked
      setTimeout(() => {
        const isConnected = checkConnectionStatus();
        
        if (isConnected) {
          // Successful reconnection
          console.log('WebSocket reconnection successful!');
          resetReconnection();
        } else {
          // Connection failed despite server being available
          console.log('WebSocket reconnection failed despite server availability.');
          increaseBackoff();
          scheduleReconnection();
        }
      }, 2000); // Give a couple seconds for connection to establish
    } else {
      // Server not available, try again later
      console.log('Server not available, continuing reconnection attempts.');
      increaseBackoff();
      scheduleReconnection();
    }
  } catch (error) {
    console.error('Error during reconnection attempt:', error);
    increaseBackoff();
    scheduleReconnection();
  }
};

/**
 * Increases the backoff delay for the next reconnection attempt
 */
const increaseBackoff = () => {
  // Increase delay with exponential backoff
  reconnectState.currentDelay = Math.min(
    reconnectState.currentDelay * RECONNECT_CONFIG.backoffMultiplier,
    RECONNECT_CONFIG.maxDelay
  );
};

/**
 * Resets the reconnection state
 */
const resetReconnection = () => {
  reconnectState.isReconnecting = false;
  reconnectState.attempts = 0;
  reconnectState.currentDelay = RECONNECT_CONFIG.initialDelay;
  
  // Update UI state
  wsState.reconnecting.value = false;
  wsState.reconnectAttempt.value = 0;
  wsState.nextReconnectIn.value = 0;
  
  if (reconnectState.timer) {
    clearTimeout(reconnectState.timer);
    reconnectState.timer = null;
  }
  
  // Emit an event when reconnection is complete
  if (typeof document !== 'undefined') {
    document.dispatchEvent(new CustomEvent('websocket-reconnected', { 
      detail: { userId: reconnectState.userId } 
    }));
  }
};

/**
 * Stops the reconnection process
 */
export const stopReconnection = () => {
  reconnectState.isReconnecting = false;
  
  // Update UI state
  wsState.reconnecting.value = false;
  wsState.reconnectAttempt.value = 0;
  wsState.nextReconnectIn.value = 0;
  
  if (reconnectState.timer) {
    clearTimeout(reconnectState.timer);
    reconnectState.timer = null;
  }
  
  console.log('WebSocket reconnection process stopped.');
};

/**
 * Checks if the server is available using a lightweight ping request
 * @returns {Promise<boolean>} True if server is reachable, false otherwise
 */
const checkServerAvailability = async () => {
  try {
    // Get the base URL from the WebSocket URL
    const wsUrl = window.VUE_APP_API_URL || 'http://localhost:8080/api';
    const baseUrl = wsUrl.replace('/ws', '').replace('/api', '/api/health');
    
    // Make a HEAD request to the server health endpoint (or any lightweight endpoint)
    const response = await fetch(`${baseUrl}`, {
      method: 'HEAD',
      // Short timeout to avoid long waits
      signal: AbortSignal.timeout(3000)
    });
    
    return response.ok;
  } catch (error) {
    console.log('Server availability check failed:', error.message);
    return false;
  }
};

/**
 * Returns the current reconnection state (for UI display)
 */
export const getReconnectionState = () => ({
  isReconnecting: reconnectState.isReconnecting,
  attempts: reconnectState.attempts,
  nextAttemptIn: reconnectState.isReconnecting ? Math.round(reconnectState.currentDelay / 1000) : 0
});

export default {
  startReconnection,
  stopReconnection,
  getReconnectionState
};