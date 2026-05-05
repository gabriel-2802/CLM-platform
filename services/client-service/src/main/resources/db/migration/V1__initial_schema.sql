-- create schema
CREATE SCHEMA IF NOT EXISTS clients;

-- ============================================================================
-- enum types
-- ============================================================================
-- CompanyType: SRL | PFA | II | ASOC | SA | SPARL
-- YesNoNa: DA | NU | NU_E_CAZUL
-- TaxType: MICRO_1 | MICRO_3 | PROFIT
-- TaxFrequency: DA_LUNAR | DA_TRIM | NU
-- Administration: (all existing values — DGRF_BUCURESTI … UFO_SANNICOLAU_MARE)

-- ============================================================================
-- table: clients.clients
-- ============================================================================
CREATE TABLE clients.clients (
                                 id                      BIGSERIAL PRIMARY KEY,
                                 name                    VARCHAR(255) NOT NULL,
                                 company_type            VARCHAR(50)  NOT NULL,
                                 tax_id                  VARCHAR(50)  NOT NULL UNIQUE,
                                 active                  BOOLEAN      NOT NULL,
                                 verification_date       TIMESTAMP,
                                 address                 VARCHAR(255),
                                 administration          VARCHAR(50)  NOT NULL,
                                 tax_type                VARCHAR(50),
                                 vat_payer               VARCHAR(50)  NOT NULL,
                                 vat_on_collection       BOOLEAN,
                                 has_eu_vat_code         BOOLEAN,
                                 eu_vat_code             VARCHAR(255),
                                 eu_operation            BOOLEAN,
                                 dividends               BOOLEAN,
                                 employees               VARCHAR(255),
                                 cash_register           BOOLEAN,
                                 hq_expiration_date      TIMESTAMP,
                                 admin_mandate_expiration TIMESTAMP,
                                 fiscal_certificate_date TIMESTAMP,
                                 payer_sheet_date        TIMESTAMP,
                                 fiscal_vector_date      TIMESTAMP,
                                 created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- table: clients.client_details
-- ============================================================================
CREATE TABLE clients.client_details (
                                        id                        BIGSERIAL PRIMARY KEY,
                                        client_id                 BIGINT      NOT NULL UNIQUE,
                                        uc_registry               BOOLEAN     NOT NULL,
                                        fiscal_evidence_registry  VARCHAR(50) NOT NULL,
                                        money_laundering_office   BOOLEAN     NOT NULL,
                                        internal_rules            BOOLEAN     NOT NULL,
                                        accounting_policies_manual BOOLEAN    NOT NULL,
                                        revisal_address           BOOLEAN     NOT NULL,
                                        itm_password              VARCHAR(255),
                                        online_declarations       BOOLEAN     NOT NULL,
                                        fiscal_file_access        VARCHAR(50) NOT NULL,
                                        CONSTRAINT fk_client_details_client FOREIGN KEY (client_id) REFERENCES clients.clients(id) ON DELETE CASCADE
);

-- ============================================================================
-- table: clients.work_points
-- ============================================================================
CREATE TABLE clients.work_points (
                                     id             BIGSERIAL PRIMARY KEY,
                                     client_id      BIGINT      NOT NULL,
                                     name           VARCHAR(255) NOT NULL,
                                     valid_from     TIMESTAMP   NOT NULL,
                                     valid_to       TIMESTAMP,
                                     administration VARCHAR(50) NOT NULL,
                                     uc_registry    BOOLEAN     NOT NULL,
                                     employee_count INTEGER     NOT NULL,
                                     tax_id         VARCHAR(50),
                                     cash_register  BOOLEAN     NOT NULL,
                                     CONSTRAINT fk_work_points_client FOREIGN KEY (client_id) REFERENCES clients.clients(id) ON DELETE CASCADE
);

-- ============================================================================
-- table: clients.client_histories
-- ============================================================================
CREATE TABLE clients.client_histories (
                                          id                   BIGSERIAL PRIMARY KEY,
                                          client_id            BIGINT          NOT NULL,
                                          year                 INTEGER         NOT NULL,
                                          turnover             NUMERIC(19, 2)  NOT NULL,
                                          inventory            BOOLEAN         NOT NULL,
                                          june_semester_balance VARCHAR(50)    NOT NULL,
                                          annual_balance        VARCHAR(50)    NOT NULL,
                                          CONSTRAINT fk_client_histories_client FOREIGN KEY (client_id) REFERENCES clients.clients(id) ON DELETE CASCADE,
                                          CONSTRAINT uk_client_histories_client_year UNIQUE (client_id, year)
);

-- ============================================================================
-- table: clients.user_clients
-- ============================================================================
CREATE TABLE clients.user_clients (
                                      id        BIGSERIAL PRIMARY KEY,
                                      user_id   BIGINT NOT NULL,
                                      client_id BIGINT NOT NULL,
                                      CONSTRAINT fk_user_clients_client FOREIGN KEY (client_id) REFERENCES clients.clients(id) ON DELETE CASCADE,
                                      CONSTRAINT uk_user_clients_user_client UNIQUE (user_id, client_id)
);

-- ============================================================================
-- indexes
-- ============================================================================
CREATE INDEX idx_clients_tax_id          ON clients.clients(tax_id);
CREATE INDEX idx_clients_active          ON clients.clients(active);
CREATE INDEX idx_client_details_client_id ON clients.client_details(client_id);
CREATE INDEX idx_client_histories_client_id ON clients.client_histories(client_id);
CREATE INDEX idx_client_histories_year   ON clients.client_histories(year);
CREATE INDEX idx_work_points_client_id   ON clients.work_points(client_id);
CREATE INDEX idx_user_clients_client_id  ON clients.user_clients(client_id);
CREATE INDEX idx_user_clients_user_id    ON clients.user_clients(user_id);