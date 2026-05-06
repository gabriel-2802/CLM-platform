-- =============================================================================
-- V4__add_audit_fields.sql
-- Add audit trail fields to the contract table for tracking creation, termination,
-- and signed document upload events along with the user IDs responsible for each.
-- =============================================================================

ALTER TABLE clm.contract
ADD COLUMN generated_at TIMESTAMP NULL,
ADD COLUMN generated_by_user_id INTEGER NULL,
ADD COLUMN terminated_at TIMESTAMP NULL,
ADD COLUMN terminated_by_user_id INTEGER NULL,
ADD COLUMN uploaded_signed_at TIMESTAMP NULL,
ADD COLUMN uploaded_signed_by_user_id INTEGER NULL;

-- Create indexes for audit field queries
CREATE INDEX idx_contract_generated_at ON clm.contract(generated_at);
CREATE INDEX idx_contract_terminated_at ON clm.contract(terminated_at);
CREATE INDEX idx_contract_uploaded_signed_at ON clm.contract(uploaded_signed_at);

