package com.bitlord.authservice.controller;

import com.bitlord.authservice.dto.AuthResponse;
import com.bitlord.authservice.dto.LoginRequest;
import com.bitlord.authservice.dto.RegisterRequest;
import com.bitlord.authservice.model.Role;
import com.bitlord.authservice.model.User;
import com.bitlord.authservice.repository.UserRepository;
import com.bitlord.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for authentication and user management endpoints.
 */
@RestController
@RequestMapping("/auth") // All endpoints in this controller are prefixed with /auth
public class AuthController {

    private final AuthService authService;         // Handles core auth logic (register, login, refresh, logout)
    private final UserRepository userRepository;   // Direct DB access for user records
    private final PasswordEncoder passwordEncoder; // BCrypt encoder for hashing passwords

    // Constructor injection — preferred over @Autowired for testability
    public AuthController(AuthService authService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user account.
     * POST /auth/register
     * Returns 201 Created with access + refresh tokens on success.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Delegate registration logic to AuthService and return 201 Created
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    /**
     * Authenticates an existing user with email and password.
     * POST /auth/login
     * Returns 200 OK with access + refresh tokens on success.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Delegate login logic to AuthService and return 200 OK
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Issues a new access token using a valid refresh token.
     * POST /auth/refresh
     * Expects body: { "refreshToken": "<token>" }
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> request) {
        // Extract the refresh token from the request body
        String refreshToken = request.get("refreshToken");

        // Return 400 Bad Request if refresh token is missing from the body
        if (refreshToken == null) {
            return ResponseEntity.badRequest().build();
        }

        // Validate and rotate the refresh token, then return a new token pair
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    /**
     * Logs out the user by invalidating their refresh token.
     * POST /auth/logout
     * Expects body: { "refreshToken": "<token>" }
     * The Authorization header is accepted but not used here — refresh token invalidation is enough.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String token,
                                       @RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        // Only invalidate if a refresh token was actually provided
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        return ResponseEntity.ok().build();
    }

    // -------------------------------------------------------------------------
    // Admin Endpoints
    // Note: The API Gateway should ideally secure these, but we also check the
    // X-User-Role header here as a second layer of protection.
    // -------------------------------------------------------------------------

    /**
     * Creates a new ADMIN user. Only accessible by SUPER_ADMIN.
     * POST /auth/admin/create
     * X-User-Role header is injected by the API Gateway from the validated JWT.
     */
    @PostMapping("/admin/create")
    public ResponseEntity<?> createAdmin(@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
                                         @Valid @RequestBody RegisterRequest request) {
        // Role check — reject anyone who isn't SUPER_ADMIN
        if (!"SUPER_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only SUPER_ADMIN can create admins");
        }

        // Prevent duplicate accounts with the same email
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email is already taken");
        }

        // Build the new admin User entity manually
        User admin = new User();
        admin.setName(request.getName());
        admin.setEmail(request.getEmail());
        admin.setPasswordHash(passwordEncoder.encode(request.getPassword())); // Hash the plain-text password
        admin.setRole(Role.ADMIN);   // Assign ADMIN role
        admin.setActive(true);       // Account is active immediately

        userRepository.save(admin);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Admin created successfully"));
    }

    /**
     * Activates or deactivates a user account. Only accessible by SUPER_ADMIN.
     * PATCH /auth/admin/{userId}/status
     * Expects body: { "active": true/false }
     */
    @PatchMapping("/admin/{userId}/status")
    public ResponseEntity<?> updateAdminStatus(@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
                                               @PathVariable Long userId,
                                               @RequestBody Map<String, Boolean> request) {
        // Role check — only SUPER_ADMIN can toggle user account status
        if (!"SUPER_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only SUPER_ADMIN can change status");
        }

        // Look up the target user by ID — return 404 if not found
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Update the active flag — defaults to true if not provided in the body
        user.setActive(request.getOrDefault("active", true));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "User status updated"));
    }

    /**
     * Returns a list of all registered users. Only accessible by SUPER_ADMIN.
     * GET /auth/users
     */
    @GetMapping("/users")
    public ResponseEntity<?> listUsers(@RequestHeader(value = "X-User-Role", defaultValue = "") String role) {
        // Role check — only SUPER_ADMIN can view the full user list
        if (!"SUPER_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only SUPER_ADMIN can view all users");
        }

        List<User> users = userRepository.findAll();

        // Strip password hashes before sending the response — never expose them via API
        users.forEach(u -> u.setPasswordHash(null));

        return ResponseEntity.ok(users);
    }

    /**
     * Returns the currently authenticated user's own profile.
     * GET /auth/users/me
     * X-User-Id header is injected by the API Gateway from the validated JWT.
     */
    @GetMapping("/users/me")
    public ResponseEntity<?> getMe(@RequestHeader(value = "X-User-Id", defaultValue = "") String userId) {
        // If X-User-Id is missing, the request didn't pass through the gateway properly
        if (userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Look up the user by their ID — return 404 if not found
        User user = userRepository.findById(Long.valueOf(userId)).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Remove password hash before returning the user profile
        user.setPasswordHash(null);

        return ResponseEntity.ok(user);
    }
}