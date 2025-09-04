package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.ISO;
import com.example.QualityManagementSystem.model.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClauseLibraryResponse {
    private Long clauseId;
    private String clauseNumber;
    private String clauseName;
    private String description;
    private ISO standard;
    private String version;
    private LocalDateTime effectiveDate;
    private boolean isActive;
    private String category;
    private RiskLevel riskLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
