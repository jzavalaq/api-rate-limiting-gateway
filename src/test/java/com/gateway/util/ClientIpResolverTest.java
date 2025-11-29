package com.gateway.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ClientIpResolver.
 */
class ClientIpResolverTest {

    @Test
    void resolveClientIp_withXForwardedFor_returnsFirstIp() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header("X-Forwarded-For", "192.168.1.1, 10.0.0.1, 172.16.0.1")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();

        // When
        String clientIp = ClientIpResolver.resolveClientIp(exchange);

        // Then
        assertEquals("192.168.1.1", clientIp);
    }

    @Test
    void resolveClientIp_withXForwardedForSingleIp_returnsIp() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header("X-Forwarded-For", "192.168.1.100")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();

        // When
        String clientIp = ClientIpResolver.resolveClientIp(exchange);

        // Then
        assertEquals("192.168.1.100", clientIp);
    }

    @Test
    void resolveClientIp_withXForwardedForWithSpaces_trimsIp() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header("X-Forwarded-For", "  192.168.1.1 , 10.0.0.1  ")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();

        // When
        String clientIp = ClientIpResolver.resolveClientIp(exchange);

        // Then
        assertEquals("192.168.1.1", clientIp);
    }

    @Test
    void resolveClientIp_withEmptyXForwardedFor_fallsBackToXRealIp() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header("X-Forwarded-For", "")
                .header("X-Real-IP", "192.168.1.2")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();

        // When
        String clientIp = ClientIpResolver.resolveClientIp(exchange);

        // Then
        assertEquals("192.168.1.2", clientIp);
    }

    @Test
    void resolveClientIp_withoutXForwardedFor_usesXRealIp() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header("X-Real-IP", "192.168.1.2")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();

        // When
        String clientIp = ClientIpResolver.resolveClientIp(exchange);

        // Then
        assertEquals("192.168.1.2", clientIp);
    }

    @Test
    void resolveClientIp_withoutHeaders_usesRemoteAddress() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .remoteAddress(new InetSocketAddress("192.168.1.3", 12345))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();

        // When
        String clientIp = ClientIpResolver.resolveClientIp(exchange);

        // Then
        assertEquals("192.168.1.3", clientIp);
    }

    @Test
    void resolveClientIp_withoutAnyIp_returnsUnknown() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();

        // When
        String clientIp = ClientIpResolver.resolveClientIp(exchange);

        // Then
        assertEquals("unknown", clientIp);
    }

    @Test
    void resolveClientIp_withEmptyXRealIp_fallsBackToRemoteAddress() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header("X-Real-IP", "")
                .remoteAddress(new InetSocketAddress("10.0.0.1", 8080))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();

        // When
        String clientIp = ClientIpResolver.resolveClientIp(exchange);

        // Then
        assertEquals("10.0.0.1", clientIp);
    }

    @Test
    void resolveClientIp_xForwardedForTakesPrecedenceOverXRealIp() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header("X-Forwarded-For", "192.168.1.1")
                .header("X-Real-IP", "192.168.1.2")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();

        // When
        String clientIp = ClientIpResolver.resolveClientIp(exchange);

        // Then
        assertEquals("192.168.1.1", clientIp);
    }
}
