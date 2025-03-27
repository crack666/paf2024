import { defineStore } from 'pinia';
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { queueService } from '@/services/api';
import { initializeWebSocket, disconnectWebSocket } from '@/services/websocket';

export const useQueueStore = defineStore('queue', () => {
  const queues = ref([]);
  const currentQueue = ref(null);
  const completedTasks = ref({});
  const loading = ref(false);
  const error = ref(null);
  
  // Set up WebSocket event listeners for real-time updates
  function setupWebSocketHandlers() {
    // Listen for queue updates
    document.addEventListener('queue-update', handleQueueUpdate);
    
    // Listen for task status updates
    document.addEventListener('task-status-update', handleTaskStatusUpdate);
    
    // Listen for task result updates
    document.addEventListener('task-result-update', handleTaskResultUpdate);
  }
  
  // Clean up WebSocket event listeners
  function cleanupWebSocketHandlers() {
    document.removeEventListener('queue-update', handleQueueUpdate);
    document.removeEventListener('task-status-update', handleTaskStatusUpdate);
    document.removeEventListener('task-result-update', handleTaskResultUpdate);
  }
  
  // Handle queue updates from WebSocket
  function handleQueueUpdate(event) {
    const update = event.detail;
    console.log('Queue store received queue update:', update);
    
    // Refresh the queue if it's the one we're currently viewing
    if (currentQueue.value && currentQueue.value.id === update.queueId) {
      fetchQueueById(update.queueId);
    }
    
    // Refresh all queues list to keep it up-to-date
    fetchAllQueues();
  }
  
  // Handle task status updates from WebSocket
  function handleTaskStatusUpdate(event) {
    const taskUpdate = event.detail;
    console.log('Queue store received task status update:', taskUpdate);
    
    // Update the task in the current queue if it exists
    if (currentQueue.value && currentQueue.value.tasks) {
      const taskIndex = currentQueue.value.tasks.findIndex(task => task.id === taskUpdate.id);
      if (taskIndex !== -1) {
        // Update the task with the new data
        currentQueue.value.tasks[taskIndex] = { ...currentQueue.value.tasks[taskIndex], ...taskUpdate };
      } else if (taskUpdate.status === 'DONE' || taskUpdate.status === 'ERROR') {
        // If this is a completed task, fetch the completed tasks
        fetchCompletedTasks(currentQueue.value.id);
      }
    }
  }
  
  // Handle task result updates from WebSocket
  function handleTaskResultUpdate(event) {
    const resultUpdate = event.detail;
    console.log('Queue store received task result update:', resultUpdate);
    
    if (resultUpdate.taskId && resultUpdate.result) {
      // Store the result
      completedTasks.value[resultUpdate.taskId] = resultUpdate.result;
      
      // Refresh completed tasks for current queue
      if (currentQueue.value) {
        fetchCompletedTasks(currentQueue.value.id);
      }
    }
  }

  // Actions
  async function fetchAllQueues() {
    loading.value = true;
    error.value = null;
    try {
      const response = await queueService.getAllQueues();
      queues.value = response.data;
      return response.data;
    } catch (err) {
      console.error('Error fetching queues:', err);
      error.value = err.response?.data?.message || 'Failed to fetch queues';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchQueueById(queueId) {
    loading.value = true;
    error.value = null;
    try {
      const response = await queueService.getQueueById(queueId);
      currentQueue.value = response.data;
      
      // Also fetch completed tasks for this queue
      await fetchCompletedTasks(queueId);
      
      return response.data;
    } catch (err) {
      console.error('Error fetching queue:', err);
      error.value = err.response?.data?.message || 'Failed to fetch queue';
      throw err;
    } finally {
      loading.value = false;
    }
  }
  
  // Fetch completed tasks with their results
  async function fetchCompletedTasks(queueId) {
    try {
      const response = await queueService.getCompletedTasks(queueId);
      
      // Convert from array of tasks to a map of taskId -> result
      const tasksMap = {};
      if (Array.isArray(response.data)) {
        response.data.forEach(task => {
          if (task.taskStatus === 'DONE' && task.id) {
            // Create a simple result object if none exists
            tasksMap[task.id] = {
              id: task.id,
              taskId: task.id,
              output: task.output || "Task completed",
              executionTime: task.executionTime || 0,
              successful: true
            };
          }
        });
      } else {
        // Handle case where response might already be in the expected format
        completedTasks.value = response.data;
        return response.data;
      }
      
      completedTasks.value = tasksMap;
      return tasksMap;
    } catch (err) {
      console.error('Error fetching completed tasks:', err);
      // Don't set error here to avoid disrupting the UI for a secondary feature
      return {};
    }
  }
  
  // Get all tasks for a queue, including both current and completed ones
  async function fetchAllQueueTasks(queueId, status = null) {
    loading.value = true;
    error.value = null;
    try {
      const response = await queueService.getAllQueueTasks(queueId, status);
      return response.data;
    } catch (err) {
      console.error('Error fetching all queue tasks:', err);
      error.value = err.response?.data?.message || 'Failed to fetch queue tasks';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function createQueue(queueData) {
    loading.value = true;
    error.value = null;
    try {
      const response = await queueService.createQueue(queueData);
      // Add the new queue to our local state
      queues.value.push(response.data);
      return response.data;
    } catch (err) {
      console.error('Error creating queue:', err);
      error.value = err.response?.data?.message || 'Failed to create queue';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function addTaskToQueue(queueId, taskId) {
    loading.value = true;
    error.value = null;
    try {
      const response = await queueService.addTaskToQueue(queueId, taskId);
      // Refresh the queue to get updated tasks
      if (currentQueue.value && currentQueue.value.id === queueId) {
        await fetchQueueById(queueId);
      }
      return response.data;
    } catch (err) {
      console.error('Error adding task to queue:', err);
      error.value = err.response?.data?.message || 'Failed to add task to queue';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function processNextTask(queueId) {
    loading.value = true;
    error.value = null;
    try {
      const response = await queueService.processNextTask(queueId);
      // Refresh the queue to get updated state
      if (currentQueue.value && currentQueue.value.id === queueId) {
        await fetchQueueById(queueId);
      }
      return response.data;
    } catch (err) {
      console.error('Error processing next task:', err);
      error.value = err.response?.data?.message || 'Failed to process next task';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function processAllTasks(queueId) {
    loading.value = true;
    error.value = null;
    try {
      const response = await queueService.processAllTasks(queueId);
      // Refresh the queue to get updated state
      if (currentQueue.value && currentQueue.value.id === queueId) {
        await fetchQueueById(queueId);
      }
      return response.data;
    } catch (err) {
      console.error('Error processing all tasks:', err);
      error.value = err.response?.data?.message || 'Failed to process all tasks';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // Initialize WebSocket handlers
  function init(userId = null) {
    setupWebSocketHandlers();
    
    // Return cleanup function
    return () => {
      cleanupWebSocketHandlers();
    };
  }
  
  // Reset store state
  function reset() {
    queues.value = [];
    currentQueue.value = null;
    completedTasks.value = {};
    loading.value = false;
    error.value = null;
    
    // Clean up WebSocket handlers
    cleanupWebSocketHandlers();
  }

  return {
    // State
    queues,
    currentQueue,
    completedTasks,
    loading,
    error,
    
    // Actions
    fetchAllQueues,
    fetchQueueById,
    fetchCompletedTasks,
    fetchAllQueueTasks,
    createQueue,
    addTaskToQueue,
    processNextTask,
    processAllTasks,
    init,
    reset
  };
});