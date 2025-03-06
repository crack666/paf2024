package de.vfh.paf.tasklist.domain.service;

import de.vfh.paf.tasklist.domain.model.RunnableTask;
import de.vfh.paf.tasklist.domain.model.Status;
import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.model.TaskResult;
import de.vfh.paf.tasklist.domain.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Service for executing tasks.
 * This service is responsible for running tasks at their scheduled time
 * and when all dependencies are met.
 * 
 * This demonstrates concurrent programming concepts with a thread pool
 * for executing tasks in parallel.
 */
@Service
public class TaskProcessorService {
    private static final Logger logger = LoggerFactory.getLogger(TaskProcessorService.class);
    
    private final TaskService taskService;
    private final TaskRegistry taskRegistry;
    private final NotificationService notificationService;
    private ExecutorService taskThreadPool;
    
    @Value("${tasklist.concurrent.thread-pool-size:5}")
    private int threadPoolSize;
    
    @Value("${tasklist.concurrent.max-queue-size:100}")
    private int maxQueueSize;
    
    @org.springframework.beans.factory.annotation.Autowired
    public TaskProcessorService(TaskService taskService, TaskRegistry taskRegistry, NotificationService notificationService) {
        this.taskService = taskService;
        this.taskRegistry = taskRegistry;
        this.notificationService = notificationService;
    }
    
