-- ============================================================================
-- CLM Platform: Contracts Module - Remove page_number column
-- Version: 5.0
-- Created: 2026-03-29
-- Purpose: Removes the page_number column from template_field table
-- ============================================================================

SET search_path TO contracts;

-- ============================================================================
-- REMOVE PAGE_NUMBER COLUMN
-- ============================================================================

-- Drop the page_number column if it exists
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'contracts'
        AND table_name = 'template_field'
        AND column_name = 'page_number'
    ) THEN
        ALTER TABLE contracts.template_field
        DROP COLUMN page_number;
    END IF;
END
$$;

-- ============================================================================
-- UPDATE VALIDATION TRIGGER
-- ============================================================================

-- Re-create the validation trigger without page_number validation
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

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

