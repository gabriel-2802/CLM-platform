-- ============================================================================
-- CLM Platform: Contracts Module - Validation Constraints
-- Version: 2.0
-- Created: 2026-03-29
-- Purpose: Adds additional validation and integrity constraints
-- ============================================================================

SET search_path TO contracts;

-- ============================================================================
-- VALIDATE TABLE STRUCTURE (Idempotent checks)
-- ============================================================================

-- Check contract_template table exists and has required columns
DO $$
BEGIN
    -- Verify document_content is bytea type
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'contracts'
        AND table_name = 'contract_template'
        AND column_name = 'document_content'
        AND udt_name != 'bytea'
    ) THEN
        ALTER TABLE contracts.contract_template
        ALTER COLUMN document_content TYPE BYTEA USING document_content::BYTEA;
    END IF;

    -- Add NOT NULL constraint to created_at if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'contracts'
        AND table_name = 'contract_template'
        AND column_name = 'created_at'
        AND is_nullable = 'NO'
    ) THEN
        ALTER TABLE contracts.contract_template
        ALTER COLUMN created_at SET NOT NULL;
    END IF;

    -- Add NOT NULL constraint to updated_at if missing
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'contracts'
        AND table_name = 'contract_template'
        AND column_name = 'updated_at'
        AND is_nullable = 'NO'
    ) THEN
        ALTER TABLE contracts.contract_template
        ALTER COLUMN updated_at SET NOT NULL;
    END IF;
END
$$;

-- ============================================================================
-- ADD VALIDATION TRIGGERS
-- ============================================================================

-- Create trigger function for contract_template validation
CREATE OR REPLACE FUNCTION contracts.validate_contract_template()
RETURNS TRIGGER AS $$
BEGIN
    -- Validate template_name is not empty
    IF TRIM(NEW.template_name) = '' THEN
        RAISE EXCEPTION 'template_name cannot be empty';
    END IF;

    -- Validate document_format is valid
    IF NEW.document_format NOT IN ('PDF', 'DOCX', 'WORD') THEN
        RAISE EXCEPTION 'document_format must be PDF or DOCX, got: %', NEW.document_format;
    END IF;

    -- Validate field_count is not negative
    IF NEW.field_count < 0 THEN
        RAISE EXCEPTION 'field_count cannot be negative';
    END IF;

    -- Validate is_fully_mapped is boolean
    IF NEW.is_fully_mapped IS NOT NULL AND
       NEW.is_fully_mapped NOT IN (TRUE, FALSE) THEN
        RAISE EXCEPTION 'is_fully_mapped must be TRUE or FALSE';
    END IF;

    -- Update timestamp
    NEW.updated_at = CURRENT_TIMESTAMP;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Drop trigger if it exists to allow recreating it
DROP TRIGGER IF EXISTS trg_validate_contract_template ON contracts.contract_template;

-- Create trigger for insert and update
CREATE TRIGGER trg_validate_contract_template
BEFORE INSERT OR UPDATE ON contracts.contract_template
FOR EACH ROW
EXECUTE FUNCTION contracts.validate_contract_template();

-- ============================================================================
-- CREATE VALIDATION TRIGGER FOR TEMPLATE_FIELD
-- ============================================================================

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

DROP TRIGGER IF EXISTS trg_validate_template_field ON contracts.template_field;

CREATE TRIGGER trg_validate_template_field
BEFORE INSERT OR UPDATE ON contracts.template_field
FOR EACH ROW
EXECUTE FUNCTION contracts.validate_template_field();

-- ============================================================================
-- CREATE VALIDATION TRIGGER FOR GENERATED_CONTRACT
-- ============================================================================

