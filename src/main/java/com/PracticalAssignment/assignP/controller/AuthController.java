package com.PracticalAssignment.assignP.controller;

import org.springframework.web.bind.annotation.*;
import com.PracticalAssignment.assignP.dto.LoginRequest;
import com.PracticalAssignment.assignP.dto.RefreshRequest;
import com.PracticalAssignment.assignP.dto.RegisterRequest;
import com.PracticalAssignment.assignP.dto.LogoutRequest;
import com.PracticalAssignment.assignP.dto.UserDTO;
import com.PracticalAssignment.assignP.service.AuthService;
import com.PracticalAssignment.assignP.dto.ApiResponse;
import com.PracticalAssignment.assignP.dto.AuthResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping({"/api/auth", "/auth"})
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDTO>> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO userDTO = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(userDTO, "Registration successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthResponse authResponse = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Token refreshed"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.getToken());
        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }
}
