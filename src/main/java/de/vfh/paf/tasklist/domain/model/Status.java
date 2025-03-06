package de.vfh.paf.tasklist.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents the possible states of a Task in the system.
 * This is an example of the State pattern, where a task's behavior
 * changes based on its current state.
 */
@Schema(description = "Task status representing its current state in the lifecycle")
public enum Status {
    @Schema(description = "Task has been created but not yet started or queued")
    CREATED,
    
    @Schema(description = "Task is in a queue waiting to be processed")
    QUEUED,
    
    @Schema(description = "Task is currently being processed")
    RUNNING,
    
    @Schema(description = "Task has been completed successfully")
    DONE
}