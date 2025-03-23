package de.vfh.paf.tasklist.domain.service;

import de.vfh.paf.tasklist.domain.model.Notification;
import de.vfh.paf.tasklist.domain.model.NotificationStatus;
import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.repository.NotificationRepository;
import de.vfh.paf.tasklist.domain.repository.TaskRepository;
import de.vfh.paf.tasklist.domain.repository.TaskResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@Import({NotificationService.class, TaskService.class})
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TaskResultRepository taskResultRepository;

    @Autowired
    private TaskService taskService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    private List<Notification> sentNotifications;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        notificationRepository.deleteAll();
        taskResultRepository.deleteAll();

        sentNotifications = new CopyOnWriteArrayList<>();

        // Replace the notification sender in the service with our test version
        // that tracks sent notifications
        notificationService.setNotificationSender(notification -> {
            notification.transitionTo(NotificationStatus.SENT);
            sentNotifications.add(notification);
            return true;
        });
    }

    @Test
    void shouldNotifyAboutOverdueTasks() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Add an overdue task
        Task overdueTask = taskService.createTask("Overdue Task", "Description", now.minusDays(1), 100);

        // Add a future task (not overdue)
        Task futureTask = taskService.createTask("Future Task", "Description", now.plusDays(1), 100);

        // Act
        int notificationsSent = notificationService.checkAndNotifyOverdueTasks();

        // Assert
        assertEquals(1, notificationsSent);

        // Verify notification was created in repository
        List<Notification> notifications = notificationRepository.findByUserId(100);
        assertEquals(1, notifications.size());

        Notification notification = notifications.get(0);
        assertEquals(100, notification.getUserId());
        assertTrue(notification.getMessage().contains("Overdue Task"));
    }

    @Test
    void shouldNotSendNotificationsWhenNoOverdueTasks() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Add only future tasks
        Task futureTask1 = taskService.createTask("Future Task 1", "Description", now.plusDays(1), 100);
        Task futureTask2 = taskService.createTask("Future Task 2", "Description", now.plusDays(2), 100);

        // Act
        int notificationsSent = notificationService.checkAndNotifyOverdueTasks();

        // Assert
        assertEquals(0, notificationsSent);

        // Verify no notifications were created
        List<Notification> notifications = notificationRepository.findByUserId(100);
        assertEquals(0, notifications.size());
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

        // Verify notification was created in repository
        List<Notification> notifications = notificationRepository.findByUserId(userId);
        assertEquals(1, notifications.size());

        Notification notification = notifications.get(0);
        assertEquals(userId, notification.getUserId());
        assertEquals(message, notification.getMessage());
    }
}