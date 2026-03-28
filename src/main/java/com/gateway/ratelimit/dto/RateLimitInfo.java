package com.gateway.ratelimit.dto;

/**
 * Rate limit information response.
 *
 * @param limit Maximum requests allowed
 * @param remaining Remaining requests in current window
 * @param resetSeconds Seconds until rate limit resets
 */
public record RateLimitInfo(
    long limit,
    long remaining,
    long resetSeconds
) {
    /**
     * Creates a new RateLimitInfo instance.
     *
     * @param limit maximum requests allowed in the current window
     * @param remaining remaining requests available
     * @param resetSeconds seconds until the rate limit resets
     * @return a new RateLimitInfo instance
     */
    public static RateLimitInfo of(long limit, long remaining, long resetSeconds) {
        return new RateLimitInfo(limit, remaining, resetSeconds);
    }
}
