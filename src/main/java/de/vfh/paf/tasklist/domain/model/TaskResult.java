package de.vfh.paf.tasklist.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents the result of a completed task.
 * This can be any type of computation result, such as a calculation, current time, etc.
 */
public class TaskResult {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);

    private final int id;
    private final String title;
    private final String content;
    private final LocalDateTime timestamp;
    private int taskId;

    /**
     * Creates a new task result.
     *
     * @param id The unique identifier for the result
     * @param title The title of the result
     * @param content The computed content
     * @param taskId The ID of the task this result belongs to
     */
    public TaskResult(int id, String title, String content, int taskId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.taskId = taskId;
    }

    /**
     * Creates a new task result with an auto-generated ID.
     *
     * @param title The title of the result
     * @param content The computed content
     * @param timestamp The time when the result was computed
     */
    public TaskResult(String title, String content, LocalDateTime timestamp) {
        this.id = ID_GENERATOR.getAndIncrement();
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
    }

    /**
     * Gets the result value for backward compatibility.
     *
     * @return The computed result content
     */
    public String getResult() {
        return content;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    // For backward compatibility
    public String getName() {
        return title;
    }

    public String getResultValue() {
        return content;
    }

    public LocalDateTime getComputedAt() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskResult that = (TaskResult) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}