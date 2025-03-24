package de.vfh.paf.tasklist.domain.service;

import de.vfh.paf.tasklist.domain.model.TaskStatus;
import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.tasks.CalculatePiTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskProcessorServiceTest {

    private final String taskClassName = CalculatePiTask.class.getName();
    @Mock
    private TaskService taskService;
    @Mock
    private TaskFactory taskRegistry;
    @Mock
    private NotificationService notificationService;
    @Mock
    private de.vfh.paf.tasklist.domain.repository.TaskResultRepository taskResultRepository;
    @InjectMocks
    private TaskProcessorService taskExecutor;
    private Task testTask;

    @BeforeEach
    void setUp() {
        // Mock task registry
        CalculatePiTask calculatePiTask = new CalculatePiTask();
        when(taskRegistry.getTaskType(taskClassName)).thenReturn(calculatePiTask);

        // Setup NotificationService mock
        when(notificationService.sendNotification(anyString(), anyString(), anyInt(), anyString(), any())).thenReturn(true);

        // Create a test task
        testTask = new Task(1, "Test Task", "Description", LocalDateTime.now().plusDays(1), TaskStatus.QUEUED, 1, taskClassName);

        // Set the task as ready to run
        ReflectionTestUtils.setField(testTask, "dependencies", new ArrayList<>());

        // Configure task service mock
        when(taskService.findById(1)).thenReturn(Optional.of(testTask));
        when(taskService.updateTask(anyInt(), any(), any(), any(), any()))
                .thenAnswer(invocation -> {
                    TaskStatus taskStatus = invocation.getArgument(4);
                    testTask.updateDetails(testTask.getTitle(), testTask.getDescription(), testTask.getDueDate(), taskStatus);
                    return testTask;
                });

        // Initialize executor with small thread pool for testing
        ReflectionTestUtils.setField(taskExecutor, "threadPoolSize", 2);
        ReflectionTestUtils.setField(taskExecutor, "maxQueueSize", 10);
        taskExecutor.initialize();
    }

    @Test
    void testExecuteTaskSync() {
        // Execute task synchronously
        Task result = taskExecutor.executeTaskSync(1);

        // Verify task was executed
        assertNotNull(result, "Task execution result should not be null");
        assertEquals(TaskStatus.DONE, result.getStatus(), "Task status should be DONE");
        assertNotNull(result.getResult(), "Task should have a result");

        // Verify interactions
        verify(taskService).findById(1);
        verify(taskService, times(2)).updateTask(anyInt(), any(), any(), any(), any());
        verify(taskRegistry).getTaskType(taskClassName);
    }

    @Test
    void testExecuteTaskAsync() throws Exception {
        // Execute task asynchronously
        CompletableFuture<Task> future = taskExecutor.executeTask(1);

        // Wait for task to complete (with timeout)
        Task result = future.get(5, TimeUnit.SECONDS);

        // Verify task was executed
        assertNotNull(result, "Task execution result should not be null");
        assertEquals(TaskStatus.DONE, result.getStatus(), "Task status should be DONE");
        assertNotNull(result.getResult(), "Task should have a result");

        // Verify interactions
        verify(taskService).findById(1);
        verify(taskService, times(2)).updateTask(anyInt(), any(), any(), any(), any());
        verify(taskRegistry).getTaskType(taskClassName);
    }

    @Test
    void testConcurrentTaskExecution() throws Exception {
        // Create multiple tasks
        int taskCount = 5;
        List<Task> tasks = new ArrayList<>();
        List<CompletableFuture<Task>> futures = new ArrayList<>();

        // Mock task service for multiple tasks
        for (int i = 1; i <= taskCount; i++) {
            Task task = new Task(i, "Test Task " + i, "Description " + i, LocalDateTime.now().minusDays(1), TaskStatus.QUEUED, 1, taskClassName);
            ReflectionTestUtils.setField(task, "dependencies", new ArrayList<>());
            tasks.add(task);
            when(taskService.findById(i)).thenReturn(Optional.of(task));
        }

        // Execute tasks concurrently
        for (int i = 1; i <= taskCount; i++) {
            futures.add(taskExecutor.executeTask(i));
        }

        // Wait for all tasks to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        // Use timeout to avoid hanging test
        try {
            allFutures.get(30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            fail("Tasks execution timed out - they might be hanging");
        }

        // Verify all tasks completed successfully
        for (CompletableFuture<Task> future : futures) {
            Task result = future.get();
            assertNotNull(result, "Task execution result should not be null");
            assertEquals(TaskStatus.DONE, result.getStatus(), "Task status should be DONE");
            assertNotNull(result.getResult(), "Task should have a result");
        }
    }

    @Test
    void testThreadPoolStats() {
        // Execute a task
        taskExecutor.executeTaskSync(1);

        // Get thread pool stats
        String stats = taskExecutor.getThreadPoolStats();

        // Verify stats are returned
        assertNotNull(stats, "Thread pool stats should not be null");
        assertTrue(stats.contains("Thread pool stats"), "Stats should contain thread pool info");
        assertTrue(stats.contains("Completed tasks"), "Stats should contain completed tasks info");
    }
}