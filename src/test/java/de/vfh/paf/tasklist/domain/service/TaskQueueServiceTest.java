package de.vfh.paf.tasklist.domain.service;

import de.vfh.paf.tasklist.domain.model.Status;
import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.model.TaskQueue;
import de.vfh.paf.tasklist.domain.model.TaskResult;
import de.vfh.paf.tasklist.domain.repository.TaskRepository;
import de.vfh.paf.tasklist.infrastructure.persistence.TaskRepositoryInMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TaskQueueServiceTest {
    private TaskQueueService taskQueueService;
    private TaskRepository taskRepository;
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskRepository = new TaskRepositoryInMemory();
        taskService = new TaskService(taskRepository);
        taskQueueService = new TaskQueueService(taskRepository);
    }

    @Test
    void shouldCreateTaskQueue() {
        // Arrange
        String queueName = "Test Queue";
        
        // Act
        TaskQueue queue = taskQueueService.createQueue(queueName);
        
        // Assert
        assertNotNull(queue);
        assertEquals(queueName, queue.getName());
        assertEquals(0, queue.getTasks().size());
    }

    @Test
    void shouldEnqueueTask() {
        // Arrange
        TaskQueue queue = taskQueueService.createQueue("Test Queue");
        Task task = taskService.createTask("Test Task", "Description", LocalDateTime.now().plusDays(1), 100);
        
        // Act
        boolean enqueued = taskQueueService.enqueueTask(queue.getId(), task.getId());
        
        // Assert
        assertTrue(enqueued);
        assertEquals(1, queue.getTasks().size());
        assertEquals(task, queue.getTasks().get(0));
        assertEquals(Status.QUEUED, task.getStatus());
    }

    @Test
    void shouldExecuteTask() throws Exception {
        // Arrange
        TaskQueue queue = taskQueueService.createQueue("Test Queue");
        Task task = taskService.createTask("Test Task", "Description", LocalDateTime.now().plusDays(1), 100);
        taskQueueService.enqueueTask(queue.getId(), task.getId());
        
        // Act
        CompletableFuture<TaskResult> futureResult = taskQueueService.executeNextTask(queue.getId(), 
                (t) -> new TaskResult(1, "Current Time", LocalDateTime.now().toString(), t.getId()));
        
        // Wait for the result with a timeout
        TaskResult result = futureResult.get(5, TimeUnit.SECONDS);
        
        // Assert
        assertNotNull(result);
        assertEquals(task.getId(), result.getTaskId());
        assertEquals("Current Time", result.getName());
        assertTrue(result.getResultValue().length() > 0);
        
        // Verify task status
        Task updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals(Status.DONE, updatedTask.getStatus());
        assertTrue(updatedTask.isCompleted());
    }

    @Test
    void shouldProcessAllTasksInQueue() throws Exception {
        // Arrange
        TaskQueue queue = taskQueueService.createQueue("Test Queue");
        
        Task task1 = taskService.createTask("Task 1", "Description", LocalDateTime.now().plusDays(1), 100);
        Task task2 = taskService.createTask("Task 2", "Description", LocalDateTime.now().plusDays(1), 100);
        Task task3 = taskService.createTask("Task 3", "Description", LocalDateTime.now().plusDays(1), 100);
        
        taskQueueService.enqueueTask(queue.getId(), task1.getId());
        taskQueueService.enqueueTask(queue.getId(), task2.getId());
        taskQueueService.enqueueTask(queue.getId(), task3.getId());
        
        // Act
        List<TaskResult> results = taskQueueService.processAllTasks(queue.getId(), 
                (t) -> new TaskResult(t.getId(), "Result for " + t.getTitle(), "Success", t.getId())).get(5, TimeUnit.SECONDS);
        
        // Assert
        assertEquals(3, results.size());
        
        // Check results
        for (TaskResult result : results) {
            assertTrue(result.getName().startsWith("Result for Task"));
            assertEquals("Success", result.getResultValue());
        }
        
        // Verify all tasks are completed
        assertTrue(taskRepository.findById(task1.getId()).orElseThrow().isCompleted());
        assertTrue(taskRepository.findById(task2.getId()).orElseThrow().isCompleted());
        assertTrue(taskRepository.findById(task3.getId()).orElseThrow().isCompleted());
        
        // Verify queue is empty
        assertEquals(0, queue.getTasks().size());
    }
}