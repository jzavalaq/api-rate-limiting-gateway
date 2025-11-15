package com.gateway.dto;

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
    public static RateLimitInfo of(long limit, long remaining, long resetSeconds) {
        return new RateLimitInfo(limit, remaining, resetSeconds);
    }
}
