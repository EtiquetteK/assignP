package com.PracticalAssignment.assignP.service;

import com.PracticalAssignment.assignP.dto.LoginRequest;
import com.PracticalAssignment.assignP.dto.RefreshRequest;
import com.PracticalAssignment.assignP.dto.UserDTO;
import com.PracticalAssignment.assignP.model.User;
import com.PracticalAssignment.assignP.repository.UserRepository;
import com.PracticalAssignment.assignP.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null); // unwrap Optional safely
        if (user != null && passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return jwtUtil.generateToken(user.getUsername(), user.getRole());
        }
        return "{\"error\":\"Invalid credentials\"}";
    }

    public UserDTO register(User user) {
        user.setRole("MEMBER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepository.save(user);

        UserDTO dto = new UserDTO();
        dto.setId(saved.getId());
        dto.setUsername(saved.getUsername());
        dto.setRole(saved.getRole());
        return dto;
    }

    public String refreshToken(RefreshRequest request) {
        String username = jwtUtil.extractUsername(request.getRefreshToken());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return jwtUtil.generateToken(user.getUsername(), user.getRole());
    }
}
