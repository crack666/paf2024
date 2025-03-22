package de.vfh.paf.tasklist.domain.repository;

import de.vfh.paf.tasklist.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for users.
 * Uses JPA for database persistence.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // JpaRepository provides the following methods:
    // - save(User user)
    // - findById(Integer id)
    // - findAll()
    // - deleteById(Integer id)
    // and more
}