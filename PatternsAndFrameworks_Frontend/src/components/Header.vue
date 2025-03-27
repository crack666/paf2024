<script setup>
import Navigation from "./Navigation.vue";
import { ref, computed, onMounted, onBeforeUnmount } from "vue";
import { wsState, checkConnectionStatus } from '@/services/websocket';

// Show connection status with heartbeat check
const isWebSocketConnected = computed(() => wsState.connected.value);

// Add regular connection check
let connectionCheckInterval;
onMounted(() => {
  // Run an initial connection check
  checkConnectionStatus();

  // Set up regular connection checking to ensure UI is accurate
  connectionCheckInterval = setInterval(() => {
    checkConnectionStatus();
  }, 3000);
});

// Clean up interval when component is destroyed
onBeforeUnmount(() => {
  if (connectionCheckInterval) {
    clearInterval(connectionCheckInterval);
  }
});

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
  <header class="header">
    <router-link to="../" class="logo-and-title">
      <img src="@/assets/images/silver_bullet.svg" alt="Silver Bullet Logo" class="logo" style="height: 50px; width: auto;"/>
      <h2 class="header-title">Silver Bullet</h2>
      <div
          :class="['connection-status', isWebSocketConnected ? 'connected' : 'disconnected']"
          :title="isWebSocketConnected ? 'Real-time notifications active' : 'Connection lost - refresh to reconnect'"
      >
        <span class="connection-indicator"></span>
        WebSocket {{ isWebSocketConnected ? 'Connected' : 'Disconnected' }}
      </div>
    </router-link>
    <Navigation />
  </header>
</template>
