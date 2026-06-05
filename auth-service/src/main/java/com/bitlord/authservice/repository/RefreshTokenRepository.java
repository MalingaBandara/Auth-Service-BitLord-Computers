package com.bitlord.authservice.repository;

import com.bitlord.authservice.model.RefreshToken;
import com.bitlord.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for RefreshToken entity.
 * Handles all database operations related to refresh tokens.
 * Extends JpaRepository to get built-in CRUD operations for RefreshToken table.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Finds a RefreshToken record by its token string value.
     * Used during token refresh flow to verify the token exists in the database.
     * Returns Optional to safely handle the case where the token is not found.
     *
     * @param token - the raw refresh token string sent by the client
     * @return Optional<RefreshToken> - the matching token record, or empty if not found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Deletes the refresh token record associated with the given user.
     * Called during logout to invalidate the user's existing refresh token.
     *
     * @param user - the User entity whose refresh token should be removed
     */
    void deleteByUser(User user);
}