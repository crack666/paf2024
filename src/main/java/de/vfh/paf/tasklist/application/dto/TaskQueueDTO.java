package de.vfh.paf.tasklist.application.dto;

import de.vfh.paf.tasklist.domain.model.TaskQueue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for TaskQueue entity.
 */
@Getter
@Schema(description = "Data Transfer Object for task queues")
public class TaskQueueDTO {

    // Getters and setters
    @Setter
    @Schema(description = "Unique identifier of the task queue")
    private int id;
    
    @Setter
    @Schema(description = "Name of the queue")
    private String name;
    
    @Schema(description = "List of tasks in the queue")
    private List<TaskDTO> tasks;
    
    @Schema(description = "When the queue was created")
    private LocalDateTime createdAt;
    
    @Schema(description = "When the queue was last updated")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Number of tasks in the queue")
    private int taskCount;
    
    public TaskQueueDTO() {
    }
    
    public TaskQueueDTO(TaskQueue taskQueue) {
        this.id = taskQueue.getId();
        this.name = taskQueue.getName();
        this.createdAt = taskQueue.getCreatedAt();
        this.updatedAt = taskQueue.getUpdatedAt();
        
        if (taskQueue.getTasks() != null) {
            this.tasks = taskQueue.getTasks().stream()
                    .map(TaskDTO::new)
                    .collect(Collectors.toList());
            this.taskCount = this.tasks.size();
        } else {
            this.taskCount = 0;
        }
    }

    public void setTasks(List<TaskDTO> tasks) {
        this.tasks = tasks;
        this.taskCount = tasks != null ? tasks.size() : 0;
    }

}