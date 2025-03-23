package de.vfh.paf.tasklist.application.dto;

import de.vfh.paf.tasklist.domain.model.TaskResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for TaskResult entity.
 */
@Setter
@Getter
@Schema(description = "Data Transfer Object for task execution results")
public class TaskResultDTO {

    // Getters and setters
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

}