package com.gateway.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting service using Bucket4j token bucket algorithm.
 *
 * <p>This service manages rate limit buckets per client identifier (typically IP address).
 * Each client gets a bucket with both per-minute and per-hour limits.</p>
 *
 * <p>The token bucket algorithm allows for burst traffic up to the bucket capacity
 * while enforcing the average rate over time.</p>
 */
@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);

    private final int requestsPerMinute;
    private final int requestsPerHour;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Constructs a new RateLimitService with the specified limits.
     *
     * @param requestsPerMinute maximum requests allowed per minute per client
     * @param requestsPerHour maximum requests allowed per hour per client
     */
    public RateLimitService(
            @Value("${rate.limit.requests-per-minute}") int requestsPerMinute,
            @Value("${rate.limit.requests-per-hour}") int requestsPerHour) {
        this.requestsPerMinute = requestsPerMinute;
        this.requestsPerHour = requestsPerHour;
    }

    /**
     * Get or create a bucket for a client identifier.
     *
     * @param clientId the client identifier (IP, API key, etc.)
     * @return the bucket for this client
     */
    public Bucket resolveBucket(String clientId) {
        return buckets.computeIfAbsent(clientId, this::createBucket);
    }

    /**
     * Create a new bucket with configured rate limits.
     *
     * @param clientId the client identifier (used for logging)
     * @return a new bucket with per-minute and per-hour limits
     */
    private Bucket createBucket(String clientId) {
        log.debug("Creating rate limit bucket for client: {}", clientId);

        // Per-minute bandwidth
        Bandwidth perMinute = Bandwidth.classic(
                requestsPerMinute,
                Refill.intervally(requestsPerMinute, Duration.ofMinutes(1))
        );

        // Per-hour bandwidth
        Bandwidth perHour = Bandwidth.classic(
                requestsPerHour,
                Refill.intervally(requestsPerHour, Duration.ofHours(1))
        );

        return Bucket.builder()
                .addLimit(perMinute)
                .addLimit(perHour)
                .build();
    }

    /**
     * Try to consume a token from the bucket.
     *
     * @param clientId the client identifier
     * @return true if the request is allowed, false if rate limited
     */
    public boolean tryConsume(String clientId) {
        Bucket bucket = resolveBucket(clientId);
        return bucket.tryConsume(1);
    }

    /**
     * Get remaining tokens for a client.
     *
     * @param clientId the client identifier
     * @return remaining tokens available
     */
    public long getRemainingTokens(String clientId) {
        Bucket bucket = resolveBucket(clientId);
        return bucket.getAvailableTokens();
    }

    /**
     * Get the time until the rate limit resets (in seconds).
     *
     * @param clientId the client identifier
     * @return seconds until reset (default 60 seconds)
     */
    public long getResetTimeSeconds(String clientId) {
        // Return 60 seconds as default reset window
        return 60;
    }

    /**
     * Get the per-minute limit.
     *
     * @return maximum requests allowed per minute
     */
    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }

    /**
     * Get the per-hour limit.
     *
     * @return maximum requests allowed per hour
     */
    public int getRequestsPerHour() {
        return requestsPerHour;
    }
}
