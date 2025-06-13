--liquibase formatted sql

--changeset aslaw:create-documents-table
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    description TEXT,
    file_path VARCHAR(500) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('COMPLAINT', 'ANSWER', 'MOTION', 'EXHIBIT', 'CONTRACT', 'CORRESPONDENCE', 'OTHER')),
    legal_case_id BIGINT NOT NULL,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP,
    CONSTRAINT fk_documents_legal_case FOREIGN KEY (legal_case_id) REFERENCES cases(id) ON DELETE CASCADE
);

--changeset aslaw:create-documents-indexes
CREATE INDEX idx_documents_legal_case_id ON documents(legal_case_id);
CREATE INDEX idx_documents_type ON documents(type);
CREATE INDEX idx_documents_created_date ON documents(created_date DESC);
CREATE INDEX idx_documents_title ON documents(title);
CREATE INDEX idx_documents_file_name ON documents(file_name); 