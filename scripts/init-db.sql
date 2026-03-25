-- Initialize CLM Platform database schemas
-- This script runs automatically when the PostgreSQL container starts

-- Create contracts schema (for Spring Boot microservice)
CREATE SCHEMA IF NOT EXISTS contracts;

-- Grant permissions to clm_user
GRANT ALL PRIVILEGES ON SCHEMA contracts TO clm_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA contracts TO clm_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA contracts TO clm_user;

-- Set search path to include both public and contracts schemas
ALTER DATABASE clm_platform SET search_path TO public, contracts;
