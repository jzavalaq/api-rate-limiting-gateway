package com.gateway.ratelimit.config;

/**
 * Gateway-wide constants for the API Rate Limiting Gateway.
 *
 * <p>Centralizes magic numbers and strings used throughout the gateway
 * to ensure consistency and ease of configuration.</p>
 */
public final class GatewayConstants {

    private GatewayConstants() {
        // Utility class - prevent instantiation
    }

    // Rate Limiting Constants
    /**
     * Default retry-after duration in seconds when rate limited.
     */
    public static final int DEFAULT_RETRY_AFTER_SECONDS = 60;

    // Header Names
    /**
     * X-Correlation-ID header name for distributed tracing.
     */
    public static final String X_CORRELATION_ID = "X-Correlation-ID";

    /**
     * X-RateLimit-Limit header name.
     */
    public static final String X_RATELIMIT_LIMIT = "X-RateLimit-Limit";

    /**
     * X-RateLimit-Remaining header name.
     */
    public static final String X_RATELIMIT_REMAINING = "X-RateLimit-Remaining";

    /**
     * X-RateLimit-Reset header name.
     */
    public static final String X_RATELIMIT_RESET = "X-RateLimit-Reset";

    /**
     * Retry-After header name.
     */
    public static final String RETRY_AFTER = "Retry-After";

    // Client Identification Headers
    /**
     * X-Forwarded-For header for proxied requests.
     */
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";

    /**
     * X-Real-IP header for proxied requests.
     */
    public static final String X_REAL_IP = "X-Real-IP";

    // Authentication
    /**
     * Bearer prefix for Authorization header.
     */
    public static final String BEARER_PREFIX = "Bearer ";

    // Unknown client identifier
    /**
     * Fallback identifier when client IP cannot be determined.
     */
    public static final String UNKNOWN_CLIENT = "unknown";
}
