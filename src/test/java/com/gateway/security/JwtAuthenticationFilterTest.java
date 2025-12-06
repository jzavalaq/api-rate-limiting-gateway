package com.gateway.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationFilter.
 */
class JwtAuthenticationFilterTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private static final String SECRET = "this-is-a-test-secret-key-that-is-at-least-256-bits-long-for-testing";
    private static final long EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = mock(JwtTokenProvider.class);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider);
    }

    @Test
    void filter_withoutAuthHeader_passesThrough() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result).verifyComplete();
        verify(chain).filter(exchange);
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    @Test
    void filter_withNonBearerAuthHeader_passesThrough() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result).verifyComplete();
        verify(chain).filter(exchange);
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    @Test
    void filter_withInvalidBearerToken_passesThrough() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());
        when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(false);

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result).verifyComplete();
        verify(chain).filter(exchange);
        verify(jwtTokenProvider).validateToken("invalid-token");
        verify(jwtTokenProvider, never()).extractUsername(anyString());
    }

    @Test
    void filter_withValidBearerToken_setsAuthentication() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.extractUsername("valid-token")).thenReturn("testuser");
        when(jwtTokenProvider.extractRoles("valid-token")).thenReturn(List.of("USER", "ADMIN"));

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result).verifyComplete();
        verify(jwtTokenProvider).validateToken("valid-token");
        verify(jwtTokenProvider).extractUsername("valid-token");
        verify(jwtTokenProvider).extractRoles("valid-token");
    }

    @Test
    void filter_withValidTokenNoRoles_setsAuthenticationWithEmptyAuthorities() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.extractUsername("valid-token")).thenReturn("testuser");
        when(jwtTokenProvider.extractRoles("valid-token")).thenReturn(null);

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result).verifyComplete();
        verify(jwtTokenProvider).extractRoles("valid-token");
    }

    @Test
    void filter_withExceptionDuringTokenProcessing_passesThrough() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.extractUsername("valid-token")).thenThrow(new RuntimeException("Token processing error"));

        // When
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result).verifyComplete();
        verify(chain).filter(exchange);
    }
}
