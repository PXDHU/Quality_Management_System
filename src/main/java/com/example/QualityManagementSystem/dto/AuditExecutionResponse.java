package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.ConformityStatus;
import com.example.QualityManagementSystem.model.Status;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Data
public class AuditExecutionResponse {
    private Long auditId;
    private String auditTitle;
    private Status auditStatus;
    private String currentPhase;
    private LocalDateTime lastActivity;
    private LocalDate startDate;
    private LocalDate endDate;
    
    private List<ChecklistExecutionDTO> checklists;
    private AuditProgressDTO progress;
    
    @Data
    public static class ChecklistExecutionDTO {
        private Long checklistId;
        private String isoStandard; // Matches entity field name
        private Status status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<ClauseExecutionDTO> clauses; // Consistent naming
    }
    
    @Data
    public static class ClauseExecutionDTO {
        private Long itemId; // Matches Checklist_item.itemId
        private Long clauseId;
        private String clauseNumber;
        private String clauseName;
        private String customText;
        private ConformityStatus conformityStatus;
        private String comments;
        private String evidenceNotes;
        private String evaluatedBy;
        private LocalDateTime evaluatedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<EvidenceDocumentDTO> evidenceDocuments;
        private boolean evaluated;
    }
    
    @Data
    public static class EvidenceDocumentDTO {
        private Long documentId; // Matches Document.documentId
        private String fileName;
        private String fileType;
        private String description;
        private Long fileSize;
        private LocalDateTime uploadedAt;
        private String uploadedBy;
        private String tags;
        private String clauseReference;
        private String department;
        private Boolean isEvidence;
    }
    
    @Data
    public static class AuditProgressDTO {
        private int totalClauses;
        private int evaluatedClauses;
        private int compliantClauses;
        private int nonCompliantClauses;
        private int partiallyCompliantClauses;
        private int notApplicableClauses;
        private double completionPercentage;
        private boolean auditComplete;
    }
}
