package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.ISO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChecklistTemplateResponse {
    private Long templateId;
    private String templateName;
    private String description;
    private ISO isoStandard;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ChecklistClauseResponse> clauses;
    
    @Data
    public static class ChecklistClauseResponse {
        private Long clauseId;
        private String clauseNumber;
        private String clauseName;
        private String customText;
        private String customDescription;
    }
}
