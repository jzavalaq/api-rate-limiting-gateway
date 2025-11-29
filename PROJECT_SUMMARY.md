# PROJECT SUMMARY

## Build Information
- **Project:** API Rate Limiting Gateway
- **Date:** 2026-03-22
- **Status:** COMPLETE
- **Test Results:** 12 tests passing,- **Duration:** ~8 minutes

- **Java Version:** 21
- **Spring Boot Version:** 3.2.5

## Features
1. **Rate Limiting** - Bucket4j token bucket algorithm with configurable per-minute/per-hour limits
2. **JWT Authentication** - Spring Security with role-based access control
3. **Request Routing** - Spring Cloud Gateway with path-based routing to backend services
4. **Circuit Breaker** - Resilience4j with automatic state transitions and fallback responses
5. **OpenAPI Documentation** - SpringDoc WebFlux UI at /swagger-ui.html

## Tech Stack
| Component | Version |
|------------|---------|
| Java | 21 |
| Spring Boot | 3.2.5 |
| Spring Cloud Gateway | 2023.0.1 |
| Bucket4j | 8.7.0 |
| Resilience4j | 2.2.0 |
| SpringDoc | 2.5.0 |
| PostgreSQL | 15 |
| H2 (dev) | PostgreSQL (prod) |

## API Endpoints

| Method | Path | Description | Auth |
|-------|------|-------------|------|
| GET | `/` | Gateway info | No |
| GET | `/health` | Health check | No |
| GET | `/actuator/health` | Actuator health | No |
| GET | `/swagger-ui.html` | OpenAPI UI | No |
| GET | `/v3/api-docs` | OpenAPI docs | No |
| ANY | `/api/v1/**` | Protected routes | Bearer JWT |

## Rate Limiting
- **Per-minute limit:** 60 requests (configurable)
- **Per-hour limit:** 1000 requests (configurable)
- **Rate limit headers:** `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`
- **429 response** with `Retry-After` header

## Docker Support
- Multi-stage Dockerfile with health check
- Docker Compose with PostgreSQL and app services
- Environment variables documented in `.env.example`

## CI/CD
- GitHub Actions workflow with build, test, Docker build stages
- Java 21, Maven caching

## Commands

```bash
# Build
./mvnw clean package -DskipTests

# Run tests
./mvnw test

# Run with Docker
docker-compose up --build
```

**Note:** Requires Java 21 and Maven 3.9.x
