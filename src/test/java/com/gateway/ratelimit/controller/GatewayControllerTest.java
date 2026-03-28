package com.gateway.ratelimit.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GatewayController.
 */
class GatewayControllerTest {

    private final GatewayController gatewayController = new GatewayController();

    @Test
    void root_returnsGatewayInfo() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/").build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();

        // When
        var result = gatewayController.root(exchange);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertTrue(response.getBody().success());
                    Map<String, Object> data = response.getBody().data();
                    assertEquals("API Rate Limiting Gateway", data.get("name"));
                    assertEquals("1.0.0", data.get("version"));
                    assertEquals("running", data.get("status"));
                    assertNotNull(data.get("endpoints"));
                })
                .verifyComplete();
    }

    @Test
    void health_returnsHealthStatus() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/health").build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();

        // When
        var result = gatewayController.health(exchange);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.getStatusCode());
                    Map<String, Object> body = response.getBody();
                    assertNotNull(body);
                    assertEquals("UP", body.get("status"));
                    @SuppressWarnings("unchecked")
                    Map<String, Object> gateway = (Map<String, Object>) body.get("gateway");
                    assertEquals("UP", gateway.get("status"));
                    assertEquals("UP", gateway.get("rateLimiter"));
                    assertEquals("UP", gateway.get("circuitBreaker"));
                })
                .verifyComplete();
    }
}
