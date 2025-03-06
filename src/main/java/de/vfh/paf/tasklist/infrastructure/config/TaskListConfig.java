package de.vfh.paf.tasklist.infrastructure.config;

import de.vfh.paf.tasklist.domain.repository.NotificationRepository;
import de.vfh.paf.tasklist.domain.repository.TaskRepository;
import de.vfh.paf.tasklist.domain.repository.UserRepository;
import de.vfh.paf.tasklist.infrastructure.persistence.TaskRepositoryInMemory;
import de.vfh.paf.tasklist.domain.service.NotificationService;
import de.vfh.paf.tasklist.domain.service.TaskQueueService;
import de.vfh.paf.tasklist.domain.service.TaskService;
import de.vfh.paf.tasklist.domain.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for the Task List application.
 * Defines Spring beans for repositories and services.
 */
@Configuration
public class TaskListConfig {

    /**
     * Creates a task repository.
     * This demonstrates the Singleton pattern through Spring's bean management.
     *
     * @return The task repository instance
     */
    @Bean
    public TaskRepository taskRepository() {
        return new TaskRepositoryInMemory();
    }

    // TaskService bean removed as it's now defined with @Service annotation
    
    // TaskQueueService bean removed as it's now defined with @Service annotation

    // NotificationRepository bean removed as it's already defined with @Repository annotation
    
    // UserRepository bean removed as it's already defined with @Repository annotation
    
    // UserService bean removed as it's already defined with @Service annotation
    
    // NotificationService bean removed as it's already defined with @Service annotation
}