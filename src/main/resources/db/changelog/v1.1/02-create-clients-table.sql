--liquibase formatted sql

--changeset fatih:7 labels:v1.1
--comment: Add client fields to users table
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20),
ADD COLUMN IF NOT EXISTS address TEXT,
ADD COLUMN IF NOT EXISTS notes TEXT;

--changeset fatih:8 labels:v1.1
--comment: Update cases table to reference users directly
ALTER TABLE cases 
DROP CONSTRAINT IF EXISTS fk_cases_client,
DROP COLUMN IF EXISTS client_id,
ADD COLUMN client_id BIGINT,
ADD CONSTRAINT fk_cases_client FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE SET NULL;

--changeset fatih:9 labels:v1.1
--comment: Create indexes for client fields
CREATE INDEX IF NOT EXISTS idx_users_phone_number ON users(phone_number);
CREATE INDEX IF NOT EXISTS idx_cases_client_id ON cases(client_id); 