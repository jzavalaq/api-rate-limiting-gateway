package com.gateway.util;

import org.springframework.web.server.ServerWebExchange;

import java.util.UUID;

/**
 * Utility methods for the API Gateway.
 */
public final class CorrelationIdUtils {

    private static final String X_CORRELATION_ID = "X-Correlation-ID";

    private CorrelationIdUtils() {
        // Utility class
    }

    /**
     * Get or create a correlation ID from the exchange.
     */
    public static String getOrCreateCorrelationId(ServerWebExchange exchange) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(X_CORRELATION_ID);
        return correlationId != null ? correlationId : UUID.randomUUID().toString();
    }

    /**
     * Generate a new correlation ID.
     */
    public static String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
