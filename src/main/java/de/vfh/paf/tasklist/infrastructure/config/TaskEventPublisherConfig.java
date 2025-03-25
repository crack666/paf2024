package de.vfh.paf.tasklist.infrastructure.config;

import de.vfh.paf.tasklist.domain.tasks.CalculatePiTask;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
public class TaskEventPublisherConfig {

    private final ApplicationEventPublisher publisher;

    public TaskEventPublisherConfig(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostConstruct
    public void init() {
        CalculatePiTask.setEventPublisher(publisher);
    }
}