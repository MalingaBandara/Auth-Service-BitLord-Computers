package com.bitlord.authservice.config;

import com.bitlord.authservice.model.Role;
import com.bitlord.authservice.model.User;
import com.bitlord.authservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Initializes the database with a default SUPER_ADMIN if none exists.
 */
@Configuration
public class DataInitializer {

    /**
     * Runs automatically once the application context is fully loaded.
     * Checks if a SUPER_ADMIN account already exists — if not, creates one.
     * This ensures there is always at least one admin account to log in with
     * on a fresh database.
     *
     * @param userRepository  - JPA repository for User database operations
     * @param passwordEncoder - used to hash the plain-text password before saving
     */
    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {

            // Check if a SUPER_ADMIN account already exists to avoid creating duplicates on restart
            if (!userRepository.existsByEmail("superadmin@bitlord.com")) {

                // Build the default SUPER_ADMIN user object
                User admin = new User();
                admin.setName("Super Admin");
                admin.setEmail("superadmin@bitlord.com");

                // Hash the plain-text password before storing it in the database
                admin.setPasswordHash(passwordEncoder.encode("superadmin123"));

                // Assign the highest privilege role
                admin.setRole(Role.SUPER_ADMIN);

                // Mark the account as active so it can log in immediately
                admin.setActive(true);

                // Persist the new admin user to the database
                userRepository.save(admin);

                // Log confirmation to the console so it's visible on first startup
                System.out.println("Default SUPER_ADMIN created: superadmin@bitlord.com / superadmin123");
            }
        };
    }
}