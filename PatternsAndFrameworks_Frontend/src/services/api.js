import axios from 'axios';

const API_URL = window.VUE_APP_API_URL || 'http://localhost:8080/api';

const apiClient = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  }
});

export const taskService = {
  // Task management
  getAllTasks() {
    return apiClient.get('/tasks');
  },
  
  getTaskById(id) {
    // Use the new enhanced details endpoint
    return apiClient.get(`/tasks/details/${id}`);
  },
  
  // Original endpoint (kept for compatibility)
  getBasicTaskById(id) {
    return apiClient.get(`/tasks/${id}`);
  },
  
  getTasksByUser(userId) {
    return apiClient.get(`/users/${userId}/tasks`);
  },
  
  getTaskProgress(taskId) {
    return apiClient.get(`/tasks/${taskId}/progress`);
  },
  
  getTaskResult(taskId) {
    return apiClient.get(`/tasks/details/${taskId}/result`);
  },
  
  createTask(taskData) {
    return apiClient.post('/tasks', taskData);
  },
  
  updateTask(id, taskData) {
    return apiClient.put(`/tasks/${id}`, taskData);
  },
  
  executeTask(id, wait = false) {
    return apiClient.post(`/tasks/${id}/execute?wait=${wait}`);
  },
  
  addTaskDependency(taskId, dependencyId) {
    return apiClient.post(`/tasks/${taskId}/dependencies/${dependencyId}`);
  },
  
  removeTaskDependency(taskId, dependencyId) {
    return apiClient.delete(`/tasks/${taskId}/dependencies/${dependencyId}`);
  },
  
  getTaskTypes() {
    return apiClient.get('/tasks/types');
  },
  
  getThreadPoolStats() {
    return apiClient.get('/tasks/thread-pool-stats');
  }
};

export const userService = {
  // User management
  getAllUsers() {
    return apiClient.get('/users');
  },
  
  getUserById(id) {
    return apiClient.get(`/users/${id}`);
  },
  
  createUser(userData) {
    return apiClient.post('/users', userData);
  },
  
  updateUser(id, userData) {
    return apiClient.put(`/users/${id}`, userData);
  },
  
  getUserNotifications(userId, read = null) {
    const params = read !== null ? `?read=${read}` : '';
    return apiClient.get(`/users/${userId}/notifications${params}`);
  }
};

export const notificationService = {
  // Notification management
  getAllNotifications() {
    return apiClient.get('/notifications');
  },
  
  getNotificationById(id) {
    return apiClient.get(`/notifications/${id}`);
  },
  
  // Get notifications for a user - alternative endpoint (in addition to userService.getUserNotifications)
  getUserNotifications(userId, read = null) {
    const params = read !== null ? `?read=${read}` : '';
    return apiClient.get(`/notifications/user/${userId}${params}`);
  },
  
  markAsRead(notificationId, userId) {
    return apiClient.post(`/notifications/${notificationId}/read`, { userId });
  },
  
  sendNotification(notificationData) {
    return apiClient.post('/notifications/send', notificationData);
  },
  
  broadcastNotification(notificationData) {
    return apiClient.post('/notifications/broadcast', notificationData);
  }
};

export const queueService = {
  // Queue management
  getAllQueues() {
    return apiClient.get('/task-queues');
  },
  
  getQueueById(id) {
    return apiClient.get(`/task-queues/${id}`);
  },
  
  createQueue(queueData) {
    return apiClient.post('/task-queues', queueData);
  },
  
  addTaskToQueue(queueId, taskId) {
    return apiClient.post(`/task-queues/${queueId}/tasks/${taskId}`);
  },
  
  getQueueTasks(queueId, status = null) {
    const params = status ? `?status=${status}` : '';
    return apiClient.get(`/task-queues/${queueId}/tasks${params}`);
  },
  
  // Get all tasks for a queue including both pending and processed ones
  getAllQueueTasks(queueId, status = null) {
    const params = status ? `?status=${status}` : '';
    return apiClient.get(`/task-queues/${queueId}/all-tasks${params}`);
  },
  
  // Get all tasks for a queue using the single endpoint and filter locally
  getCompletedTasks(queueId) {
    return apiClient.get(`/task-queues/${queueId}/all-tasks?status=DONE`);
  },
  
  processNextTask(queueId) {
    return apiClient.post(`/task-queues/${queueId}/process-next`);
  },
  
  processAllTasks(queueId) {
    return apiClient.post(`/task-queues/${queueId}/process-all`);
  },
  
  reorderQueueTasks(queueId, orderData) {
    return apiClient.post(`/task-queues/${queueId}/reorder`, orderData);
  }
};

export default {
  task: taskService,
  user: userService,
  notification: notificationService,
  queue: queueService
};