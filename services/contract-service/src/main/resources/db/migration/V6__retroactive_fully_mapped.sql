-- =============================================================================
-- V6__retroactive_fully_mapped.sql
-- Back-fill is_fully_mapped on all existing templates based on the same
-- logic used by the trg_template_field_label_mapped trigger (added in V1).
-- A template is fully mapped when it has at least one field and none of its
-- required fields are missing a label.
-- =============================================================================

UPDATE clm.document_template dt
SET    is_fully_mapped = TRUE,
       updated_at      = NOW()
WHERE  EXISTS (
           SELECT 1 FROM clm.template_field tf
           WHERE  tf.template_id = dt.id
       )
AND    NOT EXISTS (
           SELECT 1 FROM clm.template_field tf
           WHERE  tf.template_id = dt.id
             AND  tf.is_required = TRUE
             AND  tf.field_label IS NULL
       )
AND    dt.is_fully_mapped = FALSE;

UPDATE clm.document_template dt
SET    is_fully_mapped = FALSE,
       updated_at      = NOW()
WHERE  EXISTS (
           SELECT 1 FROM clm.template_field tf
           WHERE  tf.template_id = dt.id
             AND  tf.is_required = TRUE
             AND  tf.field_label IS NULL
       )
AND    dt.is_fully_mapped = TRUE;
