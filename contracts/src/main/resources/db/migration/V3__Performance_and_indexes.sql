-- ============================================================================
-- CLM Platform: Contracts Module - Performance and Indexes
-- Version: 3.0
-- Created: 2026-03-29
-- Purpose: Adds additional indexes and performance optimizations
-- ============================================================================

SET search_path TO contracts;

-- ============================================================================
-- ADDITIONAL INDEXES FOR PERFORMANCE
-- ============================================================================

-- Index for contract_template lookups by template_name (already exists in V1)
-- Creating additional helpful indexes

-- Index for searching templates by format type
CREATE INDEX IF NOT EXISTS idx_contract_template_format
ON contracts.contract_template(document_format);

-- Index for created_at (useful for sorting/filtering by date)
CREATE INDEX IF NOT EXISTS idx_contract_template_created_at
ON contracts.contract_template(created_at DESC);

-- Composite index for common queries
CREATE INDEX IF NOT EXISTS idx_contract_template_mapped_created
ON contracts.contract_template(is_fully_mapped, created_at DESC);

-- ============================================================================
-- INDEXES FOR TEMPLATE_FIELD
-- ============================================================================

-- Index for field data type lookups
CREATE INDEX IF NOT EXISTS idx_template_field_field_position
ON contracts.template_field(field_position)
WHERE field_position IS NOT NULL;

-- Index for required fields
CREATE INDEX IF NOT EXISTS idx_template_field_is_required
ON contracts.template_field(is_required);

-- Composite index for common join patterns
CREATE INDEX IF NOT EXISTS idx_template_field_template_datatype
ON contracts.template_field(template_id, data_type);

-- ============================================================================
-- INDEXES FOR FIELD_MAPPING
-- ============================================================================

-- Index for mapping status
CREATE INDEX IF NOT EXISTS idx_field_mapping_mapping_status
ON contracts.field_mapping(mapping_status);

-- Index for source location lookups
CREATE INDEX IF NOT EXISTS idx_field_mapping_source
ON contracts.field_mapping(source_table, source_column);

-- Composite index for template and status
CREATE INDEX IF NOT EXISTS idx_field_mapping_template_status
ON contracts.field_mapping(template_id, mapping_status);

-- ============================================================================
-- INDEXES FOR GENERATED_CONTRACT
-- ============================================================================

-- Index for contract status
CREATE INDEX IF NOT EXISTS idx_generated_contract_status_created
ON contracts.generated_contract(contract_status, created_at DESC);

-- Index for date range queries
CREATE INDEX IF NOT EXISTS idx_generated_contract_date_range
ON contracts.generated_contract(contract_start_date, contract_end_date);

-- Index for generated_by lookups (who created the contract)
CREATE INDEX IF NOT EXISTS idx_generated_contract_generated_by
ON contracts.generated_contract(generated_by)
WHERE generated_by IS NOT NULL;

-- Composite index for common queries
CREATE INDEX IF NOT EXISTS idx_generated_contract_template_status_date
ON contracts.generated_contract(template_id, contract_status, created_at DESC);

-- ============================================================================
-- INDEXES FOR CONTRACT_FIELD_VALUE
-- ============================================================================

-- Index for looking up values in a contract
CREATE INDEX IF NOT EXISTS idx_contract_field_value_contract_field
ON contracts.contract_field_value(generated_contract_id, template_field_id);

-- Index for field value lookups
CREATE INDEX IF NOT EXISTS idx_contract_field_value_field_value
ON contracts.contract_field_value(template_field_id, field_value);

-- ============================================================================
-- CREATE PARTITIONING STRATEGY (for future large datasets)
-- ============================================================================

-- Add comments explaining potential partitioning strategy
COMMENT ON TABLE contracts.generated_contract IS
'Individual contracts generated from templates. Stores client reference, status, and filled binary content.
FUTURE: Consider partitioning by created_at (monthly) for large datasets.
Example: PARTITION BY RANGE (created_at) ...';

-- ============================================================================
-- CREATE MATERIALIZED VIEWS FOR REPORTING
-- ============================================================================

