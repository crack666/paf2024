<script setup>
import {defineProps, onMounted, ref, watch} from 'vue';
import {onBeforeUnmount} from 'vue';
import {useRouter} from 'vue-router';
import {useAuthStore} from '@/stores/auth';
import {useTaskStore} from '@/stores/task';
import {VueDraggableNext} from 'vue-draggable-next';
import { computed } from 'vue';

const props = defineProps({
  showCreateForm: {
    type: Boolean,
    default: false
  },
  taskId: {
    type: Number,
    default: null
  }
});

const draggable = VueDraggableNext;
const authStore = useAuthStore();
const taskStore = useTaskStore();
const router = useRouter();

const isLoading = ref(true);
const error = ref(null);
const successMessage = ref('');
const dragging = ref(false);
const activeTasks = ref([]);
const showCreateTaskModal = ref(false);
const showTaskDetails = ref(false);
const selectedTask = ref(null);
const taskProgressMap = ref({});
const pollTimer = ref(null);
const selectedDependencyId = ref('');
const dependencyError = ref('');

const newTask = ref({
  title: '',
  description: '',
  // Set default due date to 2 days in the past
  dueDate: (() => {
    const twoDaysAgo = new Date();
    twoDaysAgo.setDate(twoDaysAgo.getDate() - 2);
    return twoDaysAgo.toISOString().slice(0, 16);
  })(),
  assignedUserId: null,
  taskType: '',
  typeParams: {}
});

const taskTypes = ref([]);
const typeParamsFields = ref([]);

// Angenommen, taskStore.userTasks enthält alle Tasks:
const sortedTasks = computed(() => {
  return taskStore.userTasks.slice().sort((a, b) => {
    /*if (a.taskStatus === 'DONE' && b.taskStatus === 'DONE') {
      // Bei completed tasks: neueste zuerst, also absteigend nach completedAt
      if (!a.completedAt) return 1;
      if (!b.completedAt) return -1;
      return new Date(b.completedAt) - new Date(a.completedAt);
    } else {*/
      // Für alle anderen: sortiere nach createdAt absteigend
      return new Date(b.createdAt) - new Date(a.createdAt);
    //}
  });
});

// Redirect if not logged in
if (!authStore.isLoggedIn) {
  router.push('/login');
}

// Fetch data when component mounts
onMounted(async () => {
  if (authStore.isLoggedIn) {
    try {
      isLoading.value = true;
      error.value = null;

      await Promise.all([
        loadTasks(),
        loadTaskTypes()
      ]);

      // Handle taskId from route if provided
      if (props.taskId) {
        await loadTaskDetails(props.taskId);
      }

      // Register event listener for task progress updates via WebSocket
      document.addEventListener('task-progress-update', handleTaskProgressUpdate);
    } catch (err) {
      console.error('Error loading tasks data:', err);
      error.value = 'Failed to load task data. Please try again.';
    } finally {
      isLoading.value = false;
    }
  }
});

// Watch for changes in taskId
watch(() => props.taskId, async (newTaskId) => {
  if (newTaskId) {
    await loadTaskDetails(newTaskId);
  } else {
    showTaskDetails.value = false;
    selectedTask.value = null;
  }
});

// Load tasks
async function loadTasks() {
  try {
    await taskStore.fetchUserTasks(authStore.user.id);
    activeTasks.value = [...taskStore.userTasks];
  } catch (err) {
    console.error('Error loading tasks:', err);
    throw err;
  }
}

// Load available task types
async function loadTaskTypes() {
  try {
    taskTypes.value = await taskStore.fetchTaskTypes();
  } catch (err) {
    console.error('Error loading task types:', err);
    throw err;
  }
}

// Display the create task modal
function openCreateTaskModal() {
  // Set date to current date minus 2 days
  const twoDaysAgo = new Date();
  twoDaysAgo.setDate(twoDaysAgo.getDate() - 2);
  
  newTask.value = {
    title: '',
    description: '',
    dueDate: twoDaysAgo.toISOString().slice(0, 16),
    assignedUserId: authStore.user.id,
    taskType: '',
    typeParams: {}
  };
  showCreateTaskModal.value = true;
}

// Close the create task modal
function closeCreateTaskModal() {
  showCreateTaskModal.value = false;
}

