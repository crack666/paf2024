package de.vfh.paf.tasklist.presentation.rest;

import de.vfh.paf.tasklist.application.dto.TaskDTO;
import de.vfh.paf.tasklist.application.dto.TaskQueueDTO;
import de.vfh.paf.tasklist.application.dto.TaskResultDTO;
import de.vfh.paf.tasklist.domain.model.Status;
import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.model.TaskQueue;
import de.vfh.paf.tasklist.domain.model.TaskResult;
import de.vfh.paf.tasklist.domain.service.TaskQueueService;
import de.vfh.paf.tasklist.domain.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * REST controller for task queue management.
 */
@RestController
@RequestMapping("/task-queues")
@Tag(name = "Task Queue Management", description = "APIs for managing task queues in the system")
public class TaskQueueController {
    
    private final TaskQueueService taskQueueService;
    private final TaskService taskService;
    
    public TaskQueueController(TaskQueueService taskQueueService, TaskService taskService) {
        this.taskQueueService = taskQueueService;
        this.taskService = taskService;
    }
    
    /**
     * Gets all task queues in the system.
     *
     * @return List of all task queues
     */
    @GetMapping
    @Operation(summary = "Get all task queues", description = "Retrieves a list of all task queues in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task queues retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaskQueueDTO.class))))
    })
    public ResponseEntity<List<TaskQueueDTO>> getAllQueues() {
        List<TaskQueueDTO> queueDTOs = taskQueueService.getAllQueues().stream()
                .map(TaskQueueDTO::new)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(queueDTOs);
    }
    
    /**
     * Gets a task queue by ID.
     *
     * @param id Queue ID
     * @return Task queue data
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a task queue by ID", description = "Retrieves a task queue by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task queue found",
                    content = @Content(schema = @Schema(implementation = TaskQueueDTO.class))),
            @ApiResponse(responseCode = "404", description = "Task queue not found")
    })
    public ResponseEntity<TaskQueueDTO> getQueue(
            @Parameter(description = "Queue ID", required = true) @PathVariable int id) {
        TaskQueue queue = taskQueueService.getQueue(id);
        
        if (queue == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(new TaskQueueDTO(queue));
    }
    
    /**
     * Creates a new task queue.
     *
     * @param queueDTO Queue data
     * @return Created queue
     */
    @PostMapping
    @Operation(summary = "Create a new task queue", description = "Creates a new task queue with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task queue created successfully",
                    content = @Content(schema = @Schema(implementation = TaskQueueDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<TaskQueueDTO> createQueue(
            @Parameter(description = "Queue details", required = true) @RequestBody TaskQueueDTO queueDTO) {
        
        TaskQueue queue = taskQueueService.createQueue(queueDTO.getName());
        return ResponseEntity.ok(new TaskQueueDTO(queue));
    }
    
    /**
     * Gets all tasks in a queue.
     *
     * @param id Queue ID
     * @return List of tasks
     */
    @GetMapping("/{id}/tasks")
    @Operation(summary = "Get queue tasks", description = "Retrieves all current tasks in the specified queue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaskDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Task queue not found")
    })
    public ResponseEntity<List<TaskDTO>> getQueueTasks(
            @Parameter(description = "Queue ID", required = true) @PathVariable int id,
            @Parameter(description = "Filter tasks by status") 
            @RequestParam(required = false) Status status) {
        
        TaskQueue queue = taskQueueService.getQueue(id);
        if (queue == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Task> tasks = queue.getTasks();
        
        // Filter by status if provided
        if (status != null) {
            tasks = tasks.stream()
                    .filter(task -> task.getStatus() == status)
                    .toList();
        }
        
        List<TaskDTO> taskDTOs = tasks.stream()
                .map(TaskDTO::new)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(taskDTOs);
    }
    
    /**
     * Gets all tasks associated with a queue, including current and processed ones.
     *
     * @param id Queue ID
     * @param status Optional status filter
     * @return List of all tasks
     */
    @GetMapping("/{id}/all-tasks")
    @Operation(summary = "Get all queue tasks", description = "Retrieves all tasks ever associated with the queue, including both current and processed ones")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaskDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Task queue not found")
    })
    public ResponseEntity<List<TaskDTO>> getAllQueueTasks(
            @Parameter(description = "Queue ID", required = true) @PathVariable int id,
            @Parameter(description = "Filter tasks by status") 
            @RequestParam(required = false) Status status) {
        
        TaskQueue queue = taskQueueService.getQueue(id);
        if (queue == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Task> tasks = taskQueueService.getAllQueueTasks(id);
        
        // Filter by status if provided
        if (status != null) {
            tasks = tasks.stream()
                    .filter(task -> task.getStatus() == status)
                    .toList();
        }
        
        List<TaskDTO> taskDTOs = tasks.stream()
                .map(TaskDTO::new)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(taskDTOs);
    }
    
    /**
     * Gets all processed tasks from a queue with their results.
     *
     * @param id Queue ID
     * @return Map of task IDs to results
     */
    @GetMapping("/{id}/completed-tasks")
    @Operation(summary = "Get completed tasks with results", description = "Retrieves all completed tasks from a queue with their execution results")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Completed tasks retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Task queue not found")
    })
    public ResponseEntity<Map<String, TaskResultDTO>> getCompletedTasks(
            @Parameter(description = "Queue ID", required = true) @PathVariable int id) {
        
        TaskQueue queue = taskQueueService.getQueue(id);
        if (queue == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<Integer, TaskResult> resultMap = taskQueueService.getProcessedTasksWithResults(id);
        Map<String, TaskResultDTO> dtoMap = new HashMap<>();
        
        resultMap.forEach((taskId, result) -> {
            dtoMap.put(String.valueOf(taskId), new TaskResultDTO(result));
        });
        
        return ResponseEntity.ok(dtoMap);
    }
    
    /**
     * Adds a task to a queue.
     *
     * @param queueId Queue ID
     * @param taskId Task ID
     * @return Updated queue
     */
    @PostMapping("/{queueId}/tasks/{taskId}")
    @Operation(summary = "Add task to queue", description = "Adds a task to the specified queue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task added to queue successfully",
                    content = @Content(schema = @Schema(implementation = TaskQueueDTO.class))),
            @ApiResponse(responseCode = "404", description = "Queue or task not found"),
            @ApiResponse(responseCode = "400", description = "Invalid operation")
    })
    public ResponseEntity<TaskQueueDTO> addTaskToQueue(
            @Parameter(description = "Queue ID", required = true) @PathVariable int queueId,
            @Parameter(description = "Task ID", required = true) @PathVariable int taskId) {
        
        boolean success = taskQueueService.enqueueTask(queueId, taskId);
        
        if (!success) {
            return ResponseEntity.notFound().build();
        }
        
        TaskQueue queue = taskQueueService.getQueue(queueId);
        return ResponseEntity.ok(new TaskQueueDTO(queue));
    }
    
    /**
     * Processes the next task in the queue.
     *
     * @param queueId Queue ID
     * @return Task result
     */
    @PostMapping("/{queueId}/process-next")
    @Operation(summary = "Process next task", description = "Processes the next task in the queue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task processed successfully",
                    content = @Content(schema = @Schema(implementation = TaskResultDTO.class))),
            @ApiResponse(responseCode = "404", description = "Queue not found"),
            @ApiResponse(responseCode = "204", description = "No tasks in queue")
    })
    public ResponseEntity<TaskResultDTO> processNextTask(
            @Parameter(description = "Queue ID", required = true) @PathVariable int queueId,
            @Parameter(description = "Whether to wait for task completion")
            @RequestParam(required = false, defaultValue = "true") boolean wait) {
        
        TaskQueue queue = taskQueueService.getQueue(queueId);
        if (queue == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (queue.getTasks().isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        CompletableFuture<TaskResult> future = taskQueueService.executeNextTask(queueId,
                taskService::processTask);
            
        if (wait) {
            // Wait for the task to complete
            try {
                TaskResult result = future.join();
                if (result == null) {
                    return ResponseEntity.noContent().build();
                }
                return ResponseEntity.ok(new TaskResultDTO(result));
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            // Return immediately, task is executed in background
            return ResponseEntity.accepted().build();
        }
    }
    
    /**
     * Processes all tasks in the queue.
     *
     * @param queueId Queue ID
     * @return Results of all processed tasks
     */
    @PostMapping("/{queueId}/process-all")
    @Operation(summary = "Process all tasks", description = "Processes all tasks in the queue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks processed successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaskResultDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Queue not found"),
            @ApiResponse(responseCode = "204", description = "No tasks in queue")
    })
    public ResponseEntity<List<TaskResultDTO>> processAllTasks(
            @Parameter(description = "Queue ID", required = true) @PathVariable int queueId,
            @Parameter(description = "Whether to wait for all tasks to complete")
            @RequestParam(required = false, defaultValue = "true") boolean wait) {
        
        TaskQueue queue = taskQueueService.getQueue(queueId);
        if (queue == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (queue.getTasks().isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        CompletableFuture<List<TaskResult>> future = taskQueueService.processAllTasks(queueId,
                taskService::processTask);
            
        if (wait) {
            // Wait for all tasks to complete
            try {
                List<TaskResult> results = future.join();
                if (results.isEmpty()) {
                    return ResponseEntity.noContent().build();
                }
                
                List<TaskResultDTO> resultDTOs = results.stream()
                        .filter(Objects::nonNull)
                        .map(TaskResultDTO::new)
                        .collect(Collectors.toList());
                        
                return ResponseEntity.ok(resultDTOs);
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            // Return immediately, tasks are executed in background
            return ResponseEntity.accepted().build();
        }
    }
    
    /**
     * Reorders tasks in a queue.
     *
     * @param queueId Queue ID
     * @param orderCriteria The criteria to use for ordering (e.g., "dueDate")
     * @return Updated queue
     */
    @PostMapping("/{queueId}/reorder")
    @Operation(summary = "Reorder queue tasks", description = "Reorders the tasks in the queue based on the specified criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks reordered successfully",
                    content = @Content(schema = @Schema(implementation = TaskQueueDTO.class))),
            @ApiResponse(responseCode = "404", description = "Queue not found")
    })
    public ResponseEntity<TaskQueueDTO> reorderQueueTasks(
            @Parameter(description = "Queue ID", required = true) @PathVariable int queueId,
            @Parameter(description = "Ordering criteria", example = "dueDate", required = true)
            @RequestParam String orderCriteria) {
        
        TaskQueue queue = taskQueueService.getQueue(queueId);
        if (queue == null) {
            return ResponseEntity.notFound().build();
        }
        
        queue.reorderTasks(orderCriteria);
        return ResponseEntity.ok(new TaskQueueDTO(queue));
    }
}