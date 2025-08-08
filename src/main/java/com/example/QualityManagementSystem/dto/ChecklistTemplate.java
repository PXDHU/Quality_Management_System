package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.ISO;
import lombok.Data;

import java.util.List;

@Data
public class ChecklistTemplate {
    private ISO isoStandard;
    private List<ChecklistClauseDTO> clauses;

    @Data
    public static class ChecklistClauseDTO {
        private Long clauseId;
        private String customText; // Optional override
    }
}
