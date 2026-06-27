-- Store pending contract term changes on the appendix at generation time
-- so they can be applied automatically when the appendix is signed
ALTER TABLE clm.appendix
ADD COLUMN pending_end_date    DATE,
ADD COLUMN pending_value       NUMERIC(15,2),
ADD COLUMN pending_balance     NUMERIC(15,2);
