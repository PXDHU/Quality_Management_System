package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.MappingType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComplianceMatrixResponse {
    private Map<String, List<ClauseLibraryResponse>> standardsClauses;
    private List<ComplianceMappingResponse> mappings;
    private Map<String, Map<String, MappingType>> matrix;
    private int totalMappings;
    private int verifiedMappings;
    private int pendingVerification;
}
