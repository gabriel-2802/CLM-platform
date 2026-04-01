-- =============================================================================
-- V6__cleanup_unused_indexes.sql
-- Removes unused composite indexes from V2__optimize_search_indexes.sql
--
-- ANALYSIS:
-- The application search uses JPA Specification with individual column filters.
-- Composite indexes (status_client, field_value_contract_field, search_composite)
-- are not utilized because:
-- 1. Filter combinations are dynamic and rarely consistent
-- 2. Single-column indexes already accelerate individual predicates
-- 3. Removing reduces write overhead during INSERT/UPDATE operations
-- 4. Index fragmentation and maintenance overhead is minimized
--
-- VERIFIED USED INDEXES (retained):
-- - idx_generated_contract_status (single column equality)
-- - idx_generated_contract_client_id (single column equality)
-- - idx_generated_contract_generated_by (single column equality)
-- - idx_generated_contract_created_date_range (single column range queries)
-- - idx_gc_notes_lower_trgm (GIN trigram, in V4)
-- - idx_ct_name_lower_trgm (GIN trigram, in V4)
-- - idx_cfv_field_value_lower_trgm (GIN trigram, in V4)
-- =============================================================================

-- Drop composite index for status + client_id (not used in dynamic queries)
DROP INDEX IF EXISTS contracts.idx_generated_contract_status_client;

-- Drop composite index for template status with included field
DROP INDEX IF EXISTS contracts.idx_contract_template_name_status;

-- Drop composite index for field value + contract + field
DROP INDEX IF EXISTS contracts.idx_contract_field_value_contract_field;

-- Drop composite index for complex search patterns
DROP INDEX IF EXISTS contracts.idx_generated_contract_search_composite;

