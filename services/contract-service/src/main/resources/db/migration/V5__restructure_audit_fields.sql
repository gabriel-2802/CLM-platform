-- =============================================================================
-- V5__restructure_audit_fields.sql
-- Move generated/uploaded-signed audit fields from contract to document (base).
-- Drop generated_by / generated_by_mail from document.
-- Add modified_at / modified_by_user_id to contract for terms-update audit.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Add new audit columns to the document base table
-- -----------------------------------------------------------------------------
ALTER TABLE clm.document
    ADD COLUMN generated_at           TIMESTAMP NULL,
    ADD COLUMN generated_by_user      INTEGER   NULL,
    ADD COLUMN uploaded_signed_at     TIMESTAMP NULL,
    ADD COLUMN uploaded_signed_by_user INTEGER  NULL;

-- -----------------------------------------------------------------------------
-- 2. Migrate existing data from contract → document
-- -----------------------------------------------------------------------------
UPDATE clm.document d
SET generated_at            = c.generated_at,
    generated_by_user       = c.generated_by_user_id,
    uploaded_signed_at      = c.uploaded_signed_at,
    uploaded_signed_by_user = c.uploaded_signed_by_user_id
FROM clm.contract c
WHERE c.document_id = d.id;

-- -----------------------------------------------------------------------------
-- 3. Drop old audit columns from contract (moved to document)
-- -----------------------------------------------------------------------------
DROP INDEX IF EXISTS clm.idx_contract_generated_at;
DROP INDEX IF EXISTS clm.idx_contract_uploaded_signed_at;

ALTER TABLE clm.contract
    DROP COLUMN IF EXISTS generated_at,
    DROP COLUMN IF EXISTS generated_by_user_id,
    DROP COLUMN IF EXISTS uploaded_signed_at,
    DROP COLUMN IF EXISTS uploaded_signed_by_user_id;

-- -----------------------------------------------------------------------------
-- 4. Drop stale generated_by / generated_by_mail from document
-- -----------------------------------------------------------------------------
ALTER TABLE clm.document
    DROP COLUMN IF EXISTS generated_by,
    DROP COLUMN IF EXISTS generated_by_mail;

-- -----------------------------------------------------------------------------
-- 5. Add terms-update audit columns to contract
-- -----------------------------------------------------------------------------
ALTER TABLE clm.contract
    ADD COLUMN modified_at          TIMESTAMP NULL,
    ADD COLUMN modified_by_user_id  INTEGER   NULL;

-- -----------------------------------------------------------------------------
-- 6. Indexes for new document audit columns
-- -----------------------------------------------------------------------------
CREATE INDEX idx_document_generated_at       ON clm.document (generated_at);
CREATE INDEX idx_document_uploaded_signed_at ON clm.document (uploaded_signed_at);
CREATE INDEX idx_contract_modified_at        ON clm.contract (modified_at);
