-- Migration: Add Compliance Mapping Fields and Tables
-- Version: V4
-- Description: Adds compliance mapping functionality for cross-standard clause relationships

-- Add new columns to clause_library table
ALTER TABLE clause_library 
ADD COLUMN version VARCHAR(10) NOT NULL DEFAULT '1.0',
ADD COLUMN effective_date TIMESTAMP,
ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT true,
ADD COLUMN category VARCHAR(100),
ADD COLUMN risk_level VARCHAR(20) NOT NULL DEFAULT 'MEDIUM';

-- Create compliance_mapping table
CREATE TABLE compliance_mapping (
    mapping_id BIGSERIAL PRIMARY KEY,
    source_clause_id BIGINT NOT NULL,
    target_clause_id BIGINT NOT NULL,
    mapping_type VARCHAR(30) NOT NULL,
    similarity_score DECIMAL(3,2),
    mapping_notes TEXT,
    is_verified BOOLEAN NOT NULL DEFAULT false,
    verified_by BIGINT,
    verified_at TIMESTAMP,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_source_clause FOREIGN KEY (source_clause_id) REFERENCES clause_library(clause_id),
    CONSTRAINT fk_target_clause FOREIGN KEY (target_clause_id) REFERENCES clause_library(clause_id),
    CONSTRAINT fk_verified_by FOREIGN KEY (verified_by) REFERENCES auth_user(id),
    CONSTRAINT fk_created_by FOREIGN KEY (created_by) REFERENCES auth_user(id),
    CONSTRAINT unique_clause_mapping UNIQUE (source_clause_id, target_clause_id)
);

-- Create indexes for better performance
CREATE INDEX idx_compliance_mapping_source_clause ON compliance_mapping(source_clause_id);
CREATE INDEX idx_compliance_mapping_target_clause ON compliance_mapping(target_clause_id);
CREATE INDEX idx_compliance_mapping_type ON compliance_mapping(mapping_type);
CREATE INDEX idx_compliance_mapping_verified ON compliance_mapping(is_verified);
CREATE INDEX idx_compliance_mapping_standards ON compliance_mapping(source_clause_id, target_clause_id);

-- Create index for clause library searches
CREATE INDEX idx_clause_library_standard_active ON clause_library(standard, is_active);
CREATE INDEX idx_clause_library_standard_category ON clause_library(standard, category);
CREATE INDEX idx_clause_library_standard_risk ON clause_library(standard, risk_level);
CREATE INDEX idx_clause_library_search ON clause_library USING gin(to_tsvector('english', clause_name || ' ' || description));

