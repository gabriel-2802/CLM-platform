-- Align all DATE columns: entities use LocalDate → DB must be DATE (not TIMESTAMP)

-- clients.clients
ALTER TABLE clients.clients
    ALTER COLUMN verification_date        TYPE DATE USING verification_date::date,
    ALTER COLUMN hq_expiration_date       TYPE DATE USING hq_expiration_date::date,
    ALTER COLUMN admin_mandate_expiration TYPE DATE USING admin_mandate_expiration::date,
    ALTER COLUMN fiscal_certificate_date  TYPE DATE USING fiscal_certificate_date::date,
    ALTER COLUMN payer_sheet_date         TYPE DATE USING payer_sheet_date::date,
    ALTER COLUMN fiscal_vector_date       TYPE DATE USING fiscal_vector_date::date;

-- clients.work_points
ALTER TABLE clients.work_points
    ALTER COLUMN valid_from TYPE DATE USING valid_from::date,
    ALTER COLUMN valid_to   TYPE DATE USING valid_to::date;

-- clients.tasks
ALTER TABLE clients.tasks
    ALTER COLUMN date TYPE DATE USING date::date;