// Handle task type selection - update param fields based on selected type
function handleTaskTypeChange() {
  const selectedType = taskTypes.value.find(t => t.className === newTask.value.taskType);
  if (selectedType && selectedType.parameters) {
    typeParamsFields.value = selectedType.parameters;

    // Initialize parameters with default values
    newTask.value.typeParams = {};
    selectedType.parameters.forEach(param => {
      if (param.defaultValue !== undefined) {
        newTask.value.typeParams[param.name] = param.defaultValue;
      } else {
        // Set default values based on type
        switch (param.type) {
          case 'number':
            newTask.value.typeParams[param.name] = 0;
            break;
          case 'boolean':
            newTask.value.typeParams[param.name] = false;
            break;
          default:
            newTask.value.typeParams[param.name] = '';
        }
      }
    });
  } else {
    typeParamsFields.value = [];
    newTask.value.typeParams = {};
  }
}

// Create a new task
async function createTask() {
  try {
    isLoading.value = true;
    error.value = null;
    successMessage.value = '';

    // Format the task data
    const taskData = {
      title: newTask.value.title,
      description: newTask.value.description,
      dueDate: new Date(newTask.value.dueDate).toISOString(),
      assignedUserId: authStore.user.id,
      taskType: newTask.value.taskType,
      typeParams: newTask.value.typeParams
    };

    // Create the task via the API
    const createdTask = await taskStore.createTask(taskData);

    // Show success message
    successMessage.value = `Task "${createdTask.title}" created successfully!`;

    // Reload the tasks
    await loadTasks();

    // If we're in the modal, close it
    if (showCreateTaskModal.value) {
      setTimeout(() => {
        closeCreateTaskModal();
        successMessage.value = '';
      }, 2000);
    } else {
      // If we're on the create page, redirect back to tasks list after delay
      setTimeout(() => {
        router.push('/tasks');
        successMessage.value = '';
      }, 1500);
    }
  } catch (err) {
    console.error('Error creating task:', err);
    error.value = 'Failed to create task. Please try again.';
  } finally {
    isLoading.value = false;
  }
}

// Load task details by ID
async function loadTaskDetails(taskId) {
  try {
    isLoading.value = true;
    error.value = null;

    // First check if the task is already in our local data
    let task = taskStore.userTasks.find(t => t.id === taskId);

    // If not, fetch it from the API
    if (!task) {
      try {
        task = await taskStore.fetchTaskById(taskId);
      } catch (error) {
        console.error('Error fetching task:', error);
        error.value = 'Failed to load task details. Please try again.';
        return;
      }
    }

    // Set selected task and show details
    if (task) {
      // Create a copy of the task so we can add additional properties without modifying the store
      selectedTask.value = { ...task };
      
      // Map dependencyIds to actual task objects if they exist
      if (selectedTask.value.dependencyIds && selectedTask.value.dependencyIds.length > 0) {
        // Find the referenced tasks in the store
        selectedTask.value.dependencies = selectedTask.value.dependencyIds.map(depId => {
          // Find the task in our local store
          const dependencyTask = taskStore.userTasks.find(t => t.id === depId);
          
          // If we found it, return it; otherwise fetch it or return a placeholder
          if (dependencyTask) {
            return dependencyTask;
          } else {
            // Return a placeholder with the ID that will be shown while we fetch in background
            const placeholder = { id: depId, title: `Task #${depId}`, taskStatus: 'UNKNOWN' };
            
            // Try to fetch the missing dependency in the background
            taskStore.fetchTaskById(depId)
              .then(fetchedTask => {
                // Once fetched, find the dependency in the array and update it
                const index = selectedTask.value.dependencies.findIndex(d => d.id === depId);
                if (index >= 0 && selectedTask.value) {
                  selectedTask.value.dependencies[index] = fetchedTask;
                }
              })
              .catch(err => console.error(`Could not fetch dependency task #${depId}:`, err));
              
            return placeholder;
          }
        });
      } else {
        // If no dependencyIds, initialize an empty array
        selectedTask.value.dependencies = [];
      }
      
      showTaskDetails.value = false;

      // If task is running, start polling for progress
      if (task.taskStatus === 'RUNNING') {
        startProgressPolling(task.id);
      }

      // If task is done and doesn't have a result, try to fetch it
      if (task.taskStatus === 'DONE' && !task.result) {
        try {
          taskStore.fetchTaskResult(task.id)
            .then(result => {
              if (result && selectedTask.value && selectedTask.value.id === task.id) {
                selectedTask.value.result = result;
              }
            })
            .catch(err => console.log('Could not fetch task result:', err));
        } catch (err) {
          console.log('Error fetching task result:', err);
        }
      }
    } else {
      error.value = 'Task not found';
    }
  } finally {
    isLoading.value = false;
  }
}

