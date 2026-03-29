-- V0__Reset.sql
-- Fresh start: drop all existing schemas and objects, then recreate contracts schema

DROP SCHEMA IF EXISTS contracts CASCADE;

CREATE SCHEMA contracts;

-- Ensure the schema exists before proceeding
-- (This helps with any transaction isolation issues)
GRANT USAGE ON SCHEMA contracts TO PUBLIC;
