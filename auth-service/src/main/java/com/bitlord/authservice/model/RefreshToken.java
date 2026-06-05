package com.bitlord.authservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a refresh token used for session management.
 *
 * Refresh tokens are used to generate new access tokens without requiring
 * the user to log in again, improving user experience while maintaining security.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    // Primary key for refresh token table
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique refresh token string stored in the database
    @Column(nullable = false, unique = true)
    private String token;

    // Many refresh tokens can belong to one user (lazy loaded for performance)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Expiration time of the refresh token
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    // Flag indicating whether the token has been revoked (invalidated manually)
    @Column(nullable = false)
    private boolean revoked;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }
}
