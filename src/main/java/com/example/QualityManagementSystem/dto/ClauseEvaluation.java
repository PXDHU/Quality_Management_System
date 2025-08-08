package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.ConformityStatus;
import com.example.QualityManagementSystem.model.Severity;
import lombok.Data;

import java.util.List;

@Data
public class ClauseEvaluation {
    private ConformityStatus conformityStatus;
    private Severity severity;
    private String comments;
    private List<Long> evidenceDocumentIds;
}
