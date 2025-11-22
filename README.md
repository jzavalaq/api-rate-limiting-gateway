# API Rate Limiting Gateway

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring WebFlux](https://img.shields.io/badge/Spring%20WebFlux-Reactive-purple)](https://docs.springframework.io/spring-framework/reference/web/webflux.html)
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

## Quick Start with Docker Compose

```bash
# Copy environment file and customize
cp .env.example .env
# Edit .env with your values (especially JWT_SECRET)

# Start the gateway
docker-compose up -d

# App available at http://localhost:8080
# Swagger UI at http://localhost:8080/swagger-ui.html
# Health check at http://localhost:8080/actuator/health

# View logs
docker-compose logs -f app

# Stop
docker-compose down
```

## API Examples

### Health Check

```bash
# Check gateway health
curl http://localhost:8080/actuator/health

# Custom health endpoint
curl http://localhost:8080/api/v1/health
```

### Gateway Info

```bash
# Get gateway information
curl http://localhost:8080/api/v1
```

### Rate Limiting

```bash
# Make requests - after exceeding limit, you'll get 429
for i in {1..105}; do
  curl -w "%{http_code}\n" -o /dev/null -s http://localhost:8080/api/v1/health
done
# Last requests will return 429 Too Many Requests
```

### Protected Routes

```bash
TOKEN="your-jwt-token"

# Access protected route (users service)
curl http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer $TOKEN"

# Access protected route (orders service)
curl http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer $TOKEN"

# Access protected route (products service)
curl http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer $TOKEN"

# Check rate limit headers
curl -I http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer $TOKEN"
# Response headers:
# X-RateLimit-Remaining: 99
# X-RateLimit-Limit: 100
```

### Circuit Breaker Fallbacks

```bash
# When backend services are unavailable, fallback responses are returned
curl http://localhost:8080/api/v1/fallback/users
curl http://localhost:8080/api/v1/fallback/orders
curl http://localhost:8080/api/v1/fallback/products
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Server port | 8080 |
| `SPRING_PROFILES_ACTIVE` | Active profile (dev/prod) | dev |
| `JWT_SECRET` | JWT signing key (256+ bits) | - |
| `JWT_EXPIRATION` | Token expiration in ms | 86400000 |
| `RATE_LIMIT_RPM` | Requests per minute | 60 |
| `RATE_LIMIT_RPH` | Requests per hour | 1000 |
| `ALLOWED_ORIGINS` | CORS origins (comma-separated) | http://localhost:3000 |
| `SERVICES_USER_URL` | User service URL | http://localhost:8081 |
| `SERVICES_ORDER_URL` | Order service URL | http://localhost:8082 |
| `SERVICES_PRODUCT_URL` | Product service URL | http://localhost:8083 |

### Rate Limiting Settings

```yaml
# application.properties
rate.limit.requests-per-minute=60
rate.limit.requests-per-hour=1000
```

### Circuit Breaker Settings

```yaml
# application.properties
resilience4j.circuitbreaker.configs.default.sliding-window-size=10
resilience4j.circuitbreaker.configs.default.failure-rate-threshold=50
resilience4j.circuitbreaker.configs.default.wait-duration-in-open-state=60s
```

## Docker

```bash
# Build image
docker build -t api-gateway:latest .

# Run container
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e JWT_SECRET=your-secret-key \
  -e RATE_LIMIT_RPM=100 \
  -e ALLOWED_ORIGINS=http://localhost:3000 \
  api-gateway:latest

# Docker Compose
docker-compose up -d
```

## API Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| GET | `/` | Gateway info (redirects to /api/v1) | No |
| GET | `/api/v1` | Gateway information | No |
| GET | `/api/v1/health` | Health check | No |
| GET | `/actuator/health` | Actuator health | No |
| GET | `/swagger-ui.html` | OpenAPI UI | No |
| GET | `/v3/api-docs` | OpenAPI docs | No |
| ANY | `/api/v1/users/**` | User service routes | Bearer JWT |
| ANY | `/api/v1/orders/**` | Order service routes | Bearer JWT |
| ANY | `/api/v1/products/**` | Product service routes | Bearer JWT |
| GET | `/api/v1/fallback/users` | User service fallback | No |
| GET | `/api/v1/fallback/orders` | Order service fallback | No |
| GET | `/api/v1/fallback/products` | Product service fallback | No |

## License

MIT License - see [LICENSE](LICENSE)
