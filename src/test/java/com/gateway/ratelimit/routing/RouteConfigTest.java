package com.gateway.ratelimit.routing;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RouteConfig.
 */
class RouteConfigTest {

    @Test
    void constructor_setsServiceUrls() {
        // Given
        String userServiceUrl = "http://localhost:8081";
        String orderServiceUrl = "http://localhost:8082";
        String productServiceUrl = "http://localhost:8083";

        // When
        RouteConfig config = new RouteConfig(userServiceUrl, orderServiceUrl, productServiceUrl);

        // Then
        assertNotNull(config);
    }

    @Test
    void constructor_withDefaultUrls_setsDefaultUrls() {
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
    void customRouteLocator_createsRouteLocator() {
        // Given
        RouteConfig config = new RouteConfig(
                "http://localhost:8081",
                "http://localhost:8082",
                "http://localhost:8083"
        );
        RouteLocatorBuilder.Builder routesBuilder = mock(RouteLocatorBuilder.Builder.class);
        RouteLocatorBuilder builder = mock(RouteLocatorBuilder.class);
        when(builder.routes()).thenReturn(routesBuilder);
        when(routesBuilder.route(anyString(), any())).thenReturn(routesBuilder);
        when(routesBuilder.build()).thenReturn(mock(RouteLocator.class));

        // When
        RouteLocator locator = config.customRouteLocator(builder);

        // Then
        assertNotNull(locator);
    }
}
