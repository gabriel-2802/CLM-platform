-- =============================================================================
-- V11__drop_auto_renew_field.sql
-- Remove the auto_renew boolean field from the contract table
-- =============================================================================

-- Drop the index on auto_renew
DROP INDEX IF EXISTS clm.idx_contract_auto_renew;

-- Drop auto_renew column from contract table
ALTER TABLE clm.contract
DROP COLUMN IF EXISTS auto_renew;

