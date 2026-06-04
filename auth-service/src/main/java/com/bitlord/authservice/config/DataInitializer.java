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

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByEmail("superadmin@bitlord.com")) {
                User admin = new User();
                admin.setName("Super Admin");
                admin.setEmail("superadmin@bitlord.com");
                admin.setPasswordHash(passwordEncoder.encode("superadmin123"));
                admin.setRole(Role.SUPER_ADMIN);
                admin.setActive(true);
                userRepository.save(admin);
                System.out.println("Default SUPER_ADMIN created: superadmin@bitlord.com / superadmin123");
            }
        };
    }
}
