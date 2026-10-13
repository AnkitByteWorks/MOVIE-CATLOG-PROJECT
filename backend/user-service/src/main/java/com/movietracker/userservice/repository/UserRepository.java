package com.movietracker.userservice.repository;

import com.movietracker.userservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for persisting and querying {@link User} entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user account by unique username handle.
     *
     * @param username username to search for
     * @return Optional containing the User if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user account by primary email address.
     *
     * @param email email address to search for
     * @return Optional containing the User if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by matching either their username or email address.
     *
     * @param username username to match
     * @param email    email to match
     * @return Optional containing the User if found
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * Checks if a user already exists with the given username.
     *
     * @param username username to test
     * @return true if an account exists with this username
     */
    boolean existsByUsername(String username);

    /**
     * Checks if a user already exists with the given email address.
     *
     * @param email email to test
     * @return true if an account exists with this email
     */
    boolean existsByEmail(String email);

    /**
     * Finds a user account by case-insensitive username match.
     *
     * @param username username to look up
     * @return Optional containing the User if found
     */
    Optional<User> findByUsernameIgnoreCase(String username);

    /**
     * Searches users by case-insensitive partial username match with pagination.
     *
     * @param q        search query term
     * @param pageable pagination and sorting parameters
     * @return page of matching users
     */
    Page<User> findByUsernameContainingIgnoreCase(String q, Pageable pageable);
}
