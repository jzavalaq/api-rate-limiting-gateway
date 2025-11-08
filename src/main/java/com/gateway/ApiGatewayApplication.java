package com.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the API Rate Limiting Gateway.
 *
 * This gateway provides:
 * - Rate limiting via Bucket4j
 * - JWT authentication via Spring Security
 * - Request routing via Spring Cloud Gateway
 * - Circuit breaker via Resilience4j
 * - OpenAPI documentation via SpringDoc
 *
 * @author Jarvis
 * @version 1.0.0
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
