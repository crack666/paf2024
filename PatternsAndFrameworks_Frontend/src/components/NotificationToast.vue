<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue';
import { useNotificationStore } from '@/stores/notification';
import { useRouter } from 'vue-router';

const props = defineProps({
  notification: {
    type: Object,
    required: true
  },
  duration: {
    type: Number,
    default: 5000 // 5 seconds
  },
  autoClose: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['close']);

const visible = ref(true);
const progress = ref(100);
const notificationStore = useNotificationStore();
const router = useRouter();
let progressInterval = null;
let autoCloseTimeout = null;

// Computed properties
const urgencyClass = computed(() => {
  switch (props.notification.urgency?.toUpperCase()) {
    case 'HIGH': return 'urgency-high';
    case 'NORMAL': return 'urgency-normal';
    case 'LOW': return 'urgency-low';
    default: return 'urgency-normal';
  }
});

const typeClass = computed(() => {
  switch (props.notification.type?.toUpperCase()) {
    case 'TASK_CREATED': return 'type-created';
    case 'TASK_STARTED': return 'type-started';
    case 'TASK_COMPLETED': return 'type-completed';
    case 'TASK_ERROR': return 'type-error';
    case 'TASK_OVERDUE': return 'type-overdue';
    case 'DEADLOCK_DETECTED': return 'type-error'; // Use error styling for deadlock
    case 'SYSTEM': return 'type-system';
    default: return 'type-default';
  }
});

const hasRelatedTask = computed(() => {
  // Check for deadlock notifications with task IDs in payload
  if (props.notification.type === 'DEADLOCK_DETECTED' && props.notification.payload) {
    try {
      const payload = typeof props.notification.payload === 'string' 
        ? JSON.parse(props.notification.payload) 
        : props.notification.payload;
      return payload.taskIds && payload.taskIds.length > 0;
    } catch (e) {
      console.error('Error parsing deadlock payload', e);
    }
  }
  
  // Regular check for relatedTaskId or taskId
  return (props.notification.relatedTaskId !== null && props.notification.relatedTaskId !== undefined) ||
         (props.notification.taskId !== null && props.notification.taskId !== undefined);
});

// Format date for display
function formatDate(dateString) {
  if (!dateString) return '';
  return new Date(dateString).toLocaleString();
}

function close() {
  visible.value = false;
  clearInterval(progressInterval);
  clearTimeout(autoCloseTimeout);
  emit('close');
}

function markAsRead() {
  if (props.notification.userId && !props.notification.read) {
    notificationStore.markNotificationAsRead(props.notification.id, props.notification.userId);
  }
  close();
}

function viewDetails() {
  if (props.notification.type === 'DEADLOCK_DETECTED') {
    // Handle deadlock notifications
    if (props.notification.payload) {
      try {
        // Try to parse payload if it's a string
        const payload = typeof props.notification.payload === 'string' 
          ? JSON.parse(props.notification.payload) 
          : props.notification.payload;
          
        // Navigate to first task in deadlock chain
        if (payload.taskIds && payload.taskIds.length > 0) {
          router.push(`/tasks/${payload.taskIds[0]}`);
          markAsRead();
          return;
        }
      } catch (e) {
        console.error('Error parsing deadlock payload', e);
      }
    }
    
    // Fallback to taskId if payload parsing fails
    if (props.notification.taskId) {
      router.push(`/tasks/${props.notification.taskId}`);
    } else {
      router.push('/notifications');
    }
  } else if (props.notification.relatedTaskId) {
    // Standard related task navigation
    router.push(`/tasks/${props.notification.relatedTaskId}`);
  } else if (props.notification.taskId) {
    // Alternative task ID field
    router.push(`/tasks/${props.notification.taskId}`);
  } else {
    // Default to notifications page
    router.push('/notifications');
  }
  markAsRead();
}

onMounted(() => {
  // Start the progress timer for auto-close
  if (props.autoClose) {
    const interval = props.duration / 100;
    progressInterval = setInterval(() => {
      progress.value -= 1;
      if (progress.value <= 0) {
        close();
      }
    }, interval);
    
    autoCloseTimeout = setTimeout(() => {
      close();
    }, props.duration);
  }
});

// Clear timers on component cleanup
onBeforeUnmount(() => {
  clearInterval(progressInterval);
  clearTimeout(autoCloseTimeout);
});

// Pause timers when hovering
function pauseTimer() {
  clearInterval(progressInterval);
  clearTimeout(autoCloseTimeout);
}

// Resume timers when leaving
function resumeTimer() {
  if (props.autoClose && visible.value) {
    const remainingTime = (progress.value / 100) * props.duration;
    
    const interval = remainingTime / progress.value;
    progressInterval = setInterval(() => {
      progress.value -= 1;
      if (progress.value <= 0) {
        close();
      }
    }, interval);
    
    autoCloseTimeout = setTimeout(() => {
      close();
    }, remainingTime);
  }
}
</script>

<template>
  <transition name="toast">
    <div 
      v-if="visible" 
      class="notification-toast" 
      :class="[typeClass, urgencyClass]"
      @mouseenter="pauseTimer"
      @mouseleave="resumeTimer"
    >
      <div class="toast-content">
        <div class="toast-header">
          <span class="toast-type">{{ notification.type }}</span>
          <span class="toast-urgency">{{ notification.urgency }}</span>
          <button class="toast-close" @click="close">&times;</button>
        </div>
        
        <div class="toast-body">
          <p class="toast-message">{{ notification.message }}</p>
          <div class="toast-meta">
            <span class="toast-time">{{ formatDate(notification.timestamp) }}</span>
          </div>
        </div>
        
        <div class="toast-actions">
          <button v-if="hasRelatedTask" class="toast-btn view" @click="viewDetails">
            View Task
          </button>
          <button v-else class="toast-btn view" @click="viewDetails">
            Details
          </button>
          <button class="toast-btn dismiss" @click="markAsRead">
            Dismiss
          </button>
        </div>
      </div>
      
      <div v-if="autoClose" class="toast-progress-container">
        <div class="toast-progress" :style="{ width: `${progress}%` }"></div>
      </div>
    </div>
  </transition>
</template>