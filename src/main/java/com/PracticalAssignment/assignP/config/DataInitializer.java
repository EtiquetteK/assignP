package com.PracticalAssignment.assignP.config;

import com.PracticalAssignment.assignP.model.User;
import com.PracticalAssignment.assignP.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@Profile("!test")
public class DataInitializer {

    @Value("${app.init.data.enabled:true}")
    private boolean dataInitEnabled;

    @Bean
    public ApplicationRunner initializeData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Only run if explicitly enabled
            if (!dataInitEnabled) {
                return;
            }
            
            try {
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
            } catch (Exception e) {
                System.err.println("Warning: Failed to initialize data: " + e.getMessage());
                // Don't fail startup if data initialization fails
            }
        };
    }
}
