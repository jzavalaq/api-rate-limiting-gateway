package com.gateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI documentation configuration.
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development server"),
                        new Server().url("https://api.example.com").description("Production server")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Authorization header using the Bearer scheme.")));
    }

    private Info apiInfo() {
        return new Info()
                .title(applicationName)
                .description("""
                        Enterprise-grade API Gateway providing:

                        - **Rate Limiting**: Token bucket algorithm via Bucket4j
                        - **JWT Authentication**: Secure token-based authentication
                        - **Request Routing**: Dynamic path-based routing to backend services
                        - **Circuit Breaker**: Resilience4j integration for fault tolerance
                        - **CORS**: Configurable cross-origin resource sharing

                        ## Authentication

                        All protected endpoints require a valid JWT token in the Authorization header:

                        ```
                        Authorization: Bearer <your-jwt-token>
                        ```

                        ## Rate Limiting

                        Rate limits are applied per client IP:
                        - **Per-minute limit**: Configurable (default: 60 requests/minute)
                        - **Per-hour limit**: Configurable (default: 1000 requests/hour)

                        Rate limit headers are included in all responses:
                        - `X-RateLimit-Limit`: Maximum requests allowed
                        - `X-RateLimit-Remaining`: Remaining requests in current window
                        - `X-RateLimit-Reset`: Seconds until rate limit resets
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("API Support")
                        .email("support@example.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }
}
