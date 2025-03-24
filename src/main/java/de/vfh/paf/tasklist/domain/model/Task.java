package de.vfh.paf.tasklist.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

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
    // Getters and Setters
    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter
    @Getter
    private String title;

    @Setter
    @Getter
    private String description;

    @Setter
    @Getter
    @Column(name = "due_date")
    @NonNull
    private LocalDateTime dueDate; // Time when the task should be executed

    @Setter
    @Getter
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Getter
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @Column(name="completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    private TaskStatus taskStatus;

    @Setter
    @Getter
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "task_dependencies",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "dependency_id")
    )
    private List<Task> dependencies = new ArrayList<>();

    @Setter
    @Getter
    @Column(name = "assigned_user_id")
    private Integer assignedUserId;

    @Getter
    @Column(name = "task_class_name")
    private String taskClassName; // Fully qualified class name of the task implementation

    @Getter
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
     */
    public Task(Integer id, String title, String description, LocalDateTime dueDate, Integer assignedUserId, String taskClassName) {
        this(id, title, description, dueDate, TaskStatus.CREATED, assignedUserId, taskClassName);
    }

    /**
     * Creates a new task with all fields.
     *
     * @param id             The unique identifier for the task
     * @param title          The title of the task
     * @param description    The description of the task
     * @param dueDate        The due date for the task
     * @param taskStatus     The status of the task
     * @param assignedUserId The ID of the user assigned to the task
     * @param taskClassName  The class name of the task implementation
     */
    public Task(Integer id, String title, String description, @NonNull LocalDateTime dueDate, TaskStatus taskStatus, Integer assignedUserId, String taskClassName) {
        if (taskClassName == null) { throw new IllegalArgumentException("Task class name must not be null"); }
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.createdAt = LocalDateTime.now();
        this.taskStatus = taskStatus;
        this.assignedUserId = assignedUserId;
        this.taskClassName = taskClassName;
    }

    /**
     * Marks the task as complete and updates its status.
     */
    public void markComplete() {
        this.transitionTo(TaskStatus.DONE);
    }

    /**
     * Updates the details of the task.
     *
     * @param title       The new title
     * @param description The new description
     * @param dueDate     The new due date
     */
    public void updateDetails(String title, String description, LocalDateTime dueDate) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.taskStatus = taskStatus;
        this.updatedAt = LocalDateTime.now();
    }


    public boolean transitionTo(TaskStatus newStatus) {
        if (!this.taskStatus.canTransitionTo(newStatus)) {
            return false;
        }
        this.taskStatus = newStatus;
        this.updatedAt = LocalDateTime.now();
        if (newStatus == TaskStatus.DONE && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
        return true;
    }

    /**
     * Updates the scheduling details of the task.
     *
     * @param taskClassName The class name of the task implementation
     */
    public void updateScheduling(String taskClassName) {
        this.taskClassName = taskClassName;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Adds a dependency to this task.
     *
     * @param task The task that this task depends on
     */
    public void addDependency(Task task) {
        if (!dependencies.contains(task) && !Objects.equals(task.getId(), this.id)) {
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
        if (taskStatus != TaskStatus.QUEUED) {
            System.out.println("Task " + id + " not ready: status is " + taskStatus);
            return false;
        }
        if (taskClassName == null) {
            System.out.println("Task " + id + " not ready: no class");
            return false;
        }
        if (dueDate.isAfter(LocalDateTime.now())) {
            System.out.println("Task " + id + " not ready: scheduled in future");
            return false;
        }

        try {
            // Only check dependencies if they're initialized
            if (org.hibernate.Hibernate.isInitialized(dependencies)) {
                return dependencies.stream().allMatch(dependency -> dependency.getStatus() == TaskStatus.DONE);
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

    public TaskStatus getStatus() {
        return taskStatus;
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