-- Insert sample ISO 9001 clauses
INSERT INTO clause_library (clause_number, clause_name, description, standard, version, effective_date, category, risk_level, is_active, created_at, updated_at) VALUES
('4.1', 'Understanding the organization and its context', 'The organization shall determine external and internal issues that are relevant to its purpose and strategic direction.', 'ISO_9001', '2015', '2015-09-15 00:00:00', 'Context', 'MEDIUM', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('4.2', 'Understanding the needs and expectations of interested parties', 'The organization shall determine the interested parties that are relevant to the quality management system.', 'ISO_9001', '2015', '2015-09-15 00:00:00', 'Context', 'MEDIUM', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('4.3', 'Determining the scope of the quality management system', 'The organization shall determine the boundaries and applicability of the quality management system.', 'ISO_9001', '2015', '2015-09-15 00:00:00', 'Context', 'MEDIUM', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('4.4', 'Quality management system and its processes', 'The organization shall establish, implement, maintain and continually improve a quality management system.', 'ISO_9001', '2015', '2015-09-15 00:00:00', 'Context', 'HIGH', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('5.1', 'Leadership and commitment', 'Top management shall demonstrate leadership and commitment with respect to the quality management system.', 'ISO_9001', '2015', '2015-09-15 00:00:00', 'Leadership', 'HIGH', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('5.2', 'Policy', 'Top management shall establish, implement and maintain a quality policy.', 'ISO_9001', '2015', '2015-09-15 00:00:00', 'Leadership', 'HIGH', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('6.1', 'Actions to address risks and opportunities', 'When planning for the quality management system, the organization shall consider the issues and requirements.', 'ISO_9001', '2015', '2015-09-15 00:00:00', 'Planning', 'HIGH', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('7.1', 'Resources', 'The organization shall determine and provide the resources needed for the establishment, implementation, maintenance and continual improvement of the quality management system.', 'ISO_9001', '2015', '2015-09-15 00:00:00', 'Support', 'MEDIUM', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('8.1', 'Operational planning and control', 'The organization shall plan, implement and control the processes needed to meet requirements.', 'ISO_9001', '2015', '2015-09-15 00:00:00', 'Operation', 'HIGH', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('9.1', 'Monitoring, measurement, analysis and evaluation', 'The organization shall determine what needs to be monitored and measured.', 'ISO_9001', '2015', '2015-09-15 00:00:00', 'Evaluation', 'HIGH', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('10.1', 'Improvement', 'The organization shall determine and select opportunities for improvement and implement necessary actions.', 'ISO_9001', '2015', '2015-09-15 00:00:00', 'Improvement', 'HIGH', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample ISO 27001 clauses
INSERT INTO clause_library (clause_number, clause_name, description, standard, version, effective_date, category, risk_level, is_active, created_at, updated_at) VALUES
('4.1', 'Understanding the organization and its context', 'The organization shall determine external and internal issues that are relevant to the information security management system.', 'ISO_27001', '2013', '2013-10-01 00:00:00', 'Context', 'MEDIUM', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('4.2', 'Understanding the needs and expectations of interested parties', 'The organization shall determine the interested parties that are relevant to the information security management system.', 'ISO_27001', '2013', '2013-10-01 00:00:00', 'Context', 'MEDIUM', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('4.3', 'Determining the scope of the information security management system', 'The organization shall determine the boundaries and applicability of the information security management system.', 'ISO_27001', '2013', '2013-10-01 00:00:00', 'Context', 'MEDIUM', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('4.4', 'Information security management system', 'The organization shall establish, implement, maintain and continually improve an information security management system.', 'ISO_27001', '2013', '2013-10-01 00:00:00', 'Context', 'HIGH', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('5.1', 'Leadership and commitment', 'Top management shall demonstrate leadership and commitment with respect to the information security management system.', 'ISO_27001', '2013', '2013-10-01 00:00:00', 'Leadership', 'HIGH', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('5.2', 'Information security policy', 'Top management shall establish an information security policy.', 'ISO_27001', '2013', '2013-10-01 00:00:00', 'Leadership', 'HIGH', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('6.1', 'Actions to address risks and opportunities', 'The organization shall define and apply an information security risk assessment process.', 'ISO_27001', '2013', '2013-10-01 00:00:00', 'Planning', 'HIGH', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('7.1', 'Resources', 'The organization shall determine and provide the resources needed for the establishment, implementation, maintenance and continual improvement of the information security management system.', 'ISO_27001', '2013', '2013-10-01 00:00:00', 'Support', 'MEDIUM', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('8.1', 'Operational planning and control', 'The organization shall plan, implement and control the processes needed to meet information security requirements.', 'ISO_27001', '2013', '2013-10-01 00:00:00', 'Operation', 'HIGH', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('9.1', 'Monitoring, measurement, analysis and evaluation', 'The organization shall evaluate the information security performance and the effectiveness of the information security management system.', 'ISO_27001', '2013', '2013-10-01 00:00:00', 'Evaluation', 'HIGH', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('10.1', 'Improvement', 'The organization shall determine opportunities for improvement and implement necessary actions.', 'ISO_27001', '2013', '2013-10-01 00:00:00', 'Improvement', 'HIGH', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample compliance mappings between ISO 9001 and ISO 27001
INSERT INTO compliance_mapping (source_clause_id, target_clause_id, mapping_type, similarity_score, mapping_notes, is_verified, created_by, created_at, updated_at) VALUES
(1, 12, 'EXACT_MATCH', 0.95, 'Both clauses deal with understanding organizational context and external/internal issues', true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 13, 'EXACT_MATCH', 0.95, 'Both clauses address understanding needs and expectations of interested parties', true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 14, 'EXACT_MATCH', 0.95, 'Both clauses deal with determining the scope of the management system', true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 15, 'HIGH_SIMILARITY', 0.90, 'Both establish, implement, maintain and improve management systems', true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 16, 'EXACT_MATCH', 0.95, 'Both clauses address leadership and commitment from top management', true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 17, 'HIGH_SIMILARITY', 0.90, 'Both address policy establishment, but ISO 27001 is more specific to information security', true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 18, 'HIGH_SIMILARITY', 0.85, 'Both address risk assessment but with different focuses', true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 19, 'EXACT_MATCH', 0.95, 'Both clauses address resource determination and provision', true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 20, 'HIGH_SIMILARITY', 0.90, 'Both address operational planning and control but with different scopes', true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 21, 'HIGH_SIMILARITY', 0.90, 'Both address monitoring, measurement, analysis and evaluation', true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 22, 'EXACT_MATCH', 0.95, 'Both clauses address continual improvement', true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Create a view for easy access to compliance matrix
CREATE VIEW compliance_matrix_view AS
SELECT 
    c1.clause_number as iso9001_clause,
    c1.clause_name as iso9001_name,
    c2.clause_number as iso27001_clause,
    c2.clause_name as iso27001_name,
    cm.mapping_type,
    cm.similarity_score,
    cm.is_verified,
    cm.mapping_notes
FROM clause_library c1
CROSS JOIN clause_library c2
LEFT JOIN compliance_mapping cm ON c1.clause_id = cm.source_clause_id AND c2.clause_id = cm.target_clause_id
WHERE c1.standard = 'ISO_9001' AND c2.standard = 'ISO_27001'
ORDER BY c1.clause_number, c2.clause_number;
