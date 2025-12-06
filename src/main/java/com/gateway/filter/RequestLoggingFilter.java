package com.gateway.filter;

import com.gateway.util.CorrelationIdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Request logging filter that adds correlation IDs to requests and logs request/response details.
 *
 * <p>This filter adds a unique correlation ID to each request for distributed tracing.
 * The correlation ID is included in both request and response logs, as well as the
 * X-Correlation-ID response header.</p>
 */
@Component
public class RequestLoggingFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String X_CORRELATION_ID = "X-Correlation-ID";
    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String START_TIME_KEY = "startTime";

    /**
     * Filter incoming requests to add correlation IDs and log request/response details.
     *
     * @param exchange the server web exchange
     * @param chain the web filter chain
     * @return Mono completing when the filter chain is done
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startTime = System.currentTimeMillis();

        // Get or create correlation ID (make it effectively final)
        final String correlationId = getOrCreateCorrelationId(exchange);

        // Add correlation ID to response headers
        exchange.getResponse().getHeaders().add(X_CORRELATION_ID, correlationId);

        // Log the request
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();
        String queryString = exchange.getRequest().getURI().getQuery();
        String clientIp = getClientIp(exchange);

        log.info("REQUEST: {} {}{} correlationId={} client={}",
                method,
                path,
                queryString != null ? "?" + queryString : "",
                correlationId,
                clientIp);

        // Continue the filter chain and log the response
        return chain.filter(exchange)
                .doFinally(signalType -> {
                    long duration = System.currentTimeMillis() - startTime;
                    int status = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value()
                            : 0;

                    log.info("RESPONSE: {} {}ms correlationId={}",
                            status,
                            duration,
                            correlationId);
                });
    }

    /**
     * Get or create a correlation ID from the exchange.
     *
     * @param exchange the server web exchange
     * @return the correlation ID
     */
    private String getOrCreateCorrelationId(ServerWebExchange exchange) {
        return CorrelationIdUtils.getOrCreateCorrelationId(exchange);
    }

    /**
     * Extract client IP from request headers or remote address.
     *
     * <p>Supports X-Forwarded-For and X-Real-IP headers for proxied requests.</p>
     *
     * @param exchange the server web exchange
     * @return the client IP address
     */
    private String getClientIp(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }

        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }
}
