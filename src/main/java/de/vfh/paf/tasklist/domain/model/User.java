package de.vfh.paf.tasklist.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a user in the system who can create and manage tasks.
 */
@Entity
@Table(name = "app_users")
public class User {
    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String email;

    @Getter
    @Transient // Keep tasks transient for now to not disrupt existing functionality
    private List<Task> tasks = new ArrayList<>();

    @Getter
    @Transient // Keep notifications transient for now to not disrupt existing functionality
    private List<Notification> notifications = new ArrayList<>();

    /**
     * Default constructor required by JPA
     */
    public User() {
    }

    /**
     * Creates a new user.
     *
     * @param id    The unique identifier for the user
     * @param name  The name of the user
     * @param email The email address of the user
     */
    public User(Integer id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.tasks = new ArrayList<>();
        this.notifications = new ArrayList<>();
    }

    /**
     * Updates the user's contact information.
     *
     * @param name  The new name
     * @param email The new email address
     */
    public void updateContactInfo(String name, String email) {
        this.name = name;
        this.email = email;
    }

    /**
     * Adds a task to the user's task list.
     *
     * @param task The task to add
     */
    public void addTask(Task task) {
        if (!tasks.contains(task)) {
            tasks.add(task);
        }
    }

    /**
     * Adds a notification to the user's notification list.
     *
     * @param notification The notification to add
     */
    public void addNotification(Notification notification) {
        if (!notifications.contains(notification)) {
            notifications.add(notification);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}