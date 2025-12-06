package com.gateway.util;

import com.gateway.config.GatewayConstants;
import org.springframework.web.server.ServerWebExchange;

/**
 * Utility class for resolving client IP addresses from requests.
 *
 * <p>Supports X-Forwarded-For and X-Real-IP headers for proxied requests,
 * falling back to the remote address when headers are not present.</p>
 */
public final class ClientIpResolver {

    private ClientIpResolver() {
        // Utility class - prevent instantiation
    }

    /**
     * Extract client identifier from request.
     *
     * <p>Uses X-Forwarded-For header if available, otherwise X-Real-IP,
     * falling back to remote address.</p>
     *
     * @param exchange the server web exchange
     * @return the client identifier (IP address)
     */
    public static String resolveClientIp(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest().getHeaders().getFirst(GatewayConstants.X_FORWARDED_FOR);
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }

        String xRealIp = exchange.getRequest().getHeaders().getFirst(GatewayConstants.X_REAL_IP);
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : GatewayConstants.UNKNOWN_CLIENT;
    }
}
