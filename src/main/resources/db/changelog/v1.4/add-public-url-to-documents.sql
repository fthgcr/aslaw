-- Add public_url column to documents table for cloud storage support
ALTER TABLE documents ADD COLUMN public_url VARCHAR(1000);

-- Add comment for clarity
COMMENT ON COLUMN documents.public_url IS 'Public URL for documents stored in cloud storage (e.g., Cloudinary)'; 