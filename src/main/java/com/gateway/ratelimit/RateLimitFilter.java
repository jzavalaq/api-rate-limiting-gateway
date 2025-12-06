package com.gateway.ratelimit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.dto.ErrorResponse;
import com.gateway.util.CorrelationIdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Rate limiting filter that applies token bucket rate limiting to incoming requests.
 *
 * <p>This filter intercepts all incoming requests and checks if the client has
 * exceeded their rate limit. It adds rate limit headers to all responses and
 * returns HTTP 429 when the limit is exceeded.</p>
 *
 * <p>Client identification is based on IP address, supporting X-Forwarded-For
 * and X-Real-IP headers for proxied requests.</p>
 */
@Component
public class RateLimitFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final String X_RATELIMIT_LIMIT = "X-RateLimit-Limit";
    private static final String X_RATELIMIT_REMAINING = "X-RateLimit-Remaining";
    private static final String X_RATELIMIT_RESET = "X-RateLimit-Reset";
    private static final String X_CORRELATION_ID = "X-Correlation-ID";
    private static final String RETRY_AFTER = "Retry-After";

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    /**
     * Constructs a new RateLimitFilter.
     *
     * @param rateLimitService the rate limiting service
     * @param objectMapper the JSON object mapper for error responses
     */
    public RateLimitFilter(RateLimitService rateLimitService, ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
    }

    /**
     * Filter incoming requests and apply rate limiting.
     *
     * @param exchange the server web exchange
     * @param chain the web filter chain
     * @return Mono completing when the filter chain is done
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String clientId = getClientId(exchange);
        String correlationId = getOrCreateCorrelationId(exchange);

        // Add rate limit headers to response
        exchange.getResponse().getHeaders().add(X_RATELIMIT_LIMIT,
                String.valueOf(rateLimitService.getRequestsPerMinute()));
        exchange.getResponse().getHeaders().add(X_RATELIMIT_REMAINING,
                String.valueOf(rateLimitService.getRemainingTokens(clientId)));
        exchange.getResponse().getHeaders().add(X_RATELIMIT_RESET,
                String.valueOf(rateLimitService.getResetTimeSeconds(clientId)));
        exchange.getResponse().getHeaders().add(X_CORRELATION_ID, correlationId);

        // Check rate limit
        if (!rateLimitService.tryConsume(clientId)) {
            log.warn("Rate limit exceeded for client: {}", clientId);
            return handleRateLimited(exchange, correlationId);
        }

        return chain.filter(exchange);
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
    private String getClientId(ServerWebExchange exchange) {
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

    /**
     * Get existing correlation ID or create a new one.
     *
     * @param exchange the server web exchange
     * @return the correlation ID
     */
    private String getOrCreateCorrelationId(ServerWebExchange exchange) {
        return CorrelationIdUtils.getOrCreateCorrelationId(exchange);
    }

    /**
     * Handle rate limited response by returning HTTP 429.
     *
     * @param exchange the server web exchange
     * @param correlationId the correlation ID for the request
     * @return Mono completing when the error response is written
     */
    private Mono<Void> handleRateLimited(ServerWebExchange exchange, String correlationId) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().getHeaders().add(RETRY_AFTER, "60");

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Too Many Requests",
                "Rate limit exceeded. Please try again later.",
                exchange.getRequest().getPath().value(),
                correlationId
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        } catch (JsonProcessingException e) {
            log.error("Error serializing rate limit response", e);
            return Mono.error(e);
        }
    }
}
