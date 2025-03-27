package de.vfh.paf.tasklist.infrastructure.config;

import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.model.TaskQueue;
import de.vfh.paf.tasklist.domain.model.TaskResult;
import de.vfh.paf.tasklist.domain.repository.TaskResultRepository;
import de.vfh.paf.tasklist.domain.service.NotificationService;
import de.vfh.paf.tasklist.domain.service.TaskQueueService;
import de.vfh.paf.tasklist.domain.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class TaskListStartupRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(TaskListStartupRunner.class);

    private final TaskService taskService;
    private final TaskQueueService taskQueueService;
    private final NotificationService notificationService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final TaskResultRepository taskResultRepository;

    @Value("${tasklist.scheduling.notification-check-minutes:1}")
    private int notificationCheckMinutes;

    @Value("${tasklist.app-name:Task List Application}")
    private String appName;

    public TaskListStartupRunner(
            TaskService taskService,
            TaskQueueService taskQueueService,
            NotificationService notificationService, TaskResultRepository taskResultRepository) {
        this.taskService = taskService;
        this.taskQueueService = taskQueueService;
        this.notificationService = notificationService;
        this.taskResultRepository = taskResultRepository;
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
        Task task1 = taskService.createRunnableTask("Calculate Pi",
                "Calculate Pi to 1000 decimal places",
                LocalDateTime.now().plusMinutes(1), userId, "de.vfh.paf.tasklist.domain.tasks.CalculatePiTask");

        Task task2 = taskService.createRunnableTask("Generate Report",
                "Generate monthly sales report",
                LocalDateTime.now().plusMinutes(2), userId, "de.vfh.paf.tasklist.domain.tasks.GenerateReportTask");

        // Create dependencies
        log.info("Setting up task dependencies");
        taskService.addDependency(task1.getId(), task2.getId()); // task3 depends on task2

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
                            null,
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
                    log.info("Task result: {} - {}", result.getName(), taskResultRepository.save(result))

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