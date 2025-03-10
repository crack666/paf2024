package de.vfh.paf.tasklist.infrastructure.config;

import de.vfh.paf.tasklist.domain.model.Status;
import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.model.TaskQueue;
import de.vfh.paf.tasklist.domain.model.TaskResult;
import de.vfh.paf.tasklist.domain.repository.TaskRepository;
import de.vfh.paf.tasklist.domain.service.NotificationService;
import de.vfh.paf.tasklist.domain.service.TaskQueueService;
import de.vfh.paf.tasklist.domain.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Startup runner for the Task List application.
 * Initializes services and demonstrates task management functionality.
 */
@Component
@Slf4j
public class TaskListStartupRunner implements ApplicationRunner {

    private final TaskService taskService;
    private final TaskQueueService taskQueueService;
    private final NotificationService notificationService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    @Value("${tasklist.scheduling.notification-check-minutes:1}")
    private int notificationCheckMinutes;
    
    @Value("${tasklist.app-name:Task List Application}")
    private String appName;

    public TaskListStartupRunner(
            TaskService taskService,
            TaskQueueService taskQueueService,
            NotificationService notificationService) {
        // Demonstrating constructor-based dependency injection
        this.taskService = taskService;
        this.taskQueueService = taskQueueService;
        this.notificationService = notificationService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("{} is starting up...", appName);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void demoTaskManagement() {
        log.info("Demonstrating Task Management Features");

        // Create users (normally this would be in a UserService)
        int userId = 1;
        log.info("Creating tasks for user {}", userId);

        // Create tasks
        Task task1 = taskService.createTask("Calculate Pi", 
                "Calculate Pi to 1000 decimal places", 
                LocalDateTime.now().plusMinutes(5), userId);
        
        Task task2 = taskService.createTask("Generate Report", 
                "Generate monthly sales report", 
                LocalDateTime.now().plusMinutes(10), userId);
        
        Task task3 = taskService.createTask("Send Notifications", 
                "Send notification emails to all users", 
                LocalDateTime.now().plusMinutes(15), userId);

        // Create dependencies
        log.info("Setting up task dependencies");
        taskService.addDependency(task3.getId(), task2.getId()); // task3 depends on task2
        
        // Check for deadlocks
        boolean hasDeadlocks = taskService.detectDeadlocks();
        log.info("Deadlock detection result: {}", hasDeadlocks);

        // Create a task queue
        log.info("Creating and processing task queue");
        TaskQueue queue = taskQueueService.createQueue("Main Processing Queue");
        
        // Enqueue tasks
        taskQueueService.enqueueTask(queue.getId(), task1.getId());
        taskQueueService.enqueueTask(queue.getId(), task2.getId());
        
        // Process tasks asynchronously
        CompletableFuture<List<TaskResult>> resultsFuture = taskQueueService.processAllTasks(
                queue.getId(),
                task -> {
                    log.info("Processing task: {}", task.getTitle());
                    
                    // Simulate work
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    // Create result
                    return new TaskResult(
                            task.getId(),
                            "Result for " + task.getTitle(),
                            "Completed at " + LocalDateTime.now(),
                            task.getId()
                    );
                }
        );
        
        // Handle results when complete
        resultsFuture.thenAccept(results -> {
            log.info("All tasks processed, results: {}", results.size());
            results.forEach(result -> 
                log.info("Task result: {} - {}", result.getName(), result.getResultValue())
            );
        });
        
        // Schedule notification check
        scheduler.scheduleAtFixedRate(() -> {
            int notifications = notificationService.checkAndNotifyOverdueTasks();
            if (notifications > 0) {
                log.info("Sent {} notifications for overdue tasks", notifications);
            }
        }, 1, notificationCheckMinutes, TimeUnit.MINUTES);
    }
}