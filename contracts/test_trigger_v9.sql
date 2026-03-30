-- Manual Trigger Test Script
-- Run this AFTER applying the V9 migration to verify the triggers work correctly

-- For Template 1, force a recalculation by updating a field
UPDATE contracts.template_field
SET is_required = is_required
WHERE id IN (1, 2);

-- Check the result - should now show is_fully_mapped = true
SELECT id, template_name, is_fully_mapped, field_count, updated_at
FROM contracts.contract_template
WHERE id = 1;

-- Show all fields for template 1 to verify
SELECT id, field_label, is_required
FROM contracts.template_field
WHERE template_id = 1;

-- You should see:
-- Template 1: is_fully_mapped = TRUE (because all required fields have labels)
-- Field 1: field_label = 'c_name', is_required = TRUE
-- Field 2: field_label = 'c_comp', is_required = TRUE

