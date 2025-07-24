-- Fix documents table - Add missing columns for base64 storage

-- Add storage_type column if not exists
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'documents' AND column_name = 'storage_type') THEN
        ALTER TABLE documents ADD COLUMN storage_type VARCHAR(50) DEFAULT 'base64' NOT NULL;
    END IF;
END $$;

-- Add is_private column if not exists
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'documents' AND column_name = 'is_private') THEN
        ALTER TABLE documents ADD COLUMN is_private BOOLEAN DEFAULT TRUE;
    END IF;
END $$;

-- Add base64_content column if not exists
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'documents' AND column_name = 'base64_content') THEN
        ALTER TABLE documents ADD COLUMN base64_content TEXT;
    END IF;
END $$;

-- Make file_path nullable
DO $$ 
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'documents' AND column_name = 'file_path' 
               AND is_nullable = 'NO') THEN
        ALTER TABLE documents ALTER COLUMN file_path DROP NOT NULL;
    END IF;
END $$;

-- Update existing records to use base64 storage
UPDATE documents 
SET storage_type = 'base64', is_private = TRUE 
WHERE storage_type IS NULL OR storage_type != 'base64';

-- Add index if not exists
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes 
                   WHERE tablename = 'documents' AND indexname = 'idx_documents_storage_type') THEN
        CREATE INDEX idx_documents_storage_type ON documents(storage_type);
    END IF;
END $$;

-- Add comments
COMMENT ON COLUMN documents.base64_content IS 'Base64 encoded file content for in-database storage';
COMMENT ON COLUMN documents.storage_type IS 'Storage provider: base64, local, cloudinary';
COMMENT ON COLUMN documents.is_private IS 'Whether the document requires authentication to access'; 