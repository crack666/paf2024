<script setup>
import {computed, onMounted, onUnmounted, ref} from 'vue';
import {useRouter} from 'vue-router';
import {useAuthStore} from '@/stores/auth';
import {useQueueStore} from '@/stores/queue';
import {useTaskStore} from '@/stores/task';
import {initializeWebSocket} from '@/services/websocket';
import QueueList from '@/components/QueueList.vue';

const authStore = useAuthStore();
const queueStore = useQueueStore();
const taskStore = useTaskStore();
const router = useRouter();

const isLoading = ref(true);
const error = ref(null);
const successMessage = ref('');
const showCreateQueueModal = ref(false);
const showQueueDetailsModal = ref(false);
const showAddTaskToQueueModal = ref(false);
const selectedQueue = ref(null);
const availableTasks = ref([]);
const refreshTimer = ref(null); 
const activeTasksFilter = ref('ALL'); // Filter for task taskStatus: ALL, CREATED, RUNNING, DONE, ERROR
const allTasks = ref([]);
const wsConnected = ref(false);

const newQueue = ref({
  name: '',
  description: ''
});

// Status Icon Map
const statusIcons = {
  'CREATED': '📝',
  'QUEUED': '⏱️',
  'RUNNING': '⚙️',
  'DONE': '✅',
  'ERROR': '❌'
};

// Redirect if not logged in
if (!authStore.isLoggedIn) {
  router.push('/login');
}

// Computed property for all tasks (current and completed)
const combinedTasks = computed(() => {
  if (!selectedQueue.value) return [];
  
  // Get current tasks from selected queue
  let currentTasks = selectedQueue.value.tasks || [];
  
  // Make a deep copy to avoid modifying the original objects
  currentTasks = currentTasks.map(task => ({...task}));

  // Get completed tasks with their results
  const completedTasksArray = [];
  if (queueStore.completedTasks) {
    // Convert task IDs and results to task objects
    Object.entries(queueStore.completedTasks).forEach(([taskId, result]) => {
      // Find the task in the repository
      const task = taskStore.tasks.find(t => t.id === parseInt(taskId)) || { id: parseInt(taskId) };
      
      // Add result data to the task
      completedTasksArray.push({
        ...task,
        taskStatus: 'DONE',  // Ensure DONE status for completed tasks
        result: result
      });
    });
  }
  
  // Combine and deduplicate with proper task status prioritization
  const taskMap = new Map();
  
  // First add completed tasks
  completedTasksArray.forEach(task => {
    taskMap.set(task.id, task);
  });
  
  // Then add current tasks, but don't override DONE status with QUEUED
  currentTasks.forEach(task => {
    if (taskMap.has(task.id)) {
      // Skip if task is already marked as DONE
      const existingTask = taskMap.get(task.id);
      if (existingTask.taskStatus === 'DONE' && task.taskStatus === 'QUEUED') {
        // Keep the DONE status version
        return;
      }
    }
    taskMap.set(task.id, task);
  });
  
  return Array.from(taskMap.values());
});

// Fetch data when component mounts
onMounted(async () => {
  if (authStore.isLoggedIn) {
    try {
      isLoading.value = true;
      error.value = null;
      
      // Initialize WebSocket connection
      initializeWebSocket(authStore.user.id);
      wsConnected.value = true;
      
      // Initialize queue store WebSocket listeners
      queueStore.init(authStore.user.id);
      
      await queueStore.fetchAllQueues();
      await taskStore.fetchUserTasks(authStore.user.id);
      
      // Start automatic refreshing of data for running tasks (as fallback)
      startAutoRefresh();
    } catch (err) {
      console.error('Error loading queues data:', err);
      error.value = 'Failed to load queues data. Please try again.';
    } finally {
      isLoading.value = false;
    }
  }
});

// Stop refresh timer and clean up WebSocket when component is unmounted
onUnmounted(() => {
  stopAutoRefresh();
  queueStore.reset(); // This will clean up WebSocket handlers
});

