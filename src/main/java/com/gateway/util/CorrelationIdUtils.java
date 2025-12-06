package com.gateway.util;

import com.gateway.config.GatewayConstants;
import org.springframework.web.server.ServerWebExchange;

import java.util.UUID;

/**
 * Utility methods for the API Gateway.
 */
public final class CorrelationIdUtils {

    private CorrelationIdUtils() {
        // Utility class
    }

    /**
     * Get or create a correlation ID from the exchange.
     *
     * <p>If the request contains an X-Correlation-ID header, it will be used.
     * Otherwise, a new UUID will be generated.</p>
     *
     * @param exchange the server web exchange containing the request
     * @return the existing or newly generated correlation ID
     */
    public static String getOrCreateCorrelationId(ServerWebExchange exchange) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(GatewayConstants.X_CORRELATION_ID);
        return correlationId != null ? correlationId : UUID.randomUUID().toString();
    }

    /**
     * Generate a new correlation ID.
     *
     * @return a new UUID-based correlation ID
     */
    public static String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
