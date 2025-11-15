package com.gateway.dto;

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
    public static <T> ApiResponse<T> success(T data, String message, String correlationId) {
        return new ApiResponse<>(true, message, data, Instant.now(), correlationId);
    }

    public static <T> ApiResponse<T> success(T data, String correlationId) {
        return success(data, "Operation completed successfully", correlationId);
    }

    public static <T> ApiResponse<T> error(String message, String correlationId) {
        return new ApiResponse<>(false, message, null, Instant.now(), correlationId);
    }

    public static <T> ApiResponse<T> error(String message, T data, String correlationId) {
        return new ApiResponse<>(false, message, data, Instant.now(), correlationId);
    }
}
