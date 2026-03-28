-- ============================================================================
-- CLM Platform: Contracts Module Schema
-- Version: 1.0
-- Created: 2026-03-28
-- Purpose: Creates the complete schema for contract management system
-- ============================================================================

-- Create the contracts schema
CREATE SCHEMA IF NOT EXISTS contracts;

-- Set schema search path for this session
SET search_path TO contracts;

-- ============================================================================
-- TABLE: contract_template
-- Purpose: Stores contract blueprint templates with extracted fields
-- ============================================================================
CREATE TABLE IF NOT EXISTS contracts.contract_template (
    id BIGSERIAL PRIMARY KEY,

    -- Template identification
    template_name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),

    -- Document format (PDF, DOCX)
    document_format VARCHAR(10) NOT NULL,

    -- Binary template content (large - lazily loaded)
    document_content BYTEA,

    -- Field extraction metadata
    field_count INTEGER NOT NULL DEFAULT 0,
    is_fully_mapped BOOLEAN NOT NULL DEFAULT FALSE,

    -- Audit timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes on contract_template
CREATE INDEX idx_contract_template_name ON contracts.contract_template(template_name);
CREATE INDEX idx_contract_template_is_fully_mapped ON contracts.contract_template(is_fully_mapped);

-- ============================================================================
-- TABLE: template_field
-- Purpose: Represents individual placeholders extracted from templates
-- ============================================================================
CREATE TABLE IF NOT EXISTS contracts.template_field (
    id BIGSERIAL PRIMARY KEY,

    -- Foreign key to contract template
    template_id BIGINT NOT NULL REFERENCES contracts.contract_template(id) ON DELETE CASCADE,

    -- Field identification
    field_name VARCHAR(255) NOT NULL,
    field_label VARCHAR(255),

    -- Field metadata
    data_type VARCHAR(50) NOT NULL DEFAULT 'STRING',
    field_position INTEGER,
    page_number INTEGER,

    -- Field requirements and formatting
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    format_pattern VARCHAR(255),

    CONSTRAINT uk_template_field_name UNIQUE(template_id, field_name)
);

-- Create indexes on template_field
CREATE INDEX idx_template_field_template_position ON contracts.template_field(template_id, field_position);
CREATE INDEX idx_template_field_template_name ON contracts.template_field(template_id, field_name);
CREATE INDEX idx_template_field_data_type ON contracts.template_field(data_type);

-- ============================================================================
-- TABLE: field_mapping
-- Purpose: Maps template fields to source database columns with transformations
-- ============================================================================
CREATE TABLE IF NOT EXISTS contracts.field_mapping (
    id BIGSERIAL PRIMARY KEY,

    -- Foreign keys
    template_id BIGINT NOT NULL REFERENCES contracts.contract_template(id) ON DELETE CASCADE,
    template_field_id BIGINT NOT NULL REFERENCES contracts.template_field(id) ON DELETE CASCADE,

    -- Source data location
    source_table VARCHAR(100) NOT NULL,
    source_column VARCHAR(100) NOT NULL,

    -- Transformation rules
    data_transformation VARCHAR(255),
    mapping_status VARCHAR(50) NOT NULL DEFAULT 'MAPPED',

    -- Notes and metadata
    notes VARCHAR(500),

    -- Audit timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Ensure one mapping per field per template
    CONSTRAINT uk_field_mapping_template_field UNIQUE(template_id, template_field_id)
);

-- Create indexes on field_mapping
CREATE INDEX idx_field_mapping_template ON contracts.field_mapping(template_id, source_table, source_column);
CREATE INDEX idx_field_mapping_field ON contracts.field_mapping(template_field_id);
CREATE INDEX idx_field_mapping_status ON contracts.field_mapping(mapping_status);

