package de.vfh.paf.tasklist.domain.service;

import de.vfh.paf.tasklist.domain.model.Status;
import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.model.TaskQueue;
import de.vfh.paf.tasklist.domain.model.TaskResult;
import de.vfh.paf.tasklist.domain.repository.TaskRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for managing task queues and executing tasks.
 */
@org.springframework.stereotype.Service
public class TaskQueueService {
    private final TaskRepository taskRepository;
    private final Map<Integer, TaskQueue> queues = new HashMap<>();
    private final AtomicInteger queueIdGenerator = new AtomicInteger(1);
    private final AtomicInteger resultIdGenerator = new AtomicInteger(1);

    /**
     * Creates a new task queue service.
     *
     * @param taskRepository The repository for tasks
     */
    @org.springframework.beans.factory.annotation.Autowired
    public TaskQueueService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Creates a new task queue.
     *
     * @param name The name of the queue
     * @return The created queue
     */
    public TaskQueue createQueue(String name) {
        int id = queueIdGenerator.getAndIncrement();
        TaskQueue queue = new TaskQueue(id, name);
        queues.put(id, queue);
        return queue;
    }

    /**
     * Adds a task to a queue.
     *
     * @param queueId The ID of the queue
     * @param taskId The ID of the task to add
     * @return true if the task was added, false otherwise
     */
    public boolean enqueueTask(int queueId, int taskId) {
        TaskQueue queue = queues.get(queueId);
        if (queue == null) {
            return false;
        }

        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isEmpty()) {
            return false;
        }

        Task task = optionalTask.get();
        queue.enqueueTask(task);
        taskRepository.save(task);
        return true;
    }

    /**
     * Executes the next task in the queue.
     *
     * @param queueId The ID of the queue
     * @param taskProcessor The function to process the task and generate a result
     * @return A CompletableFuture that will contain the result of the task
     */
    public CompletableFuture<TaskResult> executeNextTask(int queueId, Function<Task, TaskResult> taskProcessor) {
        TaskQueue queue = queues.get(queueId);
        if (queue == null) {
            return CompletableFuture.completedFuture(null);
        }

        Task task = queue.dequeueTask();
        if (task == null) {
            return CompletableFuture.completedFuture(null);
        }

        // Save the task with the RUNNING status
        taskRepository.save(task);

        // Execute the task asynchronously
        return CompletableFuture.supplyAsync(() -> {
            TaskResult result = taskProcessor.apply(task);
            
            // Mark the task as complete
            task.markComplete();
            taskRepository.save(task);
            
            return result;
        });
    }

    /**
     * Processes all tasks in the queue in parallel.
     *
     * @param queueId The ID of the queue
     * @param taskProcessor The function to process each task and generate a result
     * @return A CompletableFuture that will contain the results of all tasks
     */
    public CompletableFuture<List<TaskResult>> processAllTasks(int queueId, Function<Task, TaskResult> taskProcessor) {
        TaskQueue queue = queues.get(queueId);
        if (queue == null) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        List<Task> tasks = new ArrayList<>(queue.getTasks());
        List<CompletableFuture<TaskResult>> futures = new ArrayList<>();

        // Process each task in the queue
        for (int i = 0; i < tasks.size(); i++) {
            // Each executeNextTask call will dequeue a task and process it
            futures.add(executeNextTask(queueId, taskProcessor));
        }

        // Combine all futures into a single future that completes when all tasks are done
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }

    /**
     * Gets a task queue by its ID.
     *
     * @param queueId The ID of the queue
     * @return The queue, or null if not found
     */
    public TaskQueue getQueue(int queueId) {
        return queues.get(queueId);
    }

    /**
     * Gets all available task queues.
     *
     * @return A list of all queues
     */
    public List<TaskQueue> getAllQueues() {
        return new ArrayList<>(queues.values());
    }
}