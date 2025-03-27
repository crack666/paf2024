package de.vfh.paf.tasklist.domain.service;

import de.vfh.paf.tasklist.application.dto.NotificationDTO;
import de.vfh.paf.tasklist.domain.model.Notification;
import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.repository.NotificationRepository;
import de.vfh.paf.tasklist.domain.repository.TaskRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Service for managing notifications.
 */
@Service
@Transactional
public class NotificationService {
    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;
    private final Function<Notification, Boolean> notificationSender;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Creates a new notification service with default dependencies.
     *
     * @param taskRepository         The repository for tasks
     * @param notificationRepository The repository for notifications
     * @param messagingTemplate      The WebSocket messaging template
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
     * @param taskRepository         The repository for tasks
     * @param notificationRepository The repository for notifications
     * @param notificationSender     The function to send notifications
     * @param messagingTemplate      The WebSocket messaging template
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
     * Only sends notifications for tasks that haven't been notified yet.
     *
     * @return The number of notifications sent
     */
    public int checkAndNotifyOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> overdueTasks = taskRepository.findOverdueTasks(now);
        int notificationsSent = 0;

        for (Task task : overdueTasks) {
            // Check if there's already an active (unread) notification for this task being overdue
            // regardless of which user it was sent to
            List<Notification> existingNotifications = 
                    notificationRepository.findUnreadByTypeAndRelatedTaskId("TASK_OVERDUE", task.getId());

            // Skip if there's at least one unread overdue notification for this task (for any user)
            if (!existingNotifications.isEmpty()) {
                org.slf4j.LoggerFactory.getLogger(NotificationService.class).debug(
                    "Skipping notification for overdue task {}: existing unread notification found", task.getId());
                continue;
            }

            String message = String.format("Task '%s' is overdue. Due date was %s",
                    task.getTitle(), task.getDueDate().toString());

            boolean sent = sendNotification("TASK_OVERDUE", "HIGH", task.getAssignedUserId(), message, task.getId());
            if (sent) {
                notificationsSent++;
                org.slf4j.LoggerFactory.getLogger(NotificationService.class).info(
                    "Sent notification for overdue task {}", task.getId());
            }
        }

