-- =============================================================================
-- V10__add_contract_details.sql
-- Extract mutable financial/temporal fields from contract into contract_details
-- to support versioned contract amendments backed by appendix documents.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Drop removed columns from contract table
-- -----------------------------------------------------------------------------
DROP INDEX IF EXISTS clm.idx_contract_validity;
DROP INDEX IF EXISTS clm.idx_contract_modified_at;

ALTER TABLE clm.contract
    DROP COLUMN IF EXISTS contract_value,
    DROP COLUMN IF EXISTS contract_balance,
    DROP COLUMN IF EXISTS contract_start_date,
    DROP COLUMN IF EXISTS contract_end_date,
    DROP COLUMN IF EXISTS modified_at,
    DROP COLUMN IF EXISTS modified_by_user_id;


-- -----------------------------------------------------------------------------
-- 2. Create contract_details table
-- -----------------------------------------------------------------------------
CREATE TABLE clm.contract_details (
    id                   BIGSERIAL      PRIMARY KEY,
    contract_id          BIGINT         NOT NULL
        REFERENCES clm.contract (document_id) ON DELETE CASCADE ON UPDATE CASCADE,
    contract_value       NUMERIC(12, 2),
    contract_balance     NUMERIC(12, 2) NOT NULL DEFAULT 0,
    start_date           DATE,
    end_date             DATE,
    created_at           TIMESTAMP      NOT NULL DEFAULT NOW(),
    created_by_user_id   INTEGER,
    appendix_id          BIGINT         UNIQUE
        REFERENCES clm.appendix (document_id) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE INDEX idx_contract_details_contract ON clm.contract_details (contract_id);
