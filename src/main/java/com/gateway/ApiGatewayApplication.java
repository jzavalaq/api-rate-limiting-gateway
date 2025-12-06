package com.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main entry point for the API Rate Limiting Gateway.
 *
 * <p>This gateway provides:</p>
 * <ul>
 *   <li>Rate limiting via Bucket4j (token bucket algorithm)</li>
 *   <li>JWT authentication via Spring Security</li>
 *   <li>Request routing via Spring Cloud Gateway</li>
 *   <li>Circuit breaker via Resilience4j</li>
 *   <li>OpenAPI documentation via SpringDoc</li>
 * </ul>
 *
 * @author Jarvis
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
