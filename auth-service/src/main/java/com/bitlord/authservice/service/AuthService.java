package com.bitlord.authservice.service;

import com.bitlord.authservice.dto.AuthResponse;
import com.bitlord.authservice.dto.LoginRequest;
import com.bitlord.authservice.dto.RegisterRequest;
import com.bitlord.authservice.model.RefreshToken;
import com.bitlord.authservice.model.Role;
import com.bitlord.authservice.model.User;
import com.bitlord.authservice.repository.RefreshTokenRepository;
import com.bitlord.authservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service handling registration, login, and token generation logic.
 */
@Service
public class AuthService {

    // Repository for querying and saving User records from the database
    private final UserRepository userRepository;

    // Repository for querying and saving RefreshToken records from the database
    private final RefreshTokenRepository refreshTokenRepository;

    // Spring Security utility for hashing and verifying passwords (e.g. BCrypt)
    private final PasswordEncoder passwordEncoder;

    // Service responsible for generating and parsing JWT access tokens
    private final JwtService jwtService;

    // Refresh token expiry duration in seconds — pulled from application.properties/yml
    @Value("${app.jwt.refresh-token-expiry}")
    private long refreshTokenDuration;

    // Constructor injection — preferred over @Autowired for better testability
    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new customer user.
     * Validates email uniqueness, hashes the password, saves the user,
     * and returns a fresh access token + refresh token pair.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // Reject registration if the email is already linked to an existing account
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already taken");
        }

        // Build a new User entity from the incoming registration request
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); // Hash the raw password before storing
        user.setRole(Role.CUSTOMER); // All self-registered users get the CUSTOMER role by default
        user.setActive(true); // Account is active immediately upon registration

        // Persist the new user to the database
        User savedUser = userRepository.save(user);

        // Generate a short-lived JWT access token for the newly registered user
        String accessToken = jwtService.generateToken(savedUser);

        // Generate and persist a long-lived refresh token for the user
        RefreshToken refreshToken = createRefreshToken(savedUser);

        // Return both tokens along with the user's ID and role
        return new AuthResponse(accessToken, refreshToken.getToken(), savedUser.getId(), savedUser.getRole().name());
    }

    /**
     * Authenticates a user and returns tokens.
     * Validates email, password, and account status before issuing new tokens.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {

        // Look up the user by email — throw a generic error to avoid revealing whether email exists
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Compare the raw incoming password against the stored hashed password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Prevent deactivated accounts from logging in
        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated");
        }

        // Generate a new JWT access token for the authenticated user
        String accessToken = jwtService.generateToken(user);

        // Revoke old refresh tokens — ensures only one active session per user
        refreshTokenRepository.deleteByUser(user);

        // Create and persist a fresh refresh token for this login session
        RefreshToken refreshToken = createRefreshToken(user);

        // Return both tokens along with the user's ID and role
        return new AuthResponse(accessToken, refreshToken.getToken(), user.getId(), user.getRole().name());
    }

    /**
     * Uses a refresh token to issue a new access token.
     * Looks up the token, checks expiry, and returns a fresh access token
     * while reusing the same refresh token.
     */
    @Transactional
    public AuthResponse refresh(String tokenRequest) {
        return refreshTokenRepository.findByToken(tokenRequest)
                .map(this::verifyExpiration)   // Check if the refresh token is still valid
                .map(RefreshToken::getUser)    // Extract the User associated with the refresh token
                .map(user -> {
                    // Issue a new access token for the user
                    String accessToken = jwtService.generateToken(user);
                    // Return the new access token along with the same refresh token (not rotated)
                    return new AuthResponse(accessToken, tokenRequest, user.getId(), user.getRole().name());
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    /**
     * Revokes a refresh token effectively logging out the user.
     * If the token exists in the database, it is deleted — invalidating the session.
     */
    @Transactional
    public void logout(String refreshToken) {
        // Find the refresh token and delete it if it exists — silently ignored if not found
        refreshTokenRepository.findByToken(refreshToken).ifPresent(refreshTokenRepository::delete);
    }

    /**
     * Creates and saves a new RefreshToken entity for the given user.
     * Token is a random UUID string with an expiry calculated from the configured duration.
     */
    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenDuration)); // Set expiry based on config value
        refreshToken.setToken(UUID.randomUUID().toString()); // Generate a unique random token string
        refreshToken.setRevoked(false); // Token is active and not yet revoked

        // Persist the refresh token and return the saved entity
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Checks whether the given refresh token has expired.
     * If expired, deletes the token from the database and throws an exception.
     * If still valid, returns the token for further processing.
     */
    private RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            // Token has passed its expiry time — remove it from the database
            refreshTokenRepository.delete(token);
            throw new RuntimeException(token.getToken() + " Refresh token was expired. Please make a new signin request");
        }
        // Token is still valid — return it to continue the refresh flow
        return token;
    }
}