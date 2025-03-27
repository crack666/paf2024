import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { taskService } from '@/services/api';

export const useTaskStore = defineStore('task', () => {
  const tasks = ref([]);
  const userTasks = ref([]);
  const currentTask = ref(null);
  const taskTypes = ref([]);
  const loading = ref(false);
  const error = ref(null);
  const taskProgress = ref(null);
  const taskResult = ref(null);

  // Computed properties
  const pendingTasks = computed(() => 
    tasks.value.filter(task => ['CREATED', 'QUEUED'].includes(task.status))
  );
  
  const runningTasks = computed(() => 
    tasks.value.filter(task => task.status === 'RUNNING')
  );
  
  const completedTasks = computed(() => 
    tasks.value.filter(task => task.status === 'DONE')
  );

  // Actions
  async function fetchAllTasks() {
    loading.value = true;
    error.value = null;
    try {
      const response = await taskService.getAllTasks();
      tasks.value = response.data;
      return response.data;
    } catch (err) {
      console.error('Error fetching tasks:', err);
      error.value = err.response?.data?.message || 'Failed to fetch tasks';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchUserTasks(userId) {
    loading.value = true;
    error.value = null;
    try {
      const response = await taskService.getTasksByUser(userId);
      userTasks.value = response.data;
      return response.data;
    } catch (err) {
      console.error('Error fetching user tasks:', err);
      error.value = err.response?.data?.message || 'Failed to fetch user tasks';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchTaskById(taskId) {
    loading.value = true;
    error.value = null;
    try {
      const response = await taskService.getTaskById(taskId);
      currentTask.value = response.data;
      
      // If task has a result, update the taskResult ref
      if (response.data.result) {
        taskResult.value = response.data.result;
      } else if (response.data.status === 'DONE') {
        // Try to fetch result separately if task is done but result is missing
        await fetchTaskResult(taskId);
      }
      
      return response.data;
    } catch (err) {
      console.error('Error fetching task:', err);
      error.value = err.response?.data?.message || 'Failed to fetch task';
      throw err;
    } finally {
      loading.value = false;
    }
  }
  
  async function fetchTaskResult(taskId) {
    try {
      const response = await taskService.getTaskResult(taskId);
      taskResult.value = response.data;
      
      // Update the current task with the result if it exists
      if (currentTask.value && currentTask.value.id === parseInt(taskId)) {
        currentTask.value.result = response.data;
      }
      
      return response.data;
    } catch (err) {
      console.error('Error fetching task result:', err);
      return null;
    }
  }

  async function fetchTaskTypes() {
    loading.value = true;
    error.value = null;
    try {
      const response = await taskService.getTaskTypes();
      taskTypes.value = response.data;
      return response.data;
    } catch (err) {
      console.error('Error fetching task types:', err);
      error.value = err.response?.data?.message || 'Failed to fetch task types';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function createTask(taskData) {
    loading.value = true;
    error.value = null;
    try {
      const response = await taskService.createTask(taskData);
      // Add the new task to our local state
      tasks.value.push(response.data);
      if (taskData.assignedUserId && userTasks.value.length > 0) {
        userTasks.value.push(response.data);
      }
      return response.data;
    } catch (err) {
      console.error('Error creating task:', err);
      error.value = err.response?.data?.message || 'Failed to create task';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function updateTask(taskId, taskData) {
    loading.value = true;
    error.value = null;
    try {
      const response = await taskService.updateTask(taskId, taskData);
      // Update the task in our local state
      const updatedTask = response.data;
      tasks.value = tasks.value.map(task => 
        task.id === updatedTask.id ? updatedTask : task
      );
      userTasks.value = userTasks.value.map(task => 
        task.id === updatedTask.id ? updatedTask : task
      );
      if (currentTask.value && currentTask.value.id === updatedTask.id) {
        currentTask.value = updatedTask;
      }
      return updatedTask;
    } catch (err) {
      console.error('Error updating task:', err);
      error.value = err.response?.data?.message || 'Failed to update task';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function executeTask(taskId, wait = false) {
    loading.value = true;
    error.value = null;
    try {
      const response = await taskService.executeTask(taskId, wait);
      const executedTask = response.data;
      
      // Update the task in our local state
      tasks.value = tasks.value.map(task => 
        task.id === executedTask.id ? executedTask : task
      );
      userTasks.value = userTasks.value.map(task => 
        task.id === executedTask.id ? executedTask : task
      );
      if (currentTask.value && currentTask.value.id === executedTask.id) {
        currentTask.value = executedTask;
      }
      
      return executedTask;
    } catch (err) {
      console.error('Error executing task:', err);
      error.value = err.response?.data?.message || 'Failed to execute task';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchTaskProgress(taskId) {
    try {
      const response = await taskService.getTaskProgress(taskId);
      taskProgress.value = response.data;
      return response.data;
    } catch (err) {
      console.error('Error fetching task progress:', err);
      return null;
    }
  }

  async function addTaskDependency(taskId, dependencyId) {
    loading.value = true;
    error.value = null;
    try {
      const response = await taskService.addTaskDependency(taskId, dependencyId);
      // Refresh the task to get updated dependencies
      await fetchTaskById(taskId);
      return response.data;
    } catch (err) {
      console.error('Error adding task dependency:', err);
      error.value = err.response?.data?.message || 'Failed to add task dependency';
      throw err;
    } finally {
      loading.value = false;
    }
  }
  
  async function removeTaskDependency(taskId, dependencyId) {
    loading.value = true;
    error.value = null;
    try {
      const response = await taskService.removeTaskDependency(taskId, dependencyId);
      // Refresh the task to get updated dependencies
      await fetchTaskById(taskId);
      return response.data;
    } catch (err) {
      console.error('Error removing task dependency:', err);
      error.value = err.response?.data?.message || 'Failed to remove task dependency';
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // Reset store state
  function reset() {
    tasks.value = [];
    userTasks.value = [];
    currentTask.value = null;
    taskProgress.value = null;
    taskResult.value = null;
    loading.value = false;
    error.value = null;
  }

  return {
    // State
    tasks,
    userTasks,
    currentTask,
    taskTypes,
    loading,
    error,
    taskProgress,
    taskResult,
    
    // Computed
    pendingTasks,
    runningTasks,
    completedTasks,
    
    // Actions
    fetchAllTasks,
    fetchUserTasks,
    fetchTaskById,
    fetchTaskTypes,
    createTask,
    updateTask,
    executeTask,
    fetchTaskProgress,
    fetchTaskResult,
    addTaskDependency,
    removeTaskDependency,
    reset
  };
});