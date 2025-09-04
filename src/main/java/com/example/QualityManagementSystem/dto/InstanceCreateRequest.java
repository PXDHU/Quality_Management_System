package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.ConformityStatus;
import com.example.QualityManagementSystem.model.Severity;
import lombok.Data;

@Data
public class InstanceCreateRequest {
    private Long auditId;          // required
    private Long checklistItemId;  // required (Checklist_item id)
    private ConformityStatus conformityStatus; // optional (defaults to CONFORMITY)
    private Severity severity;     // optional
    private String comments;       // optional
}
