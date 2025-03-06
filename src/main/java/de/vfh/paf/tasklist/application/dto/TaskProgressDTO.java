package de.vfh.paf.tasklist.application.dto;

import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.tasks.CalculatePiTask;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for task progress information.
 */
@Schema(description = "Data Transfer Object for task progress information")
public class TaskProgressDTO {
    
    @Schema(description = "Task ID")
    private int taskId;
    
    @Schema(description = "Task title")
    private String title;
    
    @Schema(description = "Task implementation class name")
    private String taskClassName;
    
    @Schema(description = "Task current status")
    private String status;
    
    @Schema(description = "Progress percentage (0-100)")
    private int progressPercentage;
    
    @Schema(description = "Start time of the task execution")
    private LocalDateTime startTime;
    
    @Schema(description = "Elapsed time in milliseconds")
    private long elapsedTimeMillis;
    
    @Schema(description = "Estimated time remaining in milliseconds (-1 if unknown)")
    private long estimatedTimeRemainingMillis;
    
    @Schema(description = "Current calculated value (for tasks that support it)")
    private String currentValue;
    
    @Schema(description = "Whether the task has a readable progress")
    private boolean hasProgress;
    
    public TaskProgressDTO() {
    }
    
    public TaskProgressDTO(Task task) {
        this.taskId = task.getId();
        this.title = task.getTitle();
        this.taskClassName = task.getTaskClassName();
        this.status = task.getStatus().toString();
        this.hasProgress = false;
        this.progressPercentage = 0;
        
        // For CalculatePiTask, we can get detailed progress
        if (task.getTaskClassName() != null && 
                task.getTaskClassName().equals(CalculatePiTask.class.getName()) &&
                task.getStatus().toString().equals("RUNNING")) {
            
            CalculatePiTask.ProgressData progressData = CalculatePiTask.getProgress(task.getId());
            if (progressData != null) {
                this.hasProgress = true;
                this.progressPercentage = progressData.getProgressPercentage();
                this.startTime = progressData.getStartTime();
                this.elapsedTimeMillis = progressData.getElapsedTimeMillis();
                this.estimatedTimeRemainingMillis = progressData.getEstimatedTimeRemainingMillis();
                this.currentValue = String.format("%.10f", progressData.getCurrentValue());
            }
        }
        
        // For completed tasks, set progress to 100%
        if (task.getStatus().toString().equals("DONE")) {
            this.hasProgress = true;
            this.progressPercentage = 100;
            if (task.getResult() != null) {
                this.currentValue = task.getResult().getResultValue();
            }
        }
    }
    
    // Getters and setters
    public int getTaskId() {
        return taskId;
    }
    
    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getTaskClassName() {
        return taskClassName;
    }
    
    public void setTaskClassName(String taskClassName) {
        this.taskClassName = taskClassName;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public int getProgressPercentage() {
        return progressPercentage;
    }
    
    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public long getElapsedTimeMillis() {
        return elapsedTimeMillis;
    }
    
    public void setElapsedTimeMillis(long elapsedTimeMillis) {
        this.elapsedTimeMillis = elapsedTimeMillis;
    }
    
    public long getEstimatedTimeRemainingMillis() {
        return estimatedTimeRemainingMillis;
    }
    
    public void setEstimatedTimeRemainingMillis(long estimatedTimeRemainingMillis) {
        this.estimatedTimeRemainingMillis = estimatedTimeRemainingMillis;
    }
    
    public String getCurrentValue() {
        return currentValue;
    }
    
    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }
    
    public boolean isHasProgress() {
        return hasProgress;
    }
    
    public void setHasProgress(boolean hasProgress) {
        this.hasProgress = hasProgress;
    }
}