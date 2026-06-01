-- Add signDate and effectiveDate columns to appendix table
ALTER TABLE clm.appendix
ADD COLUMN sign_date DATE,
ADD COLUMN effective_date DATE;

-- Populate the new date columns with generated_at from parent document table
UPDATE clm.appendix a
SET sign_date = CAST(d.generated_at AS DATE),
    effective_date = CAST(d.generated_at AS DATE)
FROM clm.document d
WHERE a.document_id = d.id AND d.generated_at IS NOT NULL;

-- Create index for sign_date for faster queries
CREATE INDEX idx_appendix_sign_date ON clm.appendix(sign_date);

-- Create index for effective_date for efficient date-based filtering
CREATE INDEX idx_appendix_effective_date ON clm.appendix(effective_date);

