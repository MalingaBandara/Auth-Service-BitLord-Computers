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

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.jwt.refresh-token-expiry}")
    private long refreshTokenDuration; // in seconds

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new customer user.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already taken");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CUSTOMER);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        String accessToken = jwtService.generateToken(savedUser);
        RefreshToken refreshToken = createRefreshToken(savedUser);

        return new AuthResponse(accessToken, refreshToken.getToken(), savedUser.getId(), savedUser.getRole().name());
    }

    /**
     * Authenticates a user and returns tokens.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated");
        }

        String accessToken = jwtService.generateToken(user);
        
        // Revoke old tokens
        refreshTokenRepository.deleteByUser(user);
        
        RefreshToken refreshToken = createRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken.getToken(), user.getId(), user.getRole().name());
    }

    /**
     * Uses a refresh token to issue a new access token.
     */
    @Transactional
    public AuthResponse refresh(String tokenRequest) {
        return refreshTokenRepository.findByToken(tokenRequest)
                .map(this::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = jwtService.generateToken(user);
                    return new AuthResponse(accessToken, tokenRequest, user.getId(), user.getRole().name());
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    /**
     * Revokes a refresh token effectively logging out the user.
     */
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(refreshTokenRepository::delete);
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenDuration));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    private RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException(token.getToken() + " Refresh token was expired. Please make a new signin request");
        }
        return token;
    }
}
