package de.vfh.paf.tasklist.infrastructure.config;

import de.vfh.paf.tasklist.domain.model.Notification;
import de.vfh.paf.tasklist.domain.model.Status;
import de.vfh.paf.tasklist.domain.model.Task;
import de.vfh.paf.tasklist.domain.model.User;
import de.vfh.paf.tasklist.domain.repository.NotificationRepository;
import de.vfh.paf.tasklist.domain.repository.TaskRepository;
import de.vfh.paf.tasklist.domain.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Initializes sample data for the application.
 */
@Configuration
public class DataInitializer {

    /**
     * Creates sample data for development and demo purposes.
     */
    @Bean
    public CommandLineRunner initData(UserRepository userRepository, 
                                     TaskRepository taskRepository,
                                     NotificationRepository notificationRepository) {
        return args -> {
            // Only initialize if the database is empty
            if (userRepository.count() == 0) {
                // Create users
                User admin = new User(null, "Admin User", "admin@example.com");
                User john = new User(null, "John Doe", "john@example.com");
                User jane = new User(null, "Jane Smith", "jane@example.com");
                
                List<User> users = Arrays.asList(admin, john, jane);
                userRepository.saveAll(users);
                
                // Create tasks
                Task task1 = new Task(null, "Complete project plan", 
                        "Create a comprehensive project plan for the Q2 release", 
                        LocalDateTime.now().plusDays(5), false, Status.CREATED, 
                        john.getId(), null, null);
                
                Task task2 = new Task(null, "Review code changes", 
                        "Code review for the authentication module PR", 
                        LocalDateTime.now().plusDays(2), false, Status.CREATED, 
                        jane.getId(), null, null);
                
                Task task3 = new Task(null, "Deploy to staging", 
                        "Deploy the latest changes to the staging environment", 
                        LocalDateTime.now().plusDays(3), false, Status.CREATED, 
                        admin.getId(), null, null);
                
                List<Task> tasks = Arrays.asList(task1, task2, task3);
                taskRepository.saveAll(tasks);
                
                // Add task dependencies
                task3.addDependency(task2);
                taskRepository.save(task3);
                
                // Create notifications
                Notification notification1 = new Notification(null, "Welcome to the Task List application!", 
                        "NORMAL", "WELCOME", john.getId(), null);
                notification1.send();
                
                Notification notification2 = new Notification(null, "New task assigned to you", 
                        "HIGH", "TASK_ASSIGNED", jane.getId(), task2.getId());
                notification2.send();
                
                Notification notification3 = new Notification(null, "System maintenance scheduled for next weekend", 
                        "NORMAL", "SYSTEM", admin.getId(), null);
                notification3.send();
                
                List<Notification> notifications = Arrays.asList(notification1, notification2, notification3);
                notificationRepository.saveAll(notifications);
                
                System.out.println("Initialized sample data with " + users.size() + " users, " + 
                        tasks.size() + " tasks, and " + notifications.size() + " notifications");
            }
        };
    }
}