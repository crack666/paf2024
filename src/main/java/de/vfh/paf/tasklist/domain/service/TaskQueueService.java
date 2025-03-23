package de.vfh.paf.tasklist.domain.service;

import de.vfh.paf.tasklist.domain.model.Status;
import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.model.TaskQueue;
import de.vfh.paf.tasklist.domain.model.TaskResult;
import de.vfh.paf.tasklist.domain.repository.TaskRepository;
import de.vfh.paf.tasklist.presentation.websocket.TaskWebSocketController;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for managing task queues and executing tasks.
 */
@org.springframework.stereotype.Service
public class TaskQueueService {
    private final TaskRepository taskRepository;
    private final de.vfh.paf.tasklist.domain.repository.TaskResultRepository taskResultRepository;
    private final Map<Integer, TaskQueue> queues = new HashMap<>();
    // Keep track of processed tasks for each queue with their results
    private final Map<Integer, Map<Integer, TaskResult>> queueProcessedTasks = new ConcurrentHashMap<>();
    private final AtomicInteger queueIdGenerator = new AtomicInteger(1);
    private final AtomicInteger resultIdGenerator = new AtomicInteger(1);
    private TaskWebSocketController taskWebSocketController; // Not final to allow setter injection

    /**
     * Creates a new task queue service.
     *
     * @param taskRepository       The repository for tasks
     * @param taskResultRepository The repository for task results
     */
    @org.springframework.beans.factory.annotation.Autowired
    public TaskQueueService(TaskRepository taskRepository, de.vfh.paf.tasklist.domain.repository.TaskResultRepository taskResultRepository) {
        this.taskRepository = taskRepository;
        this.taskResultRepository = taskResultRepository;
    }

    /**
     * Sets the WebSocket controller (to break circular dependency).
     *
     * @param taskWebSocketController The WebSocket controller for task updates
     */
    @org.springframework.beans.factory.annotation.Autowired
    public void setTaskWebSocketController(TaskWebSocketController taskWebSocketController) {
        this.taskWebSocketController = taskWebSocketController;
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
        queueProcessedTasks.put(id, new ConcurrentHashMap<>());

        // Notify clients about queue creation (if WebSocket controller is available)
        if (taskWebSocketController != null) {
            taskWebSocketController.sendQueueUpdate(id, null, "CREATED");
        }
        return queue;
    }

    /**
     * Adds a task to a queue.
     *
     * @param queueId The ID of the queue
     * @param taskId  The ID of the task to add
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

        // Notify clients that a task has been added to the queue (if WebSocket controller is available)
        if (taskWebSocketController != null) {
            taskWebSocketController.sendQueueUpdate(queueId, task, "ADDED");
            taskWebSocketController.sendTaskStatusUpdate(task);
        }

        return true;
    }

    /**
     * Executes the next task in the queue.
     *
     * @param queueId       The ID of the queue
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
        task.updateDetails(task.getTitle(), task.getDescription(), task.getDueDate(), Status.RUNNING);
        taskRepository.save(task);

        // Notify that task status is now RUNNING (if WebSocket controller is available)
        if (taskWebSocketController != null) {
            taskWebSocketController.sendQueueUpdate(queueId, task, "STARTED");
            taskWebSocketController.sendTaskStatusUpdate(task);
        }

        // Execute the task asynchronously
        return CompletableFuture.supplyAsync(() -> {
            TaskResult result = taskProcessor.apply(task);

            // Ensure the result has the correct task ID
            if (result != null && result.getTaskId() == null) {
                result.setTaskId(task.getId());
                // Save the result to the database
                taskResultRepository.save(result);
            }

            // Mark the task as complete
            task.markComplete();
            taskRepository.save(task);

            // Store the completed task and its result with the queue
            if (queueProcessedTasks.containsKey(queueId)) {
                queueProcessedTasks.get(queueId).put(task.getId(), result);
            }

            // Notify that task is now DONE with result (if WebSocket controller is available)
            if (taskWebSocketController != null) {
                taskWebSocketController.sendQueueUpdate(queueId, task, "COMPLETED");
                taskWebSocketController.sendTaskStatusUpdate(task);
                taskWebSocketController.sendTaskResultUpdate(task, result);
            }

            return result;
        });
    }

    /**
     * Processes all tasks in the queue in parallel.
     *
     * @param queueId       The ID of the queue
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

    /**
     * Gets all tasks that have been processed by a specific queue, along with their results.
     *
     * @param queueId The ID of the queue
     * @return A map where keys are task IDs and values are the corresponding results
     */
    public Map<Integer, TaskResult> getProcessedTasksWithResults(int queueId) {
        return queueProcessedTasks.getOrDefault(queueId, Map.of());
    }

    /**
     * Gets all tasks associated with a queue, including pending and processed ones.
     *
     * @param queueId The ID of the queue
     * @return A list of all tasks associated with the queue
     */
    public List<Task> getAllQueueTasks(int queueId) {
        TaskQueue queue = queues.get(queueId);
        if (queue == null) {
            return List.of();
        }

        // Get current tasks in the queue
        List<Task> currentTasks = new ArrayList<>(queue.getTasks());

        // Get IDs of processed tasks
        Set<Integer> processedTaskIds = queueProcessedTasks.getOrDefault(queueId, Map.of()).keySet();

        // Fetch processed tasks from repository
        List<Task> processedTasks = processedTaskIds.stream()
                .map(taskRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        // Combine both lists
        List<Task> allTasks = new ArrayList<>(currentTasks);
        allTasks.addAll(processedTasks);

        return allTasks;
    }
}