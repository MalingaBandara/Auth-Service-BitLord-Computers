package com.bitlord.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for customer registration.
 *
 * This class is used to capture user input data when a new user
 * registers an account in the system.
 */
public class RegisterRequest {

    @NotBlank(message = "Name is required")// User's name must not be empty or null
    private String name;

    // Email must not be empty and must follow valid email format
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    // Password must not be empty and must be at least 8 characters long
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    // Getters and Setters

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
