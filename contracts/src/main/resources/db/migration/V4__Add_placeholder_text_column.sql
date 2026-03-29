-- ============================================================================
-- CLM Platform: Contracts Module - Add missing placeholder_text column
-- Version: 4.0
-- Created: 2026-03-29
-- Purpose: Adds placeholder_text column to template_field table
-- ============================================================================

SET search_path TO contracts;

-- ============================================================================
-- ADD MISSING COLUMN TO TEMPLATE_FIELD
-- ============================================================================

-- Add placeholder_text column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'contracts'
        AND table_name = 'template_field'
        AND column_name = 'placeholder_text'
    ) THEN
        ALTER TABLE contracts.template_field
        ADD COLUMN placeholder_text VARCHAR(255);

        -- Add a comment explaining the column
        COMMENT ON COLUMN contracts.template_field.placeholder_text IS
        'The actual placeholder text captured from the document (e.g., "......" or similar pattern)';
    END IF;
END
$$;

-- ============================================================================
-- UPDATE VALIDATION TRIGGER FOR TEMPLATE_FIELD IF NEEDED
-- ============================================================================

-- Re-create or update the validation trigger to handle the new column
CREATE OR REPLACE FUNCTION contracts.validate_template_field()
RETURNS TRIGGER AS $$
BEGIN
    -- Validate field_name is not empty
    IF TRIM(NEW.field_name) = '' THEN
        RAISE EXCEPTION 'field_name cannot be empty';
    END IF;

    -- Validate data_type is valid
    IF NEW.data_type NOT IN ('STRING', 'INTEGER', 'DATE', 'DECIMAL', 'BOOLEAN', 'TEXT') THEN
        RAISE EXCEPTION 'data_type must be one of: STRING, INTEGER, DATE, DECIMAL, BOOLEAN, TEXT';
    END IF;

    -- Validate field_position is not negative
    IF NEW.field_position IS NOT NULL AND NEW.field_position < 0 THEN
        RAISE EXCEPTION 'field_position cannot be negative';
    END IF;

    -- Validate page_number is not negative
    IF NEW.page_number IS NOT NULL AND NEW.page_number < 0 THEN
        RAISE EXCEPTION 'page_number cannot be negative';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- ENSURE INDEXES ARE CREATED
-- ============================================================================

-- Create index on placeholder_text if it doesn't exist (for searching by pattern)
CREATE INDEX IF NOT EXISTS idx_template_field_placeholder_text
ON contracts.template_field(placeholder_text)
WHERE placeholder_text IS NOT NULL;

