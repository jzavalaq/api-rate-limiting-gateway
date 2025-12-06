# API Rate Limiting Gateway

An enterprise-grade API Gateway built with Spring Boot and Spring Cloud Gateway, providing rate limiting, JWT authentication, request routing, circuit breaker pattern, and comprehensive OpenAPI documentation.

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Runtime environment |
| Spring Boot | 3.2.5 | Application framework |
| Spring Cloud Gateway | 2023.0.1 | API Gateway / Request routing |
| Spring Security | - | JWT Authentication |
| Bucket4j | 8.7.0 | Rate limiting (token bucket algorithm) |
| Resilience4j | 2.2.0 | Circuit breaker pattern |
| SpringDoc OpenAPI | 2.5.0 | API documentation |
| Caffeine | - | Caching for rate limit buckets |

## Prerequisites

- Java 21 (Temurin recommended)
- Maven 3.9+
- Docker (optional, for containerized deployment)

## Build Instructions

```bash
# Clone the repository
cd api-rate-limiting-gateway

# Build the project
mvn clean package -DskipTests

# Build with tests
mvn clean package

# Compile only
mvn compile -q

# Run tests
mvn test -q
```

## Run Instructions

```bash
# Run with Maven
mvn spring-boot:run

# Run with environment variables
JWT_SECRET=your-secure-secret-key-here-min-256-bits-long \
RATE_LIMIT_RPM=100 \
RATE_LIMIT_RPH=2000 \
mvn spring-boot:run

# Run the JAR directly
java -jar target/api-rate-limiting-gateway-1.0.0-SNAPSHOT.jar
```

## Docker Run Instructions

```bash
# Build the Docker image
docker build -t api-rate-limiting-gateway:1.0.0 .

# Run the container
docker run -d \
  --name api-gateway \
  -p 8080:8080 \
  -e JWT_SECRET=your-secure-secret-key-here-min-256-bits-long \
  -e RATE_LIMIT_RPM=100 \
  -e RATE_LIMIT_RPH=2000 \
  -e ALLOWED_ORIGINS=http://localhost:3000,https://example.com \
  api-rate-limiting-gateway:1.0.0

# View logs
docker logs -f api-gateway

# Stop the container
docker stop api-gateway
```

## Quick Start with Docker Compose

```bash
# Copy environment file
cp .env.example .env

# Edit .env with your values
# - Set JWT_SECRET to a secure 256-bit random string
# - Set POSTGRES_PASSWORD to a secure password
# - Update ALLOWED_ORIGINS for your frontend

# Start the stack
docker-compose up -d

# App available at http://localhost:8080
# Swagger UI at http://localhost:8080/swagger-ui.html

# View logs
docker-compose logs -f app

# Stop the stack
docker-compose down
```

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | 8080 | Server port |
| `JWT_SECRET` | change-me-in-production-min-256-bits | Secret key for JWT signing (min 256 bits) |
| `JWT_EXPIRATION` | 86400000 | JWT token expiration in milliseconds (24h) |
| `RATE_LIMIT_RPM` | 60 | Requests per minute per client |
| `RATE_LIMIT_RPH` | 1000 | Requests per hour per client |
| `ALLOWED_ORIGINS` | http://localhost:3000 | Comma-separated CORS allowed origins |
| `SERVICES_USER_URL` | http://localhost:8081 | User service URL |
| `SERVICES_ORDER_URL` | http://localhost:8082 | Order service URL |
| `SERVICES_PRODUCT_URL` | http://localhost:8083 | Product service URL |

## API Endpoints

### Health Check

```bash
# Gateway health
curl -s http://localhost:8080/health

# Spring Actuator health
curl -s http://localhost:8080/actuator/health
```

### Gateway Info

```bash
# Root endpoint - gateway information
curl -s http://localhost:8080/
```

### Protected API Routes (Requires JWT)

```bash
# User service (requires JWT)
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/v1/users

# Order service (requires JWT)
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/v1/orders

# Product service (requires JWT)
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/v1/products
```

### Fallback Endpoints (Circuit Breaker)

```bash
# User service fallback
curl -s http://localhost:8080/api/v1/fallback/users

# Order service fallback
curl -s http://localhost:8080/api/v1/fallback/orders

# Product service fallback
curl -s http://localhost:8080/api/v1/fallback/products
```

### OpenAPI Documentation

```bash
# OpenAPI JSON spec
curl -s http://localhost:8080/v3/api-docs

# Access Swagger UI
# Open browser to: http://localhost:8080/swagger-ui.html
```

## Rate Limiting

Rate limits are applied per client IP address using the token bucket algorithm:

- **Per-minute limit**: 60 requests (configurable via `RATE_LIMIT_RPM`)
- **Per-hour limit**: 1000 requests (configurable via `RATE_LIMIT_RPH`)

Rate limit headers are included in all responses:

| Header | Description |
|--------|-------------|
| `X-RateLimit-Limit` | Maximum requests allowed per minute |
| `X-RateLimit-Remaining` | Remaining requests in current window |
| `X-RateLimit-Reset` | Seconds until rate limit resets |

When rate limited, the API returns HTTP 429 with a `Retry-After` header.

## Authentication

All protected endpoints require a valid JWT token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

The JWT token must contain:
- `sub`: Subject (username)
- `roles`: Array of role strings

## Circuit Breaker

The gateway uses Resilience4j circuit breaker with the following configuration:

- **Failure rate threshold**: 50%
- **Slow call rate threshold**: 50%
- **Slow call duration**: 3 seconds
- **Sliding window size**: 10 calls
- **Wait duration in open state**: 60 seconds

When a backend service fails, the circuit breaker opens and returns a fallback response.

## License

MIT License
