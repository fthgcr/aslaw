--liquibase formatted sql

--changeset fatih:5 labels:v1.1
--comment: Create cases table
CREATE TABLE IF NOT EXISTS cases (
    id BIGSERIAL PRIMARY KEY,
    version BIGINT DEFAULT 0,
    created_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_modified_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    case_number VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL CHECK (status IN ('OPEN', 'IN_PROGRESS', 'PENDING', 'CLOSED')),
    type VARCHAR(50) NOT NULL CHECK (type IN ('CAR_DEPRECIATION', 'CIVIL', 'CRIMINAL', 'FAMILY', 'CORPORATE', 'REAL_ESTATE', 'INTELLECTUAL_PROPERTY', 'OTHER')),
    filing_date DATE NOT NULL,
    assigned_user_id BIGINT,
    FOREIGN KEY (assigned_user_id) REFERENCES users(id) ON DELETE SET NULL
);

--changeset fatih:6 labels:v1.1
--comment: Create indexes for cases table
CREATE INDEX IF NOT EXISTS idx_cases_case_number ON cases(case_number);
CREATE INDEX IF NOT EXISTS idx_cases_status ON cases(status);
CREATE INDEX IF NOT EXISTS idx_cases_type ON cases(type);
CREATE INDEX IF NOT EXISTS idx_cases_filing_date ON cases(filing_date);
CREATE INDEX IF NOT EXISTS idx_cases_assigned_user_id ON cases(assigned_user_id);
CREATE INDEX IF NOT EXISTS idx_cases_deleted ON cases(deleted); 