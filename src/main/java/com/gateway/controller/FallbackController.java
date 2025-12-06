package com.gateway.controller;

import com.gateway.config.GatewayConstants;
import com.gateway.dto.ApiResponse;
import com.gateway.util.CorrelationIdUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Fallback controller for circuit breaker responses.
 *
 * <p>These endpoints are invoked when backend services are unavailable
 * and the circuit breaker is in open state.</p>
 */
@RestController
@RequestMapping("/api/v1/fallback")
@Tag(name = "Fallback", description = "Fallback endpoints for circuit breaker responses")
public class FallbackController {

    /**
     * Fallback endpoint for user service unavailability.
     *
     * @param exchange the server web exchange
     * @return service unavailable response with retry information
     */
    @Operation(
        summary = "User service fallback",
        description = "Returns a fallback response when the user service is unavailable due to circuit breaker"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "User service is currently unavailable")
    })
    @GetMapping("/users")
    public Mono<ResponseEntity<ApiResponse<Map<String, String>>>> usersFallback(ServerWebExchange exchange) {
        String correlationId = CorrelationIdUtils.getOrCreateCorrelationId(exchange);
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(
                        "User service is currently unavailable. Please try again later.",
                        Map.of("service", "user-service", "retryAfter",
                                String.valueOf(GatewayConstants.DEFAULT_RETRY_AFTER_SECONDS)),
                        correlationId)));
    }

    /**
     * Fallback endpoint for order service unavailability.
     *
     * @param exchange the server web exchange
     * @return service unavailable response with retry information
     */
    @Operation(
        summary = "Order service fallback",
        description = "Returns a fallback response when the order service is unavailable due to circuit breaker"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Order service is currently unavailable")
    })
    @GetMapping("/orders")
    public Mono<ResponseEntity<ApiResponse<Map<String, String>>>> ordersFallback(ServerWebExchange exchange) {
        String correlationId = CorrelationIdUtils.getOrCreateCorrelationId(exchange);
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(
                        "Order service is currently unavailable. Please try again later.",
                        Map.of("service", "order-service", "retryAfter",
                                String.valueOf(GatewayConstants.DEFAULT_RETRY_AFTER_SECONDS)),
                        correlationId)));
    }

    /**
     * Fallback endpoint for product service unavailability.
     *
     * @param exchange the server web exchange
     * @return service unavailable response with retry information
     */
    @Operation(
        summary = "Product service fallback",
        description = "Returns a fallback response when the product service is unavailable due to circuit breaker"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Product service is currently unavailable")
    })
    @GetMapping("/products")
    public Mono<ResponseEntity<ApiResponse<Map<String, String>>>> productsFallback(ServerWebExchange exchange) {
        String correlationId = CorrelationIdUtils.getOrCreateCorrelationId(exchange);
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(
                        "Product service is currently unavailable. Please try again later.",
                        Map.of("service", "product-service", "retryAfter",
                                String.valueOf(GatewayConstants.DEFAULT_RETRY_AFTER_SECONDS)),
                        correlationId)));
    }
}
