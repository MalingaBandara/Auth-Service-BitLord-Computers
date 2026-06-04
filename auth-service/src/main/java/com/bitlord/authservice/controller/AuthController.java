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
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthService authService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String token,
                                       @RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        return ResponseEntity.ok().build();
    }

    // Admin Endpoints
    // Note: The API Gateway should ideally secure these, but we also check the X-User-Role header here for safety.

    @PostMapping("/admin/create")
    public ResponseEntity<?> createAdmin(@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
                                         @Valid @RequestBody RegisterRequest request) {
        if (!"SUPER_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only SUPER_ADMIN can create admins");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email is already taken");
        }
        User admin = new User();
        admin.setName(request.getName());
        admin.setEmail(request.getEmail());
        admin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        userRepository.save(admin);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Admin created successfully"));
    }

    @PatchMapping("/admin/{userId}/status")
    public ResponseEntity<?> updateAdminStatus(@RequestHeader(value = "X-User-Role", defaultValue = "") String role,
                                               @PathVariable Long userId,
                                               @RequestBody Map<String, Boolean> request) {
        if (!"SUPER_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only SUPER_ADMIN can change status");
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        user.setActive(request.getOrDefault("active", true));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User status updated"));
    }

    @GetMapping("/users")
    public ResponseEntity<?> listUsers(@RequestHeader(value = "X-User-Role", defaultValue = "") String role) {
        if (!"SUPER_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only SUPER_ADMIN can view all users");
        }
        List<User> users = userRepository.findAll();
        // Nullify password hashes before returning
        users.forEach(u -> u.setPasswordHash(null));
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/me")
    public ResponseEntity<?> getMe(@RequestHeader(value = "X-User-Id", defaultValue = "") String userId) {
        if (userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userRepository.findById(Long.valueOf(userId)).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        user.setPasswordHash(null);
        return ResponseEntity.ok(user);
    }
}
