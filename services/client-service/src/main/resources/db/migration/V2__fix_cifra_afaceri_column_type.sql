-- fix turnover column type from double precision to numeric(19,2)
ALTER TABLE clients.client_histories
ALTER COLUMN turnover TYPE NUMERIC(19, 2);