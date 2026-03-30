-- V7__Update_fully_mapped_trigger_for_required_fields.sql
-- Update trigger function and index to only check REQUIRED fields
-- This ensures the is_fully_mapped status only considers mandatory fields

SET search_path TO contracts;

-- ============================================
-- DROP existing index (to be recreated with new definition)
-- ============================================
DROP INDEX IF EXISTS contracts.idx_template_field_label_null;

-- ============================================
-- REPLACE TRIGGER FUNCTION: Update template's fully_mapped status
-- ============================================
-- Updated to check only REQUIRED fields (is_required = true)
-- Optional fields (is_required = false) are now ignored in the fully_mapped check

CREATE OR REPLACE FUNCTION update_template_fully_mapped_status()
RETURNS TRIGGER AS $$
DECLARE
    template_id BIGINT;
    all_mapped BOOLEAN;
BEGIN
    -- Get the template ID from the affected field
    template_id := NEW.template_id;

    -- Check if all REQUIRED fields for this template now have a field_label
    -- Return true if no required fields exist (empty template) OR all required fields have labels
    all_mapped := NOT EXISTS (
        SELECT 1 FROM contracts.template_field
        WHERE template_id = template_id
        AND is_required = true
        AND field_label IS NULL
    );

    -- Update the template's fully_mapped status
    UPDATE contracts.contract_template
    SET is_fully_mapped = all_mapped,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = template_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- CREATE OPTIMIZED INDEX
-- ============================================
-- New index optimized for checking required fields with null labels
-- Composite index on (template_id, is_required) with WHERE clause for better performance

CREATE INDEX IF NOT EXISTS idx_template_field_label_null
ON contracts.template_field(template_id, is_required)
WHERE field_label IS NULL AND is_required = true;

