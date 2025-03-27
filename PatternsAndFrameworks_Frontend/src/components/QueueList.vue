<script setup>
import { ref, onMounted, onUnmounted, computed, defineProps, defineEmits } from 'vue';
import { useQueueStore } from '@/stores/queue';
import { useTaskStore } from '@/stores/task';

const props = defineProps({
  limit: {
    type: Number,
    default: null
  },
  showCreateButton: {
    type: Boolean,
    default: true
  },
  compact: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['view-queue', 'add-task', 'process-queue', 'create-queue']);

const queueStore = useQueueStore();
const taskStore = useTaskStore();

const isLoading = ref(false);
const error = ref(null);
const refreshTimer = ref(null);

// Status Icon Map
const statusIcons = {
  'CREATED': '📝',
  'QUEUED': '⏱️',
  'RUNNING': '⚙️',
  'DONE': '✅',
  'ERROR': '❌'
};

// Computed properties
const displayedQueues = computed(() => {
  if (props.limit && queueStore.queues.length > props.limit) {
    return queueStore.queues.slice(0, props.limit);
  }
  return queueStore.queues;
});

// Load detailed queue information for each queue
async function loadQueueDetails() {
  isLoading.value = true;
  try {
    // Fetch detailed information for each queue in parallel
    const promises = displayedQueues.value.map(queue => 
      queueStore.fetchQueueById(queue.id));
    await Promise.all(promises);
  } catch (err) {
    console.error('Error loading queue details:', err);
    error.value = 'Failed to load queue details';
  } finally {
    isLoading.value = false;
  }
}

// Call loadQueueDetails when component is mounted
onMounted(async () => {
  if (queueStore.queues.length > 0) {
    await loadQueueDetails();
  }
  
  // Start auto-refresh for running tasks
  startAutoRefresh();
});

// Stop refresh timer when component is unmounted
onUnmounted(() => {
  stopAutoRefresh();
});

// Auto-refresh data for running tasks
function startAutoRefresh() {
  // Clear any existing timer
  stopAutoRefresh();
  
  // Refresh data every 5 seconds if there are running tasks
  refreshTimer.value = setInterval(async () => {
    const hasRunningTasks = queueStore.queues.some(queue => 
      queue.tasks && queue.tasks.some(task => task.taskStatus === 'RUNNING')
    );
    
    if (hasRunningTasks) {
      try {
        // Refresh all queues
        await queueStore.fetchAllQueues();
        // Then reload details
        if (queueStore.queues.length > 0) {
          await loadQueueDetails();
        }
      } catch (err) {
        console.error('Error refreshing queue data:', err);
      }
    }
  }, 5000); // 5 seconds refresh interval
}

// Stop auto refresh timer
function stopAutoRefresh() {
  if (refreshTimer.value) {
    clearInterval(refreshTimer.value);
    refreshTimer.value = null;
  }
}

// Event handlers
function handleViewQueue(queue) {
  emit('view-queue', queue);
}

function handleAddTask(queue) {
  emit('add-task', queue);
}

function handleProcessQueue(queueId) {
  emit('process-queue', queueId);
}

function handleCreateQueue() {
  emit('create-queue');
}

// Calculate task counts for a queue
function getTaskCounts(queue) {
  if (!queue.tasks || queue.tasks.length === 0) {
    return { total: queue.taskCount || 0, queued: 0, running: 0, completed: 0 };
  }
  
  return {
    total: queue.tasks.length,
    queued: queue.tasks.filter(t => t.taskStatus === 'QUEUED').length,
    running: queue.tasks.filter(t => t.taskStatus === 'RUNNING').length,
    completed: queue.tasks.filter(t => t.taskStatus === 'DONE').length
  };
}
</script>

<template>
  <div class="queue-list-container">
    <div class="list-header" v-if="showCreateButton && !props.compact">
      <h2>Task Queues</h2>
      <button @click="handleCreateQueue" class="glowing-button">Create Queue</button>
    </div>
    
    <div v-if="isLoading" class="loading-state">
      Loading queues...
    </div>
    
    <div v-else-if="error" class="error-state">
      {{ error }}
    </div>
    
    <div v-else-if="queueStore.queues.length === 0" class="empty-state">
      <p>No task queues available.</p>
    </div>
    
    <div v-else :class="['queues-grid', { compact: props.compact }]">
      <div v-for="queue in displayedQueues" :key="queue.id" class="queue-card">
        <div class="queue-header">
          <h3>{{ queue.name }}</h3>
        </div>
        
        <p v-if="!compact" class="queue-description">{{ queue.description }}</p>
        
        <!-- Detailed Task Stats -->
        <div class="task-counts">
          <div class="count-item">
            <span class="count-label">Total</span>
            <span class="count-value">{{ getTaskCounts(queue).total }}</span>
          </div>
          <div class="count-item">
            <span class="count-label">Pending</span>
            <span class="count-value">{{ getTaskCounts(queue).queued }}</span>
          </div>
          <div class="count-item">
            <span class="count-label">Running</span>
            <span class="count-value">{{ getTaskCounts(queue).running }}</span>
          </div>
          <div class="count-item">
            <span class="count-label">Done</span>
            <span class="count-value">{{ getTaskCounts(queue).completed }}</span>
          </div>
        </div>
        
        <!-- Tasks Table (for non-compact view) -->
        <div v-if="!compact && queue.tasks && queue.tasks.length > 0" class="queue-tasks">
          <h4>Tasks in Queue</h4>
          <table class="tasks-table">
            <thead>
              <tr>
                <th>Status</th>
                <th>Task Name</th>
                <th>Type</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="task in queue.tasks" :key="task.id">
                <td class="taskStatus-cell">
                  <span :class="['taskStatus-icon', task.taskStatus.toLowerCase()]" :title="task.taskStatus">
                    {{ statusIcons[task.taskStatus] || '❓' }}
                  </span>
                </td>
                <td>{{ task.title }}</td>
                <td>{{ task.taskType || task.taskClassName?.split('.').pop() || 'Standard Task' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <!-- Compact view: show task list without table -->
        <div v-else-if="compact && queue.tasks && queue.tasks.length > 0" class="compact-tasks">
          <h4>Tasks:</h4>
          <div v-for="task in queue.tasks.slice(0, 3)" :key="task.id" class="compact-task-item">
            <span :class="['taskStatus-icon', task.taskStatus.toLowerCase()]" :title="task.taskStatus">
              {{ statusIcons[task.taskStatus] || '❓' }}
            </span>
            <span class="task-title">{{ task.title }}</span>
            <span class="task-type">({{ task.taskType || task.taskClassName?.split('.').pop() || 'Standard Task' }})</span>
          </div>
          <div v-if="queue.tasks.length > 3" class="more-tasks">
            +{{ queue.tasks.length - 3 }} more tasks
          </div>
        </div>
        
        <div class="queue-actions">
          <button @click="handleViewQueue(queue)" class="action-button">View</button>
          <button v-if="!compact" @click="handleAddTask(queue)" class="action-button">Add Task</button>
          <button @click="handleProcessQueue(queue.id)" class="action-button execute">Process</button>
        </div>
      </div>
    </div>
    
    <div v-if="props.limit && queueStore.queues.length > props.limit" class="view-all">
      <button @click="$router.push('/task-queues')" class="view-all-button">View All Queues</button>
    </div>
  </div>
</template>