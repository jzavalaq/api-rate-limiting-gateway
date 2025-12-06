package com.gateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FallbackController.
 */
class FallbackControllerTest {

    private final FallbackController fallbackController = new FallbackController();

    @Test
    void usersFallback_returnsServiceUnavailable() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/fallback/users").build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();

        // When
        var result = fallbackController.usersFallback(exchange);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertFalse(response.getBody().success());
                    assertTrue(response.getBody().message().contains("User service"));
                    Map<String, String> data = response.getBody().data();
                    assertEquals("user-service", data.get("service"));
                    assertEquals("60", data.get("retryAfter"));
                })
                .verifyComplete();
    }

    @Test
    void ordersFallback_returnsServiceUnavailable() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/fallback/orders").build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();

        // When
        var result = fallbackController.ordersFallback(exchange);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertFalse(response.getBody().success());
                    assertTrue(response.getBody().message().contains("Order service"));
                    Map<String, String> data = response.getBody().data();
                    assertEquals("order-service", data.get("service"));
                    assertEquals("60", data.get("retryAfter"));
                })
                .verifyComplete();
    }

    @Test
    void productsFallback_returnsServiceUnavailable() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/fallback/products").build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();

        // When
        var result = fallbackController.productsFallback(exchange);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertFalse(response.getBody().success());
                    assertTrue(response.getBody().message().contains("Product service"));
                    Map<String, String> data = response.getBody().data();
                    assertEquals("product-service", data.get("service"));
                    assertEquals("60", data.get("retryAfter"));
                })
                .verifyComplete();
    }
}
