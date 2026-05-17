-- Hibernate 6 sends @Enumerated(EnumType.STRING) as character varying.
-- PostgreSQL does not implicitly cast varchar → negotiation_status, so we
-- convert the column to VARCHAR and enforce values via a CHECK constraint.

ALTER TABLE negotiations.negotiation
    ALTER COLUMN status TYPE VARCHAR(20)
        USING status::VARCHAR;

ALTER TABLE negotiations.negotiation
    ADD CONSTRAINT chk_negotiation_status
        CHECK (status IN ('DRAFT', 'SENT', 'ACCEPTED', 'REJECTED'));
