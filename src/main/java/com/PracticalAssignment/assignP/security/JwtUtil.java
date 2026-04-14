package com.PracticalAssignment.assignP.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import javax.crypto.SecretKey;

@Component
public class JwtUtil {
    private final String secret = "loginanddie12345";
    private final long expiration = 3600000; // 1 hour
    private final SecretKey signingKey;

    public JwtUtil() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // HS256 requires a key of at least 32 bytes.
        if (keyBytes.length < 32) {
            keyBytes = Arrays.copyOf(keyBytes, 32);
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, String role) {
        return Jwts.builder()
            .setSubject(username)
            .claim("role", role)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();
    }

    public String extractUsername(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(signingKey)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
}

}
