-- =============================================================================
-- V2__add_auto_renewal_field.sql
-- Add the auto_renewal boolean field to the contract table
-- =============================================================================

-- Add auto_renewal column to contract table with default value of false
ALTER TABLE clm.contract
ADD COLUMN auto_renew BOOLEAN NOT NULL DEFAULT FALSE;

-- Create index on auto_renewal for better query performance
CREATE INDEX idx_contract_auto_renewal ON clm.contract (auto_renewal)
WHERE auto_renewal = TRUE;

