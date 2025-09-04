-- Add enhanced fields to document table for Document Management module
ALTER TABLE document 
ADD COLUMN file_size BIGINT NOT NULL DEFAULT 0,
ADD COLUMN tags VARCHAR(500),
ADD COLUMN clause_reference VARCHAR(100),
ADD COLUMN department VARCHAR(100),
ADD COLUMN is_evidence BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN last_accessed TIMESTAMP,
ADD COLUMN access_count INTEGER NOT NULL DEFAULT 0;

-- Create indexes for better search performance
CREATE INDEX idx_document_file_name ON document(file_name);
CREATE INDEX idx_document_file_type ON document(file_type);
CREATE INDEX idx_document_department ON document(department);
CREATE INDEX idx_document_clause_reference ON document(clause_reference);
CREATE INDEX idx_document_is_evidence ON document(is_evidence);
CREATE INDEX idx_document_uploaded_at ON document(uploaded_at);
CREATE INDEX idx_document_last_accessed ON document(last_accessed);
CREATE INDEX idx_document_access_count ON document(access_count);
CREATE INDEX idx_document_tags ON document(tags);

-- Create document_nc junction table for many-to-many relationship
CREATE TABLE document_nc (
    document_id BIGINT NOT NULL,
    non_conformity_id BIGINT NOT NULL,
    PRIMARY KEY (document_id, non_conformity_id),
    FOREIGN KEY (document_id) REFERENCES document(document_id) ON DELETE CASCADE,
    FOREIGN KEY (non_conformity_id) REFERENCES non_conformity(non_conformity_id) ON DELETE CASCADE
);

-- Create indexes for document_nc table
CREATE INDEX idx_document_nc_document_id ON document_nc(document_id);
CREATE INDEX idx_document_nc_nc_id ON document_nc(non_conformity_id);
