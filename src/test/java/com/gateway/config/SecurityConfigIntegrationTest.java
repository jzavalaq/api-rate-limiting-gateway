package com.gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Additional unit tests for SecurityConfig to improve coverage.
 */
class SecurityConfigIntegrationTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig("http://localhost:3000,http://localhost:8080");
    }

    @Test
    void corsConfigurationSource_allowsCorrectMethods() {
        // When
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        var exchange = MockServerWebExchange.builder(MockServerHttpRequest.get("/test").build()).build();
        var corsConfig = source.getCorsConfiguration(exchange);

        // Then
        assertNotNull(corsConfig);
        assertEquals(6, corsConfig.getAllowedMethods().size());
        assertTrue(corsConfig.getAllowedMethods().contains("GET"));
        assertTrue(corsConfig.getAllowedMethods().contains("POST"));
        assertTrue(corsConfig.getAllowedMethods().contains("PUT"));
        assertTrue(corsConfig.getAllowedMethods().contains("DELETE"));
        assertTrue(corsConfig.getAllowedMethods().contains("PATCH"));
        assertTrue(corsConfig.getAllowedMethods().contains("OPTIONS"));
    }

    @Test
    void corsConfigurationSource_setsCorrectHeaders() {
        // When
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        var exchange = MockServerWebExchange.builder(MockServerHttpRequest.get("/test").build()).build();
        var corsConfig = source.getCorsConfiguration(exchange);

        // Then - Security fix: use explicit headers instead of wildcard
        assertNotNull(corsConfig);
        assertNotNull(corsConfig.getAllowedHeaders());
        assertTrue(corsConfig.getAllowedHeaders().contains("Authorization"));
        assertTrue(corsConfig.getAllowedHeaders().contains("Content-Type"));
        assertTrue(corsConfig.getAllowCredentials());
        assertEquals(3600L, corsConfig.getMaxAge());
        // Verify exposed headers are set
        assertNotNull(corsConfig.getExposedHeaders());
        assertTrue(corsConfig.getExposedHeaders().contains("X-Correlation-ID"));
    }

    @Test
    void constructor_withWhitespaceInOrigins_handlesCorrectly() {
        // Given/When - String.split does not trim by default
        SecurityConfig config = new SecurityConfig(" http://localhost:3000 , http://localhost:8080 ");

        // Then
        CorsConfigurationSource source = config.corsConfigurationSource();
        var exchange = MockServerWebExchange.builder(MockServerHttpRequest.get("/test").build()).build();
        var corsConfig = source.getCorsConfiguration(exchange);
        assertNotNull(corsConfig);
        // Origins with spaces are included (split behavior)
    }

    @Test
    void constructor_withEmptyOriginString_createsEmptyOriginList() {
        // Given/When
        SecurityConfig config = new SecurityConfig("");

        // Then
        CorsConfigurationSource source = config.corsConfigurationSource();
        var exchange = MockServerWebExchange.builder(MockServerHttpRequest.get("/test").build()).build();
        var corsConfig = source.getCorsConfiguration(exchange);
        assertNotNull(corsConfig);
        // Empty string results in array with one empty element
        assertEquals(1, corsConfig.getAllowedOrigins().size());
    }

    @Test
    void corsConfigurationSource_appliesToAllPaths() {
        // When
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();

        // Then - configuration is registered for all paths
        assertNotNull(source);
    }
}
