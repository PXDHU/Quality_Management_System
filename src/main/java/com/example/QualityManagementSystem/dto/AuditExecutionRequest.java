package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.ConformityStatus;
import com.example.QualityManagementSystem.model.Severity;
import lombok.Data;

import java.util.List;

@Data
public class AuditExecutionRequest {
    private Long checklistItemId;
    private ConformityStatus conformityStatus;
    private String comments;
    private String evidenceNotes;
    private List<Long> evidenceDocumentIds;
    private String evaluatedBy;
    private Severity severity;  // LOW, MEDIUM, MAJOR
}