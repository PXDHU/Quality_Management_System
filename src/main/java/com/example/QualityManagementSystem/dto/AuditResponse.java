package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.Status;
import com.example.QualityManagementSystem.model.ISO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditResponse {
    public Long id;
    public String title;
    public String scope;
    public String objectives;
    public LocalDate startDate;
    public LocalDate endDate;
    public Status status;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    // Enhanced fields for audit planning
    public String auditType;
    public String department;
    public String location;
    public String notes;
    public String createdBy;
    public String createdByName;

    // ✅ Only names, not full entities
    public List<String> auditorNames;
    public List<Long> auditorIds;

    // ✅ Lightweight nested DTOs
    public List<ChecklistDTO> checklists;
    public List<InstanceDTO> instances;
    public List<DocumentDTO> documents;

    // Progress tracking
    public Integer totalChecklists;
    public Integer completedChecklists;
    public Double progressPercentage;

    // --- Nested DTOs ---
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChecklistDTO {
        public Long checklistId;
        public ISO isoStandard;
        public Status status;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
        public List<ChecklistItemDTO> clauses;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChecklistItemDTO {
        public Long itemId;
        public Long clauseId;
        public String clauseNumber;
        public String clauseName;
        public String customText;
        public String conformityStatus;
        public String comments;
        public String evidenceNotes;
        public String evaluatedBy;
        public LocalDateTime evaluatedAt;
        public Boolean evaluated;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InstanceDTO {
        public Long id;
        public String conformityStatus;
        public String comments;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DocumentDTO {
        public Long id;
        public String fileName;
        public String fileType;
        public String description;
    }
}
