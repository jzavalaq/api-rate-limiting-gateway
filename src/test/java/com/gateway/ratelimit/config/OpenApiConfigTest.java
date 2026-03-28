package com.gateway.ratelimit.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OpenApiConfig.
 */
class OpenApiConfigTest {

    @Test
    void customOpenAPI_createsOpenAPI() {
        // Given
        OpenApiConfig config = new OpenApiConfig("test-gateway");

        // When
        OpenAPI openAPI = config.customOpenAPI();

        // Then
        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
        assertEquals("test-gateway", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
    }

    @Test
    void customOpenAPI_hasSecurityScheme() {
        // Given
        OpenApiConfig config = new OpenApiConfig("test-gateway");

        // When
        OpenAPI openAPI = config.customOpenAPI();

        // Then
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes());
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey("bearerAuth"));

        SecurityScheme scheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertEquals(SecurityScheme.Type.HTTP, scheme.getType());
        assertEquals("bearer", scheme.getScheme());
        assertEquals("JWT", scheme.getBearerFormat());
    }

    @Test
    void customOpenAPI_hasServers() {
        // Given
        OpenApiConfig config = new OpenApiConfig("test-gateway");

        // When
        OpenAPI openAPI = config.customOpenAPI();

        // Then
        assertNotNull(openAPI.getServers());
        assertFalse(openAPI.getServers().isEmpty());
        assertEquals(2, openAPI.getServers().size());
    }

    @Test
    void customOpenAPI_hasSecurityRequirement() {
        // Given
        OpenApiConfig config = new OpenApiConfig("test-gateway");

        // When
        OpenAPI openAPI = config.customOpenAPI();

        // Then
        assertNotNull(openAPI.getSecurity());
        assertFalse(openAPI.getSecurity().isEmpty());
    }

    @Test
    void customOpenAPI_hasContactAndLicense() {
        // Given
        OpenApiConfig config = new OpenApiConfig("test-gateway");

        // When
        OpenAPI openAPI = config.customOpenAPI();

        // Then
        Info info = openAPI.getInfo();
        assertNotNull(info.getContact());
        assertEquals("API Support", info.getContact().getName());
        assertEquals("support@example.com", info.getContact().getEmail());

        assertNotNull(info.getLicense());
        assertEquals("MIT License", info.getLicense().getName());
    }
}
