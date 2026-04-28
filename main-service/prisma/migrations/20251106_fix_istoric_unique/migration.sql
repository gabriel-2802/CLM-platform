-- Fix Istoric uniqueness: drop any incorrect unique on clientId and add composite unique on (clientId, anul)
-- Note: Use IF EXISTS to be safe across environments

-- Try to drop a potential wrong unique index on clientId if it exists
DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM   pg_indexes
    WHERE  schemaname = 'public'
    AND    indexname = 'Istoric_clientId_key'
  ) THEN
    EXECUTE 'DROP INDEX IF EXISTS "public"."Istoric_clientId_key"';
  END IF;
END $$;

-- Create the composite unique if not already present
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM   pg_indexes
    WHERE  schemaname = 'public'
    AND    indexname = 'Istoric_clientId_anul_key'
  ) THEN
    EXECUTE 'CREATE UNIQUE INDEX "Istoric_clientId_anul_key" ON "public"."Istoric" ("clientId", "anul")';
  END IF;
END $$;
