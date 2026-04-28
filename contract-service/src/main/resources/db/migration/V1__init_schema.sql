-- =============================================================================
-- V1__init_schema.sql
-- Full baseline — clm schema with JOINED-inheritance document hierarchy.
-- Tables: document_template, template_field, document (base),
--         contract (subclass), appendix (subclass), document_field_value.
-- Covers: enums, tables, indexes, constraints, trigger, search functions,
--         and trigram search indexes.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 0. SCHEMA
-- -----------------------------------------------------------------------------
CREATE SCHEMA clm;


-- -----------------------------------------------------------------------------
-- 1. ENUM TYPES
-- -----------------------------------------------------------------------------

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'contract_status_enum') THEN
        CREATE TYPE clm.contract_status_enum AS ENUM (
            'PENDING_SIGNATURE', 'ACTIVE', 'TERMINATED', 'ARCHIVED'
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'appendix_status_enum') THEN
        CREATE TYPE clm.appendix_status_enum AS ENUM (
            'DRAFT', 'SIGNED'
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'document_format_enum') THEN
        CREATE TYPE clm.document_format_enum AS ENUM (
            'PDF', 'DOCX'
        );
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'data_type_enum') THEN
        CREATE TYPE clm.data_type_enum AS ENUM (
            'STRING', 'DATE', 'NUMBER', 'BOOLEAN', 'CURRENCY'
        );
    END IF;
END
$$;


-- -----------------------------------------------------------------------------
-- 2. TABLES
-- -----------------------------------------------------------------------------

