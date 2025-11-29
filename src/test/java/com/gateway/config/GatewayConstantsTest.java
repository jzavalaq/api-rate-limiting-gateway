package com.gateway.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GatewayConstants.
 */
class GatewayConstantsTest {

    @Test
    void defaultRetryAfterSeconds_is60() {
        assertEquals(60, GatewayConstants.DEFAULT_RETRY_AFTER_SECONDS);
    }

    @Test
    void xCorrelationId_hasCorrectValue() {
        assertEquals("X-Correlation-ID", GatewayConstants.X_CORRELATION_ID);
    }

    @Test
    void xRateLimitLimit_hasCorrectValue() {
        assertEquals("X-RateLimit-Limit", GatewayConstants.X_RATELIMIT_LIMIT);
    }

    @Test
    void xRateLimitRemaining_hasCorrectValue() {
        assertEquals("X-RateLimit-Remaining", GatewayConstants.X_RATELIMIT_REMAINING);
    }

    @Test
    void xRateLimitReset_hasCorrectValue() {
        assertEquals("X-RateLimit-Reset", GatewayConstants.X_RATELIMIT_RESET);
    }

    @Test
    void retryAfter_hasCorrectValue() {
        assertEquals("Retry-After", GatewayConstants.RETRY_AFTER);
    }

    @Test
    void xForwardedFor_hasCorrectValue() {
        assertEquals("X-Forwarded-For", GatewayConstants.X_FORWARDED_FOR);
    }

    @Test
    void xRealIp_hasCorrectValue() {
        assertEquals("X-Real-IP", GatewayConstants.X_REAL_IP);
    }

    @Test
    void bearerPrefix_hasCorrectValue() {
        assertEquals("Bearer ", GatewayConstants.BEARER_PREFIX);
    }

    @Test
    void unknownClient_hasCorrectValue() {
        assertEquals("unknown", GatewayConstants.UNKNOWN_CLIENT);
    }

    @Test
    void constructor_isPrivate() throws Exception {
        // GatewayConstants is a utility class with private constructor
        // Verify the constructor exists and is private
        var constructor = GatewayConstants.class.getDeclaredConstructor();
        assertFalse(constructor.canAccess(null));
    }
}
