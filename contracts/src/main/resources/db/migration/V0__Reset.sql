-- ============================================================================
-- RESET SCRIPT - Clean up corrupted migration history
-- ============================================================================

-- This script is intentionally empty and is only used to mark a point
-- in the migration history. The actual reset is handled by the application.

-- If you need to clean up the migration history, run these commands manually:
-- DROP TABLE IF EXISTS public.flyway_schema_history CASCADE;
-- DROP SCHEMA IF EXISTS contracts CASCADE;

-- Then restart the application to re-run all migrations from scratch.

