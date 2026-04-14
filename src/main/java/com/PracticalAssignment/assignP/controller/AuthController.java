package com.PracticalAssignment.assignP.controller;

import org.springframework.web.bind.annotation.*;
import com.PracticalAssignment.assignP.dto.LoginRequest;
import com.PracticalAssignment.assignP.dto.RefreshRequest;
import com.PracticalAssignment.assignP.dto.UserDTO;
import com.PracticalAssignment.assignP.model.User;
import com.PracticalAssignment.assignP.service.AuthService;

@RestController
@RequestMapping({"/api/auth", "/auth"})
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public UserDTO register(@RequestBody User user) {
        return authService.register(user);
    }

    @PostMapping("/refresh")
    public String refresh(@RequestBody RefreshRequest request) {
        return authService.refreshToken(request);
    }
}
