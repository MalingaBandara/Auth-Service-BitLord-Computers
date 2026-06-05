package com.bitlord.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for user login.
 *
 * This class is used to receive login credentials from the client
 * when a user attempts to authenticate into the system.
 */
public class LoginRequest {

    @NotBlank(message = "Email is required")  // Email must not be null or empty
    @Email(message = "Email should be valid") // Ensures the provided email follows a valid email format
    private String email;

    @NotBlank(message = "Password is required") // Password must not be null or empty
    private String password;

    // Getters and Setters

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
