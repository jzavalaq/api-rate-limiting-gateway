package com.gateway.ratelimit.ratelimit;

import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RateLimitService.
 */
class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService(60, 1000);
    }

    @Test
    void tryConsume_FirstRequest_ReturnsTrue() {
        String clientId = "test-client-1";
        assertTrue(rateLimitService.tryConsume(clientId));
    }

    @Test
    void tryConsume_SameClientMultipleRequests_ReturnsTrueInitially() {
        String clientId = "test-client-2";

        // Should allow multiple requests within limit
        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimitService.tryConsume(clientId), "Request " + i + " should be allowed");
        }
    }

    @Test
    void getRemainingTokens_NewClient_ReturnsLimit() {
        String clientId = "test-client-3";
        long remaining = rateLimitService.getRemainingTokens(clientId);
        assertEquals(60, remaining);
    }

    @Test
    void getRemainingTokens_AfterConsume_DecreasesByOne() {
        String clientId = "test-client-4";
        rateLimitService.tryConsume(clientId);
        long remaining = rateLimitService.getRemainingTokens(clientId);
        assertEquals(59, remaining);
    }

    @Test
    void getRequestsPerMinute_ReturnsConfiguredValue() {
        assertEquals(60, rateLimitService.getRequestsPerMinute());
    }

    @Test
    void getRequestsPerHour_ReturnsConfiguredValue() {
        assertEquals(1000, rateLimitService.getRequestsPerHour());
    }

    @Test
    void tryConsume_DifferentClients_HaveSeparateBuckets() {
        String client1 = "client-1";
        String client2 = "client-2";

        // Consume for client 1
        assertTrue(rateLimitService.tryConsume(client1));
        assertEquals(59, rateLimitService.getRemainingTokens(client1));

        // Client 2 should still have full bucket
        assertEquals(60, rateLimitService.getRemainingTokens(client2));
    }

    @Test
    void resolveBucket_sameClient_returnsSameBucket() {
        String clientId = "same-client";
        Bucket bucket1 = rateLimitService.resolveBucket(clientId);
        Bucket bucket2 = rateLimitService.resolveBucket(clientId);

        assertSame(bucket1, bucket2);
    }

    @Test
    void resolveBucket_differentClients_returnsDifferentBuckets() {
        String client1 = "client-a";
        String client2 = "client-b";
        Bucket bucket1 = rateLimitService.resolveBucket(client1);
        Bucket bucket2 = rateLimitService.resolveBucket(client2);

        assertNotSame(bucket1, bucket2);
    }

    @Test
    void getResetTimeSeconds_returnsDefault60() {
        String clientId = "test-client-reset";
        assertEquals(60L, rateLimitService.getResetTimeSeconds(clientId));
    }

    @Test
    void tryConsume_exceedsMinuteLimit_returnsFalse() {
        String clientId = "high-volume-client";

        // Exhaust the per-minute limit (60 requests)
        for (int i = 0; i < 60; i++) {
            assertTrue(rateLimitService.tryConsume(clientId));
        }

        // The 61st request should fail
        assertFalse(rateLimitService.tryConsume(clientId));
    }
}
