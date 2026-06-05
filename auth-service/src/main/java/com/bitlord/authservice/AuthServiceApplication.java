package com.bitlord.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Auth Service.
 * Responsible for starting the Spring Boot application and setting up context.
 */
@SpringBootApplication // Marks this as a Spring Boot app — enables auto-configuration, component scanning, and configuration
public class AuthServiceApplication {

    // Entry point of the Auth Service — launches the embedded server and initializes the Spring context
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args); // Bootstraps the Spring Boot application
    }
}