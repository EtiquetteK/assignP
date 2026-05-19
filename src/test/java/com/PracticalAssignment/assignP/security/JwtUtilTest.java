package com.PracticalAssignment.assignP.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String testSecret;

    @BeforeEach
    void setUp() {
        // Use a test secret key (32+ bytes for HS256)
        testSecret = "test-secret-key-for-jwt-testing-12345678";
        jwtUtil = new JwtUtil(testSecret);
    }

    @Test
    void testGenerateToken() {
        String username = "testuser";
        String role = "MEMBER";
        String token = jwtUtil.generateToken(username, role);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    void testExtractUsernameFromToken() {
        String username = "testuser";
        String role = "ADMIN";
        String token = jwtUtil.generateToken(username, role);

        String extracted = jwtUtil.extractUsername(token);

        assertEquals(username, extracted);
    }

    @Test
    void testIsTokenValid() {
        String username = "testuser";
        String role = "MEMBER";
        String token = jwtUtil.generateToken(username, role);

        boolean valid = jwtUtil.isTokenValid(token);

        assertTrue(valid);
    }

    @Test
    void testIsTokenInvalidWithMalformedToken() {
        String malformedToken = "not.a.valid.token";

        boolean valid = jwtUtil.isTokenValid(malformedToken);

        assertFalse(valid);
    }

    @Test
    void testExtractAllClaims() {
        String username = "testuser";
        String role = "ADMIN";
        String token = jwtUtil.generateToken(username, role);

        Claims claims = jwtUtil.extractAllClaims(token);

        assertNotNull(claims);
        assertEquals(username, claims.getSubject());
        assertEquals(role, claims.get("role"));
    }
}