// Start auto refresh timer
function startAutoRefresh() {
  // Clear any existing timer
  stopAutoRefresh();
  
  // Refresh data every 5 seconds
  refreshTimer.value = setInterval(async () => {
    // Only refresh if we're showing a queue with running tasks
    if (selectedQueue.value && 
        selectedQueue.value.tasks && 
        selectedQueue.value.tasks.some(t => t.taskStatus === 'RUNNING')) {
      try {
        // Refresh the selected queue
        await queueStore.fetchQueueById(selectedQueue.value.id);
        // Also refresh all queues to keep the list up-to-date
        await queueStore.fetchAllQueues();
      } catch (error) {
        console.error('Error refreshing queue data:', error);
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

// Get filtered tasks based on taskStatus (uses combinedTasks for completed ones)
function getFilteredTasks(queue) {
  if (!queue) return [];
  
  // Use combinedTasks which includes both current and completed tasks
  const tasks = combinedTasks.value;
  
  if (activeTasksFilter.value === 'ALL') {
    return tasks;
  }
  
  return tasks.filter(task => task.taskStatus === activeTasksFilter.value);
}

// Display the create queue modal
function openCreateQueueModal() {
  newQueue.value = {
    name: '',
    description: ''
  };
  showCreateQueueModal.value = true;
}

// Close the create queue modal
function closeCreateQueueModal() {
  showCreateQueueModal.value = false;
}

// Create a new queue
async function createQueue() {
  try {
    isLoading.value = true;
    error.value = null;
    successMessage.value = '';
    
    // Create the queue via the API
    const createdQueue = await queueStore.createQueue(newQueue.value);
    
    // Show success message
    successMessage.value = `Queue "${createdQueue.name}" created successfully!`;
    
    // Close the modal after a delay to allow user to see the success message
    setTimeout(() => {
      closeCreateQueueModal();
      successMessage.value = '';
    }, 2000);
  } catch (err) {
    console.error('Error creating queue:', err);
    error.value = 'Failed to create queue. Please try again.';
  } finally {
    isLoading.value = false;
  }
}

// View queue details
async function viewQueueDetails(queue) {
  selectedQueue.value = queue;
  try {
    isLoading.value = true;
    // Fetch detailed queue information
    const queueWithTasks = await queueStore.fetchQueueById(queue.id);
    
    // Also fetch all tasks, including completed ones
    await queueStore.fetchAllQueueTasks(queue.id);
    
    selectedQueue.value = queueWithTasks;
    showQueueDetailsModal.value = true;
  } catch (err) {
    console.error('Error fetching queue details:', err);
    error.value = 'Failed to load queue details. Please try again.';
  } finally {
    isLoading.value = false;
  }
}

// Close queue details modal
function closeQueueDetailsModal() {
  showQueueDetailsModal.value = false;
  selectedQueue.value = null;
}

// Open add task to queue modal
function openAddTaskToQueueModal(queue) {
  selectedQueue.value = queue;
  // Filter out tasks that are already in the queue
  const queueTaskIds = new Set(queue.tasks ? queue.tasks.map(t => t.id) : []);
  availableTasks.value = taskStore.userTasks.filter(
    task => !queueTaskIds.has(task.id) && task.taskStatus === 'CREATED'
  );
  showAddTaskToQueueModal.value = true;
}

// Close add task to queue modal
function closeAddTaskToQueueModal() {
  showAddTaskToQueueModal.value = false;
}

// Add task to queue
async function addTaskToQueue(taskId) {
  try {
    isLoading.value = true;
    error.value = null;
    successMessage.value = '';
    
    await queueStore.addTaskToQueue(selectedQueue.value.id, taskId);
    
    // Refresh queue details
    await queueStore.fetchQueueById(selectedQueue.value.id);
    
    // Show success message
    successMessage.value = 'Task added to queue successfully!';
    
    // Close the modal after a delay
    setTimeout(() => {
      closeAddTaskToQueueModal();
      successMessage.value = '';
    }, 2000);
  } catch (err) {
    console.error('Error adding task to queue:', err);
    error.value = 'Failed to add task to queue. Please try again.';
  } finally {
    isLoading.value = false;
  }
}

// Process next task
async function processNextTask(queueId) {
  try {
    isLoading.value = true;
    error.value = null;
    successMessage.value = '';
    
    await queueStore.processNextTask(queueId);
    
    // Show success message
    successMessage.value = 'Processing next task...';
    
    // Clear message after delay
    setTimeout(() => {
      successMessage.value = '';
    }, 2000);
  } catch (err) {
    console.error('Error processing next task:', err);
    error.value = 'Failed to process next task. Please try again.';
  } finally {
    isLoading.value = false;
  }
}

// Process all tasks
async function processAllTasks(queueId) {
  try {
    isLoading.value = true;
    error.value = null;
    successMessage.value = '';
    
    await queueStore.processAllTasks(queueId);
    
    // Show success message
    successMessage.value = 'Processing all tasks...';
    
    // Clear message after delay
    setTimeout(() => {
      successMessage.value = '';
    }, 2000);
    
    // Refresh queue details after a short delay
    setTimeout(async () => {
      if (selectedQueue.value && selectedQueue.value.id === queueId) {
        await queueStore.fetchQueueById(queueId);
      }
    }, 1000);
  } catch (err) {
    console.error('Error processing all tasks:', err);
    error.value = 'Failed to process all tasks. Please try again.';
  } finally {
    isLoading.value = false;
  }
}

// Execute a single task
async function executeTask(taskId) {
  try {
    isLoading.value = true;
    error.value = null;
    successMessage.value = '';
    
    await taskStore.executeTask(taskId);
    
    // Show success message
    successMessage.value = 'Executing task...';
    
    // Clear message after delay
    setTimeout(() => {
      successMessage.value = '';
    }, 2000);
    
    // Refresh queue details after a short delay
    setTimeout(async () => {
      if (selectedQueue.value) {
        await queueStore.fetchQueueById(selectedQueue.value.id);
      }
    }, 1000);
  } catch (err) {
    console.error('Error executing task:', err);
    error.value = 'Failed to execute task. Please try again.';
  } finally {
    isLoading.value = false;
  }
}

// Format date for display
function formatDate(dateString) {
  if (!dateString) return 'Not scheduled';
  return new Date(dateString).toLocaleString();
}

// Calculate task counts for a queue using combinedTasks which has correct statuses
function getTaskCounts(queue) {
  // If we're not viewing a queue in detail, use the queue's tasks
  if (queue && (!selectedQueue.value || queue.id !== selectedQueue.value.id)) {
    if (!queue.tasks || queue.tasks.length === 0) {
      return { total: 0, created: 0, running: 0, completed: 0 };
    }
    
    return {
      total: queue.tasks.length,
      created: queue.tasks.filter(t => t.taskStatus === 'CREATED').length,
      running: queue.tasks.filter(t => t.taskStatus === 'RUNNING').length,
      completed: queue.tasks.filter(t => t.taskStatus === 'DONE').length
    };
  }
  
  // If we're viewing a queue in detail, use combinedTasks which has more accurate status
  if (!selectedQueue.value || !combinedTasks.value.length) {
    return { total: 0, created: 0, running: 0, completed: 0 };
  }
  
  const tasks = combinedTasks.value;
  return {
    total: tasks.length,
    created: tasks.filter(t => t.taskStatus === 'CREATED').length,
    running: tasks.filter(t => t.taskStatus === 'RUNNING').length,
    completed: tasks.filter(t => t.taskStatus === 'DONE').length
  };
}
</script>

<template>
  <div class="queues-container">
    <!-- Loading and error states -->
    <div v-if="isLoading && !showCreateQueueModal && !showQueueDetailsModal && !showAddTaskToQueueModal" class="loading-state">
      Loading queues...
    </div>
    
    <div v-else-if="error && !showCreateQueueModal && !showQueueDetailsModal && !showAddTaskToQueueModal" class="error-state">
      {{ error }}
    </div>
    
    <!-- Queues content -->
    <div v-else class="queues-content">
      <div class="queues-header">
        <h1>Task Queues Management</h1>
        <div class="header-actions">
          <button @click="router.push('/tasks/create')" class="secondary-button">Create Task</button>
          <button @click="openCreateQueueModal" class="glowing-button">Create New Queue</button>
        </div>
      </div>
      
      <div v-if="successMessage" class="feedback success">
        {{ successMessage }}
      </div>
      
      <!-- Empty state -->
      <div v-if="queueStore.queues.length === 0" class="empty-state">
        <p>No task queues available. Create your first queue to get started!</p>
      </div>
      
      <!-- Use QueueList Component -->
      <div v-else>
        <QueueList 
          :compact="false"
          :showCreateButton="false"
          @view-queue="viewQueueDetails"
          @add-task="openAddTaskToQueueModal"
          @process-queue="processAllTasks"
          @create-queue="openCreateQueueModal"
        />
      </div>
    </div>
    
    <!-- Create Queue Modal -->
    <div v-if="showCreateQueueModal" class="modal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>Create New Queue</h2>
          <button @click="closeCreateQueueModal" class="close-button">&times;</button>
        </div>
        
        <div class="modal-body">
          <div v-if="successMessage" class="feedback success">
            {{ successMessage }}
          </div>
          
          <div v-if="error" class="feedback error">
            {{ error }}
          </div>
          
          <form @submit.prevent="createQueue">
            <div class="form-group">
              <label for="queueName">Queue Name</label>
              <input id="queueName" v-model="newQueue.name" type="text" required />
            </div>
            
            <div class="form-group">
              <label for="queueDescription">Description</label>
              <textarea id="queueDescription" v-model="newQueue.description" rows="4"></textarea>
            </div>
            
            <div class="modal-actions">
              <button type="submit" class="submit-button" :disabled="isLoading">
                {{ isLoading ? 'Creating...' : 'Create Queue' }}
              </button>
              <button type="button" @click="closeCreateQueueModal" class="cancel-button">Cancel</button>
            </div>
          </form>
        </div>
      </div>
    </div>
    
    <!-- Queue Details Modal -->
    <div v-if="showQueueDetailsModal && selectedQueue" class="modal">
      <div class="modal-content large">
        <div class="modal-header">
          <h2>Queue Details: {{ selectedQueue.name }}</h2>
          <button @click="closeQueueDetailsModal" class="close-button">&times;</button>
        </div>
        
        <div class="modal-body">
          <div v-if="successMessage" class="feedback success">
            {{ successMessage }}
          </div>
          
          <div v-if="error" class="feedback error">
            {{ error }}
          </div>
          
          <div class="queue-detail-header">
            <div class="queue-info">
              <p>{{ selectedQueue.description }}</p>
              <p><strong>ID:</strong> {{ selectedQueue.id }}</p>
            </div>
            
            <div class="queue-actions">
              <button @click="openAddTaskToQueueModal(selectedQueue)" class="action-button">Add Task</button>
              <button @click="processNextTask(selectedQueue.id)" class="action-button">Process Next</button>
              <button @click="processAllTasks(selectedQueue.id)" class="action-button execute">Process All</button>
            </div>
          </div>
          
          <div class="task-counts">
            <div class="count-item" @click="activeTasksFilter = 'ALL'" :class="{ active: activeTasksFilter === 'ALL' }">
              <span class="count-label">Total Tasks</span>
              <span class="count-value">{{ getTaskCounts(selectedQueue).total }}</span>
            </div>
            <div class="count-item" @click="activeTasksFilter = 'CREATED'" :class="{ active: activeTasksFilter === 'CREATED' }">
              <span class="count-label">Pending</span>
              <span class="count-value">{{ getTaskCounts(selectedQueue).created }}</span>
            </div>
            <div class="count-item" @click="activeTasksFilter = 'RUNNING'" :class="{ active: activeTasksFilter === 'RUNNING' }">
              <span class="count-label">Running</span>
              <span class="count-value">{{ getTaskCounts(selectedQueue).running }}</span>
            </div>
            <div class="count-item" @click="activeTasksFilter = 'DONE'" :class="{ active: activeTasksFilter === 'DONE' }">
              <span class="count-label">Completed</span>
              <span class="count-value">{{ getTaskCounts(selectedQueue).completed }}</span>
            </div>
          </div>
          
          <!-- Filter Control Tabs -->
          <div class="filter-tabs">
            <div 
              v-for="filter in ['ALL', 'CREATED', 'RUNNING', 'DONE']" 
              :key="filter" 
              @click="activeTasksFilter = filter"
              :class="['filter-tab', { active: activeTasksFilter === filter }]"
            >
              {{ filter === 'ALL' ? 'All Tasks' : 
                 filter === 'CREATED' ? 'Pending' : 
                 filter === 'RUNNING' ? 'Running' : 'Completed' }}
            </div>
          </div>
          
          <!-- Tasks Table -->
          <div class="queue-tasks">
            <h3>
              {{ activeTasksFilter === 'ALL' ? 'All Tasks in Queue' : 
                 activeTasksFilter === 'CREATED' ? 'Pending Tasks' : 
                 activeTasksFilter === 'RUNNING' ? 'Running Tasks' : 'Completed Tasks' }}
              <span class="tasks-count">({{ getFilteredTasks(selectedQueue).length }})</span>
            </h3>
            
            <div v-if="getFilteredTasks(selectedQueue).length === 0" class="empty-tasks">
              <p v-if="!selectedQueue.tasks || selectedQueue.tasks.length === 0">
                No tasks in this queue. Add tasks to get started!
              </p>
              <p v-else>
                No {{ activeTasksFilter === 'CREATED' ? 'pending' : 
                      activeTasksFilter === 'RUNNING' ? 'running' : 
                      activeTasksFilter === 'DONE' ? 'completed' : '' }} 
                tasks found.
              </p>
            </div>
            
            <table v-else class="tasks-table">
              <thead>
                <tr>
                  <th>Status</th>
                  <th>Task Name</th>
                  <th>Type</th>
                  <th>Due Date</th>
                  <th v-if="activeTasksFilter === 'RUNNING'">Progress</th>
                  <th v-if="activeTasksFilter === 'DONE'">Completion Time</th>
                  <th v-if="activeTasksFilter === 'DONE'">Result</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="task in getFilteredTasks(selectedQueue)" :key="task.id">
                  <td class="taskStatus-cell">
                    <span :class="['taskStatus-icon', task.taskStatus.toLowerCase()]" :title="task.taskStatus">
                      {{ statusIcons[task.taskStatus] || '❓' }}
                    </span>
                  </td>
                  <td>{{ task.title || `Task #${task.id}` }}</td>
                  <td>{{ task.taskType  }}</td>
                  <td>{{ formatDate(task.dueDate) }}</td>
                  
                  <!-- Progress column for running tasks -->
                  <td v-if="activeTasksFilter === 'RUNNING'" class="progress-cell">
                    <div class="task-progress">
                      <div class="progress-bar">
                        <div class="progress-fill animate-pulse" style="width: 40%"></div>
                      </div>
                      <span class="progress-text">Running...</span>
                    </div>
                  </td>
                  
                  <!-- Completion time for done tasks -->
                  <td v-if="activeTasksFilter === 'DONE'">
                    {{ formatDate(task.completedAt) }}
                  </td>
                  
                  <!-- Result summary for done tasks -->
                  <td v-if="activeTasksFilter === 'DONE'" class="result-cell">
                    <div v-if="queueStore.completedTasks[task.id]" class="result-summary">
                      <div class="result-header">Result:</div>
                      <div class="result-content">
                        {{ queueStore.completedTasks[task.id].output || 'No result data' }}
                      </div>
                    </div>
                    <div v-else class="no-result">No result data available</div>
                  </td>
                  
                  <!-- Action buttons -->
                  <td class="actions-cell">
                    <button @click="router.push(`/tasks/${task.id}`)" class="mini-button view">View</button>
                    <button 
                      v-if="task.taskStatus === 'CREATED'"
                      @click="executeTask(task.id)" 
                      class="mini-button run">
                      Run
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          
          <div class="modal-actions">
            <button @click="closeQueueDetailsModal" class="cancel-button">Close</button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Add Task to Queue Modal -->
    <div v-if="showAddTaskToQueueModal" class="modal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>Add Task to {{ selectedQueue?.name }}</h2>
          <button @click="closeAddTaskToQueueModal" class="close-button">&times;</button>
        </div>
        
        <div class="modal-body">
          <div v-if="successMessage" class="feedback success">
            {{ successMessage }}
          </div>
          
          <div v-if="error" class="feedback error">
            {{ error }}
          </div>
          
          <div v-if="availableTasks.length === 0" class="empty-state">
            <p>No available tasks to add. Create some tasks first!</p>
          </div>
          
          <div v-else class="task-list">
            <div v-for="task in availableTasks" :key="task.id" class="available-task">
              <div class="task-info">
                <h4>{{ task.title }}</h4>
                <p>{{ task.description }}</p>
                <div class="task-meta">
                  <span class="meta-item">Type: {{ task.taskType || task.taskClassName?.split('.').pop() || 'Standard Task' }}</span>
                  <span class="meta-item">Due: {{ formatDate(task.dueDate) }}</span>
                </div>
              </div>
              <button @click="addTaskToQueue(task.id)" class="action-button">Add to Queue</button>
            </div>
          </div>
          
          <div class="modal-actions">
            <button @click="closeAddTaskToQueueModal" class="cancel-button">Close</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>