package de.vfh.paf.tasklist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Task List application.
 * This application demonstrates Domain-Driven Design patterns and concurrency concepts.
 */
@SpringBootApplication
@EnableScheduling
public class TaskListApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskListApplication.class, args);
    }
}