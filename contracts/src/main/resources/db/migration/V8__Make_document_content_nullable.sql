-- Migration: Make document_content column nullable
-- Description: Allows contracts to be created without document content initially.
--              Document content can be generated/populated asynchronously.
-- Date: 2026-03-30

ALTER TABLE contracts.generated_contract
    ALTER COLUMN document_content DROP NOT NULL;

-- Add comment explaining the change
COMMENT ON COLUMN contracts.generated_contract.document_content IS
'The final filled document in BYTEA format. Nullable to allow asynchronous document generation.';

