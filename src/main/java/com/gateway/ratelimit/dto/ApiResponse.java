package com.gateway.ratelimit.dto;

import java.time.Instant;

/**
 * Standard API response wrapper.
 *
 * @param <T> the type of data in the response
 */
public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    Instant timestamp,
    String correlationId
) {
    /**
     * Creates a successful API response with custom message.
     *
     * @param data the response data
     * @param message the success message
     * @param correlationId the request correlation ID for tracing
     * @param <T> the type of data
     * @return ApiResponse with success status
     */
    public static <T> ApiResponse<T> success(T data, String message, String correlationId) {
        return new ApiResponse<>(true, message, data, Instant.now(), correlationId);
    }

    /**
     * Creates a successful API response with default message.
     *
     * @param data the response data
     * @param correlationId the request correlation ID for tracing
     * @param <T> the type of data
     * @return ApiResponse with success status
     */
    public static <T> ApiResponse<T> success(T data, String correlationId) {
        return success(data, "Operation completed successfully", correlationId);
    }

    /**
     * Creates an error API response without data.
     *
     * @param message the error message
     * @param correlationId the request correlation ID for tracing
     * @param <T> the type of data (will be null)
     * @return ApiResponse with error status
     */
    public static <T> ApiResponse<T> error(String message, String correlationId) {
        return new ApiResponse<>(false, message, null, Instant.now(), correlationId);
    }

    /**
     * Creates an error API response with additional data.
     *
     * @param message the error message
     * @param data additional error data
     * @param correlationId the request correlation ID for tracing
     * @param <T> the type of data
     * @return ApiResponse with error status
     */
    public static <T> ApiResponse<T> error(String message, T data, String correlationId) {
        return new ApiResponse<>(false, message, data, Instant.now(), correlationId);
    }
}
