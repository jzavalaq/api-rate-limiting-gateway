package com.gateway.ratelimit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RateLimitExceededException.
 */
class RateLimitExceededExceptionTest {

    @Test
    void constructor_withMessage_setsMessage() {
        // Given
        String message = "Rate limit exceeded";

        // When
        RateLimitExceededException exception = new RateLimitExceededException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructor_withMessageAndCause_setsMessageAndCause() {
        // Given
        String message = "Rate limit exceeded";
        Throwable cause = new RuntimeException("Underlying error");

        // When
        RateLimitExceededException exception = new RateLimitExceededException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
