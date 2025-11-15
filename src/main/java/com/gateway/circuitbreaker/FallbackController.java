package com.gateway.circuitbreaker;

import com.gateway.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

/**
 * Fallback controller for circuit breaker responses.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/users")
    public Mono<ResponseEntity<ApiResponse<Map<String, String>>>> usersFallback(ServerWebExchange exchange) {
        String correlationId = getCorrelationId(exchange);
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(
                        "User service is currently unavailable. Please try again later.",
                        Map.of("service", "user-service", "retryAfter", "60"),
                        correlationId)));
    }

    @GetMapping("/orders")
    public Mono<ResponseEntity<ApiResponse<Map<String, String>>>> ordersFallback(ServerWebExchange exchange) {
        String correlationId = getCorrelationId(exchange);
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(
                        "Order service is currently unavailable. Please try again later.",
                        Map.of("service", "order-service", "retryAfter", "60"),
                        correlationId)));
    }

    @GetMapping("/products")
    public Mono<ResponseEntity<ApiResponse<Map<String, String>>>> productsFallback(ServerWebExchange exchange) {
        String correlationId = getCorrelationId(exchange);
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(
                        "Product service is currently unavailable. Please try again later.",
                        Map.of("service", "product-service", "retryAfter", "60"),
                        correlationId)));
    }

    private String getCorrelationId(ServerWebExchange exchange) {
        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
        return correlationId != null ? correlationId : UUID.randomUUID().toString();
    }
}
