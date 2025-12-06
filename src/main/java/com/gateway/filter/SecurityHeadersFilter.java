package com.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

/**
 * Filter that adds security-related headers to all responses.
 *
 * <p>This filter adds the following security headers:</p>
 * <ul>
 *   <li>X-Content-Type-Options: nosniff - Prevents MIME type sniffing</li>
 *   <li>X-Frame-Options: DENY - Prevents clickjacking via iframes</li>
 *   <li>Referrer-Policy: strict-origin-when-cross-origin - Controls referrer information</li>
 *   <li>Permissions-Policy - Restricts browser features</li>
 *   <li>X-Permitted-Cross-Domain-Policies: none - Restricts cross-domain access</li>
 *   <li>Cache-Control: no-store for sensitive endpoints - Prevents caching of API responses</li>
 * </ul>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class SecurityHeadersFilter implements WebFilter {

    private static final String X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    private static final String X_FRAME_OPTIONS = "X-Frame-Options";
    private static final String REFERRER_POLICY = "Referrer-Policy";
    private static final String PERMISSIONS_POLICY = "Permissions-Policy";
    private static final String X_PERMITTED_CROSS_DOMAIN_POLICIES = "X-Permitted-Cross-Domain-Policies";
    private static final String CACHE_CONTROL = "Cache-Control";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        HttpHeaders headers = exchange.getResponse().getHeaders();

        // Prevent MIME type sniffing
        headers.add(X_CONTENT_TYPE_OPTIONS, "nosniff");

        // Prevent clickjacking
        headers.add(X_FRAME_OPTIONS, "DENY");

        // Control referrer information
        headers.add(REFERRER_POLICY, "strict-origin-when-cross-origin");

        // Restrict browser features
        headers.add(PERMISSIONS_POLICY, "geolocation=(), microphone=(), camera=(), payment=(), usb=()");

        // Restrict cross-domain policies
        headers.add(X_PERMITTED_CROSS_DOMAIN_POLICIES, "none");

        // Prevent caching of API responses
        String path = exchange.getRequest().getPath().value();
        if (path.startsWith("/api/")) {
            headers.add(CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0");
            headers.add("Pragma", "no-cache");
        }

        return chain.filter(exchange);
    }
}
