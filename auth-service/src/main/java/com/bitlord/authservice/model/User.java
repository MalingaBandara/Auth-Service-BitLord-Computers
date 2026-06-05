package com.bitlord.authservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a user in the system (Customer, Admin, Super Admin).
 *
 * This entity is mapped to the "users" table in the database
 * and stores all core user-related information.
 */
@Entity
@Table(name = "users")
public class User {

    // Primary key of the users table
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User's name (cannot be null)
    @Column(nullable = false)
    private String name;

    // User's email (must be unique and not null)
    @Column(nullable = false, unique = true)
    private String email;

    // Hashed password stored securely (not plain text)
    @Column(nullable = false)
    private String passwordHash;

    // User role stored as STRING (e.g., ADMIN, CUSTOMER, SUPER_ADMIN)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Indicates whether the user account is active or disabled
    @Column(nullable = false)
    private boolean active;

    // Timestamp when the user was created (not updatable after insert)
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Timestamp when the user was last updated
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Automatically sets creation and update timestamps before inserting into DB
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();   // Set creation time
        updatedAt = LocalDateTime.now();   // Set initial update time
    }


    /**
     * Automatically updates the updatedAt field before updating the entity
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();   // Set latest update time
    }


    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
