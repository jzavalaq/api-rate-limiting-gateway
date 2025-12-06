package com.gateway.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RateLimitFilter.
 */
class RateLimitFilterTest {

    private RateLimitService rateLimitService;
    private ObjectMapper objectMapper;
    private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUp() {
        rateLimitService = mock(RateLimitService.class);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        rateLimitFilter = new RateLimitFilter(rateLimitService, objectMapper);
    }

    @Test
    void filter_withAllowedRequest_passesThrough() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());
        when(rateLimitService.tryConsume(anyString())).thenReturn(true);
        when(rateLimitService.getRequestsPerMinute()).thenReturn(60);
        when(rateLimitService.getRemainingTokens(anyString())).thenReturn(59L);
        when(rateLimitService.getResetTimeSeconds(anyString())).thenReturn(60L);

        // When
        Mono<Void> result = rateLimitFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result).verifyComplete();
        verify(chain).filter(exchange);
    }

    @Test
    void filter_withRateLimited_returns429() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        WebFilterChain chain = mock(WebFilterChain.class);
        when(rateLimitService.tryConsume(anyString())).thenReturn(false);
        when(rateLimitService.getRequestsPerMinute()).thenReturn(60);
        when(rateLimitService.getRemainingTokens(anyString())).thenReturn(0L);
        when(rateLimitService.getResetTimeSeconds(anyString())).thenReturn(60L);

        // When
        Mono<Void> result = rateLimitFilter.filter(exchange, chain);

        // Then
        StepVerifier.create(result).verifyComplete();
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(exchange);
    }

    @Test
    void filter_addsRateLimitHeaders() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());
        when(rateLimitService.tryConsume(anyString())).thenReturn(true);
        when(rateLimitService.getRequestsPerMinute()).thenReturn(60);
        when(rateLimitService.getRemainingTokens(anyString())).thenReturn(59L);
        when(rateLimitService.getResetTimeSeconds(anyString())).thenReturn(60L);

        // When
        StepVerifier.create(rateLimitFilter.filter(exchange, chain)).verifyComplete();

        // Then
        HttpHeaders headers = exchange.getResponse().getHeaders();
        assertEquals("60", headers.getFirst("X-RateLimit-Limit"));
        assertEquals("59", headers.getFirst("X-RateLimit-Remaining"));
        assertEquals("60", headers.getFirst("X-RateLimit-Reset"));
        assertNotNull(headers.getFirst("X-Correlation-ID"));
    }

    @Test
    void filter_usesXForwardedForHeader() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("X-Forwarded-For", "192.168.1.1, 10.0.0.1")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());
        when(rateLimitService.tryConsume(anyString())).thenReturn(true);
        when(rateLimitService.getRequestsPerMinute()).thenReturn(60);
        when(rateLimitService.getRemainingTokens(anyString())).thenReturn(59L);
        when(rateLimitService.getResetTimeSeconds(anyString())).thenReturn(60L);

        // When
        StepVerifier.create(rateLimitFilter.filter(exchange, chain)).verifyComplete();

        // Then
        verify(rateLimitService).tryConsume("192.168.1.1");
    }

    @Test
    void filter_usesXRealIpHeader() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("X-Real-IP", "192.168.1.2")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());
        when(rateLimitService.tryConsume(anyString())).thenReturn(true);
        when(rateLimitService.getRequestsPerMinute()).thenReturn(60);
        when(rateLimitService.getRemainingTokens(anyString())).thenReturn(59L);
        when(rateLimitService.getResetTimeSeconds(anyString())).thenReturn(60L);

        // When
        StepVerifier.create(rateLimitFilter.filter(exchange, chain)).verifyComplete();

        // Then
        verify(rateLimitService).tryConsume("192.168.1.2");
    }

    @Test
    void filter_usesRemoteAddressWhenNoHeaders() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .remoteAddress(new InetSocketAddress("192.168.1.3", 12345))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());
        when(rateLimitService.tryConsume(anyString())).thenReturn(true);
        when(rateLimitService.getRequestsPerMinute()).thenReturn(60);
        when(rateLimitService.getRemainingTokens(anyString())).thenReturn(59L);
        when(rateLimitService.getResetTimeSeconds(anyString())).thenReturn(60L);

        // When
        StepVerifier.create(rateLimitFilter.filter(exchange, chain)).verifyComplete();

        // Then
        verify(rateLimitService).tryConsume("192.168.1.3");
    }

    @Test
    void filter_rateLimitedResponse_setsCorrectContentType() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        WebFilterChain chain = mock(WebFilterChain.class);
        when(rateLimitService.tryConsume(anyString())).thenReturn(false);
        when(rateLimitService.getRequestsPerMinute()).thenReturn(60);
        when(rateLimitService.getRemainingTokens(anyString())).thenReturn(0L);
        when(rateLimitService.getResetTimeSeconds(anyString())).thenReturn(60L);

        // When
        StepVerifier.create(rateLimitFilter.filter(exchange, chain)).verifyComplete();

        // Then
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
        assertEquals("60", exchange.getResponse().getHeaders().getFirst("Retry-After"));
    }
}
