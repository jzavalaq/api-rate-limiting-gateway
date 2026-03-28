package com.gateway.ratelimit.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Circuit breaker configuration using Resilience4j.
 *
 * <p>Configures circuit breakers for backend services with customizable
 * thresholds for failure rates, slow calls, and state transitions.</p>
 */
@Configuration
public class GatewayCircuitBreakerConfig {

    private static final Logger log = LoggerFactory.getLogger(GatewayCircuitBreakerConfig.class);

    /**
     * Create the circuit breaker registry with default configuration.
     *
     * @return the circuit breaker registry
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // 50% failure rate threshold
                .slowCallRateThreshold(50) // 50% slow call rate threshold
                .slowCallDurationThreshold(Duration.ofSeconds(3)) // 3 seconds is considered slow
                .permittedNumberOfCallsInHalfOpenState(3)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5) // Minimum calls before calculating failure rate
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(Exception.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);

        // Register event listeners for monitoring
        registry.getEventPublisher().onEntryAdded(event -> {
            log.info("Circuit breaker added: {}", event.getAddedEntry().getName());
        });

        return registry;
    }

    /**
     * Create the backend service circuit breaker with event listeners.
     *
     * @param registry the circuit breaker registry
     * @return the configured circuit breaker
     */
    @Bean
    public CircuitBreaker backendServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreaker circuitBreaker = registry.circuitBreaker("backendService");

        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> log.warn(
                        "Circuit breaker '{}' state changed from {} to {}",
                        event.getCircuitBreakerName(),
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()))
                .onError(event -> log.error(
                        "Circuit breaker '{}' recorded error: {}",
                        event.getCircuitBreakerName(),
                        event.getThrowable().getMessage()))
                .onSuccess(event -> log.debug(
                        "Circuit breaker '{}' recorded success ({}ms)",
                        event.getCircuitBreakerName(),
                        event.getElapsedDuration().toMillis()));

        return circuitBreaker;
    }

    /**
     * Create the time limiter configuration for circuit breaker timeouts.
     *
     * @return the time limiter configuration
     */
    @Bean
    public TimeLimiterConfig timeLimiterConfig() {
        return TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))
                .cancelRunningFuture(true)
                .build();
    }
}
