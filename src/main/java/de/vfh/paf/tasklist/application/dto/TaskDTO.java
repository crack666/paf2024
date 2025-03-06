package de.vfh.paf.tasklist.application.dto;

import de.vfh.paf.tasklist.domain.model.Status;
import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.model.TaskResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for Task entity.
 * This is an example of the DTO pattern, which provides a lightweight
 * representation of domain objects for data transfer.
 */
@Schema(description = "Data Transfer Object for Task information")
public class TaskDTO {
    @Schema(description = "Unique identifier of the task", example = "1")
    private int id;
    
    @Schema(description = "Title of the task", example = "Complete project documentation", required = true)
    private String title;
    
    @Schema(description = "Detailed description of the task", example = "Write comprehensive documentation for the project including architecture diagrams")
    private String description;
    
    @Schema(description = "Due date and time for the task", example = "2025-12-31T23:59:59")
    private LocalDateTime dueDate;
    
    @Schema(description = "Whether the task has been completed", example = "false")
    private boolean completed;
    
    @Schema(description = "Current status of the task", example = "IN_PROGRESS")
    private Status status;
    
    @Schema(description = "IDs of tasks that this task depends on", example = "[1, 2, 3]")
    private List<Integer> dependencyIds;
    
    @Schema(description = "ID of the user assigned to this task", example = "42")
    private int assignedUserId;
    
    @Schema(description = "The fully qualified class name of the task implementation", 
            example = "de.vfh.paf.tasklist.domain.tasks.CalculatePiTask")
    private String taskClassName;
    
    @Schema(description = "Time when the task should be executed", 
            example = "2025-12-31T10:00:00")
    private LocalDateTime scheduledTime;
    
    @Schema(description = "Creation timestamp of the task")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp of the task")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Task execution result")
    private TaskResultDTO result;

    public TaskDTO() {
    }

    public TaskDTO(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.dueDate = task.getDueDate();
        this.completed = task.isCompleted();
        this.status = task.getStatus();
        this.dependencyIds = task.getDependencies().stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        this.assignedUserId = task.getAssignedUserId();
        this.taskClassName = task.getTaskClassName();
        this.scheduledTime = task.getScheduledTime();
        this.createdAt = task.getCreatedAt();
        this.updatedAt = task.getUpdatedAt();
        
        // Convert task result if available
        TaskResult taskResult = task.getResult();
        if (taskResult != null) {
            this.result = new TaskResultDTO(taskResult);
        }
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Integer> getDependencyIds() {
        return dependencyIds;
    }

    public void setDependencyIds(List<Integer> dependencyIds) {
        this.dependencyIds = dependencyIds;
    }

    public int getAssignedUserId() {
        return assignedUserId;
    }

    public void setAssignedUserId(int assignedUserId) {
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
    
    public TaskResultDTO getResult() {
        return result;
    }
    
    public void setResult(TaskResultDTO result) {
        this.result = result;
    }
}
