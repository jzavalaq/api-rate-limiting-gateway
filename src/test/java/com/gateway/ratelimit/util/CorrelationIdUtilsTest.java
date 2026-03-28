package com.gateway.ratelimit.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CorrelationIdUtils.
 */
class CorrelationIdUtilsTest {

    @Test
    void getOrCreateCorrelationId_withExistingHeader_returnsExistingId() {
        // Given
        String existingId = "existing-correlation-id";
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header("X-Correlation-ID", existingId)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();

        // When
        String result = CorrelationIdUtils.getOrCreateCorrelationId(exchange);

        // Then
        assertEquals(existingId, result);
    }

    @Test
    void getOrCreateCorrelationId_withoutHeader_returnsNewId() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.builder(request).build();

        // When
        String result = CorrelationIdUtils.getOrCreateCorrelationId(exchange);

        // Then
        assertNotNull(result);
        // Verify it's a valid UUID
        assertDoesNotThrow(() -> UUID.fromString(result));
    }

    @Test
    void generateCorrelationId_returnsValidUuid() {
        // When
        String result = CorrelationIdUtils.generateCorrelationId();

        // Then
        assertNotNull(result);
        assertDoesNotThrow(() -> UUID.fromString(result));
    }

    @Test
    void generateCorrelationId_generatesUniqueIds() {
        // When
        String id1 = CorrelationIdUtils.generateCorrelationId();
        String id2 = CorrelationIdUtils.generateCorrelationId();

        // Then
        assertNotEquals(id1, id2);
    }
}
