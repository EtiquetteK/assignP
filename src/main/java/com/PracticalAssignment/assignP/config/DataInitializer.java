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

    @Value("${app.init.admin.username:admin}")
    private String adminUsername;

    @Value("${app.init.admin.password}")
    private String adminPassword;

    @Value("${app.init.member.username:member}")
    private String memberUsername;

    @Value("${app.init.member.password}")
    private String memberPassword;

    @Bean
    public ApplicationRunner initializeData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Only run if explicitly enabled
            if (!dataInitEnabled) {
                return;
            }
            
            try {
                // Check if admin already exists
                if (userRepository.findByUsername(adminUsername).isEmpty()) {
                    User admin = new User();
                    admin.setUsername(adminUsername);
                    admin.setPassword(passwordEncoder.encode(adminPassword));
                    admin.setRole("ADMIN");
                    userRepository.save(admin);
                    System.out.println("✓ Admin account created: username=" + adminUsername);
                } else {
                    System.out.println("✓ Admin account already exists");
                }

                // Optionally create a test member account
                if (userRepository.findByUsername(memberUsername).isEmpty()) {
                    User member = new User();
                    member.setUsername(memberUsername);
                    member.setPassword(passwordEncoder.encode(memberPassword));
                    member.setRole("MEMBER");
                    userRepository.save(member);
                    System.out.println("✓ Member account created: username=" + memberUsername);
                }
            } catch (Exception e) {
                System.err.println("Warning: Failed to initialize data: " + e.getMessage());
                // Don't fail startup if data initialization fails
            }
        };
    }
}

