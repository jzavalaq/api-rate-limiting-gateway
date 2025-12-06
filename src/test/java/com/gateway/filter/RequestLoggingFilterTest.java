package com.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RequestLoggingFilter.
 */
class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void filter_addsCorrelationIdHeader() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        // Then
        String correlationId = exchange.getResponse().getHeaders().getFirst("X-Correlation-ID");
        assertNotNull(correlationId);
        assertFalse(correlationId.isEmpty());
    }

    @Test
    void filter_usesExistingCorrelationId() {
        // Given
        String existingId = "existing-correlation-id";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("X-Correlation-ID", existingId)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        // Then
        String correlationId = exchange.getResponse().getHeaders().getFirst("X-Correlation-ID");
        assertEquals(existingId, correlationId);
    }

    @Test
    void filter_withQueryString_logsSuccessfully() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test?param=value").build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When/Then
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void filter_withXForwardedFor_logsClientIp() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("X-Forwarded-For", "192.168.1.1, 10.0.0.1")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When/Then
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void filter_withXRealIp_logsClientIp() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("X-Real-IP", "192.168.1.2")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When/Then
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void filter_withRemoteAddress_logsClientIp() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .remoteAddress(new InetSocketAddress("192.168.1.3", 12345))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When/Then
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void filter_logsResponseStatus() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When/Then
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
    }

    @Test
    void filter_callsChainFilter() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        // When
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        // Then
        verify(chain).filter(exchange);
    }
}
