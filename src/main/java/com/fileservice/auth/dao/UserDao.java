package com.fileservice.auth.dao;

import com.fileservice.auth.pojo.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Data access abstraction for {@link User} entities.
 * <p>
 * Extends Spring Data JPA's {@link JpaRepository} to provide common CRUD
 * operations and declares additional finder methods used by the authentication
 * layer.
 */
@Repository
public interface UserDao extends JpaRepository<User, Long> {

    /**
     * Returns the user with the given email, if it exists.
     *
     * @param email unique user email (also used as username)
     * @return an {@link Optional} containing the matching user, or empty if none found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user with the given email already exists.
     *
     * @param email unique user email (also used as username)
     * @return {@code true} if a user exists, {@code false} otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Finds an active user by email.
     *
     * @param email unique email address of the user
     * @return optional user if found and active, otherwise empty
     */
    Optional<User> findByEmailAndActiveTrue(String email);
}
