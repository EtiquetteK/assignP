package com.PracticalAssignment.assignP.security;

import com.PracticalAssignment.assignP.service.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TokenBlacklistServiceTest {

    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService();
    }

    @Test
    void testBlacklistToken() {
        String token = "test_token_123";
        tokenBlacklistService.blacklistToken(token);

        assertTrue(tokenBlacklistService.isBlacklisted(token));
    }

    @Test
    void testIsNotBlacklistedByDefault() {
        String token = "test_token_456";

        assertFalse(tokenBlacklistService.isBlacklisted(token));
    }

    @Test
    void testMultipleTokensBlacklisting() {
        String token1 = "token_1";
        String token2 = "token_2";
        String token3 = "token_3";

        tokenBlacklistService.blacklistToken(token1);
        tokenBlacklistService.blacklistToken(token2);
        tokenBlacklistService.blacklistToken(token3);

        assertTrue(tokenBlacklistService.isBlacklisted(token1));
        assertTrue(tokenBlacklistService.isBlacklisted(token2));
        assertTrue(tokenBlacklistService.isBlacklisted(token3));
    }

    @Test
    void testBlacklistPersistence() {
        String token = "persistent_token";
        tokenBlacklistService.blacklistToken(token);

        // Token should remain blacklisted across multiple checks
        assertTrue(tokenBlacklistService.isBlacklisted(token));
        assertTrue(tokenBlacklistService.isBlacklisted(token));
        assertTrue(tokenBlacklistService.isBlacklisted(token));
    }

    @Test
    void testBlacklistNullToken() {
        // Should handle null gracefully
        tokenBlacklistService.blacklistToken(null);
        assertFalse(tokenBlacklistService.isBlacklisted(null));
    }
}

