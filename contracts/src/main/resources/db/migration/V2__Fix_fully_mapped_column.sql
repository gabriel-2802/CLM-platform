-- V2__Fix_fully_mapped_column.sql
-- Rename fully_mapped to is_fully_mapped to match the entity model

SET search_path TO contracts;

-- Rename the column to match the JPA entity field name (isFullyMapped -> is_fully_mapped)
ALTER TABLE contract_template
RENAME COLUMN fully_mapped TO is_fully_mapped;

-- Ensure the column has the correct constraints
ALTER TABLE contract_template
ALTER COLUMN is_fully_mapped SET NOT NULL,
ALTER COLUMN is_fully_mapped SET DEFAULT false;