// Cleanup event listener on component unmount
onBeforeUnmount(() => {
  document.removeEventListener('task-progress-update', handleTaskProgressUpdate);
});

// View task details
function viewTaskDetails(task) {
  // If we're in list view, update the URL to the task's detail page
  router.push(`/tasks/${task.id}`);

  // No need to set task details here as the watch handler will take care of it
}

// Close task details modal
function closeTaskDetails() {
  showTaskDetails.value = false;
  selectedTask.value = null;

  // If we were viewing a task detail page, go back to the tasks list
  if (props.taskId) {
    router.push('/tasks');
  }
}

// Function to execute a task
async function executeTask(taskId) {
  error.value = null;
  successMessage.value = '';

  try {
    await taskStore.executeTask(taskId, false);
    successMessage.value = 'Task execution started successfully!';

    // Refresh tasks after a short delay
    setTimeout(async () => {
      await loadTasks();

      // Check if this task is currently selected
      if (selectedTask.value && selectedTask.value.id === taskId) {
        // Update selected task with the latest data
        const updatedTask = taskStore.userTasks.find(t => t.id === taskId);
        if (updatedTask) {
          selectedTask.value = updatedTask;
        }
      }
    }, 1000);
  } catch (err) {
    console.error('Error executing task:', err);
    error.value = 'Failed to execute task. Please try again.';
  }
}

// Add a dependency to a task
async function addDependency() {
  if (!selectedDependencyId.value) return;

  dependencyError.value = '';
  try {
    // Call API to add dependency
    const updatedTask = await taskStore.addTaskDependency(selectedTask.value.id, selectedDependencyId.value);
    
    // Reset selection
    selectedDependencyId.value = '';
    
    // Success message
    successMessage.value = 'Dependency added successfully!';
    
    // Update the dependencies array in the selectedTask
    if (updatedTask && updatedTask.dependencyIds) {
      // Find the newly added dependency ID (the one that wasn't there before)
      const newDependencyIds = updatedTask.dependencyIds.filter(
        id => !selectedTask.value.dependencyIds || !selectedTask.value.dependencyIds.includes(id)
      );
      
      // Update the selectedTask dependencyIds array
      selectedTask.value.dependencyIds = updatedTask.dependencyIds;
      
      // For each new dependency ID, find or fetch the task and add it to dependencies
      for (const newDepId of newDependencyIds) {
        // Look for the task in the store
        const dependencyTask = taskStore.userTasks.find(t => t.id === newDepId);
        
        if (dependencyTask) {
          // If found in store, add to dependencies array
          if (!selectedTask.value.dependencies) {
            selectedTask.value.dependencies = [];
          }
          selectedTask.value.dependencies.push(dependencyTask);
        } else {
          // If not in store, fetch it and add
          try {
            const fetchedTask = await taskStore.fetchTaskById(newDepId);
            if (!selectedTask.value.dependencies) {
              selectedTask.value.dependencies = [];
            }
            selectedTask.value.dependencies.push(fetchedTask);
          } catch (fetchErr) {
            console.error(`Could not fetch dependency task #${newDepId}:`, fetchErr);
            // Add a placeholder anyway
            if (!selectedTask.value.dependencies) {
              selectedTask.value.dependencies = [];
            }
            selectedTask.value.dependencies.push({ 
              id: newDepId, 
              title: `Task #${newDepId}`, 
              taskStatus: 'UNKNOWN' 
            });
          }
        }
      }
      
      // Refresh the tasks list to make sure changes are reflected in the main list
      if (authStore.isLoggedIn) {
        await loadTasks();
      }
    }
    
    // Clear success message after a few seconds
    setTimeout(() => {
      successMessage.value = '';
    }, 3000);
  } catch (err) {
    console.error('Error adding dependency:', err);
    // Check if this is a deadlock error (400 taskStatus code)
    if (err.response && err.response.status === 400) {
      dependencyError.value = 'Deadlock detected! This dependency would create a circular reference.';
    } else {
      dependencyError.value = 'Failed to add dependency. Please try again.';
    }
  }
}