        return notificationsSent;
    }

    /**
     * Sends a notification to a user.
     *
     * @param userId  The ID of the user to notify
     * @param message The notification message
     * @return true if the notification was sent successfully, false otherwise
     */
    public boolean sendNotification(int userId, String message) {
        return sendNotification("NOTIFICATION", "NORMAL", userId, message, null);
    }

    /**
     * Sends a detailed notification to a user.
     *
     * @param type          The type of notification
     * @param urgency       The urgency level
     * @param userId        The ID of the user to notify
     * @param message       The notification message
     * @param relatedTaskId The ID of the related task (optional)
     * @return true if the notification was sent successfully, false otherwise
     */
    public boolean sendNotification(String type, String urgency, int userId, String message, Integer relatedTaskId) {
        // For task-related notifications, we need to check if there's already ANY notification
        // of this type for this task ID (regardless of user or read status) to prevent duplicates
        if (relatedTaskId != null && (
                type.equals("TASK_OVERDUE") || 
                type.equals("TASK_STARTED") || 
                type.equals("TASK_COMPLETED") || 
                type.equals("TASK_ERROR") ||
                type.equals("DEADLOCK_DETECTED"))) {
                
            List<Notification> existingTaskNotifications = 
                    notificationRepository.findByTypeAndRelatedTaskId(type, relatedTaskId);
                    
            if (!existingTaskNotifications.isEmpty()) {
                org.slf4j.LoggerFactory.getLogger(NotificationService.class).debug(
                    "Skipping notification of type {} for task {}: notification already exists (read or unread)", 
                    type, relatedTaskId);
                return false;
            }
        } else {
            // For user-specific notifications (non-task related), check if the user already 
            // has an unread notification of this type
            List<Notification> existingUserNotifications =
                    notificationRepository.findByTypeAndUserIdAndRelatedTaskId(type, userId, relatedTaskId);
                    
            if (existingUserNotifications.stream().anyMatch(n -> !n.isRead())) {
                return false;
            }
        }

        // Create notification in CREATED state - let JPA generate the ID
        Notification notification = new Notification(null, message, urgency, type, userId, relatedTaskId);

        // Use the notificationSender to send and transition to SENT state
        boolean sent = notificationSender.apply(notification);

        if (sent) {
            // Save to repository
            Notification savedNotification = notificationRepository.save(notification);

            // Send WebSocket notification - use NotificationDTO directly
            NotificationDTO dto = new NotificationDTO(savedNotification);

            // Send to topic for general notifications
            messagingTemplate.convertAndSend("/topic/notifications", dto);

            // Send to user-specific channel
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(userId),
                    "/queue/notifications",
                    dto
            );
        }

        return sent;
    }

    /**
     * Broadcasts a system notification to all users.
     *
     * @param type          The type of notification
     * @param message       The notification message
     * @param urgency       The urgency level
     * @param relatedTaskId The ID of the related task (if applicable)
     * @return true if the notification was sent successfully
     */
    public boolean broadcastSystemNotification(String type, String message, String urgency, Integer relatedTaskId) {
        // For task-related system notifications, check if there's already ANY notification
        // with this type and task ID (regardless of read status) to prevent duplicates
        if (relatedTaskId != null) {
            List<Notification> existingTaskNotifications = 
                    notificationRepository.findByTypeAndRelatedTaskId(type, relatedTaskId);
                    
            if (!existingTaskNotifications.isEmpty()) {
                org.slf4j.LoggerFactory.getLogger(NotificationService.class).debug(
                    "Skipping system broadcast of type {} for task {}: notification already exists (read or unread)", 
                    type, relatedTaskId);
                return false;
            }
        } else {
            // For non-task related system broadcasts, check if there's any unread notification of this type
            List<Notification> existingNotifications =
                    notificationRepository.findByTypeAndReadStatus(type, false);
                    
            // Only send if there are no unread notifications of this type
            if (!existingNotifications.isEmpty()) {
                org.slf4j.LoggerFactory.getLogger(NotificationService.class).debug(
                    "Skipping system broadcast of type {}: existing unread notification found", type);
                return false;
            }
        }

        Notification notification = new Notification(null, message, urgency, type, 0, relatedTaskId); // System user ID is 0

        // Save to repository
        Notification savedNotification = notificationRepository.save(notification);

        // Create DTO for WebSocket - use NotificationDTO directly
        NotificationDTO dto = new NotificationDTO(savedNotification);

        // Broadcast to all connected clients
        messagingTemplate.convertAndSend("/topic/system", dto);

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
        return notificationRepository.findByUserId(userId);
    }

    /**
     * Finds notifications for a user with a specific read status.
     *
     * @param userId The ID of the user
     * @param read   The read status
     * @return List of notifications
     */
    public List<Notification> findByUserIdAndReadStatus(int userId, boolean read) {
        return notificationRepository.findByUserIdAndReadStatus(userId, read);
    }
    
    /**
     * Finds notifications of a specific type with a specific read status.
     *
     * @param type The type of notification
     * @param read The read status
     * @return List of notifications
     */
    public List<Notification> findByTypeAndReadStatus(String type, boolean read) {
        return notificationRepository.findByTypeAndReadStatus(type, read);
    }

    /**
     * Marks a notification as read.
     *
     * @param userId         The ID of the user
     * @param notificationId The ID of the notification
     * @return The updated notification, or null if not found
     */
    public Notification markAsRead(int userId, int notificationId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);

        if (optionalNotification.isEmpty() || optionalNotification.get().getUserId() != userId) {
            return null;
        }

        Notification notification = optionalNotification.get();
        boolean marked = notification.markAsRead();
        if (marked) {
            notificationRepository.save(notification);
        }
        return notification;
    }

    /**
     * Sets a custom notification sender function.
     * Used mainly for testing to override the default notification sender.
     *
     * @param notificationSender The function to use for sending notifications
     */
    public void setNotificationSender(Function<Notification, Boolean> notificationSender) {
        // This field is final in a normal application context but is provided for testing
        // We use reflection to allow setting it in tests
        try {
            java.lang.reflect.Field field = this.getClass().getDeclaredField("notificationSender");
            field.setAccessible(true);
            field.set(this, notificationSender);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set notification sender", e);
        }
    }

}