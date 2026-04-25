-- =============================================================================
-- V2__add_auto_renewal_field.sql
-- Add the auto_renew boolean field to the contract table
-- =============================================================================

-- Add auto_renew column to contract table with default value of false
ALTER TABLE clm.contract
ADD COLUMN auto_renew BOOLEAN NOT NULL DEFAULT FALSE;

-- Create index on auto_renew for better query performance
CREATE INDEX idx_contract_auto_renew ON clm.contract (auto_renew)
WHERE auto_renew = TRUE;

