package com.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

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
        assertEquals("testuser", username);
    }

    @Test
    void extractRoles_WithValidToken_ReturnsRoles() {
        String token = createTestToken();
        List<String> roles = jwtTokenProvider.extractRoles(token);
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("USER", roles.get(0));
    }

    @Test
    void extractRoles_WithTokenWithoutRoles_ReturnsNull() {
        String token = createTestTokenWithoutRoles();
        List<String> roles = jwtTokenProvider.extractRoles(token);
        assertNull(roles);
    }

    @Test
    void extractExpiration_WithValidToken_ReturnsExpiration() {
        String token = createTestToken();
        Date expiration = jwtTokenProvider.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void isTokenExpired_WithValidToken_ReturnsFalse() {
        String token = createTestToken();
        assertFalse(jwtTokenProvider.isTokenExpired(token));
    }

    @Test
    void isTokenExpired_WithExpiredToken_ReturnsTrue() {
        String token = createExpiredTestToken();
        assertTrue(jwtTokenProvider.isTokenExpired(token));
    }

    @Test
    void extractClaim_WithValidToken_ExtractsClaim() {
        String token = createTestToken();
        String subject = jwtTokenProvider.extractClaim(token, claims -> claims.getSubject());
        assertEquals("testuser", subject);
    }

    @Test
    void validateToken_WithExpiredToken_ReturnsFalse() {
        String expiredToken = createExpiredTestToken();
        assertFalse(jwtTokenProvider.validateToken(expiredToken));
    }

    @Test
    void validateToken_WithWrongSignature_ReturnsFalse() {
        String tokenWithWrongSignature = Jwts.builder()
                .subject("testuser")
                .claim("roles", List.of("USER"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(Keys.hmacShaKeyFor("different-secret-key-for-testing-with-256-bits-min".getBytes(StandardCharsets.UTF_8)))
                .compact();
        assertFalse(jwtTokenProvider.validateToken(tokenWithWrongSignature));
    }

    @Test
    void constructor_withValidParams_createsProvider() {
        // Given/When
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, EXPIRATION);

        // Then
        assertNotNull(provider);
    }

    @Test
    void extractUsername_withValidToken_returnsSubject() {
        // Given
        String token = createTestToken();

        // When
        String username = jwtTokenProvider.extractUsername(token);

        // Then
        assertEquals("testuser", username);
    }

    @Test
    void extractClaim_withCustomClaim_returnsValue() {
        // Given
        String token = createTestToken();

        // When
        Object roles = jwtTokenProvider.extractClaim(token, claims -> claims.get("roles"));

        // Then
        assertNotNull(roles);
    }

    private String createTestToken() {
        return Jwts.builder()
                .subject("testuser")
                .claim("roles", List.of("USER"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private String createTestTokenWithoutRoles() {
        return Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private String createExpiredTestToken() {
        return Jwts.builder()
                .subject("testuser")
                .claim("roles", List.of("USER"))
                .issuedAt(new Date(System.currentTimeMillis() - 20000))
                .expiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
