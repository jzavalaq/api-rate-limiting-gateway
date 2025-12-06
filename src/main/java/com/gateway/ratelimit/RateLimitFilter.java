package com.gateway.ratelimit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.config.GatewayConstants;
import com.gateway.dto.ErrorResponse;
import com.gateway.util.ClientIpResolver;
import com.gateway.util.CorrelationIdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        String clientId = ClientIpResolver.resolveClientIp(exchange);
        String correlationId = CorrelationIdUtils.getOrCreateCorrelationId(exchange);

        // Add rate limit headers to response
        exchange.getResponse().getHeaders().add(GatewayConstants.X_RATELIMIT_LIMIT,
                String.valueOf(rateLimitService.getRequestsPerMinute()));
        exchange.getResponse().getHeaders().add(GatewayConstants.X_RATELIMIT_REMAINING,
                String.valueOf(rateLimitService.getRemainingTokens(clientId)));
        exchange.getResponse().getHeaders().add(GatewayConstants.X_RATELIMIT_RESET,
                String.valueOf(rateLimitService.getResetTimeSeconds(clientId)));
        exchange.getResponse().getHeaders().add(GatewayConstants.X_CORRELATION_ID, correlationId);

        // Check rate limit
        if (!rateLimitService.tryConsume(clientId)) {
            log.warn("Rate limit exceeded for client: {}", clientId);
            return handleRateLimited(exchange, correlationId);
        }

        return chain.filter(exchange);
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
        exchange.getResponse().getHeaders().add(GatewayConstants.RETRY_AFTER,
                String.valueOf(GatewayConstants.DEFAULT_RETRY_AFTER_SECONDS));

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
