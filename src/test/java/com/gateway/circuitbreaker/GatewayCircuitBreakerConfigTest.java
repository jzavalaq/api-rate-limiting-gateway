package com.gateway.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GatewayCircuitBreakerConfig.
 */
class GatewayCircuitBreakerConfigTest {

    private final GatewayCircuitBreakerConfig config = new GatewayCircuitBreakerConfig();

    @Test
    void circuitBreakerRegistry_createsRegistry() {
        // When
        CircuitBreakerRegistry registry = config.circuitBreakerRegistry();

        // Then
        assertNotNull(registry);
    }

    @Test
    void circuitBreakerRegistry_hasDefaultConfig() {
        // When
        CircuitBreakerRegistry registry = config.circuitBreakerRegistry();
        CircuitBreakerConfig defaultConfig = registry.getDefaultConfig();

        // Then
        assertNotNull(defaultConfig);
        assertEquals(50.0f, defaultConfig.getFailureRateThreshold());
        assertEquals(50.0f, defaultConfig.getSlowCallRateThreshold());
        assertEquals(Duration.ofSeconds(3), defaultConfig.getSlowCallDurationThreshold());
        assertEquals(3, defaultConfig.getPermittedNumberOfCallsInHalfOpenState());
        assertEquals(10, defaultConfig.getSlidingWindowSize());
        assertEquals(5, defaultConfig.getMinimumNumberOfCalls());
    }

    @Test
    void backendServiceCircuitBreaker_createsCircuitBreaker() {
        // Given
        CircuitBreakerRegistry registry = config.circuitBreakerRegistry();

        // When
        CircuitBreaker circuitBreaker = config.backendServiceCircuitBreaker(registry);

        // Then
        assertNotNull(circuitBreaker);
        assertEquals("backendService", circuitBreaker.getName());
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
    }

    @Test
    void timeLimiterConfig_createsConfig() {
        // When
        TimeLimiterConfig timeLimiterConfig = config.timeLimiterConfig();

        // Then
        assertNotNull(timeLimiterConfig);
        assertEquals(Duration.ofSeconds(5), timeLimiterConfig.getTimeoutDuration());
        assertTrue(timeLimiterConfig.shouldCancelRunningFuture());
    }

    @Test
    void circuitBreakerRegistry_createsCircuitBreakerWithConfig() {
        // When
        CircuitBreakerRegistry registry = config.circuitBreakerRegistry();
        CircuitBreaker circuitBreaker = registry.circuitBreaker("test-cb");

        // Then
        assertNotNull(circuitBreaker);
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
    }
}
