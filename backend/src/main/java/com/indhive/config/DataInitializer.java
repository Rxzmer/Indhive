package com.indhive.config;

import com.indhive.model.User;
import com.indhive.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminUsername = "admin";

            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setPassword(passwordEncoder.encode("admin123")); // Contraseña cifrada
                admin.setEmail("admin@indhive.com");
                admin.setRoles("ROLE_ADMIN,ROLE_USER");
                userRepository.save(admin);
                System.out.println("Usuario admin creado automáticamente.");
            } else {
                System.out.println("Usuario admin ya existe.");
            }
        };
    }
}
