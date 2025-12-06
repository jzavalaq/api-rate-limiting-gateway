package com.gateway.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ApiResponse record.
 */
class ApiResponseTest {

    @Test
    void success_withDataAndMessage_returnsSuccessResponse() {
        // Given
        String data = "test-data";
        String message = "Test message";
        String correlationId = "test-correlation-id";

        // When
        ApiResponse<String> response = ApiResponse.success(data, message, correlationId);

        // Then
        assertTrue(response.success());
        assertEquals(message, response.message());
        assertEquals(data, response.data());
        assertEquals(correlationId, response.correlationId());
        assertNotNull(response.timestamp());
    }

    @Test
    void success_withDataOnly_returnsSuccessWithDefaultMessage() {
        // Given
        String data = "test-data";
        String correlationId = "test-correlation-id";

        // When
        ApiResponse<String> response = ApiResponse.success(data, correlationId);

        // Then
        assertTrue(response.success());
        assertEquals("Operation completed successfully", response.message());
        assertEquals(data, response.data());
        assertEquals(correlationId, response.correlationId());
    }

    @Test
    void error_withMessageOnly_returnsErrorResponse() {
        // Given
        String message = "Error occurred";
        String correlationId = "test-correlation-id";

        // When
        ApiResponse<Object> response = ApiResponse.error(message, correlationId);

        // Then
        assertFalse(response.success());
        assertEquals(message, response.message());
        assertNull(response.data());
        assertEquals(correlationId, response.correlationId());
    }

    @Test
    void error_withMessageAndData_returnsErrorResponseWithData() {
        // Given
        String message = "Error occurred";
        String data = "error-details";
        String correlationId = "test-correlation-id";

        // When
        ApiResponse<String> response = ApiResponse.error(message, data, correlationId);

        // Then
        assertFalse(response.success());
        assertEquals(message, response.message());
        assertEquals(data, response.data());
        assertEquals(correlationId, response.correlationId());
    }

    @Test
    void timestamp_isSetOnCreation() {
        // When
        ApiResponse<String> response = ApiResponse.success("data", "correlation-id");

        // Then
        assertNotNull(response.timestamp());
        assertTrue(response.timestamp().isBefore(java.time.Instant.now().plusSeconds(1)));
    }
}