-- Drop existing views if they exist
DROP VIEW IF EXISTS contracts.v_template_mapping_status CASCADE;
DROP VIEW IF EXISTS contracts.v_generated_contract_summary CASCADE;

-- View: Template Field Mapping Status
CREATE VIEW contracts.v_template_mapping_status AS
SELECT
    t.id as template_id,
    t.template_name,
    t.document_format,
    COUNT(DISTINCT tf.id) as total_fields,
    COUNT(DISTINCT fm.id) as mapped_fields,
    COUNT(DISTINCT tf.id) - COUNT(DISTINCT fm.id) as unmapped_fields,
    ROUND(
        COUNT(DISTINCT fm.id)::NUMERIC / NULLIF(COUNT(DISTINCT tf.id), 0) * 100, 2
    ) as mapping_percentage,
    t.is_fully_mapped,
    t.created_at,
    t.updated_at
FROM contracts.contract_template t
LEFT JOIN contracts.template_field tf ON t.id = tf.template_id
LEFT JOIN contracts.field_mapping fm ON tf.template_id = fm.template_id
    AND tf.id = fm.template_field_id
GROUP BY t.id, t.template_name, t.document_format, t.is_fully_mapped, t.created_at, t.updated_at;

COMMENT ON VIEW contracts.v_template_mapping_status IS
'Summary view showing template mapping completion percentage and status';

-- View: Generated Contract Summary
CREATE VIEW contracts.v_generated_contract_summary AS
SELECT
    gc.id as contract_id,
    gc.template_id,
    t.template_name,
    gc.client_id,
    gc.generated_by,
    gc.contract_status,
    gc.contract_value,
    gc.contract_start_date,
    gc.contract_end_date,
    CASE
        WHEN gc.contract_end_date < CURRENT_DATE THEN 'EXPIRED'
        WHEN gc.contract_start_date > CURRENT_DATE THEN 'FUTURE'
        WHEN gc.contract_status = 'SIGNED' THEN 'ACTIVE'
        ELSE 'INACTIVE'
    END as contract_status_calculated,
    (gc.contract_end_date - gc.contract_start_date)::INTEGER as contract_duration_days,
    gc.created_at,
    COUNT(DISTINCT cfv.id) as field_values_count
FROM contracts.generated_contract gc
LEFT JOIN contracts.contract_template t ON gc.template_id = t.id
LEFT JOIN contracts.contract_field_value cfv ON gc.id = cfv.generated_contract_id
GROUP BY
    gc.id, gc.template_id, t.template_name, gc.client_id, gc.generated_by,
    gc.contract_status, gc.contract_value, gc.contract_start_date,
    gc.contract_end_date, gc.created_at;

COMMENT ON VIEW contracts.v_generated_contract_summary IS
'Summary view showing generated contracts with calculated status and duration';

-- ============================================================================
-- CREATE UTILITY FUNCTIONS FOR COMMON OPERATIONS
-- ============================================================================

-- Function to check if a template is ready for use
CREATE OR REPLACE FUNCTION contracts.is_template_ready_for_use(p_template_id BIGINT)
RETURNS BOOLEAN AS $$
DECLARE
    v_total_fields INTEGER;
    v_mapped_fields INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_total_fields
    FROM contracts.template_field
    WHERE template_id = p_template_id;

    IF v_total_fields = 0 THEN
        RETURN FALSE; -- No fields to map
    END IF;

    SELECT COUNT(*) INTO v_mapped_fields
    FROM contracts.field_mapping
    WHERE template_id = p_template_id;

    RETURN v_mapped_fields = v_total_fields;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

COMMENT ON FUNCTION contracts.is_template_ready_for_use(BIGINT) IS
'Checks if a template has all its fields mapped and is ready for use';

-- ============================================================================
-- STATISTICS AND ANALYSIS
-- ============================================================================

-- Analyze all tables in the contracts schema
ANALYZE contracts.contract_template;
ANALYZE contracts.template_field;
ANALYZE contracts.field_mapping;
ANALYZE contracts.generated_contract;
ANALYZE contracts.contract_field_value;

