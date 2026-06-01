CREATE TABLE negotiations.terminated_contract (
    id             BIGSERIAL   PRIMARY KEY,
    contract_id    BIGINT      NOT NULL UNIQUE,
    terminated_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_terminated_contract ON negotiations.terminated_contract (contract_id);
