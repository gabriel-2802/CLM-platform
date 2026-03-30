-- =============================================================================
-- V1__init_schema.sql
-- Full baseline migration — drops all previous state and rebuilds from scratch.
-- Covers: enums, tables, indexes, FK constraints, and the auto-mapping trigger.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 0. WIPE FLYWAY HISTORY + SCHEMA
-- -----------------------------------------------------------------------------
-- Clears all previous migration records so Flyway treats this as a clean slate.
-- The table lives in the default 'public' schema, unaffected by the CASCADE below.

CREATE SCHEMA contracts;


-- -----------------------------------------------------------------------------
-- 1. ENUM TYPES
-- -----------------------------------------------------------------------------

DO $$
BEGIN
    -- ContractStatus
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'contract_status_enum') THEN
CREATE TYPE contracts.contract_status_enum AS ENUM (
            'PENDING_SIGNATURE',
            'ACTIVE',
            'TERMINATED',
            'ARCHIVED'
        );
END IF;

    -- DocumentFormat
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'document_format_enum') THEN
CREATE TYPE contracts.document_format_enum AS ENUM (
            'PDF',
            'DOCX',
            'ZIP'
        );
END IF;

    -- DataType
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'data_type_enum') THEN
CREATE TYPE contracts.data_type_enum AS ENUM (
            'STRING',
            'DATE',
            'NUMBER',
            'BOOLEAN',
            'CURRENCY'
        );
END IF;
END
$$;


-- -----------------------------------------------------------------------------
-- 2. TABLES
-- -----------------------------------------------------------------------------

-- 2a. contract_template
CREATE TABLE contracts.contract_template (
                                             id                BIGSERIAL       PRIMARY KEY,
                                             template_name     VARCHAR(255)    NOT NULL UNIQUE,
                                             description       VARCHAR(500),
                                             document_format   contracts.document_format_enum  NOT NULL,
                                             document_content  BYTEA           NOT NULL,
                                             field_count       INTEGER         NOT NULL DEFAULT 0,
                                             is_fully_mapped   BOOLEAN         NOT NULL DEFAULT FALSE,
                                             created_at        TIMESTAMP       NOT NULL DEFAULT NOW(),
                                             updated_at        TIMESTAMP       NOT NULL DEFAULT NOW()
);


-- 2b. template_field
CREATE TABLE contracts.template_field (
                                          id                BIGSERIAL       PRIMARY KEY,
                                          template_id       BIGINT          NOT NULL
                                              REFERENCES contracts.contract_template (id)
                                                  ON DELETE CASCADE,
                                          field_label       VARCHAR(255)    DEFAULT NULL,
                                          data_type         contracts.data_type_enum  NOT NULL DEFAULT 'STRING',
                                          placeholder_text  VARCHAR(255),
                                          field_position    INTEGER,
                                          is_required       BOOLEAN         NOT NULL DEFAULT TRUE,
                                          format_pattern    VARCHAR(255)
);

CREATE INDEX idx_template_field_template_position
    ON contracts.template_field (template_id, field_position);


-- 2c. generated_contract
CREATE TABLE contracts.generated_contract (
                                              id                        BIGSERIAL       PRIMARY KEY,
                                              template_id               BIGINT          NOT NULL
                                                  REFERENCES contracts.contract_template (id),
                                              client_id                 INTEGER         NOT NULL,
                                              contract_status           contracts.contract_status_enum  NOT NULL DEFAULT 'PENDING_SIGNATURE',
                                              generated_by              INTEGER,
                                              generated_by_mail         VARCHAR(255),
                                              document_content          BYTEA,
                                              signed_document_content   BYTEA,
                                              contract_value            NUMERIC(12, 2),
                                              contract_start_date       DATE,
                                              contract_end_date         DATE,
                                              notes                     VARCHAR(1000),
                                              termination_date          DATE,
                                              reasons_for_termination   VARCHAR(1000)   NOT NULL DEFAULT '',
                                              created_at                TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_generated_contract_template_client
    ON contracts.generated_contract (template_id, client_id);

CREATE INDEX idx_generated_contract_validity
    ON contracts.generated_contract (contract_start_date, contract_end_date);

CREATE INDEX idx_generated_contract_created_at
    ON contracts.generated_contract (created_at);


-- 2d. contract_field_value
CREATE TABLE contracts.contract_field_value (
                                                id                    BIGSERIAL   PRIMARY KEY,
                                                generated_contract_id BIGINT      NOT NULL
                                                    REFERENCES contracts.generated_contract (id)
                                                        ON DELETE CASCADE,
                                                template_field_id     BIGINT      NOT NULL
                                                    REFERENCES contracts.template_field (id),
                                                field_value           TEXT        NOT NULL,
                                                created_at            TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_contract_field_value_contract
    ON contracts.contract_field_value (generated_contract_id);

CREATE INDEX idx_contract_field_value_field
    ON contracts.contract_field_value (template_field_id);


-- -----------------------------------------------------------------------------
-- 3. AUTO-MAPPING FUNCTION & TRIGGER
--
-- When a field_label is set (non-null) on a template_field row, check whether
-- ALL required fields for that template now have a non-null label.
-- If yes → mark the parent template as is_fully_mapped = TRUE.
-- If any required field still has no label → ensure the flag is FALSE.
-- -----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION contracts.fn_check_template_fully_mapped()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
v_template_id       BIGINT;
    v_unmapped_required INTEGER;
BEGIN
    -- Only act when field_label changes to a non-null value
    IF NEW.field_label IS NULL THEN
        RETURN NEW;
END IF;

    v_template_id := NEW.template_id;

    -- Count required fields that still have no label (excluding the current row
    -- which has already been updated by the time AFTER triggers fire)
SELECT COUNT(*)
INTO   v_unmapped_required
FROM   contracts.template_field
WHERE  template_id  = v_template_id
  AND  is_required  = TRUE
  AND  field_label  IS NULL;

IF v_unmapped_required = 0 THEN
        -- Every required field has a label → mark template as fully mapped
UPDATE contracts.contract_template
SET    is_fully_mapped = TRUE,
       updated_at      = NOW()
WHERE  id = v_template_id
  AND  is_fully_mapped = FALSE;   -- avoid unnecessary writes
ELSE
        -- At least one required field still unmapped → ensure flag is FALSE
UPDATE contracts.contract_template
SET    is_fully_mapped = FALSE,
       updated_at      = NOW()
WHERE  id = v_template_id
  AND  is_fully_mapped = TRUE;
END IF;

RETURN NEW;
END;
$$;


CREATE TRIGGER trg_template_field_label_mapped
    AFTER INSERT OR UPDATE OF field_label
                    ON contracts.template_field
                        FOR EACH ROW
                        EXECUTE FUNCTION contracts.fn_check_template_fully_mapped();