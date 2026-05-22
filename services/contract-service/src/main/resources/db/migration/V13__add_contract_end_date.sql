-- =============================================================================
-- V13__add_contract_end_date.sql
-- Add end_date to contract table, populated from most recent ContractDetails
-- =============================================================================

-- Add end_date column (initially nullable for migration)
ALTER TABLE clm.contract
ADD COLUMN end_date DATE;

-- Populate end_date with the end_date from the most recent ContractDetails for each contract
-- "Most recent" is determined by the MAX created_at timestamp
UPDATE clm.contract c
SET end_date = (
    SELECT cd.end_date
    FROM clm.contract_details cd
    WHERE cd.contract_id = c.document_id
    ORDER BY cd.created_at DESC
    LIMIT 1
)
WHERE EXISTS (
    SELECT 1
    FROM clm.contract_details cd
    WHERE cd.contract_id = c.document_id
);

-- For contracts with no contract_details (should not exist), use a default
UPDATE clm.contract
SET end_date = CURRENT_DATE
WHERE end_date IS NULL;

-- Make end_date NOT NULL (contracts must have an end_date)
ALTER TABLE clm.contract
ALTER COLUMN end_date SET NOT NULL;

-- Create index on end_date for query performance
CREATE INDEX idx_contract_end_date ON clm.contract (end_date);


