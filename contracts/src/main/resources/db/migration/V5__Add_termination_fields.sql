-- V5__Add_termination_fields.sql
-- Add termination tracking columns to generated_contract table
-- Used to track contract termination dates and reasons

SET search_path TO contracts;

-- ============================================
-- ADD TERMINATION COLUMNS
-- ============================================

-- Add termination date column (nullable, for contracts that terminate)
ALTER TABLE contracts.generated_contract
ADD COLUMN termination_date DATE;

-- Add reasons for termination column (empty string by default)
ALTER TABLE contracts.generated_contract
ADD COLUMN reasons_for_termination VARCHAR(1000) DEFAULT '';

-- ============================================
-- COLUMN CONSTRAINTS
-- ============================================

-- Ensure termination_date is nullable (already is by default)
ALTER TABLE contracts.generated_contract
ALTER COLUMN termination_date DROP NOT NULL;

-- Ensure reasons_for_termination has NOT NULL constraint with default
ALTER TABLE contracts.generated_contract
ALTER COLUMN reasons_for_termination SET NOT NULL,
ALTER COLUMN reasons_for_termination SET DEFAULT '';

-- ============================================
-- INDEX FOR TERMINATION QUERIES
-- ============================================

-- Add index for termination status queries (find terminated contracts)
CREATE INDEX idx_generated_contract_termination_date
ON contracts.generated_contract(termination_date DESC)
WHERE termination_date IS NOT NULL;

-- ============================================
-- DOCUMENTATION
-- ============================================
/*
New columns:
  - termination_date: DATE (nullable)
    Purpose: Records when a contract was terminated
    Default: NULL (contract is active if NULL)

  - reasons_for_termination: VARCHAR(1000) (NOT NULL, default: '')
    Purpose: Stores the reason(s) why a contract was terminated
    Default: Empty string (no reason specified)

Use Cases:
  1. Contract Status = VOID: termination_date is set and reasons_for_termination is populated
  2. Contract Status = ARCHIVED: May have termination_date if contract ended due to expiry
  3. Finding recently terminated contracts: Query by termination_date DESC
  4. Contract lifecycle auditing: termination_date + reasons_for_termination provide full context

Migration Order:
  V1: Initial schema (contract_template, template_field, generated_contract, contract_field_value)
  V2: Fix fully_mapped column naming
  V3: Add fully_mapped trigger
  V4: Add generated_by_mail column
  V5: Add termination tracking fields (THIS MIGRATION)
*/

