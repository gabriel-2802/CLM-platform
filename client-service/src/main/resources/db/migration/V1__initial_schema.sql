-- Create schema
CREATE SCHEMA IF NOT EXISTS clients;

-- ============================================================================
-- ENUM TYPES
-- ============================================================================
-- Tip: SRL | PFA | II | ASOC | SA | SPARL
-- DaNuNuECazul (YesNoNa): DA | NU | NU_E_CAZUL
-- Impozit: MICRO_1 | MICRO_3 | PROFIT
-- DaLunarTrim (TaxFrequency): DA_LUNAR | DA_TRIM | NU
-- Administratie: (all existing values — DGRF_BUCURESTI … UFO_SANNICOLAU_MARE)

-- ============================================================================
-- TABLE: clients.clients
-- ============================================================================
CREATE TABLE clients.clients (
    id BIGSERIAL PRIMARY KEY,
    denumire VARCHAR(255) NOT NULL,
    tip VARCHAR(50) NOT NULL,
    cui VARCHAR(50) NOT NULL UNIQUE,
    activa BOOLEAN NOT NULL,
    data_verificarii TIMESTAMP,
    adresa VARCHAR(255),
    administratie VARCHAR(50) NOT NULL,
    impozit VARCHAR(50),
    platitor_tva VARCHAR(50) NOT NULL,
    tva_la_incasare BOOLEAN,
    are_cod_tva_ue BOOLEAN,
    cod_tva_ue VARCHAR(255),
    operatiune_ue BOOLEAN,
    dividende BOOLEAN,
    salariati VARCHAR(255),
    casa_de_marcat BOOLEAN,
    data_exp_sediu_social TIMESTAMP,
    data_exp_mandat_admin TIMESTAMP,
    data_certificat_fiscal TIMESTAMP,
    data_fisa_platitor TIMESTAMP,
    data_vect_fiscal TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- TABLE: clients.detalii
-- ============================================================================
CREATE TABLE clients.detalii (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL UNIQUE,
    registru_uc BOOLEAN NOT NULL,
    registru_ev_fiscala VARCHAR(50) NOT NULL,
    of_spalare_bani BOOLEAN NOT NULL,
    regulament_ordine_interioara BOOLEAN NOT NULL,
    manual_politici_contabile BOOLEAN NOT NULL,
    adresa_revisal BOOLEAN NOT NULL,
    parola_itm VARCHAR(255),
    depunere_declaratii_online BOOLEAN NOT NULL,
    acces_dosar_fiscal VARCHAR(50) NOT NULL,
    CONSTRAINT fk_detalii_client FOREIGN KEY (client_id) REFERENCES clients.clients(id) ON DELETE CASCADE
);

-- ============================================================================
-- TABLE: clients.puncte_de_lucru
-- ============================================================================
CREATE TABLE clients.puncte_de_lucru (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    denumire VARCHAR(255) NOT NULL,
    de_la TIMESTAMP NOT NULL,
    pana_la TIMESTAMP,
    administratie VARCHAR(50) NOT NULL,
    registru_uc BOOLEAN NOT NULL,
    salariati INTEGER NOT NULL,
    cui VARCHAR(50),
    casa_de_marcat BOOLEAN NOT NULL,
    CONSTRAINT fk_puncte_de_lucru_client FOREIGN KEY (client_id) REFERENCES clients.clients(id) ON DELETE CASCADE
);

-- ============================================================================
-- TABLE: clients.istorice
-- ============================================================================
CREATE TABLE clients.istorice (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    anul INTEGER NOT NULL,
    cifra_afaceri DOUBLE PRECISION NOT NULL,
    inventar BOOLEAN NOT NULL,
    bilant_sem_iun VARCHAR(50) NOT NULL,
    bilant_anual VARCHAR(50) NOT NULL,
    CONSTRAINT fk_istorice_client FOREIGN KEY (client_id) REFERENCES clients.clients(id) ON DELETE CASCADE,
    CONSTRAINT uk_istorice_client_anul UNIQUE (client_id, anul)
);

-- ============================================================================
-- TABLE: clients.user_clients
-- ============================================================================
CREATE TABLE clients.user_clients (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    CONSTRAINT fk_user_clients_client FOREIGN KEY (client_id) REFERENCES clients.clients(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_clients_user_client UNIQUE (user_id, client_id)
);

-- ============================================================================
-- INDEXES
-- ============================================================================
CREATE INDEX idx_clients_cui ON clients.clients(cui);
CREATE INDEX idx_clients_activa ON clients.clients(activa);
CREATE INDEX idx_detalii_client_id ON clients.detalii(client_id);
CREATE INDEX idx_istorice_client_id ON clients.istorice(client_id);
CREATE INDEX idx_istorice_anul ON clients.istorice(anul);
CREATE INDEX idx_puncte_de_lucru_client_id ON clients.puncte_de_lucru(client_id);
CREATE INDEX idx_user_clients_client_id ON clients.user_clients(client_id);
CREATE INDEX idx_user_clients_user_id ON clients.user_clients(user_id);

