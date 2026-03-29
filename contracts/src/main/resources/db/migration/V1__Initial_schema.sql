-- V1__Initial_schema.sql
-- Create core schema for contract management system
-- 4 primary tables: contract_template, template_field, generated_contract, contract_field_value

-- Set schema search path first before any operations
SET search_path TO contracts;

-- ============================================
-- CREATE ENUM TYPES
-- ============================================

CREATE TYPE contracts.document_format_enum AS ENUM ('PDF', 'DOCX');

CREATE TYPE contracts.data_type_enum AS ENUM ('STRING', 'DATE', 'NUMBER', 'BOOLEAN', 'CURRENCY', 'ENUM');

CREATE TYPE contracts.contract_status_enum AS ENUM ('GENERATED', 'SIGNED', 'ARCHIVED', 'VOID');

-- ============================================
-- TABLE 1: contract_template
-- Blueprint for contract documents
-- ============================================

CREATE TABLE contracts.contract_template (
    id BIGSERIAL PRIMARY KEY,
    template_name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    document_format contracts.document_format_enum NOT NULL,
    document_content BYTEA NOT NULL,
    field_count INTEGER NOT NULL DEFAULT 0,
    fully_mapped BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_contract_template_name ON contracts.contract_template(template_name);
CREATE INDEX idx_contract_template_created ON contracts.contract_template(created_at DESC);

-- ============================================
-- TABLE 2: template_field
-- Placeholders extracted from templates
-- ============================================

CREATE TABLE contracts.template_field (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES contracts.contract_template(id) ON DELETE CASCADE ON UPDATE CASCADE,
    field_label VARCHAR(255),
    data_type contracts.data_type_enum NOT NULL DEFAULT 'STRING',
    placeholder_text VARCHAR(255),
    field_position INTEGER,
    is_required BOOLEAN NOT NULL DEFAULT true,
    format_pattern VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_template_field_template_position ON contracts.template_field(template_id, field_position);
CREATE INDEX idx_template_field_template_name ON contracts.template_field(template_id, field_label);
CREATE INDEX idx_template_field_template ON contracts.template_field(template_id);

-- ============================================
-- TABLE 3: generated_contract
-- Final filled documents for clients
-- ============================================

CREATE TABLE contracts.generated_contract (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES contracts.contract_template(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    client_id INTEGER NOT NULL,
    contract_status contracts.contract_status_enum NOT NULL DEFAULT 'GENERATED',
    generated_by INTEGER,
    document_content BYTEA NOT NULL,
    contract_value NUMERIC(12, 2),
    contract_start_date DATE,
    contract_end_date DATE,
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_generated_contract_template_client ON contracts.generated_contract(template_id, client_id);
CREATE INDEX idx_generated_contract_validity ON contracts.generated_contract(contract_start_date, contract_end_date);
CREATE INDEX idx_generated_contract_created_at ON contracts.generated_contract(created_at DESC);
CREATE INDEX idx_generated_contract_template ON contracts.generated_contract(template_id);
CREATE INDEX idx_generated_contract_client ON contracts.generated_contract(client_id);
CREATE INDEX idx_generated_contract_status ON contracts.generated_contract(contract_status);

-- ============================================
-- TABLE 4: contract_field_value
-- Audit trail for field values injected into contracts
-- ============================================

CREATE TABLE contracts.contract_field_value (
    id BIGSERIAL PRIMARY KEY,
    generated_contract_id BIGINT NOT NULL REFERENCES contracts.generated_contract(id) ON DELETE CASCADE ON UPDATE CASCADE,
    template_field_id BIGINT NOT NULL REFERENCES contracts.template_field(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    field_value TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_contract_field_value_contract ON contracts.contract_field_value(generated_contract_id);
CREATE INDEX idx_contract_field_value_field ON contracts.contract_field_value(template_field_id);
CREATE INDEX idx_contract_field_value_contract_field ON contracts.contract_field_value(generated_contract_id, template_field_id);

-- ============================================
-- SET UP UPDATE TIMESTAMP TRIGGER (for updated_at)
-- ============================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER contract_template_updated_at_trigger
    BEFORE UPDATE ON contracts.contract_template
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

