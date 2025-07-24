--liquibase formatted sql

--changeset liquibase:add-base64-content-to-documents splitStatements:false runOnChange:false
-- Add base64 content field to documents table for in-database storage

-- Add base64 content column
ALTER TABLE documents 
ADD COLUMN base64_content LONGTEXT;

-- Update storage_type default to base64 for new documents
ALTER TABLE documents 
ALTER COLUMN storage_type SET DEFAULT 'base64';

-- Update is_private default to true for base64 storage
ALTER TABLE documents 
ALTER COLUMN is_private SET DEFAULT TRUE;

-- Make filePath nullable for base64 storage
ALTER TABLE documents 
ALTER COLUMN file_path DROP NOT NULL;

-- Add index for faster queries
CREATE INDEX idx_documents_storage_type ON documents(storage_type);

-- Add comments for documentation
COMMENT ON COLUMN documents.base64_content IS 'Base64 encoded file content for in-database storage';
COMMENT ON COLUMN documents.storage_type IS 'Storage provider: base64, local, cloudinary';
COMMENT ON COLUMN documents.is_private IS 'Whether the document requires authentication to access'; 