package de.vfh.paf.tasklist.domain.service;

import de.vfh.paf.tasklist.domain.model.User;
import de.vfh.paf.tasklist.infrastructure.persistence.UserRepositoryJpaAdapter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing users.
 */
@Service
public class UserService {
    private final UserRepositoryJpaAdapter userRepository;

    /**
     * Creates a new user service.
     *
     * @param userRepository The repository for users
     */
    @org.springframework.beans.factory.annotation.Autowired
    public UserService(UserRepositoryJpaAdapter userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Creates a new user.
     *
     * @param name  The name of the user
     * @param email The email address of the user
     * @return The created user
     */
    public User createUser(String name, String email) {
        User user = new User(null, name, email); // null ID will be assigned by adapter
        return userRepository.save(user);
    }

    /**
     * Updates a user.
     *
     * @param id    The ID of the user
     * @param name  The new name
     * @param email The new email address
     * @return The updated user, or null if not found
     */
    public User updateUser(int id, String name, String email) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return null;
        }

        User user = optionalUser.get();
        user.updateContactInfo(name, email);
        userRepository.save(user);
        return user;
    }

    /**
     * Finds a user by ID.
     *
     * @param id The ID of the user
     * @return Optional containing the user, or empty if not found
     */
    public Optional<User> findById(int id) {
        return userRepository.findById(id);
    }

    /**
     * Gets all users.
     *
     * @return List of all users
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }
}