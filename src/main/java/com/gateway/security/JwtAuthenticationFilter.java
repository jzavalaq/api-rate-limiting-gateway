package com.gateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * JWT authentication filter for validating JWT tokens on incoming requests.
 *
 * <p>This filter extracts JWT tokens from the Authorization header, validates them,
 * and sets up the Spring Security context with the authenticated user's details.</p>
 *
 * <p>Requests without a valid JWT token are not blocked but continue without
 * authentication, allowing public endpoints to be accessible.</p>
 */
@Component
public class JwtAuthenticationFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Constructs a new JwtAuthenticationFilter.
     *
     * @param jwtTokenProvider the JWT token provider for validation
     */
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Filter incoming requests and validate JWT tokens.
     *
     * @param exchange the server web exchange
     * @param chain the web filter chain
     * @return Mono completing when the filter chain is done
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("Invalid JWT token received");
            return chain.filter(exchange);
        }

        try {
            String username = jwtTokenProvider.extractUsername(token);
            List<String> roles = jwtTokenProvider.extractRoles(token);

            List<SimpleGrantedAuthority> authorities = roles != null
                    ? roles.stream().map(SimpleGrantedAuthority::new).toList()
                    : List.of();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            log.debug("Authenticated user: {} with roles: {}", username, roles);

            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage(), e);
            return chain.filter(exchange);
        }
    }
}
