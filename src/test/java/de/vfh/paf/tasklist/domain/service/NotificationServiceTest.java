package de.vfh.paf.tasklist.domain.service;

import de.vfh.paf.tasklist.application.dto.NotificationDTO;
import de.vfh.paf.tasklist.application.dto.NotificationPayload;
import de.vfh.paf.tasklist.domain.model.Notification;
import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.repository.NotificationRepository;
import de.vfh.paf.tasklist.domain.repository.TaskRepository;
import de.vfh.paf.tasklist.infrastructure.persistence.TaskRepositoryInMemory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import static org.mockito.Mockito.verify;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {
    private NotificationService notificationService;
    private TaskRepository taskRepository;
    private NotificationRepository notificationRepository;
    private List<Notification> sentNotifications;
    private SimpMessagingTemplate messagingTemplateMock;

    @BeforeEach
    void setUp() {
        taskRepository = new TaskRepositoryInMemory();
        notificationRepository = new NotificationRepository();
        sentNotifications = new CopyOnWriteArrayList<>();
        
        // Create mock for SimpMessagingTemplate
        messagingTemplateMock = Mockito.mock(SimpMessagingTemplate.class);
        
        // Custom notification sender function
        Function<Notification, Boolean> notificationSender = notification -> {
            notification.send();
            sentNotifications.add(notification);
            return true;
        };
        
        // Create a notification service with a custom notification sender
        notificationService = new NotificationService(taskRepository, notificationRepository, notificationSender, messagingTemplateMock);
    }

    @Test
    void shouldNotifyAboutOverdueTasks() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        // Add an overdue task
        Task overdueTask = new Task(1, "Overdue Task", now.minusDays(1), 100);
        taskRepository.save(overdueTask);
        
        // Add a future task (not overdue)
        Task futureTask = new Task(2, "Future Task", now.plusDays(1), 100);
        taskRepository.save(futureTask);
        
        // Act
        int notificationsSent = notificationService.checkAndNotifyOverdueTasks();
        
        // Assert
        assertEquals(1, notificationsSent);
        assertEquals(1, sentNotifications.size());
        
        Notification notification = sentNotifications.get(0);
        assertEquals(100, notification.getUserId());
        assertTrue(notification.getMessage().contains("Overdue Task"));
    }

    @Test
    void shouldNotSendNotificationsWhenNoOverdueTasks() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        // Add only future tasks
        Task futureTask1 = new Task(1, "Future Task 1", now.plusDays(1), 100);
        Task futureTask2 = new Task(2, "Future Task 2", now.plusDays(2), 100);
        taskRepository.save(futureTask1);
        taskRepository.save(futureTask2);
        
        // Act
        int notificationsSent = notificationService.checkAndNotifyOverdueTasks();
        
        // Assert
        assertEquals(0, notificationsSent);
        assertEquals(0, sentNotifications.size());
    }

    @Test
    void shouldSendCustomNotification() {
        // Arrange
        int userId = 100;
        String message = "Custom notification message";
        
        // Act
        boolean result = notificationService.sendNotification(userId, message);
        
        // Assert
        assertTrue(result);
        assertEquals(1, sentNotifications.size());
        
        Notification notification = sentNotifications.get(0);
        assertEquals(userId, notification.getUserId());
        assertEquals(message, notification.getMessage());
    }
    
    @Test
    void shouldBroadcastSystemNotification() {
        // Arrange
        String type = "SYSTEM_ALERT";
        String message = "System maintenance in 10 minutes";
        String urgency = "HIGH";
        
        // Act
        boolean result = notificationService.broadcastSystemNotification(type, message, urgency);
        
        // Assert
        assertTrue(result);
        
        // Verify that a notification was saved to the repository with system user ID (0)
        List<Notification> systemNotifications = notificationService.findByUserId(0);
        assertEquals(1, systemNotifications.size());
        
        Notification notification = systemNotifications.get(0);
        assertEquals(0, notification.getUserId());
        assertEquals(type, notification.getType());
        assertEquals(message, notification.getMessage());
        assertEquals(urgency, notification.getUrgency());
        
        // Verify that messagingTemplate was called
        verify(messagingTemplateMock).convertAndSend("/topic/system", 
                NotificationPayload.fromDto(new NotificationDTO(notification)));
    }
}