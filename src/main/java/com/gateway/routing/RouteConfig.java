package com.gateway.routing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway routing configuration.
 * Defines routes to backend services with circuit breaker support.
 */
@Configuration
public class RouteConfig {

    private final String userServiceUrl;
    private final String orderServiceUrl;
    private final String productServiceUrl;

    public RouteConfig(
            @Value("${services.user.url:http://localhost:8081}") String userServiceUrl,
            @Value("${services.order.url:http://localhost:8082}") String orderServiceUrl,
            @Value("${services.product.url:http://localhost:8083}") String productServiceUrl) {
        this.userServiceUrl = userServiceUrl;
        this.orderServiceUrl = orderServiceUrl;
        this.productServiceUrl = productServiceUrl;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User service route with circuit breaker
                .route("user-service", r -> r
                        .path("/api/v1/users/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("userServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/users"))
                                .stripPrefix(3)
                                .addRequestHeader("X-Gateway", "api-rate-limiting-gateway"))
                        .uri(userServiceUrl))

                // Order service route with circuit breaker
                .route("order-service", r -> r
                        .path("/api/v1/orders/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("orderServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/orders"))
                                .stripPrefix(3)
                                .addRequestHeader("X-Gateway", "api-rate-limiting-gateway"))
                        .uri(orderServiceUrl))

                // Product service route with circuit breaker
                .route("product-service", r -> r
                        .path("/api/v1/products/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("productServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/products"))
                                .stripPrefix(3)
                                .addRequestHeader("X-Gateway", "api-rate-limiting-gateway"))
                        .uri(productServiceUrl))

                // Health check endpoint
                .route("health", r -> r
                        .path("/health")
                        .filters(f -> f.setPath("/actuator/health"))
                        .uri("no://op"))

                .build();
    }
}
