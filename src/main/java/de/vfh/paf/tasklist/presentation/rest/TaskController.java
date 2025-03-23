package de.vfh.paf.tasklist.presentation.rest;

import de.vfh.paf.tasklist.application.dto.TaskDTO;
import de.vfh.paf.tasklist.application.dto.TaskProgressDTO;
import de.vfh.paf.tasklist.application.dto.TaskTypeDTO;
import de.vfh.paf.tasklist.application.service.TaskManagerService;
import de.vfh.paf.tasklist.domain.model.RunnableTask;
import de.vfh.paf.tasklist.domain.model.Status;
import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.service.TaskFactory;
import de.vfh.paf.tasklist.domain.service.TaskProcessorService;
import de.vfh.paf.tasklist.domain.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for task management.
 * This is an example of the Controller pattern in the presentation layer.
 */
@RestController
@RequestMapping("/tasks")
@Tag(name = "Task Management", description = "APIs for managing tasks in the system")
public class TaskController {
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;
    private final TaskManagerService taskManagerService;
    private final TaskFactory taskRegistry;
    private final TaskProcessorService taskExecutor;

    public TaskController(TaskService taskService, TaskManagerService taskManagerService,
                          TaskFactory taskRegistry, TaskProcessorService taskExecutor) {
        this.taskService = taskService;
        this.taskManagerService = taskManagerService;
        this.taskRegistry = taskRegistry;
        this.taskExecutor = taskExecutor;
    }

