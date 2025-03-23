package de.vfh.paf.tasklist.infrastructure.config;

import de.vfh.paf.tasklist.domain.model.User;
import de.vfh.paf.tasklist.domain.repository.UserRepository;
import de.vfh.paf.tasklist.infrastructure.persistence.UserRepositoryJpaAdapter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuration for data persistence.
 */
@Configuration
@EnableJpaRepositories(basePackages = "de.vfh.paf.tasklist.domain.repository")
public class PersistenceConfig {

    /**
     * Initialize default data if the database is empty.
     * This ensures compatibility with the existing application, which expects some default users.
     */
    @Bean(name = "persistenceInitData")
    public CommandLineRunner persistenceInitData(UserRepository userRepository, UserRepositoryJpaAdapter adapter) {
        return args -> {
            // If no users exist, create default users
            if (userRepository.count() == 0) {
                // Create default admin user
                User admin = new User(null, "Admin User", "admin@example.com");
                userRepository.save(admin);

                // Create a test user
                User testUser = new User(null, "Test User", "test@example.com");
                userRepository.save(testUser);

                System.out.println("Initialized default users in the database");
            }
        };
    }
}