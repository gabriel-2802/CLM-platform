-- V6__Update_contract_status_enum_and_add_signed_document.sql
-- Update contract status enum values and add signed document column
-- Changes status enum from (GENERATED, SIGNED, ARCHIVED, VOID) to (PENDING_SIGNATURE, ACTIVE, TERMINATED, ARCHIVED)
-- Adds signed_document_content column to store the signed version of contracts

SET search_path TO contracts;

-- ============================================
-- DROP OLD ENUM AND CREATE NEW ONE
-- ============================================

-- First, remove the DEFAULT constraint that depends on the old enum
ALTER TABLE contracts.generated_contract
ALTER COLUMN contract_status DROP DEFAULT;

-- Now convert the column to text so we can drop the enum
ALTER TABLE contracts.generated_contract
ALTER COLUMN contract_status TYPE VARCHAR(50);

-- Now we can safely drop the old enum type
DROP TYPE IF EXISTS contracts.contract_status_enum;

-- Create new enum type with updated values
CREATE TYPE contracts.contract_status_enum AS ENUM (
    'PENDING_SIGNATURE',
    'ACTIVE',
    'TERMINATED',
    'ARCHIVED'
);

-- Change the column back to the new enum type
ALTER TABLE contracts.generated_contract
ALTER COLUMN contract_status TYPE contracts.contract_status_enum USING contract_status::contracts.contract_status_enum;

-- Add DEFAULT constraint back with new default value
ALTER TABLE contracts.generated_contract
ALTER COLUMN contract_status SET DEFAULT 'PENDING_SIGNATURE'::contracts.contract_status_enum;

-- ============================================
-- MIGRATION OF EXISTING DATA
-- ============================================
-- Map old statuses to new ones:
-- GENERATED -> PENDING_SIGNATURE
-- SIGNED -> ACTIVE
-- VOID -> TERMINATED
-- ARCHIVED -> ARCHIVED (no change)

UPDATE contracts.generated_contract
SET contract_status = 'PENDING_SIGNATURE'::contracts.contract_status_enum
WHERE contract_status::text = 'GENERATED';

UPDATE contracts.generated_contract
SET contract_status = 'ACTIVE'::contracts.contract_status_enum
WHERE contract_status::text = 'SIGNED';

UPDATE contracts.generated_contract
SET contract_status = 'TERMINATED'::contracts.contract_status_enum
WHERE contract_status::text = 'VOID';

-- ============================================
-- ADD SIGNED DOCUMENT COLUMN
-- ============================================

-- Add column for storing the signed version of the document
ALTER TABLE contracts.generated_contract
ADD COLUMN signed_document_content BYTEA;

-- Create index for finding contracts with signed documents
CREATE INDEX idx_generated_contract_signed
ON contracts.generated_contract(contract_status)
WHERE contract_status = 'ACTIVE';

-- ============================================
-- DOCUMENTATION
-- ============================================
/*
Changes made in this migration:

1. CONTRACT STATUS ENUM UPDATE:
   Old values: GENERATED, SIGNED, ARCHIVED, VOID
   New values: PENDING_SIGNATURE, ACTIVE, TERMINATED, ARCHIVED

   Mapping:
   - GENERATED -> PENDING_SIGNATURE (contract awaiting signature)
   - SIGNED -> ACTIVE (contract signed and in effect)
   - VOID -> TERMINATED (contract terminated early)
   - ARCHIVED -> ARCHIVED (no change)

2. NEW COLUMN:
   - signed_document_content: BYTEA (nullable)
     Purpose: Store the digitally signed version of the contract
     When set: After contract is signed (status changes to ACTIVE)
     Relationship: Paired with status = ACTIVE

3. INDEX:
   - idx_generated_contract_signed: For finding active signed contracts

Status Workflow:
  1. Contract generated -> PENDING_SIGNATURE (unsigned)
  2. Client signs -> ACTIVE (signed, signed_document_content populated)
  3. Either:
     a. Contract expires/completes -> ARCHIVED
     b. Contract ends early -> TERMINATED (with termination_date, reasons_for_termination)

Migration Order:
  V1: Initial schema
  V2: Fix fully_mapped column naming
  V3: Add fully_mapped trigger
  V4: Add generated_by_mail column
  V5: Add termination tracking (terminationDate, reasonsForTermination)
  V6: Update contract status enum and add signed document (THIS MIGRATION)
*/

