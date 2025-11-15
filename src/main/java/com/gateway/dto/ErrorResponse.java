package com.gateway.dto;

import java.time.Instant;
import java.util.List;

/**
 * Standard error response for API errors.
 *
 * @param status HTTP status code
 * @param error Error type
 * @param message Error message
 * @param path Request path
 * @param timestamp When the error occurred
 * @param correlationId Request correlation ID
 * @param details Additional error details
 */
public record ErrorResponse(
    int status,
    String error,
    String message,
    String path,
    Instant timestamp,
    String correlationId,
    List<String> details
) {
    public static ErrorResponse of(int status, String error, String message, String path, String correlationId) {
        return new ErrorResponse(status, error, message, path, Instant.now(), correlationId, List.of());
    }

    public static ErrorResponse of(int status, String error, String message, String path, String correlationId, List<String> details) {
        return new ErrorResponse(status, error, message, path, Instant.now(), correlationId, details);
    }
}
