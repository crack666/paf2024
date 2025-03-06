package de.vfh.paf.tasklist.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a task in the task list application.
 * Tasks can have dependencies on other tasks and can be assigned to users.
 */
public class Task {
    private final int id;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private boolean isCompleted;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Status status;
    private final List<Task> dependencies;
    private final int assignedUserId;
    private String taskClassName; // Fully qualified class name of the task implementation
    private LocalDateTime scheduledTime; // Time when the task should be executed
    private TaskResult result; // Result of the task execution

    /**
     * Creates a new task with the required fields.
     *
     * @param id The unique identifier for the task
     * @param title The title of the task
     * @param dueDate The due date for the task
     * @param assignedUserId The ID of the user assigned to the task
     */
    public Task(int id, String title, LocalDateTime dueDate, int assignedUserId) {
        this(id, title, null, dueDate, false, Status.CREATED, assignedUserId, null, null);
    }

    /**
     * Creates a new task with essential fields including the task class.
     *
     * @param id The unique identifier for the task
     * @param title The title of the task
     * @param description The description of the task
     * @param dueDate The due date for the task
     * @param assignedUserId The ID of the user assigned to the task
     * @param taskClassName The class name of the task implementation
     * @param scheduledTime The time when the task should be executed
     */
    public Task(int id, String title, String description, LocalDateTime dueDate, 
                int assignedUserId, String taskClassName, LocalDateTime scheduledTime) {
        this(id, title, description, dueDate, false, Status.CREATED, 
             assignedUserId, taskClassName, scheduledTime);
    }

    /**
     * Creates a new task with all fields.
     *
     * @param id The unique identifier for the task
     * @param title The title of the task
     * @param description The description of the task
     * @param dueDate The due date for the task
     * @param isCompleted Whether the task is completed
     * @param status The status of the task
     * @param assignedUserId The ID of the user assigned to the task
     * @param taskClassName The class name of the task implementation
     * @param scheduledTime The time when the task should be executed
     */
    public Task(int id, String title, String description, LocalDateTime dueDate, boolean isCompleted, 
                Status status, int assignedUserId, String taskClassName, LocalDateTime scheduledTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
        this.createdAt = LocalDateTime.now();
        this.status = status;
        this.dependencies = new ArrayList<>();
        this.assignedUserId = assignedUserId;
        this.taskClassName = taskClassName;
        this.scheduledTime = scheduledTime;
    }

    /**
     * Marks the task as complete and updates its status.
     */
    public void markComplete() {
        this.isCompleted = true;
        this.status = Status.DONE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the details of the task.
     *
     * @param title The new title
     * @param description The new description
     * @param dueDate The new due date
     * @param status The new status
     */
    public void updateDetails(String title, String description, LocalDateTime dueDate, Status status) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
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
     * Sets the result of the task execution.
     *
     * @param result The result of the task execution
     */
    public void setResult(TaskResult result) {
        this.result = result;
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
        if (isCompleted || taskClassName == null) {
            return false;
        }
        
        if (scheduledTime != null && scheduledTime.isAfter(LocalDateTime.now())) {
            return false;
        }
        
        return dependencies.stream()
                .allMatch(Task::isCompleted);
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Status getStatus() {
        return status;
    }

    public List<Task> getDependencies() {
        return dependencies;
    }

    public int getAssignedUserId() {
        return assignedUserId;
    }
    
    public String getTaskClassName() {
        return taskClassName;
    }
    
    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }
    
    public TaskResult getResult() {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}