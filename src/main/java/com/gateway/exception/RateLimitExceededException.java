package com.gateway.exception;

/**
 * Exception thrown when rate limit is exceeded.
 *
 * <p>This exception indicates that a client has made too many requests
 * and has been throttled by the rate limiting mechanism.</p>
 */
public class RateLimitExceededException extends RuntimeException {

    /**
     * Constructs a new RateLimitExceededException with the specified message.
     *
     * @param message the error message
     */
    public RateLimitExceededException(String message) {
        super(message);
    }

    /**
     * Constructs a new RateLimitExceededException with the specified message and cause.
     *
     * @param message the error message
     * @param cause the underlying cause
     */
    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
