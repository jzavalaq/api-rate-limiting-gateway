package com.gateway.exception;

/**
 * Exception thrown when a backend service is unavailable.
 *
 * <p>This exception indicates that a backend service cannot be reached
 * or is not responding, typically due to circuit breaker activation.</p>
 */
public class ServiceUnavailableException extends RuntimeException {

    /**
     * Constructs a new ServiceUnavailableException with the specified message.
     *
     * @param message the error message
     */
    public ServiceUnavailableException(String message) {
        super(message);
    }

    /**
     * Constructs a new ServiceUnavailableException with the specified message and cause.
     *
     * @param message the error message
     * @param cause the underlying cause
     */
    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
