package de.vfh.paf.tasklist.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Represents a queue of tasks that can be processed in a specific order.
 */
@Getter
public class TaskQueue {
    // Getters
    private final int id;
    private final String name;
    /**
     * -- GETTER --
     *  Returns all tasks in the queue.
     *
     * @return A list of tasks
     */
    private final List<Task> tasks;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Creates a new task queue.
     *
     * @param id The unique identifier for the queue
     * @param name The name of the queue
     */
    public TaskQueue(int id, String name) {
        this.id = id;
        this.name = name;
        this.tasks = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Adds a task to the end of the queue.
     *
     * @param task The task to add
     */
    public void enqueueTask(Task task) {
        if (!tasks.contains(task)) {
            task.updateDetails(task.getTitle(), task.getDescription(), task.getDueDate(), Status.QUEUED);
            tasks.add(task);
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Removes and returns the first task in the queue.
     *
     * @return The first task in the queue, or null if the queue is empty
     */
    public Task dequeueTask() {
        if (tasks.isEmpty()) {
            return null;
        }
        
        Task task = tasks.removeFirst();
        task.updateDetails(task.getTitle(), task.getDescription(), task.getDueDate(), Status.RUNNING);
        this.updatedAt = LocalDateTime.now();
        return task;
    }

    /**
     * Returns the first task in the queue without removing it.
     *
     * @return The first task in the queue, or null if the queue is empty
     */
    public Task peekNextTask() {
        return tasks.isEmpty() ? null : tasks.getFirst();
    }

    /**
     * Removes a task from the queue by its ID.
     *
     * @param taskId The ID of the task to remove
     * @return true if the task was removed, false otherwise
     */
    public boolean removeTask(int taskId) {
        boolean removed = tasks.removeIf(task -> task.getId() == taskId);
        if (removed) {
            this.updatedAt = LocalDateTime.now();
        }
        return removed;
    }

    /**
     * Reorders the tasks in the queue based on the specified criteria.
     *
     * @param orderCriteria The criteria to use for ordering (e.g., "dueDate")
     */
    public void reorderTasks(String orderCriteria) {
        if ("dueDate".equals(orderCriteria)) {
            tasks.sort(Comparator.comparing(Task::getDueDate));
        }
        // Additional ordering criteria could be added here
        
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskQueue taskQueue = (TaskQueue) o;
        return id == taskQueue.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}