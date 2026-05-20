-- =============================================================================
-- V9__add_termination_due_status.sql
-- Add TERMINATION_DUE status to contract_status_enum
-- =============================================================================

ALTER TYPE clm.contract_status_enum ADD VALUE 'TERMINATION_DUE' BEFORE 'TERMINATED';

