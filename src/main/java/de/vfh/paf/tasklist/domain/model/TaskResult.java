package de.vfh.paf.tasklist.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents the result of a completed task.
 * This can be any type of computation result, such as a calculation, current time, etc.
 */
@Setter
@Getter
@Entity
@Table(name = "task_results")
public class TaskResult {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;

    @Column(length = 4000)
    private String content;

    @Column(name = "timestamp", updatable = false)
    private LocalDateTime timestamp;

    @Column(name = "task_id")
    private Integer taskId;

    /**
     * Default constructor required by JPA
     */
    public TaskResult() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Creates a new task result.
     *
     * @param id      The unique identifier for the result
     * @param title   The title of the result
     * @param content The computed content
     * @param taskId  The ID of the task this result belongs to
     */
    public TaskResult(Integer id, String title, String content, Integer taskId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.taskId = taskId;
    }

    /**
     * Creates a new task result with an auto-generated ID.
     *
     * @param title     The title of the result
     * @param content   The computed content
     * @param timestamp The time when the result was computed
     */
    public TaskResult(String title, String content, LocalDateTime timestamp) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
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

    // For backward compatibility
    public String getResult() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskResult that = (TaskResult) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}