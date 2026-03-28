package com.gateway.ratelimit.routing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway routing configuration.
 *
 * <p>Defines routes to backend services with circuit breaker support.
 * Each route is configured with a fallback URI for graceful degradation
 * when backend services are unavailable.</p>
 */
@Configuration
public class RouteConfig {

    private final String userServiceUrl;
    private final String orderServiceUrl;
    private final String productServiceUrl;

    /**
     * Constructs a new RouteConfig with backend service URLs.
     *
     * @param userServiceUrl the user service URL
     * @param orderServiceUrl the order service URL
     * @param productServiceUrl the product service URL
     */
    public RouteConfig(
            @Value("${services.user.url:http://localhost:8081}") String userServiceUrl,
            @Value("${services.order.url:http://localhost:8082}") String orderServiceUrl,
            @Value("${services.product.url:http://localhost:8083}") String productServiceUrl) {
        this.userServiceUrl = userServiceUrl;
        this.orderServiceUrl = orderServiceUrl;
        this.productServiceUrl = productServiceUrl;
    }

    /**
     * Configure the custom route locator for the gateway.
     *
     * @param builder the route locator builder
     * @return the configured route locator
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User service route with circuit breaker
                .route("user-service", r -> r
                        .path("/api/v1/users/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("userServiceCircuitBreaker")
                                        .setFallbackUri("forward:/api/v1/fallback/users"))
                                .stripPrefix(3)
                                .addRequestHeader("X-Gateway", "api-rate-limiting-gateway"))
                        .uri(userServiceUrl))

                // Order service route with circuit breaker
                .route("order-service", r -> r
                        .path("/api/v1/orders/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("orderServiceCircuitBreaker")
                                        .setFallbackUri("forward:/api/v1/fallback/orders"))
                                .stripPrefix(3)
                                .addRequestHeader("X-Gateway", "api-rate-limiting-gateway"))
                        .uri(orderServiceUrl))

                // Product service route with circuit breaker
                .route("product-service", r -> r
                        .path("/api/v1/products/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("productServiceCircuitBreaker")
                                        .setFallbackUri("forward:/api/v1/fallback/products"))
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
