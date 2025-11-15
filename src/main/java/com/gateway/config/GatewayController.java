package com.gateway.config;

import com.gateway.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

/**
 * Gateway health and info endpoints.
 */
@RestController
public class GatewayController {

    @GetMapping("/")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> root(ServerWebExchange exchange) {
        String correlationId = getCorrelationId(exchange);
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

    private String getCorrelationId(ServerWebExchange exchange) {
        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
        return correlationId != null ? correlationId : UUID.randomUUID().toString();
    }
}
