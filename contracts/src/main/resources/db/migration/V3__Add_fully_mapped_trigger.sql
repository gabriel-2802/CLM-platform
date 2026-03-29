-- V3__Add_fully_mapped_trigger.sql
-- Add database trigger to automatically update is_fully_mapped status
-- This ensures consistency at the database level when fields are updated

SET search_path TO contracts;

-- ============================================
-- TRIGGER FUNCTION: Update template's fully_mapped status
-- ============================================
-- This function checks if all fields in a template have a field_label (are fully mapped)
-- and updates the template's is_fully_mapped flag accordingly.

CREATE OR REPLACE FUNCTION update_template_fully_mapped_status()
RETURNS TRIGGER AS $$
DECLARE
    template_id BIGINT;
    all_mapped BOOLEAN;
BEGIN
    -- Get the template ID from the affected field
    template_id := NEW.template_id;

    -- Check if all fields for this template now have a field_label
    -- Return true if no fields exist (empty template) OR all fields have labels
    all_mapped := NOT EXISTS (
        SELECT 1 FROM contracts.template_field
        WHERE template_id = template_id
        AND field_label IS NULL
    );

    -- Update the template's fully_mapped status
    UPDATE contracts.contract_template
    SET is_fully_mapped = all_mapped,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = template_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- TRIGGER: After update on template_field
-- ============================================
-- Fires whenever a template_field is updated, particularly when field_label is set.
-- This ensures the contract_template.is_fully_mapped is always in sync.

CREATE TRIGGER template_field_update_trigger
    AFTER UPDATE ON contracts.template_field
    FOR EACH ROW
    WHEN (OLD.field_label IS DISTINCT FROM NEW.field_label)
    EXECUTE FUNCTION update_template_fully_mapped_status();

-- ============================================
-- TRIGGER: After insert on template_field
-- ============================================
-- Also trigger when new fields are inserted (though they typically don't have labels initially).
-- This ensures consistency if fields are inserted with labels.

CREATE TRIGGER template_field_insert_trigger
    AFTER INSERT ON contracts.template_field
    FOR EACH ROW
    EXECUTE FUNCTION update_template_fully_mapped_status();

-- ============================================
-- INDEX OPTIMIZATION
-- ============================================
-- Add index to optimize the CHECK in the trigger function
-- This index helps the NOT EXISTS query run efficiently

CREATE INDEX IF NOT EXISTS idx_template_field_label_null
ON contracts.template_field(template_id)
WHERE field_label IS NULL;

