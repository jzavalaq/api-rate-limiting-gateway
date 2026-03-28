package com.gateway.ratelimit.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SecurityConfig.
 */
class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig("http://localhost:3000,http://localhost:8080");
    }

    @Test
    void corsConfigurationSource_createsConfig() {
        // When
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();

        // Then
        assertNotNull(source);
        var exchange = MockServerWebExchange.builder(MockServerHttpRequest.get("/test").build()).build();
        var corsConfig = source.getCorsConfiguration(exchange);
        assertNotNull(corsConfig);
        assertTrue(corsConfig.getAllowedOrigins().contains("http://localhost:3000"));
        assertTrue(corsConfig.getAllowedOrigins().contains("http://localhost:8080"));
    }

    @Test
    void corsConfigurationSource_hasAllowedMethods() {
        // When
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        var exchange = MockServerWebExchange.builder(MockServerHttpRequest.get("/test").build()).build();
        var corsConfig = source.getCorsConfiguration(exchange);

        // Then
        assertTrue(corsConfig.getAllowedMethods().contains("GET"));
        assertTrue(corsConfig.getAllowedMethods().contains("POST"));
        assertTrue(corsConfig.getAllowedMethods().contains("PUT"));
        assertTrue(corsConfig.getAllowedMethods().contains("DELETE"));
        assertTrue(corsConfig.getAllowedMethods().contains("PATCH"));
        assertTrue(corsConfig.getAllowedMethods().contains("OPTIONS"));
    }

    @Test
    void corsConfigurationSource_allowsCredentials() {
        // When
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        var exchange = MockServerWebExchange.builder(MockServerHttpRequest.get("/test").build()).build();
        var corsConfig = source.getCorsConfiguration(exchange);

        // Then
        assertTrue(corsConfig.getAllowCredentials());
    }

    @Test
    void constructor_singleOrigin_parsesCorrectly() {
        // Given/When
        SecurityConfig singleOriginConfig = new SecurityConfig("http://localhost:3000");

        // Then
        CorsConfigurationSource source = singleOriginConfig.corsConfigurationSource();
        var exchange = MockServerWebExchange.builder(MockServerHttpRequest.get("/test").build()).build();
        var corsConfig = source.getCorsConfiguration(exchange);
        assertEquals(1, corsConfig.getAllowedOrigins().size());
    }

    @Test
    void constructor_multipleOrigins_parsesCorrectly() {
        // Given/When
        SecurityConfig multiOriginConfig = new SecurityConfig("http://localhost:3000,http://localhost:8080,http://example.com");

        // Then
        CorsConfigurationSource source = multiOriginConfig.corsConfigurationSource();
        var exchange = MockServerWebExchange.builder(MockServerHttpRequest.get("/test").build()).build();
        var corsConfig = source.getCorsConfiguration(exchange);
        assertEquals(3, corsConfig.getAllowedOrigins().size());
    }

    @Test
    void corsConfigurationSource_hasCorrectMaxAge() {
        // When
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        var exchange = MockServerWebExchange.builder(MockServerHttpRequest.get("/test").build()).build();
        var corsConfig = source.getCorsConfiguration(exchange);

        // Then
        assertEquals(3600L, corsConfig.getMaxAge());
    }

    @Test
    void corsConfigurationSource_allowsSpecificHeaders() {
        // When
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        var exchange = MockServerWebExchange.builder(MockServerHttpRequest.get("/test").build()).build();
        var corsConfig = source.getCorsConfiguration(exchange);

        // Then - Security fix: use explicit headers instead of wildcard
        assertNotNull(corsConfig.getAllowedHeaders());
        assertTrue(corsConfig.getAllowedHeaders().contains("Authorization"));
        assertTrue(corsConfig.getAllowedHeaders().contains("Content-Type"));
        assertTrue(corsConfig.getAllowedHeaders().contains("X-Correlation-ID"));
    }
}
