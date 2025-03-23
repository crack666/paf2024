package de.vfh.paf.tasklist.domain.service;

import de.vfh.paf.tasklist.domain.model.Status;
import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.repository.TaskRepository;
import de.vfh.paf.tasklist.domain.repository.TaskResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(TaskService.class)
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskResultRepository taskResultRepository;

    @BeforeEach
    void setUp() {
        // Clear the repositories before each test
        taskRepository.deleteAll();
        taskResultRepository.deleteAll();
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
        assertEquals(createdTask.getId(), savedTask.get().getId());
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
        assertEquals(updatedTask.getId(), savedTask.get().getId());
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
        assertTrue(savedTask.get().getDependencies().stream()
                .anyMatch(dep -> dep.getId().equals(dependency.getId())));
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

    @Test
    void shouldDetectDeadlockWithTwoTasks() {
        // Arrange
        Task task1 = taskService.createTask("Task 1", "Description",
                LocalDateTime.now().plusDays(1), 100);
        Task task2 = taskService.createTask("Task 2", "Description",
                LocalDateTime.now().plusDays(1), 100);

        // Create a cycle: task1 -> task2 -> task1
        taskService.addDependency(task1.getId(), task2.getId());
        taskService.addDependency(task2.getId(), task1.getId());

        // Act
        boolean hasDeadlock = taskService.detectDeadlocks();

        // Assert
        assertTrue(hasDeadlock);
    }

    @Test
    void shouldNotDetectDeadlockWithoutCycle() {
        // Arrange
        Task task1 = taskService.createTask("Task 1", "Description",
                LocalDateTime.now().plusDays(1), 100);
        Task task2 = taskService.createTask("Task 2", "Description",
                LocalDateTime.now().plusDays(1), 100);
        Task task3 = taskService.createTask("Task 3", "Description",
                LocalDateTime.now().plusDays(1), 100);

        // Create a linear dependency chain: task1 -> task2 -> task3
        taskService.addDependency(task1.getId(), task2.getId());
        taskService.addDependency(task2.getId(), task3.getId());

        // Act
        boolean hasDeadlock = taskService.detectDeadlocks();

        // Assert
        assertFalse(hasDeadlock);
    }

    @Test
    void shouldDetectDeadlockInComplexDependencyGraph() {
        // Arrange
        Task task1 = taskService.createTask("Task 1", "Description",
                LocalDateTime.now().plusDays(1), 100);
        Task task2 = taskService.createTask("Task 2", "Description",
                LocalDateTime.now().plusDays(1), 100);
        Task task3 = taskService.createTask("Task 3", "Description",
                LocalDateTime.now().plusDays(1), 100);
        Task task4 = taskService.createTask("Task 4", "Description",
                LocalDateTime.now().plusDays(1), 100);
        Task task5 = taskService.createTask("Task 5", "Description",
                LocalDateTime.now().plusDays(1), 100);

        // Create a complex dependency graph with a cycle
        // task1 -> task2 -> task3 -> task5
        //       -> task4 -↗     ↑
        //                        ↓
        //                      task5 -> task3
        taskService.addDependency(task1.getId(), task2.getId());
        taskService.addDependency(task1.getId(), task4.getId());
        taskService.addDependency(task2.getId(), task3.getId());
        taskService.addDependency(task4.getId(), task3.getId());
        taskService.addDependency(task3.getId(), task5.getId());
        taskService.addDependency(task5.getId(), task3.getId()); // This creates the cycle

        // Act
        boolean hasDeadlock = taskService.detectDeadlocks();

        // Assert
        assertTrue(hasDeadlock);
    }
}