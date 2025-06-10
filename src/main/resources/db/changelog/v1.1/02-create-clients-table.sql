--liquibase formatted sql

--changeset fatih:7 labels:v1.1
--comment: Create clients table
CREATE TABLE IF NOT EXISTS clients (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT DEFAULT 0,
    created_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_modified_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    password VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    phone_number VARCHAR(20),
    address TEXT,
    notes TEXT
);

--changeset fatih:8 labels:v1.1
--comment: Add client_id column to cases table
ALTER TABLE cases ADD COLUMN client_id BIGINT;
ALTER TABLE cases ADD CONSTRAINT fk_cases_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE SET NULL;

--changeset fatih:9 labels:v1.1
--comment: Create indexes for clients table and client_id in cases
CREATE INDEX IF NOT EXISTS idx_clients_username ON clients(username);
CREATE INDEX IF NOT EXISTS idx_clients_email ON clients(email);
CREATE INDEX IF NOT EXISTS idx_clients_enabled ON clients(enabled);
CREATE INDEX IF NOT EXISTS idx_clients_active ON clients(active);
CREATE INDEX IF NOT EXISTS idx_cases_client_id ON cases(client_id); 