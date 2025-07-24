--liquibase formatted sql

--changeset liquibase:make-email-nullable splitStatements:false runOnChange:false
-- Make email field nullable in users table

-- Remove NOT NULL constraint from email column
ALTER TABLE users 
ALTER COLUMN email DROP NOT NULL;

-- Add comment
COMMENT ON COLUMN users.email IS 'User email address (optional for some user types)'; 