    @PostConstruct
    public void initialize() {
        // Create a bounded thread pool with a linked blocking queue
        // This demonstrates thread pool management for concurrent task execution
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(maxQueueSize);
        
        // Create a thread pool with custom thread factory to set meaningful names
        taskThreadPool = new ThreadPoolExecutor(
                threadPoolSize,             // Core pool size
                threadPoolSize,             // Maximum pool size
                60L,                       // Keep alive time
                TimeUnit.SECONDS,
                workQueue,
                r -> {
                    Thread thread = Thread.ofVirtual()
                            .name("task-executor-")
                            .unstarted(r);
                    return thread;
                }
        );
        
        logger.info("TaskExecutor initialized with thread pool size: {}", threadPoolSize);
    }
    
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down task executor thread pool");
        taskThreadPool.shutdown();
        try {
            if (!taskThreadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                taskThreadPool.shutdownNow();
                if (!taskThreadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    logger.error("Task thread pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            taskThreadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Executes a task by its ID.
     * The task will only be executed if it's ready to run.
     * The task execution happens in a separate thread from the caller.
     *
     * @param taskId The ID of the task to execute
     * @return Future containing the task with its result, or null if the task couldn't be executed
     */
    public CompletableFuture<Task> executeTask(int taskId) {
        // Create a CompletableFuture for the result
        CompletableFuture<Task> future = new CompletableFuture<>();
        
        // Find the task and check if it's ready to run
        taskService.findById(taskId)
                .filter(Task::isReadyToRun)
                .ifPresentOrElse(
                        task -> {
                            // Submit the task to the thread pool
                            CompletableFuture.supplyAsync(() -> runTask(task), taskThreadPool)
                                    .thenAccept(future::complete)
                                    .exceptionally(ex -> {
                                        logger.error("Error executing task: {}", ex.getMessage(), ex);
                                        future.completeExceptionally(ex);
                                        return null;
                                    });
                        },
                        () -> future.complete(null)
                );
        
        return future;
    }
    
    /**
     * Executes a task synchronously (blocks until completion).
     * Used when you need the result immediately.
     *
     * @param taskId The ID of the task to execute
     * @return The task with its result, or null if the task couldn't be executed
     */
    public Task executeTaskSync(int taskId) {
        try {
            return executeTask(taskId).get(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Task execution interrupted: {}", e.getMessage());
            return null;
        } catch (ExecutionException | TimeoutException e) {
            logger.error("Error or timeout executing task: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Executes the task implementation and updates the task with the result.
     *
     * @param task The task to execute
     * @return The updated task with results
     */
    private Task runTask(Task task) {
        logger.info("Executing task: {} (ID: {}) in thread: {}", 
                task.getTitle(), task.getId(), Thread.currentThread().getName());
        
        try {
            // Update status to running
            task.updateDetails(
                    task.getTitle(),
                    task.getDescription(),
                    task.getDueDate(),
                    Status.RUNNING
            );
            Task updatedTask = taskService.updateTask(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getDueDate(),
                    Status.RUNNING
            );
            
            // Send notification that task has started
            notificationService.sendNotification(
                    "TASK_STARTED",
                    "NORMAL",
                    updatedTask.getAssignedUserId(),
                    String.format("Task '%s' has started execution", updatedTask.getTitle()),
                    updatedTask.getId()
            );
            
            // Get the task implementation
            RunnableTask taskImplementation = taskRegistry.getTaskType(updatedTask.getTaskClassName());
            if (taskImplementation == null) {
                logger.error("Task implementation not found: {}", updatedTask.getTaskClassName());
                return updatedTask;
            }
            
            // Run the task implementation
            TaskResult result = taskImplementation.run(updatedTask);
            
            // Update the task with the result
            updatedTask.setResult(result);
            updatedTask.markComplete();
            
            // Save the updated task
            Task completedTask = taskService.updateTask(
                    updatedTask.getId(),
                    updatedTask.getTitle(),
                    updatedTask.getDescription(),
                    updatedTask.getDueDate(),
                    Status.DONE
            );
            
            // Send notification that task has completed
            notificationService.sendNotification(
                    "TASK_COMPLETED",
                    "HIGH",
                    completedTask.getAssignedUserId(),
                    String.format("Task '%s' has been completed", completedTask.getTitle()),
                    completedTask.getId()
            );
            
            return completedTask;
        } catch (Exception e) {
            logger.error("Error executing task: {} (ID: {})", task.getTitle(), task.getId(), e);
            
            // Send notification about task execution error
            notificationService.sendNotification(
                    "TASK_ERROR",
                    "HIGH",
                    task.getAssignedUserId(),
                    String.format("Error executing task '%s': %s", task.getTitle(), e.getMessage()),
                    task.getId()
            );
            
            return task;
        }
    }
    
    /**
     * Scheduled task that runs every minute to check for tasks that are ready to run.
     * Any ready tasks are executed in parallel using the thread pool.
     */
    @Scheduled(fixedRateString = "${tasklist.scheduling.task-check-seconds:60}000")
    public void checkForReadyTasks() {
        logger.debug("Checking for ready tasks...");
        
        List<Task> readyTasks = taskService.findReadyToRunTasks();
        logger.debug("Found {} ready tasks", readyTasks.size());
        
        if (!readyTasks.isEmpty()) {
            logger.info("Executing {} ready tasks in parallel", readyTasks.size());
            
            // Execute each ready task in parallel using the thread pool
            List<CompletableFuture<Task>> futures = readyTasks.stream()
                    .map(task -> executeTask(task.getId()))
                    .collect(Collectors.toList());
            
            // Optionally, wait for all tasks to complete
            // CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
    }
    
    /**
     * Get the current task thread pool statistics.
     * This can be used for monitoring thread pool usage.
     *
     * @return A string containing thread pool statistics
     */
    public String getThreadPoolStats() {
        if (taskThreadPool instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) taskThreadPool;
            return String.format(
                    "Thread pool stats: " +
                    "Active threads: %d, Pool size: %d, Core pool size: %d, " +
                    "Task count: %d, Completed tasks: %d, Queue size: %d",
                    executor.getActiveCount(),
                    executor.getPoolSize(),
                    executor.getCorePoolSize(),
                    executor.getTaskCount(),
                    executor.getCompletedTaskCount(),
                    executor.getQueue().size()
            );
        }
        return "Thread pool stats not available";
    }
}