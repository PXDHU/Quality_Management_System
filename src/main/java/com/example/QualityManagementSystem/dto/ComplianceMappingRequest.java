package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.MappingType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComplianceMappingRequest {
    private Long sourceClauseId;
    private Long targetClauseId;
    private MappingType mappingType;
    private Double similarityScore;
    private String mappingNotes;
}
