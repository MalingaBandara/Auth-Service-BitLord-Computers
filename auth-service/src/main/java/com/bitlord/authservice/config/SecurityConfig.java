package com.bitlord.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for Auth Service.
 * We disable csrf and allow public access to auth endpoints.
 * Inter-service or gateway will handle JWT validation for protected routes.
 */
@Configuration  // Marks this class as a Spring configuration class — beans defined here are registered in the app context
@EnableWebSecurity  // Enables Spring Security's web security support and registers the filter chain
public class SecurityConfig {

    /**
     * Registers BCryptPasswordEncoder as the app-wide password hashing strategy.
     * BCrypt automatically handles salting, so the same plain password
     * produces a different hash each time — safe for storing user passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Defines the main HTTP security filter chain for the Auth Service.
     * Configures which endpoints are public, disables CSRF, and enforces stateless sessions.
     *
     * @param http - Spring's HttpSecurity builder used to configure security rules
     * @return the fully built SecurityFilterChain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF protection — not needed here because this service is stateless
                // and does not use browser-based session cookies (JWT is used instead)
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        // Allow unauthenticated access to all /auth/** endpoints (register, login, refresh)
                        // and /actuator/** endpoints (health checks, metrics for monitoring tools)
                        .requestMatchers("/auth/**", "/actuator/**").permitAll()

                        // All other requests must be authenticated
                        // (in practice, the API Gateway handles this — but this is a safety net)
                        .anyRequest().authenticated()
                )

                // Set session management to STATELESS — Spring will never create or use an HTTP session
                // Each request must carry its own JWT token; no server-side session is maintained
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Build and return the configured security filter chain
        return http.build();
    }
}