package com.gateway.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidationException_returnsBadRequest() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        WebExchangeBindException ex = mock(WebExchangeBindException.class);
        when(ex.getBindingResult()).thenReturn(new org.springframework.validation.BindException(new Object(), "target"));
        when(ex.getFieldErrors()).thenReturn(List.of());

        // When
        var result = handler.handleValidationException(ex, exchange);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(400, response.getBody().status());
                    assertTrue(response.getBody().error().contains("Request validation failed"));
                })
                .verifyComplete();
    }

    @Test
    void handleValidationException_withFieldErrors_returnsDetailsInResponse() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/users").build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();

        // Create mock with field errors
        WebExchangeBindException ex = mock(WebExchangeBindException.class);
        BindException bindResult = new BindException(new Object(), "target");
        bindResult.addError(new FieldError("target", "email", "must be a valid email"));
        bindResult.addError(new FieldError("target", "name", "must not be blank"));

        when(ex.getBindingResult()).thenReturn(bindResult);
        when(ex.getFieldErrors()).thenReturn(bindResult.getFieldErrors());

        // When
        var result = handler.handleValidationException(ex, exchange);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(400, response.getBody().status());
                    assertTrue(response.getBody().error().contains("Request validation failed"));
                    assertTrue(response.getBody().error().contains("email"));
                    assertTrue(response.getBody().error().contains("name"));
                })
                .verifyComplete();
    }

    @Test
    void handleGenericException_returnsInternalServerError() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        Exception ex = new RuntimeException("Unexpected error");

        // When
        var result = handler.handleGenericException(ex, exchange);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(500, response.getBody().status());
                    assertTrue(response.getBody().error().contains("An unexpected error occurred"));
                })
                .verifyComplete();
    }

    @Test
    void handleIllegalArgumentException_returnsBadRequest() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

        // When
        var result = handler.handleIllegalArgumentException(ex, exchange);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(400, response.getBody().status());
                    assertEquals("Invalid argument", response.getBody().error());
                })
                .verifyComplete();
    }

    @Test
    void handleRateLimitExceededException_returnsTooManyRequests() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        RateLimitExceededException ex = new RateLimitExceededException("Rate limit exceeded");

        // When
        var result = handler.handleRateLimitExceededException(ex, exchange);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(429, response.getBody().status());
                    assertEquals("Rate limit exceeded. Please try again later.", response.getBody().error());
                    assertEquals("60", response.getHeaders().getFirst("Retry-After"));
                })
                .verifyComplete();
    }

    @Test
    void handleServiceUnavailableException_returnsServiceUnavailable() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();
        ServiceUnavailableException ex = new ServiceUnavailableException("Service is down");

        // When
        var result = handler.handleServiceUnavailableException(ex, exchange);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
                    assertNotNull(response.getBody());
                    assertEquals(503, response.getBody().status());
                    assertEquals("Service is down", response.getBody().error());
                    assertEquals("60", response.getHeaders().getFirst("Retry-After"));
                })
                .verifyComplete();
    }
}
