package com.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;

import com.gateway.security.JwtAuthenticationFilter;

import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * Security configuration for the API Gateway.
 *
 * <p>Configures JWT-based authentication, CORS, CSRF protection, and
 * path-based authorization rules.</p>
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final String[] allowedOrigins;

    /**
     * Constructs a new SecurityConfig with allowed CORS origins.
     *
     * @param allowedOrigins comma-separated list of allowed origins
     */
    public SecurityConfig(@Value("${allowed.origins}") String allowedOrigins) {
        this.allowedOrigins = allowedOrigins.split(",");
    }

    /**
     * Configure the security filter chain.
     *
     * @param http the server HTTP security
     * @param jwtAuthenticationFilter the JWT authentication filter
     * @return the configured security web filter chain
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers
                        .frameOptions(ServerHttpSecurity.HeaderSpec.FrameOptionsSpec::disable)
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                "script-src 'self'; " +
                                "style-src 'self' 'unsafe-inline'; " +
                                "img-src 'self' data:; " +
                                "font-src 'self'; " +
                                "frame-ancestors 'none'; " +
                                "form-action 'self'"))
                        .hsts(hsts -> hsts
                                .includeSubdomains(true)
                                .maxAge(java.time.Duration.ofDays(365)))
                )
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints
                        .pathMatchers("/actuator/health").permitAll()
                        .pathMatchers("/actuator/info").permitAll()
                        .pathMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        // H2 console should be denied in all environments for security
                        .pathMatchers("/h2-console/**").denyAll()
                        .pathMatchers("/health").permitAll()
                        .pathMatchers("/").permitAll()
                        .pathMatchers("/fallback/**").permitAll()
                        // Protected endpoints
                        .pathMatchers("/api/**").authenticated()
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    /**
     * Configure CORS settings.
     *
     * @return the CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // Explicitly list allowed headers instead of wildcard for better security
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Correlation-ID",
                "X-Request-ID",
                "X-Forwarded-For",
                "X-Real-IP",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));
        configuration.setExposedHeaders(Arrays.asList(
                "X-Correlation-ID",
                "X-RateLimit-Limit",
                "X-RateLimit-Remaining",
                "X-RateLimit-Reset",
                "Retry-After"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
