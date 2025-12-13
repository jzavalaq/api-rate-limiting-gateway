# API Rate Limiting Gateway

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring WebFlux](https://img.shields.io/badge/Spring%20WebFlux-Reactive-purple)](https://docs.spring.io/spring-framework/reference/web/webflux.html)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue?logo=docker)](Dockerfile)

An enterprise-grade reactive API Gateway built with Spring Boot, Spring Cloud Gateway, and WebFlux. Provides rate limiting using the token bucket algorithm, JWT authentication, request routing, circuit breaker pattern, and comprehensive OpenAPI documentation.

## Features

- **Rate Limiting**: Token bucket algorithm via Bucket4j
- **Reactive Stack**: Non-blocking I/O with Spring WebFlux
- **Circuit Breaker**: Resilience4j for fault tolerance
- **JWT Authentication**: Secure token-based auth
- **Request Routing**: Dynamic routing via Spring Cloud Gateway
- **OpenAPI Documentation**: Swagger UI at /swagger-ui.html

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Runtime environment |
| Spring Boot | 3.2.5 | Application framework |
| Spring Cloud Gateway | 2023.0.1 | API Gateway / Routing |
| Spring WebFlux | - | Reactive programming |
| Bucket4j | 8.7.0 | Rate limiting |
| Resilience4j | 2.2.0 | Circuit breaker |
| SpringDoc OpenAPI | 2.5.0 | API documentation |

## Quick Start

```bash
# Clone and run
git clone https://github.com/jzavalaq/api-rate-limiting-gateway.git
cd api-rate-limiting-gateway

# Build and run
mvn spring-boot:run

# With custom rate limits
RATE_LIMIT_RPM=100 RATE_LIMIT_RPH=2000 mvn spring-boot:run
```

## API Examples

### Health Check

```bash
# Check gateway health
curl http://localhost:8080/actuator/health
```

### Rate Limiting

```bash
# Make requests - after exceeding limit, you'll get 429
for i in {1..105}; do
  curl -w "%{http_code}\n" -o /dev/null -s http://localhost:8080/api/test
done
# Last 5 requests will return 429 Too Many Requests
```

### Authentication

```bash
# Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","email":"user@example.com","password":"Secure123!"}'

# Login to get JWT
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"Secure123!"}'
# Returns: {"token": "eyJhbG...", "type": "Bearer"}
```

### Protected Routes

```bash
TOKEN="your-jwt-token"

# Access protected route
curl http://localhost:8080/api/v1/protected/resource \
  -H "Authorization: Bearer $TOKEN"

# Check rate limit headers
curl -I http://localhost:8080/api/v1/protected/resource \
  -H "Authorization: Bearer $TOKEN"
# Response headers:
# X-RateLimit-Remaining: 99
# X-RateLimit-Limit: 100
```

### Circuit Breaker

```bash
# Circuit breaker status
curl http://localhost:8080/actuator/circuitbreakers

# Open circuit breaker (simulate failure)
curl http://localhost:8080/api/v1/test/failure

# Check status after failures
curl http://localhost:8080/actuator/circuitbreakers
```

## Configuration

### Rate Limiting Settings

```yaml
# application.yml
rate-limit:
  requests-per-minute: 100
  requests-per-hour: 2000
  bucket-capacity: 100
  refill-tokens: 1
  refill-period: 600ms
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `JWT_SECRET` | JWT signing key (256+ bits) | - |
| `RATE_LIMIT_RPM` | Requests per minute | 100 |
| `RATE_LIMIT_RPH` | Requests per hour | 2000 |
| `ALLOWED_ORIGINS` | CORS origins | `http://localhost:3000` |

## Docker

```bash
# Build image
docker build -t api-gateway:latest .

# Run container
docker run -d -p 8080:8080 \
  -e JWT_SECRET=your-secret-key \
  -e RATE_LIMIT_RPM=100 \
  api-gateway:latest

# Docker Compose
docker-compose up -d
```

## License

MIT License - see [LICENSE](LICENSE)
