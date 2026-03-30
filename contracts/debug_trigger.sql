lc-- Comprehensive trigger debugging query
SET search_path TO contracts;

-- 1. Check if triggers exist
\echo '========== TRIGGER DEFINITIONS =========='
SELECT
    t.trigger_name,
    t.event_manipulation,
    t.action_timing,
    pg_get_triggerdef(t.oid) as trigger_definition
FROM pg_trigger t
WHERE t.tgrelid = 'contracts.template_field'::regclass
ORDER BY t.trigger_name;

-- 2. Check trigger function
\echo ''
\echo '========== TRIGGER FUNCTION =========='
SELECT
    p.proname,
    pg_get_functiondef(p.oid) as function_definition
FROM pg_proc p
WHERE p.pronamespace = 'contracts'::regnamespace
AND p.proname = 'update_template_fully_mapped_status';

-- 3. Check current template 1 state
\echo ''
\echo '========== TEMPLATE 1 CURRENT STATE =========='
SELECT
    id,
    template_name,
    is_fully_mapped,
    updated_at
FROM contracts.contract_template
WHERE id = 1;

-- 4. Check template 1 fields
\echo ''
\echo '========== TEMPLATE 1 FIELDS =========='
SELECT
    id,
    field_label,
    is_required
FROM contracts.template_field
WHERE template_id = 1
ORDER BY id;

-- 5. Check if all required fields have labels (the condition the trigger checks)
\echo ''
\echo '========== REQUIRED FIELDS WITHOUT LABELS (SHOULD BE EMPTY) =========='
SELECT
    id,
    field_label,
    is_required
FROM contracts.template_field
WHERE template_id = 1
AND is_required = true
AND field_label IS NULL;

-- 6. Check migration history
\echo ''
\echo '========== MIGRATION HISTORY =========='
SELECT
    version,
    description,
    installed_on
FROM public.flyway_schema_history
WHERE script LIKE '%fully_mapped%'
ORDER BY version;

