-- =============================================================================
-- V4__trigram_search_indexes.sql
-- Enables pg_trgm extension and replaces obsolete BTREE text indexes with
-- GIN trigram indexes for efficient case-insensitive substring search.
--
-- WHY pg_trgm + GIN?
--   BTREE indexes cannot accelerate leading-wildcard LIKE patterns ('%term%').
--   GIN indexes built with gin_trgm_ops decompose each string into character
--   trigrams, allowing PostgreSQL to perform an Index Scan instead of a
--   Sequential Scan even for '%term%' patterns on multi-million row tables.
--
-- FUNCTIONAL INDEXES ON lower(col):
--   The application queries use LOWER(col) LIKE LOWER('%term%'). PostgreSQL
--   matches the index expression lower(col) to the query expression, so an
--   Index Scan on the functional GIN index is chosen automatically.
--   Benefit: single index covers both the case-folding and the trigram scan.
-- =============================================================================

-- Enable the trigram extension (idempotent, safe to run multiple times)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ---------------------------------------------------------------------------
-- Drop obsolete plain BTREE text indexes.
-- BTREE on a text column only helps with equality and prefix (col LIKE 'foo%')
-- lookups, not with the substring patterns ('%foo%') the search uses.
-- These indexes were consuming write overhead with no query benefit.
-- ---------------------------------------------------------------------------
DROP INDEX IF EXISTS contracts.idx_generated_contract_notes_btree;
DROP INDEX IF EXISTS contracts.idx_contract_field_value_field_value_btree;

-- ---------------------------------------------------------------------------
-- GIN trigram index on lower(generated_contract.notes)
-- Accelerates: LOWER(notes) LIKE '%<term>%'
-- Expected plan: Bitmap Index Scan on idx_gc_notes_lower_trgm
-- ---------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_gc_notes_lower_trgm
    ON contracts.generated_contract
    USING GIN (lower(notes) gin_trgm_ops);

-- ---------------------------------------------------------------------------
-- GIN trigram index on lower(contract_template.template_name)
-- Accelerates: LOWER(template_name) LIKE '%<term>%'
-- Expected plan: Bitmap Index Scan on idx_ct_name_lower_trgm (via JOIN)
-- ---------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_ct_name_lower_trgm
    ON contracts.contract_template
    USING GIN (lower(template_name) gin_trgm_ops);

-- ---------------------------------------------------------------------------
-- GIN trigram index on lower(contract_template.description)
-- Accelerates: LOWER(description) LIKE '%<term>%'
-- Expected plan: Bitmap Index Scan on idx_ct_desc_lower_trgm (via JOIN)
-- ---------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_ct_desc_lower_trgm
    ON contracts.contract_template
    USING GIN (lower(description) gin_trgm_ops);

-- ---------------------------------------------------------------------------
-- GIN trigram index on lower(contract_field_value.field_value)
-- Accelerates the EXISTS correlated subquery for labelValues filtering:
--   EXISTS (SELECT 1 FROM contract_field_value cfv
--           WHERE cfv.generated_contract_id = gc.id
--             AND lower(cfv.field_value) LIKE '%<term>%')
-- Expected plan: Index Scan on idx_cfv_field_value_lower_trgm
--                combined with the FK index on generated_contract_id.
-- ---------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_cfv_field_value_lower_trgm
    ON contracts.contract_field_value
    USING GIN (lower(field_value) gin_trgm_ops);
