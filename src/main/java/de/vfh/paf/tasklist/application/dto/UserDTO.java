package de.vfh.paf.tasklist.application.dto;

import de.vfh.paf.tasklist.domain.model.Notification;
import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for User entity.
 */
@Schema(description = "Data Transfer Object for user information")
public class UserDTO {
    
    @Schema(description = "Unique identifier of the user")
    private int id;
    
    @Schema(description = "Name of the user")
    private String name;
    
    @Schema(description = "Email address of the user")
    private String email;
    
    @Schema(description = "Tasks assigned to the user")
    private List<TaskDTO> tasks;
    
    @Schema(description = "Notifications for the user")
    private List<NotificationDTO> notifications;
    
    public UserDTO() {
    }
    
    public UserDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        
        if (user.getTasks() != null) {
            this.tasks = user.getTasks().stream()
                    .map(TaskDTO::new)
                    .collect(Collectors.toList());
        }
        
        if (user.getNotifications() != null) {
            this.notifications = user.getNotifications().stream()
                    .map(NotificationDTO::new)
                    .collect(Collectors.toList());
        }
    }
    
    // Simplified constructor without tasks and notifications for creating new users
    public UserDTO(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    // Getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public List<TaskDTO> getTasks() {
        return tasks;
    }
    
    public void setTasks(List<TaskDTO> tasks) {
        this.tasks = tasks;
    }
    
    public List<NotificationDTO> getNotifications() {
        return notifications;
    }
    
    public void setNotifications(List<NotificationDTO> notifications) {
        this.notifications = notifications;
    }
}