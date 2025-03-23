package de.vfh.paf.tasklist.application.dto;

import de.vfh.paf.tasklist.domain.model.RunnableTask;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for available task types.
 */
@Setter
@Getter
@Schema(description = "Information about an available task type")
public class TaskTypeDTO {

    // Getters and setters
    @Schema(description = "Fully qualified class name of the task",
            example = "de.vfh.paf.tasklist.domain.tasks.CalculatePiTask")
    private String className;

    @Schema(description = "Human-readable name of the task", example = "Calculate Pi")
    private String name;

    @Schema(description = "Description of what the task does")
    private String description;

    public TaskTypeDTO() {
    }

    public TaskTypeDTO(String className, RunnableTask taskType) {
        this.className = className;
        this.name = taskType.getName();
        this.description = taskType.getDescription();
    }

}