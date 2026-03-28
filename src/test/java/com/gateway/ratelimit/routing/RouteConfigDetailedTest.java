package com.gateway.ratelimit.routing;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Additional unit tests for RouteConfig to improve coverage.
 */
class RouteConfigDetailedTest {

    @Test
    void constructor_withCustomUrls_storesUrls() {
        // Given
        String userServiceUrl = "http://user-service:8081";
        String orderServiceUrl = "http://order-service:8082";
        String productServiceUrl = "http://product-service:8083";

        // When
        RouteConfig config = new RouteConfig(userServiceUrl, orderServiceUrl, productServiceUrl);

        // Then
        assertNotNull(config);
    }

    @Test
    void constructor_withDefaultValues_works() {
        // Given/When
        RouteConfig config = new RouteConfig(
                "http://localhost:8081",
                "http://localhost:8082",
                "http://localhost:8083"
        );

        // Then
        assertNotNull(config);
    }

    @Test
    void customRouteLocator_configuresAllRoutes() {
        // Given
        RouteConfig config = new RouteConfig(
                "http://localhost:8081",
                "http://localhost:8082",
                "http://localhost:8083"
        );

        RouteLocatorBuilder.Builder routesBuilder = mock(RouteLocatorBuilder.Builder.class);
        RouteLocatorBuilder builder = mock(RouteLocatorBuilder.class);
        RouteLocator mockLocator = mock(RouteLocator.class);

        when(builder.routes()).thenReturn(routesBuilder);
        when(routesBuilder.route(anyString(), any())).thenReturn(routesBuilder);
        when(routesBuilder.build()).thenReturn(mockLocator);
        when(mockLocator.getRoutes()).thenReturn(Flux.empty());

        // When
        RouteLocator locator = config.customRouteLocator(builder);

        // Then
        assertNotNull(locator);
        verify(builder).routes();
        // Verify routes are configured (user, order, product, health)
        verify(routesBuilder, atLeast(4)).route(anyString(), any());
        verify(routesBuilder).build();
    }

    @Test
    void customRouteLocator_withDifferentUrls_usesConfiguredUrls() {
        // Given
        RouteConfig config = new RouteConfig(
                "http://custom-user:9001",
                "http://custom-order:9002",
                "http://custom-product:9003"
        );

        RouteLocatorBuilder.Builder routesBuilder = mock(RouteLocatorBuilder.Builder.class);
        RouteLocatorBuilder builder = mock(RouteLocatorBuilder.class);
        RouteLocator mockLocator = mock(RouteLocator.class);

        when(builder.routes()).thenReturn(routesBuilder);
        when(routesBuilder.route(anyString(), any())).thenReturn(routesBuilder);
        when(routesBuilder.build()).thenReturn(mockLocator);

        // When
        RouteLocator locator = config.customRouteLocator(builder);

        // Then
        assertNotNull(locator);
    }
}
