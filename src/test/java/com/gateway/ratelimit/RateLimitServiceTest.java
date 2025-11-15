package com.gateway.ratelimit;

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
}
