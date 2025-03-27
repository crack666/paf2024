<script setup>
import { ref, onMounted, computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useTaskStore } from '@/stores/task';
import { useNotificationStore } from '@/stores/notification';
import { useQueueStore } from '@/stores/queue';
import { useRouter } from 'vue-router';
import QueueList from '@/components/QueueList.vue';

const authStore = useAuthStore();
const taskStore = useTaskStore();
const notificationStore = useNotificationStore();
const queueStore = useQueueStore();
const router = useRouter();

const isLoading = ref(false);
const errorMessage = ref('');

// Redirect if not logged in
if (!authStore.isLoggedIn) {
  router.push('/login');
}

// Get data on component mount
onMounted(async () => {
  if (authStore.isLoggedIn && authStore.user) {
    isLoading.value = true;
    try {
      // Fetch user's tasks and notifications
      await Promise.all([
        taskStore.fetchUserTasks(authStore.user.id),
        notificationStore.fetchUserNotifications(authStore.user.id, false),
        queueStore.fetchAllQueues()
      ]);
      
      // We don't need to fetch queue details here anymore
      // This is now handled by the QueueList component
    } catch (error) {
      console.error('Failed to load dashboard data', error);
      errorMessage.value = 'Failed to load dashboard data';
    } finally {
      isLoading.value = false;
    }
  }
});

// Computed properties
const pendingTasks = computed(() => 
  taskStore.userTasks.filter(task => ['CREATED', 'QUEUED'].includes(task.taskStatus))
);

const runningTasks = computed(() => 
  taskStore.userTasks.filter(task => task.taskStatus === 'RUNNING')
);

const completedTasks = computed(() => 
  taskStore.userTasks.filter(task => task.taskStatus === 'DONE')
);

const highPriorityNotifications = computed(() => 
  notificationStore.highPriorityNotifications
);

// Function to execute a task
async function executeTask(taskId) {
  try {
    await taskStore.executeTask(taskId, false);
  } catch (error) {
    console.error('Failed to execute task', error);
  }
}

// Function to mark notification as read
async function markAsRead(notificationId) {
  if (authStore.user) {
    try {
      await notificationStore.markNotificationAsRead(notificationId, authStore.user.id);
    } catch (error) {
      console.error('Failed to mark notification as read', error);
    }
  }
}

// Function to view task details
function viewTask(taskId) {
  router.push(`/tasks/${taskId}`);
}

// Function to create a new task
function createTask() {
  router.push('/tasks/create');
}

// Queue-related functions
function handleViewQueue(queue) {
  router.push(`/task-queues`);
}

function handleAddTask(queue) {
  router.push(`/task-queues`);
}

function handleProcessQueue(queueId) {
  if (authStore.user) {
    try {
      queueStore.processAllTasks(queueId);
    } catch (error) {
      console.error('Failed to process queue', error);
    }
  }
}

function handleCreateQueue() {
  router.push('/task-queues');
}
</script>

<template>
  <div class="dashboard">
    <header class="dashboard-header" style="background-color: #ffffff; color: #000000;">
      <img src="@/assets/images/silver_bullet.svg" alt="Silver Bullet Logo" class="logo">
      <p v-if="authStore.user">Hello, {{ authStore.user.name }}!</p>
      <h1>Welcome to Your Dashboard</h1>
    </header>

    <div v-if="isLoading" class="loading-indicator">
      <p>Loading your dashboard...</p>
    </div>

    <div v-else-if="errorMessage" class="error-message">
      <p>{{ errorMessage }}</p>
    </div>

    <div v-else class="dashboard-content" style="padding-bottom: 10rem;">
      <!-- Task Summary -->
      <section class="dashboard-section">
        <h2>Task Summary</h2>
        <div class="summary-cards">
          <div class="summary-card">
            <h3>Pending Tasks</h3>
            <p class="summary-count">{{ pendingTasks.length }}</p>
          </div>
          <div class="summary-card">
            <h3>Running Tasks</h3>
            <p class="summary-count">{{ runningTasks.length }}</p>
          </div>
          <div class="summary-card">
            <h3>Completed Tasks</h3>
            <p class="summary-count">{{ completedTasks.length }}</p>
          </div>
        </div>
      </section>

      <!-- Task Lists -->
      <section class="dashboard-section">
        <div class="section-header">
          <h2>Your Tasks</h2>
          <button @click="createTask" class="glowing-button">Create New Task</button>
        </div>

        <div v-if="taskStore.userTasks.length === 0" class="empty-state">
          <p>You don't have any tasks yet. Create your first task to get started!</p>
        </div>

        <div v-else class="task-lists">
          <!-- Pending Tasks -->
          <div class="task-list">
            <h3>Tasks Ready to Run</h3>
            <div v-if="pendingTasks.length === 0" class="empty-list">No pending tasks</div>
            <div v-else class="task-items">
              <div v-for="task in pendingTasks" :key="task.id" class="task-item">
                <div class="task-item-header">
                  <h4>{{ task.title }}</h4>
                  <span class="task-taskStatus">{{ task.taskStatus }}</span>
                </div>
                <p>{{ task.description }}</p>
                <div class="task-item-actions">
                  <button @click="executeTask(task.id)" class="action-button execute">Execute</button>
                  <button @click="viewTask(task.id)" class="action-button view">View</button>
                </div>
              </div>
            </div>
          </div>

          <!-- Running Tasks -->
          <div class="task-list">
            <h3>Running Tasks</h3>
            <div v-if="runningTasks.length === 0" class="empty-list">No running tasks</div>
            <div v-else class="task-items">
              <div v-for="task in runningTasks" :key="task.id" class="task-item running">
                <div class="task-item-header">
                  <h4>{{ task.title }}</h4>
                  <span class="task-taskStatus running">{{ task.taskStatus }}</span>
                </div>
                <p>{{ task.description }}</p>
                <div class="task-progress">
                  <div class="progress-bar">
                    <div class="progress-fill" :style="{ width: '30%' }"></div>
                  </div>
                  <span>In progress...</span>
                </div>
                <div class="task-item-actions">
                  <button @click="viewTask(task.id)" class="action-button view">View</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Notifications -->
      <!-- Task Queues Section -->
      <section class="dashboard-section">
        <div class="section-header">
          <h2>Task Queues</h2>
          <div class="header-actions">
            <button @click="router.push('/tasks/create')" class="secondary-button">Add Task</button>
            <button @click="router.push('/task-queues')" class="glowing-button">Manage Queues</button>
          </div>
        </div>
        <QueueList 
          :limit="2" 
          :compact="true"
          :showCreateButton="false"
          @view-queue="handleViewQueue"
          @add-task="handleAddTask"
          @process-queue="handleProcessQueue"
          @create-queue="handleCreateQueue"
        />
      </section>

      <section class="dashboard-section">
        <h2>Important Notifications</h2>
        <div v-if="highPriorityNotifications.length === 0" class="empty-state">
          <p>No important notifications at this time.</p>
        </div>
        <div v-else class="notifications-list">
          <div v-for="notification in highPriorityNotifications" :key="notification.id" class="notification-item">
            <div class="notification-content">
              <div class="notification-header">
                <span class="notification-type">{{ notification.type }}</span>
                <span class="notification-urgency high">{{ notification.urgency }}</span>
              </div>
              <p>{{ notification.message }}</p>
              <span class="notification-time">{{ new Date(notification.timestamp).toLocaleString() }}</span>
            </div>
            <button @click="markAsRead(notification.id)" class="action-button mark-read">Mark as Read</button>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>