-- 2a. document_template  (blueprint used for filling)
CREATE TABLE clm.document_template (
    id               BIGSERIAL    PRIMARY KEY,
    template_name    VARCHAR(255) NOT NULL UNIQUE,
    description      VARCHAR(500),
    document_format  clm.document_format_enum NOT NULL,
    document_content BYTEA        NOT NULL,
    field_count      INTEGER      NOT NULL DEFAULT 0,
    is_fully_mapped  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_document_template_name    ON clm.document_template (template_name);
CREATE INDEX idx_document_template_created ON clm.document_template (created_at DESC);


-- 2b. template_field  (placeholders extracted from a template)
CREATE TABLE clm.template_field (
    id             BIGSERIAL  PRIMARY KEY,
    template_id    BIGINT     NOT NULL
        REFERENCES clm.document_template (id) ON DELETE CASCADE ON UPDATE CASCADE,
    field_label    VARCHAR(255) DEFAULT NULL,
    data_type      clm.data_type_enum NOT NULL DEFAULT 'STRING',
    field_position INTEGER,
    is_required    BOOLEAN    NOT NULL DEFAULT TRUE,
    format_pattern VARCHAR(255)
);

CREATE INDEX idx_template_field_template          ON clm.template_field (template_id);
CREATE INDEX idx_template_field_template_position ON clm.template_field (template_id, field_position);


-- 2c. document  (JOINED-inheritance base table)
--   discriminator column: document_type  ('CONTRACT' | 'APPENDIX')
--   template_id is nullable — NULL means the document was uploaded directly (non-fillable)
CREATE TABLE clm.document (
    id                      BIGSERIAL    PRIMARY KEY,
    document_type           VARCHAR(31)  NOT NULL,
    template_id             BIGINT
        REFERENCES clm.document_template (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    document_format         clm.document_format_enum,
    document_content        BYTEA,
    signed_document_content BYTEA,
    generated_by            INTEGER,
    generated_by_mail       VARCHAR(255),
    notes                   VARCHAR(1000),
    created_at              TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_document_template    ON clm.document (template_id);
CREATE INDEX idx_document_type        ON clm.document (document_type);
CREATE INDEX idx_document_created_at  ON clm.document (created_at DESC);


-- 2d. contract  (JOINED subclass of document)
CREATE TABLE clm.contract (
    document_id               BIGINT  PRIMARY KEY
        REFERENCES clm.document (id) ON DELETE CASCADE ON UPDATE CASCADE,
    client_id                 INTEGER NOT NULL,
    contract_status           clm.contract_status_enum NOT NULL DEFAULT 'PENDING_SIGNATURE',
    contract_value            NUMERIC(12, 2),
    contract_start_date       DATE,
    contract_end_date         DATE,
    termination_date          DATE,
    reasons_for_termination   VARCHAR(1000) NOT NULL DEFAULT ''
);

CREATE INDEX idx_contract_client          ON clm.contract (client_id);
CREATE INDEX idx_contract_status          ON clm.contract (contract_status);
CREATE INDEX idx_contract_template_client ON clm.contract (document_id, client_id);
CREATE INDEX idx_contract_validity        ON clm.contract (contract_start_date, contract_end_date);
CREATE INDEX idx_contract_termination     ON clm.contract (termination_date DESC)
    WHERE termination_date IS NOT NULL;
CREATE INDEX idx_contract_active          ON clm.contract (contract_status)
    WHERE contract_status = 'ACTIVE';


-- 2e. appendix  (JOINED subclass of document — auxiliary files for contracts)
CREATE TABLE clm.appendix (
    document_id       BIGINT  PRIMARY KEY
        REFERENCES clm.document (id) ON DELETE CASCADE ON UPDATE CASCADE,
    contract_id       BIGINT  NOT NULL
        REFERENCES clm.contract (document_id) ON DELETE CASCADE ON UPDATE CASCADE,
    title             VARCHAR(255) NOT NULL,
    appendix_status   clm.appendix_status_enum NOT NULL DEFAULT 'DRAFT'
);

CREATE INDEX idx_appendix_contract ON clm.appendix (contract_id);
CREATE INDEX idx_appendix_status   ON clm.appendix (appendix_status);


-- 2f. document_field_value  (audit trail of values injected into any document)
CREATE TABLE clm.document_field_value (
    id               BIGSERIAL PRIMARY KEY,
    document_id      BIGINT    NOT NULL
        REFERENCES clm.document (id) ON DELETE CASCADE ON UPDATE CASCADE,
    template_field_id BIGINT   NOT NULL
        REFERENCES clm.template_field (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    field_value      TEXT      NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_document_field_value_document ON clm.document_field_value (document_id);
CREATE INDEX idx_document_field_value_field    ON clm.document_field_value (template_field_id);


-- -----------------------------------------------------------------------------
-- 3. AUTO-MAPPING TRIGGER
--    When a template_field.field_label is set (non-null), check if ALL required
--    fields in the parent template now have a label.
--    Yes → is_fully_mapped = TRUE.  No → ensure it is FALSE.
-- -----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION clm.fn_check_template_fully_mapped()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_template_id       BIGINT;
    v_unmapped_required INTEGER;
BEGIN
    IF NEW.field_label IS NULL THEN
        RETURN NEW;
    END IF;

    v_template_id := NEW.template_id;

    SELECT COUNT(*)
    INTO   v_unmapped_required
    FROM   clm.template_field
    WHERE  template_id = v_template_id
      AND  is_required = TRUE
      AND  field_label IS NULL;

    IF v_unmapped_required = 0 THEN
        UPDATE clm.document_template
        SET    is_fully_mapped = TRUE, updated_at = NOW()
        WHERE  id = v_template_id AND is_fully_mapped = FALSE;
    ELSE
        UPDATE clm.document_template
        SET    is_fully_mapped = FALSE, updated_at = NOW()
        WHERE  id = v_template_id AND is_fully_mapped = TRUE;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_template_field_label_mapped
    AFTER INSERT OR UPDATE OF field_label
    ON clm.template_field
    FOR EACH ROW
    EXECUTE FUNCTION clm.fn_check_template_fully_mapped();


-- -----------------------------------------------------------------------------
-- 4. SEARCH FUNCTIONS
-- -----------------------------------------------------------------------------

-- 4a. Find documents matching ALL given label values (intersection)
CREATE OR REPLACE FUNCTION clm.fn_find_documents_by_label_values(
    p_label_values     TEXT[],
    p_case_insensitive BOOLEAN DEFAULT TRUE
)
RETURNS TABLE (document_id BIGINT)
LANGUAGE plpgsql
AS $$
DECLARE
    v_term TEXT;
BEGIN
    CREATE TEMP TABLE _matching_docs ON COMMIT DROP AS
        SELECT DISTINCT dfv.document_id
        FROM clm.document_field_value dfv;

    FOREACH v_term IN ARRAY p_label_values
    LOOP
        IF p_case_insensitive THEN
            DELETE FROM _matching_docs md
            WHERE NOT EXISTS (
                SELECT 1 FROM clm.document_field_value dfv
                WHERE dfv.document_id = md.document_id
                  AND LOWER(dfv.field_value) LIKE '%' || LOWER(v_term) || '%'
            );
        ELSE
            DELETE FROM _matching_docs md
            WHERE NOT EXISTS (
                SELECT 1 FROM clm.document_field_value dfv
                WHERE dfv.document_id = md.document_id
                  AND dfv.field_value LIKE '%' || v_term || '%'
            );
        END IF;
    END LOOP;

    RETURN QUERY SELECT md.document_id FROM _matching_docs md;
END;
$$;


-- 4b. Advanced contract search with optional filters
CREATE OR REPLACE FUNCTION clm.fn_search_contracts_advanced(
    p_client_id       INTEGER  DEFAULT NULL,
    p_contract_status VARCHAR  DEFAULT NULL,
    p_generated_by    INTEGER  DEFAULT NULL,
    p_created_after   DATE     DEFAULT NULL,
    p_created_before  DATE     DEFAULT NULL,
    p_notes_search    VARCHAR  DEFAULT NULL
)
RETURNS TABLE (contract_id BIGINT, score FLOAT)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT d.id, 1.0::FLOAT AS score
    FROM   clm.document  d
    JOIN   clm.contract  c ON c.document_id = d.id
    WHERE  (p_client_id       IS NULL OR c.client_id        = p_client_id)
      AND  (p_contract_status IS NULL OR c.contract_status::TEXT = p_contract_status)
      AND  (p_generated_by    IS NULL OR d.generated_by     = p_generated_by)
      AND  (p_created_after   IS NULL OR d.created_at       >= p_created_after::TIMESTAMP)
      AND  (p_created_before  IS NULL OR d.created_at       <= (p_created_before + INTERVAL '1 day')::TIMESTAMP)
      AND  (p_notes_search    IS NULL OR LOWER(d.notes)     LIKE '%' || LOWER(p_notes_search) || '%')
    ORDER BY d.created_at DESC;
END;
$$;


-- -----------------------------------------------------------------------------
-- 5. TRIGRAM SEARCH INDEXES (pg_trgm)
-- -----------------------------------------------------------------------------

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_dt_name_lower_trgm  ON clm.document_template USING GIN (LOWER(template_name) gin_trgm_ops);
CREATE INDEX idx_dt_desc_lower_trgm  ON clm.document_template USING GIN (LOWER(description)   gin_trgm_ops);
CREATE INDEX idx_doc_notes_lower_trgm ON clm.document          USING GIN (LOWER(notes)         gin_trgm_ops);
CREATE INDEX idx_dfv_value_lower_trgm ON clm.document_field_value USING GIN (LOWER(field_value) gin_trgm_ops);
