package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.ConformityStatus;
import com.example.QualityManagementSystem.model.Severity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InstanceResponse {
    private Long instanceId;
    private Long auditId;
    private Long checklistItemId;
    private Long clauseId;           // convenience: pulled from the Checklist_item
    private ConformityStatus conformityStatus;
    private Severity severity;
    private String comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
