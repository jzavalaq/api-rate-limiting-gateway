-- API Gateway Database Schema
-- Initial migration

-- Rate limit configuration table
CREATE TABLE IF NOT EXISTS rate_limit_config (
    id BIGSERIAL PRIMARY KEY,
    client_id VARCHAR(255) NOT NULL UNIQUE,
    requests_per_minute INTEGER NOT NULL DEFAULT 60,
    requests_per_hour INTEGER NOT NULL DEFAULT 1000,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

-- Route configuration table
CREATE TABLE IF NOT EXISTS route_config (
    id BIGSERIAL PRIMARY KEY,
    route_id VARCHAR(255) NOT NULL UNIQUE,
    path_pattern VARCHAR(500) NOT NULL,
    target_uri VARCHAR(500) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

-- Circuit breaker state table
CREATE TABLE IF NOT EXISTS circuit_breaker_state (
    id BIGSERIAL PRIMARY KEY,
    service_name VARCHAR(255) NOT NULL UNIQUE,
    state VARCHAR(50) NOT NULL DEFAULT 'CLOSED',
    failure_count INTEGER NOT NULL DEFAULT 0,
    success_count INTEGER NOT NULL DEFAULT 0,
    last_failure_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0
);

-- Audit log table
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    correlation_id VARCHAR(255) NOT NULL,
    method VARCHAR(10) NOT NULL,
    path VARCHAR(500) NOT NULL,
    client_ip VARCHAR(50),
    user_id VARCHAR(255),
    status_code INTEGER,
    duration_ms BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_rate_limit_client ON rate_limit_config(client_id);
CREATE INDEX IF NOT EXISTS idx_route_path ON route_config(path_pattern);
CREATE INDEX IF NOT EXISTS idx_circuit_breaker_service ON circuit_breaker_state(service_name);
CREATE INDEX IF NOT EXISTS idx_audit_correlation ON audit_log(correlation_id);
CREATE INDEX IF NOT EXISTS idx_audit_created ON audit_log(created_at);

-- Insert default route configurations
INSERT INTO route_config (route_id, path_pattern, target_uri, enabled)
VALUES
    ('user-service', '/api/v1/users/**', 'http://user-service:8081', TRUE),
    ('order-service', '/api/v1/orders/**', 'http://order-service:8082', TRUE),
    ('product-service', '/api/v1/products/**', 'http://product-service:8083', TRUE)
ON CONFLICT (route_id) DO NOTHING;
