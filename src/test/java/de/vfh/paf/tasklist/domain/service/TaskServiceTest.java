package de.vfh.paf.tasklist.domain.service;

import de.vfh.paf.tasklist.domain.model.Status;
import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.repository.TaskRepository;
import de.vfh.paf.tasklist.infrastructure.persistence.TaskRepositoryInMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceTest {
    private TaskService taskService;
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository = new TaskRepositoryInMemory();
        taskService = new TaskService(taskRepository);
    }

    @Test
    void shouldCreateTask() {
        // Arrange
        String title = "New Task";
        String description = "Task description";
        LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
        int userId = 100;

        // Act
        Task createdTask = taskService.createTask(title, description, dueDate, userId);

        // Assert
        assertNotNull(createdTask);
        assertEquals(title, createdTask.getTitle());
        assertEquals(description, createdTask.getDescription());
        assertEquals(dueDate, createdTask.getDueDate());
        assertEquals(userId, createdTask.getAssignedUserId());
        assertEquals(Status.CREATED, createdTask.getStatus());
        
        // Verify task was saved in repository
        Optional<Task> savedTask = taskRepository.findById(createdTask.getId());
        assertTrue(savedTask.isPresent());
        assertEquals(createdTask, savedTask.get());
    }

    @Test
    void shouldUpdateTask() {
        // Arrange
        Task task = taskService.createTask("Original Task", "Original description", 
                LocalDateTime.now().plusDays(1), 100);
        
        String newTitle = "Updated Task";
        String newDescription = "Updated description";
        LocalDateTime newDueDate = LocalDateTime.now().plusDays(2);
        Status newStatus = Status.QUEUED;

        // Act
        Task updatedTask = taskService.updateTask(task.getId(), newTitle, newDescription, newDueDate, newStatus);

        // Assert
        assertNotNull(updatedTask);
        assertEquals(newTitle, updatedTask.getTitle());
        assertEquals(newDescription, updatedTask.getDescription());
        assertEquals(newDueDate, updatedTask.getDueDate());
        assertEquals(newStatus, updatedTask.getStatus());
        
        // Verify task was updated in repository
        Optional<Task> savedTask = taskRepository.findById(task.getId());
        assertTrue(savedTask.isPresent());
        assertEquals(updatedTask, savedTask.get());
    }

    @Test
    void shouldCompleteTask() {
        // Arrange
        Task task = taskService.createTask("Test Task", "Description", 
                LocalDateTime.now().plusDays(1), 100);
        assertFalse(task.isCompleted());

        // Act
        Task completedTask = taskService.completeTask(task.getId());

        // Assert
        assertTrue(completedTask.isCompleted());
        assertEquals(Status.DONE, completedTask.getStatus());
        
        // Verify task was updated in repository
        Optional<Task> savedTask = taskRepository.findById(task.getId());
        assertTrue(savedTask.isPresent());
        assertTrue(savedTask.get().isCompleted());
    }

    @Test
    void shouldAddDependency() {
        // Arrange
        Task task = taskService.createTask("Main Task", "Description", 
                LocalDateTime.now().plusDays(2), 100);
        Task dependency = taskService.createTask("Dependency Task", "Description", 
                LocalDateTime.now().plusDays(1), 100);

        // Act
        Task taskWithDependency = taskService.addDependency(task.getId(), dependency.getId());

        // Assert
        assertEquals(1, taskWithDependency.getDependencies().size());
        assertTrue(taskWithDependency.getDependencies().contains(dependency));
        
        // Verify task was updated in repository
        Optional<Task> savedTask = taskRepository.findById(task.getId());
        assertTrue(savedTask.isPresent());
        assertEquals(1, savedTask.get().getDependencies().size());
        assertTrue(savedTask.get().getDependencies().contains(dependency));
    }

    @Test
    void shouldRemoveDependency() {
        // Arrange
        Task task = taskService.createTask("Main Task", "Description", 
                LocalDateTime.now().plusDays(2), 100);
        Task dependency = taskService.createTask("Dependency Task", "Description", 
                LocalDateTime.now().plusDays(1), 100);
        taskService.addDependency(task.getId(), dependency.getId());

        // Act
        Task taskWithoutDependency = taskService.removeDependency(task.getId(), dependency.getId());

        // Assert
        assertEquals(0, taskWithoutDependency.getDependencies().size());
        
        // Verify task was updated in repository
        Optional<Task> savedTask = taskRepository.findById(task.getId());
        assertTrue(savedTask.isPresent());
        assertEquals(0, savedTask.get().getDependencies().size());
    }

    @Test
    void shouldDetectDeadlock() {
        // Arrange
        Task task1 = taskService.createTask("Task 1", "Description", 
                LocalDateTime.now().plusDays(1), 100);
        Task task2 = taskService.createTask("Task 2", "Description", 
                LocalDateTime.now().plusDays(1), 100);
        Task task3 = taskService.createTask("Task 3", "Description", 
                LocalDateTime.now().plusDays(1), 100);
        
        // Create a cycle: task1 -> task2 -> task3 -> task1
        taskService.addDependency(task1.getId(), task2.getId());
        taskService.addDependency(task2.getId(), task3.getId());
        taskService.addDependency(task3.getId(), task1.getId());

        // Act
        boolean hasDeadlock = taskService.detectDeadlocks();

        // Assert
        assertTrue(hasDeadlock);
    }
}