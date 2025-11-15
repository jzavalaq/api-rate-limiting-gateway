package com.gateway.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtTokenProvider.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String SECRET = "this-is-a-test-secret-key-that-is-at-least-256-bits-long-for-testing";
    private static final long EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, EXPIRATION);
    }

    @Test
    void validateToken_WithValidToken_ReturnsTrue() {
        // A valid JWT token for testing
        String validToken = createTestToken();
        assertTrue(jwtTokenProvider.validateToken(validToken));
    }

    @Test
    void validateToken_WithInvalidToken_ReturnsFalse() {
        String invalidToken = "invalid.token.here";
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    void validateToken_WithNullToken_ReturnsFalse() {
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    @Test
    void validateToken_WithEmptyToken_ReturnsFalse() {
        assertFalse(jwtTokenProvider.validateToken(""));
    }

    @Test
    void extractUsername_WithValidToken_ReturnsUsername() {
        String token = createTestToken();
        String username = jwtTokenProvider.extractUsername(token);
        assertNotNull(username);
    }

    private String createTestToken() {
        // Create a simple test token using the provider
        // For testing purposes, we'll create a minimal valid JWT
        return io.jsonwebtoken.Jwts.builder()
                .subject("testuser")
                .claim("roles", java.util.List.of("USER"))
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(SECRET.getBytes()))
                .compact();
    }
}
