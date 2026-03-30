-- V4__Add_generated_by_mail_column.sql
-- Add email tracking for contract generation
-- Stores the email of the user who generated the contract

SET search_path TO contracts;

-- ============================================
-- Add generated_by_mail column to generated_contract table
-- ============================================
-- This column tracks the email address of the user who generated the contract
-- for auditing and notification purposes.

ALTER TABLE contracts.generated_contract
ADD COLUMN generated_by_mail VARCHAR(255);

-- Add comment for documentation
COMMENT ON COLUMN contracts.generated_contract.generated_by_mail IS
'Email address of the user who generated the contract. Used for auditing and notifications.';

-- ============================================
-- Success message
-- ============================================
-- Migration completed: generated_by_mail column added to generated_contract table

