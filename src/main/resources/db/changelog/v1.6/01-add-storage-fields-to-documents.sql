--liquibase formatted sql

--changeset liquibase:add-storage-fields-to-documents splitStatements:false runOnChange:false
-- Add storage type and privacy fields to documents table

ALTER TABLE documents 
ADD COLUMN storage_type VARCHAR(50) DEFAULT 'local' NOT NULL,
ADD COLUMN is_private BOOLEAN DEFAULT FALSE;

-- Update existing records
UPDATE documents 
SET storage_type = 'local', is_private = FALSE 
WHERE storage_type IS NULL;

-- Add comment for documentation
COMMENT ON COLUMN documents.storage_type IS 'Storage provider type: local, cloudinary';
COMMENT ON COLUMN documents.is_private IS 'Whether the document is stored privately (for Cloudinary)'; 