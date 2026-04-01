-- =============================================================================
-- V5__remove_placeholder_text_column.sql
-- Removes the placeholder_text column from template_field table.
--
-- Rationale: placeholder_text is no longer needed as placeholders are now
-- tracked internally and normalized to 4 dots via regex pattern matching.
-- =============================================================================

ALTER TABLE contracts.template_field
DROP COLUMN placeholder_text;

