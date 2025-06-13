--liquibase formatted sql

--changeset aslaw:update-documents-table-structure
--comment: Update documents table structure for new requirements

-- Rename case_id to legal_case_id for consistency
ALTER TABLE documents RENAME COLUMN case_id TO legal_case_id;

-- Update foreign key constraint name
ALTER TABLE documents DROP CONSTRAINT IF EXISTS fk_documents_case;
ALTER TABLE documents ADD CONSTRAINT fk_documents_legal_case FOREIGN KEY (legal_case_id) REFERENCES cases(id) ON DELETE CASCADE;

-- Remove unnecessary columns from BaseEntity pattern
ALTER TABLE documents DROP COLUMN IF EXISTS created_by;
ALTER TABLE documents DROP COLUMN IF EXISTS last_modified_by;
ALTER TABLE documents DROP COLUMN IF EXISTS last_modified_date;
ALTER TABLE documents DROP COLUMN IF EXISTS version;
ALTER TABLE documents DROP COLUMN IF EXISTS deleted;

-- Make file_path NOT NULL
ALTER TABLE documents ALTER COLUMN file_path SET NOT NULL;

-- Add check constraint for document type
ALTER TABLE documents ADD CONSTRAINT chk_documents_type 
    CHECK (type IN ('COMPLAINT', 'ANSWER', 'MOTION', 'EXHIBIT', 'CONTRACT', 'CORRESPONDENCE', 'OTHER'));

-- Update indexes
DROP INDEX IF EXISTS idx_documents_case_id;
CREATE INDEX idx_documents_legal_case_id ON documents(legal_case_id);
CREATE INDEX idx_documents_created_date ON documents(created_date DESC);
CREATE INDEX idx_documents_title ON documents(title);
CREATE INDEX idx_documents_file_name ON documents(file_name); 