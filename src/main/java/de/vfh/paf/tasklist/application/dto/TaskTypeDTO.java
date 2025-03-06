package de.vfh.paf.tasklist.application.dto;

import de.vfh.paf.tasklist.domain.model.RunnableTask;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for available task types.
 */
@Schema(description = "Information about an available task type")
public class TaskTypeDTO {
    
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
    
    // Getters and setters
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}