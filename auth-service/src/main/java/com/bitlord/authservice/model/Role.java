package com.bitlord.authservice.model;

/**
 * Defines the roles available in the system for RBAC (Role-Based Access Control).
 *
 * These roles are used to control access permissions across the application.
 */
public enum Role {
    CUSTOMER,  // Regular user with basic access permissions
    ADMIN, // Admin user with elevated permissions to manage system resources
    SUPER_ADMIN // Highest privilege level with full system access and control
}
