-- Migration to add new fields for comprehensive audit planning
-- This migration adds the new fields to the audit table for enhanced audit planning functionality

ALTER TABLE audit 
ADD COLUMN audit_type VARCHAR(50),
ADD COLUMN department VARCHAR(100),
ADD COLUMN location VARCHAR(100),
ADD COLUMN notes TEXT,
ADD COLUMN current_phase VARCHAR(50) DEFAULT 'PLANNING',
ADD COLUMN last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add indexes for better performance on commonly queried fields
CREATE INDEX idx_audit_status ON audit(status);
CREATE INDEX idx_audit_audit_type ON audit(audit_type);
CREATE INDEX idx_audit_department ON audit(department);
CREATE INDEX idx_audit_start_date ON audit(start_date);
CREATE INDEX idx_audit_end_date ON audit(end_date);
CREATE INDEX idx_audit_created_by ON audit(created_by);

-- Add comments for documentation
COMMENT ON COLUMN audit.audit_type IS 'Type of audit: INTERNAL, EXTERNAL, SUPPLIER, etc.';
COMMENT ON COLUMN audit.department IS 'Department being audited';
COMMENT ON COLUMN audit.location IS 'Location where audit is conducted';
COMMENT ON COLUMN audit.notes IS 'Additional notes and comments for the audit';
COMMENT ON COLUMN audit.current_phase IS 'Current phase of the audit: PLANNING, EXECUTION, REPORTING, FOLLOW_UP';
COMMENT ON COLUMN audit.last_activity IS 'Timestamp of the last activity on this audit';
