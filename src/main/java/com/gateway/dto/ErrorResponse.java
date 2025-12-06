package com.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Standard error response for API errors.
 *
 * <p>Follows the API contract format: {"error": "message", "status": 400}</p>
 *
 * @param error Error message describing what went wrong
 * @param status HTTP status code
 */
public record ErrorResponse(
    String error,
    int status
) {
    /**
     * Creates an ErrorResponse with the given status and message.
     *
     * @param status HTTP status code
     * @param errorType Error type (unused but kept for backward compatibility)
     * @param message Error message
     * @param path Request path (unused but kept for backward compatibility)
     * @param correlationId Request correlation ID (unused but kept for backward compatibility)
     * @return ErrorResponse instance
     */
    public static ErrorResponse of(int status, String errorType, String message, String path, String correlationId) {
        return new ErrorResponse(message, status);
    }

    /**
     * Creates an ErrorResponse with the given status, message, and details.
     *
     * @param status HTTP status code
     * @param errorType Error type (unused but kept for backward compatibility)
     * @param message Error message
     * @param path Request path (unused but kept for backward compatibility)
     * @param correlationId Request correlation ID (unused but kept for backward compatibility)
     * @param details Additional error details (combined into message)
     * @return ErrorResponse instance
     */
    public static ErrorResponse of(int status, String errorType, String message, String path, String correlationId, java.util.List<String> details) {
        if (details != null && !details.isEmpty()) {
            String combinedMessage = message + ": " + String.join(", ", details);
            return new ErrorResponse(combinedMessage, status);
        }
        return new ErrorResponse(message, status);
    }
}
