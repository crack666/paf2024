package de.vfh.paf.tasklist.application.service;

import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.model.TaskResult;
import de.vfh.paf.tasklist.domain.service.TaskQueueService;
import de.vfh.paf.tasklist.domain.service.TaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Application service that coordinates domain services to manage tasks.
 * This is an example of the Facade pattern, providing a simplified interface
 * to the complex subsystem of domain services.
 */
@Service
public class TaskManagerService {
    private final TaskService taskService;
    private final TaskQueueService taskQueueService;

    public TaskManagerService(TaskService taskService, TaskQueueService taskQueueService) {
        this.taskService = taskService;
        this.taskQueueService = taskQueueService;
    }

    /**
     * Creates a new task and adds it to a queue.
     *
     * @param title Task title
     * @param description Task description
     * @param dueDate Task due date
     * @param userId User ID
     * @param queueId Queue ID
     * @return The created task
     */
    public Task createAndQueueTask(String title, String description, LocalDateTime dueDate, int userId, int queueId) {
        Task task = taskService.createTask(title, description, dueDate, userId);
        taskQueueService.enqueueTask(queueId, task.getId());
        return task;
    }

    /**
     * Checks if a task has dependencies that would create a deadlock.
     *
     * @param taskId Task ID
     * @param dependencyId Dependency task ID
     * @return true if adding this dependency would create a deadlock
     */
    public boolean wouldCreateDeadlock(int taskId, int dependencyId) {
        // Temporarily add the dependency
        Task task = taskService.addDependency(taskId, dependencyId);
        
        // Check for deadlocks
        boolean hasDeadlock = taskService.detectDeadlocks();
        
        // If there's a deadlock, remove the dependency
        if (hasDeadlock) {
            taskService.removeDependency(taskId, dependencyId);
        }
        
        return hasDeadlock;
    }

    /**
     * Processes all tasks in a queue with a given processor function.
     *
     * @param queueId Queue ID
     * @param processor Function to process each task
     * @return CompletableFuture containing the list of results
     */
    public CompletableFuture<List<TaskResult>> processQueue(int queueId, Function<Task, TaskResult> processor) {
        return taskQueueService.processAllTasks(queueId, processor);
    }
}