    /**
     * Creates a new task.
     *
     * @param taskDTO Task data
     * @return Created task
     */
    @PostMapping
    @Operation(summary = "Create a new task", description = "Creates a new task with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task created successfully",
                    content = @Content(schema = @Schema(implementation = TaskDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<TaskDTO> createTask(
            @Parameter(description = "Task details", required = true) @RequestBody TaskDTO taskDTO) {

        // If task class name is provided, create a runnable task
        if (taskDTO.getTaskClassName() != null && !taskDTO.getTaskClassName().isEmpty()) {
            Task task = taskService.createRunnableTask(
                    taskDTO.getTitle(),
                    taskDTO.getDescription(),
                    taskDTO.getDueDate(),
                    taskDTO.getAssignedUserId(),
                    taskDTO.getTaskClassName(),
                    taskDTO.getScheduledTime()
            );
            return ResponseEntity.ok(new TaskDTO(task));
        } else {
            // Create a regular task
            Task task = taskService.createTask(
                    taskDTO.getTitle(),
                    taskDTO.getDescription(),
                    taskDTO.getDueDate(),
                    taskDTO.getAssignedUserId()
            );
            return ResponseEntity.ok(new TaskDTO(task));
        }
    }

    /**
     * Gets a task by ID.
     *
     * @param id Task ID
     * @return Task data
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a task by ID", description = "Retrieves a task by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found",
                    content = @Content(schema = @Schema(implementation = TaskDTO.class))),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskDTO> getTask(
            @Parameter(description = "Task ID", required = true) @PathVariable int id) {
        return taskService.findById(id)
                .map(TaskDTO::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates a task.
     *
     * @param id      Task ID
     * @param taskDTO Task data
     * @return Updated task
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a task", description = "Updates an existing task with new information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully",
                    content = @Content(schema = @Schema(implementation = TaskDTO.class))),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<TaskDTO> updateTask(
            @Parameter(description = "Task ID", required = true) @PathVariable int id,
            @Parameter(description = "Updated task details", required = true) @RequestBody TaskDTO taskDTO
    ) {
        Task updatedTask = taskService.updateTask(
                id,
                taskDTO.getTitle(),
                taskDTO.getDescription(),
                taskDTO.getDueDate(),
                taskDTO.getStatus()
        );

        if (updatedTask == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new TaskDTO(updatedTask));
    }

    /**
     * Completes a task.
     *
     * @param id Task ID
     * @return Completed task
     */
    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete a task", description = "Marks a task as completed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task completed successfully",
                    content = @Content(schema = @Schema(implementation = TaskDTO.class))),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskDTO> completeTask(
            @Parameter(description = "Task ID", required = true) @PathVariable int id) {
        Task completedTask = taskService.completeTask(id);

        if (completedTask == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new TaskDTO(completedTask));
    }

    /**
     * Adds a dependency to a task.
     *
     * @param id           Task ID
     * @param dependencyId Dependency task ID
     * @return Updated task
     */
    @PostMapping("/{id}/dependencies/{dependencyId}")
    @Operation(summary = "Add a task dependency",
            description = "Makes one task dependent on another task, with deadlock detection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dependency added successfully",
                    content = @Content(schema = @Schema(implementation = TaskDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid dependency - would create a deadlock"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskDTO> addDependency(
            @Parameter(description = "Task ID", required = true) @PathVariable int id,
            @Parameter(description = "Dependency task ID", required = true) @PathVariable int dependencyId
    ) {
        // Check if adding this dependency would create a deadlock
        if (taskManagerService.wouldCreateDeadlock(id, dependencyId)) {
            return ResponseEntity.badRequest().build();
        }

        Task updatedTask = taskService.addDependency(id, dependencyId);

        if (updatedTask == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new TaskDTO(updatedTask));
    }

    /**
     * Gets all tasks in the system.
     *
     * @return List of all tasks
     */
    @GetMapping
    @Operation(summary = "Get all tasks", description = "Retrieves a list of all tasks in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaskDTO.class))))
    })
    public ResponseEntity<List<TaskDTO>> getAllTasks(
            @Parameter(description = "Filter tasks by status (optional)")
            @RequestParam(required = false) Status status,
            @Parameter(description = "Filter tasks by user ID (optional)")
            @RequestParam(required = false) Integer userId) {

        List<Task> tasks;

        // Apply filters based on provided parameters
        if (status != null && userId != null) {
            tasks = taskService.findByUserIdAndStatus(userId, status);
        } else if (status != null) {
            tasks = taskService.findByStatus(status);
        } else if (userId != null) {
            tasks = taskService.findByUserId(userId);
        } else {
            tasks = taskService.findAll();
        }

        List<TaskDTO> taskDTOs = tasks.stream()
                .map(TaskDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(taskDTOs);
    }

    /**
     * Gets all available task types.
     *
     * @return List of all available task types
     */
    @GetMapping("/types")
    @Operation(summary = "Get all task types",
            description = "Retrieves a list of all available task types that can be executed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task types retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaskTypeDTO.class))))
    })
    public ResponseEntity<List<TaskTypeDTO>> getTaskTypes() {
        List<TaskTypeDTO> taskTypes = taskRegistry.getTaskTypeMap().keySet().stream()
                .map(s -> {
                    RunnableTask taskType = taskRegistry.getTaskType(s);
                    return new TaskTypeDTO(s, taskType);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(taskTypes);
    }

    /**
     * Executes a task immediately.
     * This endpoint has two modes:
     * 1. Synchronous (wait=true): Waits for the task to complete and returns the result
     * 2. Asynchronous (wait=false): Starts the task in a background thread and returns immediately
     *
     * @param id   Task ID
     * @param wait Whether to wait for task completion
     * @return Executed task with result (if wait=true) or accepted response (if wait=false)
     */
    @PostMapping("/{id}/execute")
    @Operation(summary = "Execute a task",
            description = "Executes a task immediately in a separate thread. Can wait for completion (sync) or return immediately (async).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task executed successfully (sync mode)",
                    content = @Content(schema = @Schema(implementation = TaskDTO.class))),
            @ApiResponse(responseCode = "202", description = "Task execution started (async mode)"),
            @ApiResponse(responseCode = "400", description = "Task is not ready to execute"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<?> executeTask(
            @Parameter(description = "Task ID", required = true) @PathVariable int id,
            @Parameter(description = "Whether to wait for task completion", example = "true")
            @RequestParam(required = false, defaultValue = "true") boolean wait) {

        // Check if task exists
        if (taskService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (wait) {
            // Synchronous execution - wait for result
            Task executedTask = taskExecutor.executeTaskSync(id);

            if (executedTask == null) {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok(new TaskDTO(executedTask));
        } else {
            // Asynchronous execution - return immediately
            taskExecutor.executeTask(id)
                    .thenApply(task -> {
                        if (task != null) {
                            logger.info("Task {} executed successfully in background", id);
                        }
                        return task;
                    })
                    .exceptionally(ex -> {
                        logger.error("Error executing task {} in background: {}", id, ex.getMessage());
                        return null;
                    });

            return ResponseEntity.accepted().body("Task execution started in background");
        }
    }

    /**
     * Gets thread pool statistics.
     * Useful for monitoring concurrent task execution.
     *
     * @return Thread pool statistics
     */
    @GetMapping("/thread-pool-stats")
    @Operation(summary = "Get thread pool statistics", description = "Returns statistics about the task executor thread pool")
    public ResponseEntity<String> getThreadPoolStats() {
        return ResponseEntity.ok(taskExecutor.getThreadPoolStats());
    }

    /**
     * Gets the progress of a running task.
     * This is especially useful for long-running tasks that support progress tracking.
     *
     * @param id Task ID
     * @return Task progress information
     */
    @GetMapping("/{id}/progress")
    @Operation(summary = "Get task progress",
            description = "Retrieves progress information for a task, especially useful for long-running tasks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task progress retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TaskProgressDTO.class))),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskProgressDTO> getTaskProgress(
            @Parameter(description = "Task ID", required = true) @PathVariable int id) {

        return taskService.findById(id)
                .map(TaskProgressDTO::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Gets all tasks with a specific status.
     *
     * @param status Task status
     * @return List of tasks with the specified status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Get tasks by status",
            description = "Retrieves all tasks with the specified status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaskDTO.class))))
    })
    public ResponseEntity<List<TaskDTO>> getTasksByStatus(
            @Parameter(description = "Task status", required = true) @PathVariable Status status) {

        List<Task> tasks = taskService.findByStatus(status);
        List<TaskDTO> taskDTOs = tasks.stream()
                .map(TaskDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(taskDTOs);
    }
}
