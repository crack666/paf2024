package de.vfh.paf.tasklist.domain.service;

import de.vfh.paf.tasklist.application.dto.NotificationDTO;
import de.vfh.paf.tasklist.application.dto.NotificationPayload;
import de.vfh.paf.tasklist.domain.model.Notification;
import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.repository.NotificationRepository;
import de.vfh.paf.tasklist.domain.repository.TaskRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for managing notifications.
 */
@Service
public class NotificationService {
    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;
    private final Function<Notification, Boolean> notificationSender;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Creates a new notification service with default dependencies.
     *
     * @param taskRepository The repository for tasks
     * @param notificationRepository The repository for notifications
     * @param messagingTemplate The WebSocket messaging template
     */
    @org.springframework.beans.factory.annotation.Autowired
    public NotificationService(TaskRepository taskRepository, 
                              NotificationRepository notificationRepository,
                              SimpMessagingTemplate messagingTemplate) {
        this(taskRepository, notificationRepository, Notification::send, messagingTemplate);
    }

    /**
     * Creates a new notification service with a custom notification sender.
     *
     * @param taskRepository The repository for tasks
     * @param notificationRepository The repository for notifications
     * @param notificationSender The function to send notifications
     * @param messagingTemplate The WebSocket messaging template
     */
    public NotificationService(TaskRepository taskRepository, 
                              NotificationRepository notificationRepository, 
                              Function<Notification, Boolean> notificationSender,
                              SimpMessagingTemplate messagingTemplate) {
        this.taskRepository = taskRepository;
        this.notificationRepository = notificationRepository;
        this.notificationSender = notificationSender;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Checks for overdue tasks and sends notifications to the assigned users.
     *
     * @return The number of notifications sent
     */
    public int checkAndNotifyOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> overdueTasks = taskRepository.findOverdueTasks(now);
        int notificationsSent = 0;

        for (Task task : overdueTasks) {
            String message = String.format("Task '%s' is overdue. Due date was %s", 
                    task.getTitle(), task.getDueDate().toString());
            
            boolean sent = sendNotification("TASK_OVERDUE", "HIGH", task.getAssignedUserId(), message, task.getId());
            if (sent) {
                notificationsSent++;
            }
        }

        return notificationsSent;
    }

    /**
     * Sends a notification to a user.
     *
     * @param userId The ID of the user to notify
     * @param message The notification message
     * @return true if the notification was sent successfully, false otherwise
     */
    public boolean sendNotification(int userId, String message) {
        return sendNotification("NOTIFICATION", "NORMAL", userId, message, null);
    }
    
    /**
     * Sends a detailed notification to a user.
     *
     * @param type The type of notification
     * @param urgency The urgency level
     * @param userId The ID of the user to notify
     * @param message The notification message
     * @param relatedTaskId The ID of the related task (optional)
     * @return true if the notification was sent successfully, false otherwise
     */
    public boolean sendNotification(String type, String urgency, int userId, String message, Integer relatedTaskId) {
        int id = notificationRepository.getNextId();
        // Create notification with the new type field
        Notification notification = new Notification(id, message, urgency, type, userId, relatedTaskId);
        boolean sent = notificationSender.apply(notification);
        
        if (sent) {
            // Save to repository
            Notification savedNotification = notificationRepository.save(notification);
            
            // Send WebSocket notification
            NotificationDTO dto = new NotificationDTO(savedNotification);
            NotificationPayload payload = NotificationPayload.fromDto(dto);
            
            // Send to topic for general notifications
            messagingTemplate.convertAndSend("/topic/notifications", payload);
            
            // Send to user-specific channel
            messagingTemplate.convertAndSendToUser(
                String.valueOf(userId), 
                "/queue/notifications", 
                payload
            );
        }
        
        return sent;
    }
    
    /**
     * Broadcasts a system notification to all users.
     *
     * @param type The type of notification
     * @param message The notification message
     * @param urgency The urgency level
     * @return true if the notification was sent successfully
     */
    public boolean broadcastSystemNotification(String type, String message, String urgency) {
        int id = notificationRepository.getNextId();
        Notification notification = new Notification(id, message, urgency, type, 0, null); // System user ID is 0
        
        // Save to repository
        notificationRepository.save(notification);
        
        // Create payload for WebSocket
        NotificationDTO dto = new NotificationDTO(notification);
        NotificationPayload payload = NotificationPayload.fromDto(dto);
        
        // Broadcast to all connected clients
        messagingTemplate.convertAndSend("/topic/system", payload);
        
        return true;
    }
    
    /**
     * Finds a notification by ID.
     *
     * @param id The ID of the notification
     * @return Optional containing the notification, or empty if not found
     */
    public Optional<Notification> findById(int id) {
        return notificationRepository.findById(id);
    }
    
    /**
     * Finds all notifications in the system.
     *
     * @return List of all notifications
     */
    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }
    
    /**
     * Finds all notifications for a user.
     *
     * @param userId The ID of the user
     * @return List of notifications
     */
    public List<Notification> findByUserId(int userId) {
        return notificationRepository.findAll().stream()
                .filter(notification -> notification.getUserId() == userId)
                .collect(Collectors.toList());
    }
    
    /**
     * Finds notifications for a user with a specific read status.
     *
     * @param userId The ID of the user
     * @param read The read status
     * @return List of notifications
     */
    public List<Notification> findByUserIdAndReadStatus(int userId, boolean read) {
        return notificationRepository.findAll().stream()
                .filter(notification -> notification.getUserId() == userId && notification.isRead() == read)
                .collect(Collectors.toList());
    }
    
    /**
     * Marks a notification as read.
     *
     * @param userId The ID of the user
     * @param notificationId The ID of the notification
     * @return The updated notification, or null if not found
     */
    public Notification markAsRead(int userId, int notificationId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        
        if (optionalNotification.isEmpty() || optionalNotification.get().getUserId() != userId) {
            return null;
        }
        
        Notification notification = optionalNotification.get();
        notification.markAsRead();
        notificationRepository.save(notification);
        return notification;
    }
}