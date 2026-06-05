package com.bitlord.authservice.repository;

import com.bitlord.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity.
 * Extends JpaRepository to get built-in CRUD operations (save, findById, delete, etc.)
 * for the User table, with Long as the primary key type.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     * Returns an Optional — empty if no user exists with that email.
     * Used during login to look up the user before verifying their password.
     *
     * @param email - the email address to search for
     * @return Optional<User> - the matching user, or empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user with the given email already exists in the database.
     * Returns true if found, false otherwise.
     * Used during registration to prevent duplicate accounts.
     *
     * @param email - the email address to check
     * @return boolean - true if the email is already registered
     */
    boolean existsByEmail(String email);
}