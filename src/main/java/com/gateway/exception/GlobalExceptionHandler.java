package com.gateway.exception;

import com.gateway.dto.ErrorResponse;
import com.gateway.util.CorrelationIdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for the API Gateway.
 *
 * <p>Handles all exceptions thrown by the gateway and returns
 * standardized error responses with correlation IDs for tracing.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors from request body binding.
     *
     * @param ex the WebExchangeBindException
     * @param exchange the server web exchange
     * @return bad request response with validation error details
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationException(
            WebExchangeBindException ex,
            ServerWebExchange exchange) {

        String correlationId = getCorrelationId(exchange);
        String path = exchange.getRequest().getPath().value();

        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        log.warn("Validation error at {}: {}", path, details);

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                "Request validation failed",
                path,
                correlationId,
                details
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse));
    }

    /**
     * Handle all unhandled exceptions.
     *
     * @param ex the exception
     * @param exchange the server web exchange
     * @return internal server error response
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(
            Exception ex,
            ServerWebExchange exchange) {

        String correlationId = getCorrelationId(exchange);
        String path = exchange.getRequest().getPath().value();

        log.error("Unhandled exception at {}: {}", path, ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                path,
                correlationId,
                List.of(ex.getMessage())
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse));
    }

    /**
     * Handle illegal argument exceptions.
     *
     * @param ex the IllegalArgumentException
     * @param exchange the server web exchange
     * @return bad request response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            ServerWebExchange exchange) {

        String correlationId = getCorrelationId(exchange);
        String path = exchange.getRequest().getPath().value();

        log.warn("Bad request at {}: {}", path, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                path,
                correlationId
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse));
    }

    /**
     * Handle rate limit exceptions.
     *
     * @param ex the RateLimitExceededException
     * @param exchange the server web exchange
     * @return too many requests response
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleRateLimitExceededException(
            RateLimitExceededException ex,
            ServerWebExchange exchange) {

        String correlationId = getCorrelationId(exchange);
        String path = exchange.getRequest().getPath().value();

        log.warn("Rate limit exceeded at {}: {}", path, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Too Many Requests",
                "Rate limit exceeded. Please try again later.",
                path,
                correlationId
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "60")
                .body(errorResponse));
    }

    /**
     * Handle service unavailable exceptions.
     *
     * @param ex the ServiceUnavailableException
     * @param exchange the server web exchange
     * @return service unavailable response
     */
    @ExceptionHandler(ServiceUnavailableException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleServiceUnavailableException(
            ServiceUnavailableException ex,
            ServerWebExchange exchange) {

        String correlationId = getCorrelationId(exchange);
        String path = exchange.getRequest().getPath().value();

        log.warn("Service unavailable at {}: {}", path, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                ex.getMessage(),
                path,
                correlationId
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", "60")
                .body(errorResponse));
    }

    private String getCorrelationId(ServerWebExchange exchange) {
        return CorrelationIdUtils.getOrCreateCorrelationId(exchange);
    }
}
