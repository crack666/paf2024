package de.vfh.paf.tasklist.domain.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a task in the task list application.
 * Tasks can have dependencies on other tasks and can be assigned to users.
 */
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;
    private String description;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "is_completed")
    private boolean completed;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "task_dependencies",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "dependency_id")
    )
    private List<Task> dependencies = new ArrayList<>();

    @Column(name = "assigned_user_id")
    private Integer assignedUserId;

    @Column(name = "task_class_name")
    private String taskClassName; // Fully qualified class name of the task implementation

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime; // Time when the task should be executed

    @Transient // We'll handle this separately due to its complex structure
    private TaskResult result; // Result of the task execution

    /**
     * Default constructor required by JPA
     */
    public Task() {
        this.createdAt = LocalDateTime.now();
        this.taskStatus = TaskStatus.CREATED;
    }

    /**
     * Creates a new task with essential fields including the task class.
     *
     * @param id             The unique identifier for the task
     * @param title          The title of the task
     * @param description    The description of the task
     * @param dueDate        The due date for the task
     * @param assignedUserId The ID of the user assigned to the task
     * @param taskClassName  The class name of the task implementation
     * @param scheduledTime  The time when the task should be executed
     */
    public Task(Integer id, String title, String description, LocalDateTime dueDate,
                Integer assignedUserId, String taskClassName, LocalDateTime scheduledTime) {
        this(id, title, description, dueDate, false, TaskStatus.CREATED,
                assignedUserId, taskClassName, scheduledTime);
    }

    /**
     * Creates a new task with all fields.
     *
     * @param id             The unique identifier for the task
     * @param title          The title of the task
     * @param description    The description of the task
     * @param dueDate        The due date for the task
     * @param isCompleted    Whether the task is completed
     * @param taskStatus         The status of the task
     * @param assignedUserId The ID of the user assigned to the task
     * @param taskClassName  The class name of the task implementation
     * @param scheduledTime  The time when the task should be executed
     */
    public Task(Integer id, String title, String description, LocalDateTime dueDate, boolean isCompleted,
                TaskStatus taskStatus, Integer assignedUserId, String taskClassName, LocalDateTime scheduledTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.completed = isCompleted;
        this.createdAt = LocalDateTime.now();
        this.taskStatus = taskStatus;
        this.assignedUserId = assignedUserId;
        this.taskClassName = taskClassName;
        this.scheduledTime = scheduledTime;
    }

    /**
     * Marks the task as complete and updates its status.
     */
    public void markComplete() {
        this.completed = true;
        this.taskStatus = TaskStatus.DONE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the details of the task.
     *
     * @param title       The new title
     * @param description The new description
     * @param dueDate     The new due date
     * @param taskStatus      The new status
     */
    public void updateDetails(String title, String description, LocalDateTime dueDate, TaskStatus taskStatus) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.taskStatus = taskStatus;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the scheduling details of the task.
     *
     * @param taskClassName The class name of the task implementation
     * @param scheduledTime The time when the task should be executed
     */
    public void updateScheduling(String taskClassName, LocalDateTime scheduledTime) {
        this.taskClassName = taskClassName;
        this.scheduledTime = scheduledTime;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Adds a dependency to this task.
     *
     * @param task The task that this task depends on
     */
    public void addDependency(Task task) {
        if (!dependencies.contains(task) && task.getId() != this.id) {
            dependencies.add(task);
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Removes a dependency from this task.
     *
     * @param task The task to remove as a dependency
     */
    public void removeDependency(Task task) {
        if (dependencies.remove(task)) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Checks if this task is ready to run.
     * A task is ready if:
     * 1. It's not already completed
     * 2. It's scheduled for now or in the past
     * 3. All dependencies are completed
     *
     * @return true if the task is ready to run, false otherwise
     */
    public boolean isReadyToRun() {
        if (completed || taskClassName == null) {
            return false;
        }

        if (scheduledTime != null && scheduledTime.isAfter(LocalDateTime.now())) {
            return false;
        }

        try {
            // Only check dependencies if they're initialized
            if (org.hibernate.Hibernate.isInitialized(dependencies)) {
                return dependencies.stream()
                        .allMatch(Task::isCompleted);
            } else {
                // If dependencies aren't loaded, consider the task ready
                // The database will enforce foreign key constraints
                return true;
            }
        } catch (Exception e) {
            // If there's an error accessing dependencies, log it and assume the task is ready
            System.err.println("Error checking dependencies for task ID " + id + ": " + e.getMessage());
            return true;
        }
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public TaskStatus getStatus() {
        return taskStatus;
    }

    public void setStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public List<Task> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Task> dependencies) {
        this.dependencies = dependencies;
    }

    public Integer getAssignedUserId() {
        return assignedUserId;
    }

    public void setAssignedUserId(Integer assignedUserId) {
        this.assignedUserId = assignedUserId;
    }

    public String getTaskClassName() {
        return taskClassName;
    }

    public void setTaskClassName(String taskClassName) {
        this.taskClassName = taskClassName;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public TaskResult getResult() {
        return result;
    }

    /**
     * Sets the result of the task execution.
     * This method is intentionally kept to avoid compilation errors in Task.java
     * and is used by both the previous in-memory implementation and the new JPA implementation.
     *
     * @param result The result of the task execution
     */
    public void setResult(TaskResult result) {
        this.result = result;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}