-- =============================================================================
-- V3__add_contract_balance.sql
-- Add the contract_balance numeric field to the contract table
-- =============================================================================

ALTER TABLE clm.contract
ADD COLUMN contract_balance NUMERIC(12, 2) NOT NULL DEFAULT 0;