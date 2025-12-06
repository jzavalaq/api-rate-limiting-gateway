package com.gateway.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RateLimitInfo record.
 */
class RateLimitInfoTest {

    @Test
    void of_createsRateLimitInfo() {
        // Given
        long limit = 60L;
        long remaining = 59L;
        long resetSeconds = 60L;

        // When
        RateLimitInfo info = RateLimitInfo.of(limit, remaining, resetSeconds);

        // Then
        assertEquals(limit, info.limit());
        assertEquals(remaining, info.remaining());
        assertEquals(resetSeconds, info.resetSeconds());
    }

    @Test
    void of_withZeroValues_createsRateLimitInfo() {
        // Given
        long limit = 0L;
        long remaining = 0L;
        long resetSeconds = 0L;

        // When
        RateLimitInfo info = RateLimitInfo.of(limit, remaining, resetSeconds);

        // Then
        assertEquals(0L, info.limit());
        assertEquals(0L, info.remaining());
        assertEquals(0L, info.resetSeconds());
    }

    @Test
    void of_withMaxValues_createsRateLimitInfo() {
        // Given
        long limit = Long.MAX_VALUE;
        long remaining = Long.MAX_VALUE;
        long resetSeconds = Long.MAX_VALUE;

        // When
        RateLimitInfo info = RateLimitInfo.of(limit, remaining, resetSeconds);

        // Then
        assertEquals(Long.MAX_VALUE, info.limit());
        assertEquals(Long.MAX_VALUE, info.remaining());
        assertEquals(Long.MAX_VALUE, info.resetSeconds());
    }
}
