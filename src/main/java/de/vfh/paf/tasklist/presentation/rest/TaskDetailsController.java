package de.vfh.paf.tasklist.presentation.rest;

import de.vfh.paf.tasklist.application.dto.TaskDTO;
import de.vfh.paf.tasklist.application.dto.TaskResultDTO;
import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.model.TaskResult;
import de.vfh.paf.tasklist.domain.repository.TaskResultRepository;
import de.vfh.paf.tasklist.domain.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Extended controller for task details.
 * This controller provides additional endpoints for retrieving detailed task information
 * including task type (derived from class name) and task results.
 */
@RestController
@RequestMapping("/tasks/details")
@Tag(name = "Task Details", description = "APIs for accessing detailed task information")
public class TaskDetailsController {
    private static final Logger logger = LoggerFactory.getLogger(TaskDetailsController.class);
    
    private final TaskService taskService;
    private final TaskResultRepository taskResultRepository;
    
    @Autowired
    public TaskDetailsController(TaskService taskService, TaskResultRepository taskResultRepository) {
        this.taskService = taskService;
        this.taskResultRepository = taskResultRepository;
    }
    
    /**
     * Gets detailed task information by ID, including task type and result.
     *
     * @param id Task ID
     * @return Enhanced task data
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get detailed task information", 
              description = "Retrieves a task with enhanced type and result information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found",
                    content = @Content(schema = @Schema(implementation = TaskDTO.class))),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskDTO> getTaskWithDetails(
            @Parameter(description = "Task ID", required = true) @PathVariable int id) {
        // Get the task from the repository
        Optional<Task> taskOptional = taskService.findById(id);
        
        if (taskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Task task = taskOptional.get();
        
        // Create a task DTO with enhanced information
        TaskDTO taskDTO = new TaskDTO(task);
        
        // Find the most recent result for this task
        List<TaskResult> results = taskResultRepository.findByTaskId(id);
        if (!results.isEmpty()) {
            // Latest result is usually the first one due to sorting
            TaskResult latestResult = results.get(0);
            taskDTO.setResult(new TaskResultDTO(latestResult));
        }
        
        return ResponseEntity.ok(taskDTO);
    }
    
    /**
     * Gets task result by task ID.
     *
     * @param id Task ID
     * @return Task result data
     */
    @GetMapping("/{id}/result")
    @Operation(summary = "Get task result", 
              description = "Retrieves the result of a task by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Result found",
                    content = @Content(schema = @Schema(implementation = TaskResultDTO.class))),
            @ApiResponse(responseCode = "404", description = "Result not found")
    })
    public ResponseEntity<TaskResultDTO> getTaskResult(
            @Parameter(description = "Task ID", required = true) @PathVariable int id) {
        
        try {
            // First, check if the task exists
            Optional<Task> taskOptional = taskService.findById(id);
            if (taskOptional.isEmpty()) {
                logger.info("Task with ID {} not found", id);
            }
            
            // Then, check for results associated with this task ID
            List<TaskResult> results = taskResultRepository.findByTaskId(id);
            
            if (results.isEmpty()) {
                logger.info("No results found for task ID {}", id);
                return ResponseEntity.notFound().build();
            }
            
            // Return the most recent result (typically the first in the list)
            TaskResult latestResult = results.get(0);
            return ResponseEntity.ok(new TaskResultDTO(latestResult));
        } catch (Exception e) {
            logger.error("Error retrieving result for task ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }
}