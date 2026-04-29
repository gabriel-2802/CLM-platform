-- Fix cifra_afaceri column type from DOUBLE PRECISION to NUMERIC(19,2)
-- This aligns the database schema with the Hibernate entity definition

ALTER TABLE clients.istorice
ALTER COLUMN cifra_afaceri TYPE NUMERIC(19, 2);