CREATE OR REPLACE FUNCTION contracts.validate_generated_contract()
RETURNS TRIGGER AS $$
BEGIN
    -- Validate contract_status is valid
    IF NEW.contract_status NOT IN ('GENERATED', 'SENT', 'SIGNED', 'EXECUTED', 'ARCHIVED', 'CANCELLED') THEN
        RAISE EXCEPTION 'contract_status must be one of: GENERATED, SENT, SIGNED, EXECUTED, ARCHIVED, CANCELLED';
    END IF;

    -- Validate contract_value is not negative
    IF NEW.contract_value IS NOT NULL AND NEW.contract_value < 0 THEN
        RAISE EXCEPTION 'contract_value cannot be negative';
    END IF;

    -- Validate date range
    IF NEW.contract_start_date IS NOT NULL AND
       NEW.contract_end_date IS NOT NULL AND
       NEW.contract_start_date > NEW.contract_end_date THEN
        RAISE EXCEPTION 'contract_start_date must be before or equal to contract_end_date';
    END IF;

    -- Validate client_id is positive
    IF NEW.client_id <= 0 THEN
        RAISE EXCEPTION 'client_id must be positive';
    END IF;

    -- Validate generated_by is positive if provided
    IF NEW.generated_by IS NOT NULL AND NEW.generated_by <= 0 THEN
        RAISE EXCEPTION 'generated_by must be positive if provided';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_validate_generated_contract ON contracts.generated_contract;

CREATE TRIGGER trg_validate_generated_contract
BEFORE INSERT OR UPDATE ON contracts.generated_contract
FOR EACH ROW
EXECUTE FUNCTION contracts.validate_generated_contract();

-- ============================================================================
-- CREATE VALIDATION TRIGGER FOR FIELD_MAPPING
-- ============================================================================

CREATE OR REPLACE FUNCTION contracts.validate_field_mapping()
RETURNS TRIGGER AS $$
BEGIN
    -- Validate source_table is not empty
    IF TRIM(NEW.source_table) = '' THEN
        RAISE EXCEPTION 'source_table cannot be empty';
    END IF;

    -- Validate source_column is not empty
    IF TRIM(NEW.source_column) = '' THEN
        RAISE EXCEPTION 'source_column cannot be empty';
    END IF;

    -- Validate mapping_status is valid
    IF NEW.mapping_status NOT IN ('MAPPED', 'UNMAPPED', 'PENDING', 'ERROR') THEN
        RAISE EXCEPTION 'mapping_status must be one of: MAPPED, UNMAPPED, PENDING, ERROR';
    END IF;

    -- Update timestamp
    NEW.updated_at = CURRENT_TIMESTAMP;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_validate_field_mapping ON contracts.field_mapping;

CREATE TRIGGER trg_validate_field_mapping
BEFORE INSERT OR UPDATE ON contracts.field_mapping
FOR EACH ROW
EXECUTE FUNCTION contracts.validate_field_mapping();

-- ============================================================================
-- ADD CHECK CONSTRAINTS
-- ============================================================================

-- Add check constraints that were in the original script
DO $$
BEGIN
    -- Check for contract date order constraint
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_schema = 'contracts'
        AND table_name = 'generated_contract'
        AND constraint_name = 'chk_contract_dates_order'
    ) THEN
        ALTER TABLE contracts.generated_contract
        ADD CONSTRAINT chk_contract_dates_order
        CHECK (contract_start_date IS NULL OR contract_end_date IS NULL OR contract_start_date <= contract_end_date);
    END IF;

    -- Check for contract value positive constraint
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_schema = 'contracts'
        AND table_name = 'generated_contract'
        AND constraint_name = 'chk_contract_value_positive'
    ) THEN
        ALTER TABLE contracts.generated_contract
        ADD CONSTRAINT chk_contract_value_positive
        CHECK (contract_value IS NULL OR contract_value > 0);
    END IF;
END
$$;

-- ============================================================================
-- UPDATE COMMENTS AND DOCUMENTATION
-- ============================================================================

COMMENT ON FUNCTION contracts.validate_contract_template() IS 'Validates contract template data before insert/update. Ensures template_name, document_format, and field counts are valid.';
COMMENT ON FUNCTION contracts.validate_template_field() IS 'Validates template field data. Ensures field_name, data_type, and positions are valid.';
COMMENT ON FUNCTION contracts.validate_generated_contract() IS 'Validates generated contract data. Ensures status, dates, values, and references are valid.';
COMMENT ON FUNCTION contracts.validate_field_mapping() IS 'Validates field mapping data. Ensures source references and mapping status are valid.';

