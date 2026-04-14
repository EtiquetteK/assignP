package com.PracticalAssignment.assignP.config;

import com.PracticalAssignment.assignP.model.User;
import com.PracticalAssignment.assignP.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public ApplicationRunner initializeData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if admin already exists
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123")); // Change this in production!
                admin.setRole("ADMIN");
                userRepository.save(admin);
                System.out.println("✓ Admin account created: username=admin, password=admin123");
            } else {
                System.out.println("✓ Admin account already exists");
            }

            // Optionally create a test member account
            if (userRepository.findByUsername("member").isEmpty()) {
                User member = new User();
                member.setUsername("member");
                member.setPassword(passwordEncoder.encode("member123")); // Change this in production!
                member.setRole("MEMBER");
                userRepository.save(member);
                System.out.println("✓ Member account created: username=member, password=member123");
            }
        };
    }
}