-- ============================================================================
-- TABLE: generated_contract
-- Purpose: Represents individual contracts generated from templates
-- ============================================================================
CREATE TABLE IF NOT EXISTS contracts.generated_contract (
    id BIGSERIAL PRIMARY KEY,

    -- Foreign key to template
    template_id BIGINT NOT NULL REFERENCES contracts.contract_template(id) ON DELETE CASCADE,

    -- Client and user references (cross-schema - no FK constraint)
    client_id INTEGER NOT NULL,
    generated_by INTEGER,

    -- Contract status and lifecycle
    contract_status VARCHAR(50) NOT NULL DEFAULT 'GENERATED',

    -- Contract content (large - lazily loaded)
    document_content BYTEA,

    -- Contract business data
    contract_value NUMERIC(12, 2),
    contract_start_date DATE,
    contract_end_date DATE,

    -- Audit trail
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes on generated_contract
CREATE INDEX idx_generated_contract_template_client ON contracts.generated_contract(template_id, client_id);
CREATE INDEX idx_generated_contract_validity ON contracts.generated_contract(contract_start_date, contract_end_date);
CREATE INDEX idx_generated_contract_created_at ON contracts.generated_contract(created_at);
CREATE INDEX idx_generated_contract_status ON contracts.generated_contract(contract_status);
CREATE INDEX idx_generated_contract_client_id ON contracts.generated_contract(client_id);

-- ============================================================================
-- TABLE: contract_field_value
-- Purpose: Audit trail of field values inserted into generated contracts
-- ============================================================================
CREATE TABLE IF NOT EXISTS contracts.contract_field_value (
    id BIGSERIAL PRIMARY KEY,

    -- Foreign keys
    generated_contract_id BIGINT NOT NULL REFERENCES contracts.generated_contract(id) ON DELETE CASCADE,
    template_field_id BIGINT NOT NULL REFERENCES contracts.template_field(id) ON DELETE CASCADE,

    -- Field value tracking
    field_value TEXT NOT NULL,
    source_value VARCHAR(1000),
    transformation_applied VARCHAR(255),

    -- Audit timestamp
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes on contract_field_value
CREATE INDEX idx_contract_field_value_contract ON contracts.contract_field_value(generated_contract_id);
CREATE INDEX idx_contract_field_value_field ON contracts.contract_field_value(template_field_id);
CREATE INDEX idx_contract_field_value_created_at ON contracts.contract_field_value(created_at);

-- ============================================================================
-- CONSTRAINTS AND VALIDATION
-- ============================================================================

-- Ensure contract dates are logically ordered
ALTER TABLE contracts.generated_contract
ADD CONSTRAINT chk_contract_dates_order
CHECK (contract_start_date IS NULL OR contract_end_date IS NULL OR contract_start_date <= contract_end_date);

-- Ensure contract value is positive
ALTER TABLE contracts.generated_contract
ADD CONSTRAINT chk_contract_value_positive
CHECK (contract_value IS NULL OR contract_value > 0);

-- ============================================================================
-- COMMENTS AND DOCUMENTATION
-- ============================================================================

COMMENT ON SCHEMA contracts IS 'CLM Platform: Contract Lifecycle Management - Dedicated schema for contract templates, field mappings, and generated contracts';

COMMENT ON TABLE contracts.contract_template IS 'Blueprint templates for automated contract generation. Stores binary content and metadata about extracted fields.';
COMMENT ON COLUMN contracts.contract_template.document_format IS 'Format of the template document (PDF or DOCX)';
COMMENT ON COLUMN contracts.contract_template.document_content IS 'Binary content of the template - lazily loaded to prevent memory issues';
COMMENT ON COLUMN contracts.contract_template.is_fully_mapped IS 'Flag indicating if all extracted fields have corresponding mappings';

COMMENT ON TABLE contracts.template_field IS 'Individual placeholders/fields extracted from contract templates. Identified by position for documents where duplicate names appear.';
COMMENT ON COLUMN contracts.template_field.field_position IS 'Zero-based index of field occurrence in document (essential for DOCX)';
COMMENT ON COLUMN contracts.template_field.page_number IS 'Page index for PDF templates; usually null for DOCX';

COMMENT ON TABLE contracts.field_mapping IS 'Defines how template fields map to source database columns. Includes optional data transformations and validation rules.';
COMMENT ON COLUMN contracts.field_mapping.source_table IS 'Name of the source table (e.g., users, clients, punct_de_lucru)';
COMMENT ON COLUMN contracts.field_mapping.data_transformation IS 'Optional transformation to apply (UPPERCASE, FORMAT_DATE_DD_MM_YYYY, etc.)';

COMMENT ON TABLE contracts.generated_contract IS 'Individual contracts generated from templates. Stores client reference, status, and filled binary content.';
COMMENT ON COLUMN contracts.generated_contract.client_id IS 'Cross-schema reference to client in main application schema';
COMMENT ON COLUMN contracts.generated_contract.generated_by IS 'Cross-schema reference to user in main application schema';
COMMENT ON COLUMN contracts.generated_contract.document_content IS 'Filled/rendered contract document - lazily loaded';

COMMENT ON TABLE contracts.contract_field_value IS 'Audit trail of all field values inserted into contracts. Records source value, transformed value, and transformation applied.';

