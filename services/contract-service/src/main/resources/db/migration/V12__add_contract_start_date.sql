-- =============================================================================
-- V12__add_contract_start_date.sql
-- Add immutable start_date to contract table, populated from earliest ContractDetails
-- =============================================================================

-- Add start_date column (initially nullable for migration)
ALTER TABLE clm.contract
ADD COLUMN start_date DATE;

-- Populate start_date with the earliest start_date from contract_details for each contract
UPDATE clm.contract c
SET start_date = (
    SELECT MIN(cd.start_date)
    FROM clm.contract_details cd
    WHERE cd.contract_id = c.document_id
)
WHERE EXISTS (
    SELECT 1
    FROM clm.contract_details cd
    WHERE cd.contract_id = c.document_id
);

-- For contracts with no contract_details (should not exist), use a default
UPDATE clm.contract
SET start_date = CURRENT_DATE
WHERE start_date IS NULL;

-- Make start_date NOT NULL (contracts must have a start_date)
ALTER TABLE clm.contract
ALTER COLUMN start_date SET NOT NULL;

-- Create index on start_date for query performance
CREATE INDEX idx_contract_start_date ON clm.contract (start_date);

