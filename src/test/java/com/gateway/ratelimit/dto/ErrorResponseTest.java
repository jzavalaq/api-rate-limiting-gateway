package com.gateway.ratelimit.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ErrorResponse record.
 */
class ErrorResponseTest {

    @Test
    void of_withBasicParams_returnsErrorResponse() {
        // Given
        int status = 400;
        String error = "Bad Request";
        String message = "Invalid input";
        String path = "/api/test";
        String correlationId = "test-correlation-id";

        // When
        ErrorResponse response = ErrorResponse.of(status, error, message, path, correlationId);

        // Then
        assertEquals(status, response.status());
        assertEquals(message, response.error());
    }

    @Test
    void of_withDetails_returnsErrorResponseWithDetails() {
        // Given
        int status = 400;
        String error = "Validation Error";
        String message = "Validation failed";
        String path = "/api/users";
        String correlationId = "test-correlation-id";
        List<String> details = List.of("Field 'name' is required", "Field 'email' is invalid");

        // When
        ErrorResponse response = ErrorResponse.of(status, error, message, path, correlationId, details);

        // Then
        assertEquals(status, response.status());
        assertTrue(response.error().contains(message));
        assertTrue(response.error().contains("Field 'name' is required"));
    }

    @Test
    void of_withEmptyDetails_returnsErrorResponseWithoutDetails() {
        // Given
        int status = 500;
        String error = "Internal Server Error";
        String message = "An error occurred";
        String path = "/api/test";
        String correlationId = "test-correlation-id";
        List<String> details = List.of();

        // When
        ErrorResponse response = ErrorResponse.of(status, error, message, path, correlationId, details);

        // Then
        assertEquals(status, response.status());
        assertEquals(message, response.error());
    }

    @Test
    void of_withNullDetails_returnsErrorResponseWithoutDetails() {
        // Given
        int status = 500;
        String error = "Internal Server Error";
        String message = "An error occurred";
        String path = "/api/test";
        String correlationId = "test-correlation-id";

        // When
        ErrorResponse response = ErrorResponse.of(status, error, message, path, correlationId, null);

        // Then
        assertEquals(status, response.status());
        assertEquals(message, response.error());
    }
}
