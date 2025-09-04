package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.ISO;
import lombok.Data;

import java.util.List;

@Data
public class ChecklistTemplateRequest {
    private String templateName;
    private String description;
    private ISO isoStandard;
    private List<ChecklistClauseRequest> clauses;
    
    @Data
    public static class ChecklistClauseRequest {
        private Long clauseId;
        private String customText;
        private String customDescription;
    }
}
