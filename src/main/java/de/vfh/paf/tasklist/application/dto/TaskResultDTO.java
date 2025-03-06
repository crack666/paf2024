package de.vfh.paf.tasklist.application.dto;

import de.vfh.paf.tasklist.domain.model.TaskResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for TaskResult entity.
 */
@Schema(description = "Data Transfer Object for task execution results")
public class TaskResultDTO {
    
    @Schema(description = "Title of the result")
    private String title;
    
    @Schema(description = "Detailed result content")
    private String content;
    
    @Schema(description = "Timestamp when the result was produced")
    private LocalDateTime timestamp;
    
    public TaskResultDTO() {
    }
    
    public TaskResultDTO(TaskResult taskResult) {
        this.title = taskResult.getTitle();
        this.content = taskResult.getContent();
        this.timestamp = taskResult.getTimestamp();
    }
    
    // Getters and setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}