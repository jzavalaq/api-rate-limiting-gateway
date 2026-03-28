package com.gateway.ratelimit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ServiceUnavailableException.
 */
class ServiceUnavailableExceptionTest {

    @Test
    void constructor_withMessage_setsMessage() {
        // Given
        String message = "Service unavailable";

        // When
        ServiceUnavailableException exception = new ServiceUnavailableException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructor_withMessageAndCause_setsMessageAndCause() {
        // Given
        String message = "Service unavailable";
        Throwable cause = new RuntimeException("Connection refused");

        // When
        ServiceUnavailableException exception = new ServiceUnavailableException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
