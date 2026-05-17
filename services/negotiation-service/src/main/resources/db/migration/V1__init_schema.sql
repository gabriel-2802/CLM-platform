-- ============================================================
-- Negotiation Service — initial schema
-- ============================================================

CREATE SCHEMA IF NOT EXISTS negotiations;

CREATE TYPE negotiations.negotiation_status AS ENUM (
    'DRAFT',
    'SENT',
    'ACCEPTED',
    'REJECTED'
);

CREATE TABLE negotiations.negotiation (
    id                  BIGSERIAL       PRIMARY KEY,
    contract_id         BIGINT          NOT NULL,
    client_id           INTEGER         NOT NULL,
    proposed_value      NUMERIC(12, 2),
    proposed_end_date   DATE,
    status              negotiations.negotiation_status NOT NULL DEFAULT 'DRAFT',
    notes               VARCHAR(2000),
    created_by_user_id  INTEGER         NOT NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    resolved_at         TIMESTAMP,

    CONSTRAINT chk_negotiation_has_proposal
        CHECK (proposed_value IS NOT NULL OR proposed_end_date IS NOT NULL)
);

CREATE INDEX idx_negotiation_contract ON negotiations.negotiation (contract_id);
CREATE INDEX idx_negotiation_client   ON negotiations.negotiation (client_id);
CREATE INDEX idx_negotiation_status   ON negotiations.negotiation (status);
