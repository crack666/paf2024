package de.vfh.paf.tasklist.presentation.rest;

import de.vfh.paf.tasklist.application.dto.NotificationDTO;
import de.vfh.paf.tasklist.application.dto.TaskDTO;
import de.vfh.paf.tasklist.application.dto.UserDTO;
import de.vfh.paf.tasklist.domain.model.Notification;
import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.model.User;
import de.vfh.paf.tasklist.domain.service.NotificationService;
import de.vfh.paf.tasklist.domain.service.TaskService;
import de.vfh.paf.tasklist.domain.service.UserService;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for user management.
 */
@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "APIs for managing users in the system")
public class UserController {
    
    private final UserService userService;
    private final TaskService taskService;
    private final NotificationService notificationService;
    
    public UserController(UserService userService, TaskService taskService, NotificationService notificationService) {
        this.userService = userService;
        this.taskService = taskService;
        this.notificationService = notificationService;
    }
    
    /**
     * Gets all users in the system.
     *
     * @return List of all users
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves a list of all users in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDTO.class))))
    })
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> userDTOs = userService.findAll().stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(userDTOs);
    }
    
    /**
     * Gets a user by ID.
     *
     * @param id User ID
     * @return User data
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a user by ID", description = "Retrieves a user by their unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDTO> getUser(
            @Parameter(description = "User ID", required = true) @PathVariable int id) {
        return userService.findById(id)
                .map(UserDTO::new)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Creates a new user.
     *
     * @param userDTO User data
     * @return Created user
     */
    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<UserDTO> createUser(
            @Parameter(description = "User details", required = true) @RequestBody UserDTO userDTO) {
        
        User user = userService.createUser(
                userDTO.getName(),
                userDTO.getEmail()
        );
        
        return ResponseEntity.ok(new UserDTO(user));
    }
    
    /**
     * Updates a user.
     *
     * @param id User ID
     * @param userDTO User data
     * @return Updated user
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a user", description = "Updates an existing user with new information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<UserDTO> updateUser(
            @Parameter(description = "User ID", required = true) @PathVariable int id,
            @Parameter(description = "Updated user details", required = true) @RequestBody UserDTO userDTO
    ) {
        User updatedUser = userService.updateUser(
                id,
                userDTO.getName(),
                userDTO.getEmail()
        );
        
        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(new UserDTO(updatedUser));
    }
    
    /**
     * Gets all tasks assigned to a user.
     *
     * @param id User ID
     * @return List of tasks
     */
    @GetMapping("/{id}/tasks")
    @Operation(summary = "Get user's tasks", description = "Retrieves all tasks assigned to the specified user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaskDTO.class)))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<TaskDTO>> getUserTasks(
            @Parameter(description = "User ID", required = true) @PathVariable int id) {
        
        if (!userService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        List<Task> tasks = taskService.findByUserId(id);
        List<TaskDTO> taskDTOs = tasks.stream()
                .map(TaskDTO::new)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(taskDTOs);
    }
    
    /**
     * Gets all notifications for a user.
     *
     * @param id User ID
     * @return List of notifications
     */
    @GetMapping("/{id}/notifications")
    @Operation(summary = "Get user's notifications", description = "Retrieves all notifications for the specified user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificationDTO.class)))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(
            @Parameter(description = "User ID", required = true) @PathVariable int id,
            @Parameter(description = "Filter notifications by read status") 
            @RequestParam(required = false) Boolean read) {
        
        if (!userService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        List<Notification> notifications;
        if (read != null) {
            notifications = notificationService.findByUserIdAndReadStatus(id, read);
        } else {
            notifications = notificationService.findByUserId(id);
        }
        
        List<NotificationDTO> notificationDTOs = notifications.stream()
                .map(NotificationDTO::new)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(notificationDTOs);
    }
    
    /**
     * Marks a notification as read.
     *
     * @param userId User ID
     * @param notificationId Notification ID
     * @return Updated notification
     */
    @PostMapping("/{userId}/notifications/{notificationId}/read")
    @Operation(summary = "Mark notification as read", description = "Marks a user's notification as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification marked as read",
                    content = @Content(schema = @Schema(implementation = NotificationDTO.class))),
            @ApiResponse(responseCode = "404", description = "User or notification not found")
    })
    public ResponseEntity<NotificationDTO> markNotificationAsRead(
            @Parameter(description = "User ID", required = true) @PathVariable int userId,
            @Parameter(description = "Notification ID", required = true) @PathVariable int notificationId) {
        
        Notification notification = notificationService.markAsRead(userId, notificationId);
        
        if (notification == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(new NotificationDTO(notification));
    }
}