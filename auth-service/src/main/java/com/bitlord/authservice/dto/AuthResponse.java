package com.bitlord.authservice.dto;

/**
 * DTO for authentication response.
 *
 * This class is used to send authentication-related data
 * back to the client after successful login or token generation.
 */
public class AuthResponse {

    private String accessToken; // JWT access token used for authorizing API requests
    private String refreshToken;  // Refresh token used to generate a new access token when it expires
    private Long userId;
    private String role;  // Role of the authenticated user (e.g., ADMIN, USER)

    public AuthResponse(String accessToken, String refreshToken, Long userId, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.role = role;
    }

    // Getters and Setters

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
