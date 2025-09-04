package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.ConformityStatus;
import lombok.Data;

import java.util.List;

@Data
public class ChecklistItemEvaluationRequest {
    private Long itemId;
    private ConformityStatus conformityStatus;
    private String comments;
    private String evidenceNotes;
    private List<Long> evidenceDocumentIds;
    private String evaluatedBy; // Added field for tracking who evaluated the item
}