// Remove a dependency from a task
async function removeTaskDependency(taskId, dependencyId) {
  dependencyError.value = '';
  try {
    // Call API to remove dependency
    const updatedTask = await taskStore.removeTaskDependency(taskId, dependencyId);
    
    // Success message
    successMessage.value = 'Dependency removed successfully!';
    
    // Update the local selectedTask state
    if (updatedTask) {
      // Update the dependencyIds array
      selectedTask.value.dependencyIds = updatedTask.dependencyIds || [];
      
      // Remove the dependency from the dependencies array
      if (selectedTask.value.dependencies) {
        selectedTask.value.dependencies = selectedTask.value.dependencies.filter(
          dep => dep.id !== dependencyId
        );
      }
      
      // Refresh the tasks list to make sure changes are reflected in the main list
      if (authStore.isLoggedIn) {
        await loadTasks();
      }
    }
    
    // Clear success message after a few seconds
    setTimeout(() => {
      successMessage.value = '';
    }, 3000);
  } catch (err) {
    console.error('Error removing dependency:', err);
    dependencyError.value = 'Failed to remove dependency. Please try again.';
  }
}

// Format date for display
function formatDate(dateString) {
  if (!dateString) return '';
  return new Date(dateString).toLocaleString();
}

// Extract task type from taskClassName or use taskType if available
function getTaskType(task) {
  if (task.taskType) {
    return task.taskType;
  } else if (task.taskClassName) {
    // Extract the class name from the fully qualified name
    const parts = task.taskClassName.split('.');
    return parts[parts.length - 1];
  }
  return 'Standard Task';
}

/**
 * Handles task progress updates received via WebSocket.
 * Expects event.detail to contain an object with at least:
 *  - taskId
 *  - percentComplete (number)
 *  - currentIteration
 *  - totalIterations
 *  - (optional) message
 */
function handleTaskProgressUpdate(event) {
  const data = event.detail;
  console.log(data);
  if (data && data.taskId != null) {
    if (data.percentComplete === undefined && data.currentIteration != null && data.totalIterations) {
      data.percentComplete = Math.floor((data.currentIteration / data.totalIterations) * 100);
    }
    // Speichere den Fortschritt für den Task in unserem Mapping
    taskProgressMap.value[data.taskId] = data;

    // Hol den Task aus dem Store und, falls er noch "CREATED" oder "QUEUED" ist, setze ihn auf "RUNNING"
    const task = taskStore.userTasks.find(t => t.id === data.taskId);
    if (task && (task.taskStatus === 'CREATED' || task.taskStatus === 'QUEUED')) {
      task.taskStatus = 'RUNNING';
    }

    // Falls der Task abgeschlossen ist, z.B. durch den completed-Flag oder 100%
    if (data.completed || data.percentComplete === 100) {
      // Aktualisiere den Task-Status im Store, damit die UI den Task als Completed anzeigt.
      // Dies kann z.B. durch einen API-Aufruf oder durch eine direkte Änderung erfolgen:
      // taskStore.markTaskAsCompleted(data.taskId);
      // Oder setze den Status direkt:
      const task = taskStore.userTasks.find(t => t.id === data.taskId);
      if (task) {
        task.taskStatus = 'DONE';
      }
    }
  }
}

// Clean up on component unmount
onMounted(() => {
});
</script>

