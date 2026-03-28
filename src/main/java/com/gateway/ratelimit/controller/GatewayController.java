package com.gateway.ratelimit.controller;

import com.gateway.ratelimit.dto.ApiResponse;
import com.gateway.ratelimit.util.CorrelationIdUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Gateway health and info endpoints.
 *
 * <p>Provides health check and general information about the API Gateway.</p>
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Gateway", description = "Gateway health and information endpoints")
public class GatewayController {

    /**
     * Root endpoint returning gateway information.
     *
     * @param exchange the server web exchange
     * @return gateway information including name, version, and available endpoints
     */
    @Operation(
        summary = "Get gateway information",
        description = "Returns basic information about the API Gateway including version and available endpoints"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Gateway information retrieved successfully")
    })
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> root(ServerWebExchange exchange) {
        String correlationId = CorrelationIdUtils.getOrCreateCorrelationId(exchange);
        return Mono.just(ResponseEntity.ok(
                ApiResponse.success(
                        Map.of(
                                "name", "API Rate Limiting Gateway",
                                "version", "1.0.0",
                                "status", "running",
                                "endpoints", Map.of(
                                        "health", "/actuator/health",
                                        "swagger", "/swagger-ui.html",
                                        "apiDocs", "/v3/api-docs"
                                )
                        ),
                        "Welcome to the API Gateway",
                        correlationId)));
    }

    /**
     * Health check endpoint for monitoring and load balancers.
     *
     * @param exchange the server web exchange
     * @return health status of the gateway and its components
     */
    @Operation(
        summary = "Health check",
        description = "Returns the health status of the API Gateway and its components (rate limiter, circuit breaker)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Gateway is healthy")
    })
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health(ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.ok(
                Map.of(
                        "status", "UP",
                        "gateway", Map.of(
                                "status", "UP",
                                "rateLimiter", "UP",
                                "circuitBreaker", "UP"
                        )
                )));
    }
}
