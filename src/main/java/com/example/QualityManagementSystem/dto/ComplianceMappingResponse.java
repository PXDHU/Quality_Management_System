package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.MappingType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComplianceMappingResponse {
    private Long mappingId;
    private ClauseLibraryResponse sourceClause;
    private ClauseLibraryResponse targetClause;
    private MappingType mappingType;
    private Double similarityScore;
    private String mappingNotes;
    private boolean isVerified;
    private String verifiedBy;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