<template>
  <div class="tasks-container" style="padding-bottom: 80rem">
    <!-- Loading and error states -->
    <div v-if="isLoading && !showCreateTaskModal && !showTaskDetails" class="loading-state">
      Loading tasks...
    </div>

    <div v-else-if="error && !showCreateTaskModal && !showTaskDetails" class="error-state">
      {{ error }}
    </div>

    <!-- Tasks content - hide the list if viewing a specific task or creating a new task -->
    <div v-else-if="!props.taskId && !props.showCreateForm" class="tasks-content">
      <div class="tasks-header">
        <h1>Your Tasks</h1>
        <button @click="openCreateTaskModal" class="glowing-button">Create New Task</button>
      </div>

      <div v-if="successMessage" class="feedback success">
        {{ successMessage }}
      </div>

      <!-- Empty state -->
      <div v-if="taskStore.userTasks.length === 0" class="empty-state">
        <p>You don't have any tasks yet. Create your first task to get started!</p>
      </div>

      <!-- Task Board -->
      <div v-else class="task-board">
        <div class="task-column">
          <h2>Created</h2>
          <div class="task-list">
            <div v-for="task in taskStore.userTasks.filter(t => t.taskStatus === 'CREATED')"
                :key="task.id"
                class="task-card"
                @click="viewTaskDetails(task)">
              <div class="task-header">
                <h3>{{ task.title }}</h3>
                <span class="task-taskStatus">{{ task.taskStatus }}</span>
              </div>
              <p class="task-description">{{ task.description }}</p>
              <div class="task-footer">
                <span class="task-due-date">Due: {{ formatDate(task.dueDate) }}</span>
                <button @click.stop="executeTask(task.id)" class="action-button">Execute</button>
              </div>
            </div>
          </div>
        </div>

        <div class="task-column">
          <h2>Queued</h2>
          <div class="task-list">
            <div v-for="task in taskStore.userTasks.filter(t => t.taskStatus === 'QUEUED')"
                 :key="task.id"
                 class="task-card"
                 @click="viewTaskDetails(task)">
              <div class="task-header">
                <h3>{{ task.title }}</h3>
                <span class="task-taskStatus">{{ task.taskStatus }}</span>
              </div>
              <p class="task-description">{{ task.description }}</p>
              <div class="task-footer">
                <span class="task-due-date">Due: {{ formatDate(task.dueDate) }}</span>
                <button @click.stop="executeTask(task.id)" class="action-button">Execute</button>
              </div>
            </div>
          </div>
        </div>

        <div class="task-column">
          <h2>Running</h2>
          <div class="task-list">
            <div v-for="task in taskStore.userTasks.filter(t => t.taskStatus === 'RUNNING')"
                 :key="task.id"
                 class="task-card running"
                 @click="viewTaskDetails(task)">
              <div class="task-header">
                <h3>{{ task.title }}</h3>
                <span class="task-taskStatus running">{{ task.taskStatus }}</span>
              </div>
              <p class="task-description">{{ task.description }}</p>
              <div class="task-progress">
                <div class="progress-bar">
                  <!-- Hier wird der Fortschritt des jeweiligen Tasks aus taskProgressMap genutzt -->
                  <div class="progress-fill" :style="{ width: (taskProgressMap[task.id]?.percentComplete || 0) + '%' }"></div>
                </div>
                <span class="progress-text">
              {{ taskProgressMap[task.id]?.percentComplete || 0 }}% Complete
            </span>
              </div>
            </div>
          </div>
        </div>

        <div class="task-column" style="padding-bottom: 120px">
          <h2>Completed</h2>
          <div class="task-list">
            <div v-for="task in sortedTasks.filter(t => t.taskStatus === 'DONE')"
                :key="task.id"
                class="task-card completed"
                @click="viewTaskDetails(task)">
              <div class="task-header">
                <h3>{{ task.title }}</h3>
                <span class="task-taskStatus completed">{{ task.taskStatus }}</span>
              </div>
              <p class="task-description">{{ task.description }}</p>
              <div class="task-footer">
                <span class="task-completion-date">Completed: {{ formatDate(task.completedAt) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Task Create Form Direct View for /tasks/create -->
    <div v-else-if="props.showCreateForm && !isLoading" class="task-create-page">
      <div class="task-detail-header-nav">
        <button @click="router.push('/tasks')" class="back-button">
          ← Back to Tasks
        </button>
      </div>

      <div class="task-create-form">
        <h1>Create New Task</h1>

        <div v-if="successMessage" class="feedback success">
          {{ successMessage }}
        </div>

        <div v-if="error" class="feedback error">
          {{ error }}
        </div>

        <form @submit.prevent="createTask">
          <div class="form-group">
            <label for="taskTitle">Title</label>
            <input id="taskTitle" v-model="newTask.title" type="text" required />
          </div>

          <div class="form-group">
            <label for="taskDescription">Description</label>
            <textarea id="taskDescription" v-model="newTask.description" rows="4"></textarea>
          </div>

          <div class="form-group">
            <label for="taskDueDate">Due Date</label>
            <input id="taskDueDate" v-model="newTask.dueDate" type="datetime-local" required />
          </div>

          <div class="form-group">
            <label for="taskType">Task Type</label>
            <select id="taskType" v-model="newTask.taskType" @change="handleTaskTypeChange" required>
              <option value="" disabled>Select a task type</option>
              <option v-for="type in taskTypes" :key="type.className" :value="type.className">
                {{ type.className }}
              </option>
            </select>
          </div>

          <!-- Dynamic fields based on task type -->
          <div v-if="typeParamsFields.length > 0" class="type-params">
            <h3>Task Parameters</h3>

            <div v-for="param in typeParamsFields" :key="param.name" class="form-group">
              <label :for="'param-' + param.name">{{ param.displayName }}</label>

              <!-- For text input -->
              <input v-if="param.type === 'string'"
                    :id="'param-' + param.name"
                    v-model="newTask.typeParams[param.name]"
                    type="text" />

              <!-- For number input -->
              <input v-else-if="param.type === 'number'"
                    :id="'param-' + param.name"
                    v-model.number="newTask.typeParams[param.name]"
                    type="number"
                    step="any" />

              <!-- For boolean input -->
              <div v-else-if="param.type === 'boolean'" class="checkbox-group">
                <input :id="'param-' + param.name"
                      v-model="newTask.typeParams[param.name]"
                      type="checkbox" />
                <label :for="'param-' + param.name">{{ param.description || 'Enable' }}</label>
              </div>
            </div>
          </div>

          <div class="form-actions">
            <button type="submit" class="submit-button" :disabled="isLoading">
              {{ isLoading ? 'Creating...' : 'Create Task' }}
            </button>
            <button type="button" @click="router.push('/tasks')" class="cancel-button">Cancel</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Task Detail Direct View for /tasks/:id -->
    <div v-else-if="props.taskId && !isLoading" class="task-detail-page" >
      <div class="task-detail-header-nav">
        <button @click="router.push('/tasks')" class="back-button">
          ← Back to Tasks
        </button>
      </div>

      <!-- Show task details immediately using the modal content but styled for a page -->
      <div v-if="selectedTask" class="task-detail-content">
        <div class="task-detail-header">
          <h1>{{ selectedTask.title }}</h1>
          <span :class="['task-taskStatus', selectedTask.taskStatus.toLowerCase()]">{{ selectedTask.taskStatus }}</span>
        </div>

        <div class="task-detail-section">
          <h2>Description</h2>
          <p>{{ selectedTask.description || 'No description provided.' }}</p>
        </div>

        <div class="task-detail-section">
          <h2>Details</h2>
          <div class="task-detail-grid">
            <div class="detail-item">
              <span class="detail-label">Task ID:</span>
              <span class="detail-value">{{ selectedTask.id }}</span>
            </div>

            <div class="detail-item">
              <span class="detail-label">Type:</span>
              <span class="detail-value">{{ getTaskType(selectedTask) }}</span>
            </div>

            <div class="detail-item">
              <span class="detail-label">Created:</span>
              <span class="detail-value">{{ formatDate(selectedTask.createdAt) }}</span>
            </div>

            <div class="detail-item">
              <span class="detail-label">Due Date:</span>
              <span class="detail-value">{{ formatDate(selectedTask.dueDate) }}</span>
            </div>

            <div v-if="selectedTask.taskStatus === 'DONE'" class="detail-item">
              <span class="detail-label">Completed:</span>
              <span class="detail-value">{{ formatDate(selectedTask.completedAt) }}</span>
            </div>
          </div>
        </div>

        <!-- Task Parameters -->
        <div v-if="selectedTask.typeParams" class="task-detail-section">
          <h2>Parameters</h2>
          <div class="task-parameters">
            <div v-for="(value, key) in selectedTask.typeParams" :key="key" class="parameter-item">
              <span class="param-name">{{ key }}:</span>
              <span class="param-value">{{ value }}</span>
            </div>
          </div>
        </div>

        <!-- Task Dependencies -->
        <div class="task-detail-section">
          <h2>Dependencies</h2>
          <div v-if="successMessage" class="feedback success">
            {{ successMessage }}
          </div>

          <div v-if="selectedTask.dependencies && selectedTask.dependencies.length > 0" class="task-dependencies">
            <div v-for="dependency in selectedTask.dependencies" :key="dependency.id" class="dependency-item">
              <div class="dependency-info">
                <p class="dependency-title">{{ dependency.title }} (Task Id: {{ dependency.id }})</p>
                <span class="dependency-taskStatus" :class="dependency.taskStatus.toLowerCase()">{{ dependency.taskStatus }}</span>
              </div>
              <button @click="removeTaskDependency(selectedTask.id, dependency.id)" class="remove-dependency-button">
                Remove
              </button>
            </div>
          </div>
          <div v-else class="no-dependencies">
            No dependencies defined for this task.
          </div>

          <!-- Add Dependency Form -->
          <div v-if="selectedTask.taskStatus !== 'DONE'" class="add-dependency-form">
            <h3>Add Dependency</h3>
            <div class="add-dependency-controls">
              <select v-model="selectedDependencyId" class="dependency-select">
                <option value="" disabled>Select a task</option>
                <option v-for="task in taskStore.userTasks.filter(t => t.id !== selectedTask.id)"
                        :key="task.id"
                        :value="task.id">
                  {{ task.title }} ({{ task.taskStatus }})
                </option>
              </select>
              <button @click="addDependency"
                      :disabled="!selectedDependencyId"
                      class="add-dependency-button">
                Add Dependency
              </button>
            </div>
            <div v-if="dependencyError" class="dependency-error">
              {{ dependencyError }}
            </div>
          </div>
        </div>

        <!-- Task Progress -->
        <div v-for="task in taskStore.userTasks.filter(t => t.taskStatus === 'RUNNING')"
             :key="task.id"
             class="task-card running"
             @click="viewTaskDetails(task)">
          <div class="task-header">
            <h3>{{ task.title }}</h3>
            <span class="task-taskStatus running">{{ task.taskStatus }}</span>
          </div>
          <p class="task-description">{{ task.description }}</p>
          <div class="task-progress">
            <div class="progress-bar">
              <!-- Dynamisch: -->
              <div class="progress-fill" :style="{ width: (taskProgressMap[task.id]?.percentComplete || 0) + '%' }"></div>
            </div>
            <span class="progress-text">{{ taskProgressMap[task.id]?.percentComplete || 0 }}% Complete</span>
          </div>
        </div>

        <!-- Task Result -->
        <div v-if="selectedTask.taskStatus === 'DONE'" class="task-detail-section">
          <h2>Result</h2>
          <div v-if="selectedTask.result" class="task-result">
            <pre>{{ typeof selectedTask.result === 'object' ? JSON.stringify(selectedTask.result, null, 2) : selectedTask.result }}</pre>
          </div>
          <div v-else class="task-result">
            <p>No result available. The task completed successfully but did not produce a result.</p>
          </div>
        </div>

        <div class="actions-container">
          <button v-if="selectedTask.taskStatus === 'CREATED'"
                 @click="executeTask(selectedTask.id)"
                 class="action-button execute">
            Execute Task
          </button>
        </div>
      </div>

      <div v-else class="error-state">
        Task not found or still loading...
      </div>
    </div>

    <!-- Create Task Modal -->
    <div v-if="showCreateTaskModal" class="modal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>Create New Task</h2>
          <button @click="closeCreateTaskModal" class="close-button">&times;</button>
        </div>

        <div class="modal-body">
          <div v-if="successMessage" class="feedback success">
            {{ successMessage }}
          </div>

          <div v-if="error" class="feedback error">
            {{ error }}
          </div>

          <form @submit.prevent="createTask">
            <div class="form-group">
              <label for="taskTitle">Title</label>
              <input id="taskTitle" v-model="newTask.title" type="text" required />
            </div>

            <div class="form-group">
              <label for="taskDescription">Description</label>
              <textarea id="taskDescription" v-model="newTask.description" rows="4"></textarea>
            </div>

            <div class="form-group">
              <label for="taskDueDate">Due Date</label>
              <input id="taskDueDate" v-model="newTask.dueDate" type="datetime-local" required />
            </div>

            <div class="form-group">
              <label for="taskType">Task Type</label>
              <select id="taskType" v-model="newTask.taskType" @change="handleTaskTypeChange" required>
                <option value="" disabled>Select a task type</option>
                <option v-for="type in taskTypes" :key="type.className" :value="type.className">
                  {{ type.className }}
                </option>
              </select>
            </div>

            <!-- Dynamic fields based on task type -->
            <div v-if="typeParamsFields.length > 0" class="type-params">
              <h3>Task Parameters</h3>

              <div v-for="param in typeParamsFields" :key="param.name" class="form-group">
                <label :for="'param-' + param.name">{{ param.displayName }}</label>

                <!-- For text input -->
                <input v-if="param.type === 'string'"
                      :id="'param-' + param.name"
                      v-model="newTask.typeParams[param.name]"
                      type="text" />

                <!-- For number input -->
                <input v-else-if="param.type === 'number'"
                      :id="'param-' + param.name"
                      v-model.number="newTask.typeParams[param.name]"
                      type="number"
                      step="any" />

                <!-- For boolean input -->
                <div v-else-if="param.type === 'boolean'" class="checkbox-group">
                  <input :id="'param-' + param.name"
                        v-model="newTask.typeParams[param.name]"
                        type="checkbox" />
                  <label :for="'param-' + param.name">{{ param.description || 'Enable' }}</label>
                </div>
              </div>
            </div>

            <div class="modal-actions">
              <button type="submit" class="submit-button" :disabled="isLoading">
                {{ isLoading ? 'Creating...' : 'Create Task' }}
              </button>
              <button type="button" @click="closeCreateTaskModal" class="cancel-button">Cancel</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Task Details Modal -->
    <div v-if="showTaskDetails && selectedTask" class="modal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>Task Details</h2>
          <button @click="closeTaskDetails" class="close-button">&times;</button>
        </div>

        <div class="modal-body"  >
          <div class="task-detail-header">
            <h3>{{ selectedTask.title }}</h3>
            <span :class="['task-taskStatus', selectedTask.taskStatus.toLowerCase()]">{{ selectedTask.taskStatus }}</span>
          </div>

          <div class="task-detail-section">
            <h4>Description</h4>
            <p>{{ selectedTask.description || 'No description provided.' }}</p>
          </div>

          <div class="task-detail-section">
            <h4>Details</h4>
            <div class="task-detail-grid">
              <div class="detail-item">
                <span class="detail-label">Task ID:</span>
                <span class="detail-value">{{ selectedTask.id }}</span>
              </div>

              <div class="detail-item">
                <span class="detail-label">Type:</span>
                <span class="detail-value">{{ selectedTask.taskType }}</span>
              </div>

              <div class="detail-item">
                <span class="detail-label">Created:</span>
                <span class="detail-value">{{ formatDate(selectedTask.createdAt) }}</span>
              </div>

              <div class="detail-item">
                <span class="detail-label">Due Date:</span>
                <span class="detail-value">{{ formatDate(selectedTask.dueDate) }}</span>
              </div>

              <div v-if="selectedTask.taskStatus === 'DONE'" class="detail-item">
                <span class="detail-label">Completed:</span>
                <span class="detail-value">{{ formatDate(selectedTask.completedAt) }}</span>
              </div>
            </div>
          </div>

          <!-- Task Parameters -->
          <div v-if="selectedTask.typeParams" class="task-detail-section">
            <h4>Parameters</h4>
            <div class="task-parameters">
              <div v-for="(value, key) in selectedTask.typeParams" :key="key" class="parameter-item">
                <span class="param-name">{{ key }}:</span>
                <span class="param-value">{{ value }}</span>
              </div>
            </div>
          </div>

          <!-- Task Progress Bereich -->
          <div v-if="selectedTask.taskStatus === 'RUNNING'" class="task-detail-section">
            <h2>Progress</h2>
            <div class="task-progress-detail">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: (taskProgressMap[selectedTask.id]?.percentComplete || 0) + '%' }"></div>
              </div>
              <span class="progress-text">{{ taskProgressMap[selectedTask.id]?.percentComplete || 0 }}% Complete</span>
              <div v-if="taskProgressMap[selectedTask.id]?.currentIteration && taskProgressMap[selectedTask.id]?.totalIterations" class="progress-info">
                <span>{{ taskProgressMap[selectedTask.id].currentIteration }} / {{ taskProgressMap[selectedTask.id].totalIterations }} iterations</span>
              </div>
              <div v-if="taskProgressMap[selectedTask.id]?.message" class="progress-message">
                {{ taskProgressMap[selectedTask.id].message }}
              </div>
            </div>
          </div>

          <!-- Task Result -->
          <div v-if="selectedTask.taskStatus === 'DONE' && selectedTask.result" class="task-detail-section">
            <h4>Result</h4>
            <div class="task-result">
              <pre>{{ typeof selectedTask.result === 'object' ? JSON.stringify(selectedTask.result, null, 2) : selectedTask.result }}</pre>
            </div>
          </div>

          <div class="modal-actions">
            <button v-if="selectedTask.taskStatus === 'CREATED'"
                   @click="executeTask(selectedTask.id)"
                   class="action-button execute">
              Execute Task
            </button>
            <button @click="closeTaskDetails" class="cancel-button">Close</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>