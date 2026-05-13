-- =============================================================================
-- V7__drop_document_created_at.sql
-- created_at is redundant with generated_at on the document table.
-- Drop the column and its index.
-- =============================================================================

DROP INDEX IF EXISTS clm.idx_document_created_at;

ALTER TABLE clm.document
    DROP COLUMN IF EXISTS created_at;
