--liquibase formatted sql

--changeset fatih:12 labels:v1.2
--comment: Add file_path column to documents table if not exists
ALTER TABLE documents ADD COLUMN IF NOT EXISTS file_path VARCHAR(500);

--changeset fatih:13 labels:v1.2
--comment: Create index for documents file_path
CREATE INDEX IF NOT EXISTS idx_documents_file_path ON documents(file_path); 