package com.example.QualityManagementSystem.service;

import com.example.QualityManagementSystem.dto.ComplianceMappingRequest;
import com.example.QualityManagementSystem.dto.ComplianceMappingResponse;
import com.example.QualityManagementSystem.dto.ComplianceMatrixResponse;
import com.example.QualityManagementSystem.dto.ClauseLibraryResponse;
import com.example.QualityManagementSystem.model.ComplianceMapping;
import com.example.QualityManagementSystem.model.Clause_library;
import com.example.QualityManagementSystem.model.MappingType;
import com.example.QualityManagementSystem.model.ISO;
import com.example.QualityManagementSystem.repository.ComplianceMappingRepository;
import com.example.QualityManagementSystem.repository.ClauseLibraryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.example.QualityManagementSystem.model.AuthUser;

@Service
@Transactional
public class ComplianceMappingService {

    @Autowired
    private ComplianceMappingRepository complianceMappingRepository;

    @Autowired
    private ClauseLibraryRepository clauseLibraryRepository;

    @Autowired
    private ClauseLibraryService clauseLibraryService;

    public ComplianceMappingResponse createMapping(ComplianceMappingRequest request, Long createdBy) {
        // Validate that both clauses exist
        Clause_library sourceClause = clauseLibraryRepository.findById(request.getSourceClauseId())
                .orElseThrow(() -> new RuntimeException("Source clause not found"));
        
        Clause_library targetClause = clauseLibraryRepository.findById(request.getTargetClauseId())
                .orElseThrow(() -> new RuntimeException("Target clause not found"));

        // Check if mapping already exists
        Optional<ComplianceMapping> existingMapping = complianceMappingRepository
                .findBySourceClause_ClauseIdAndTargetClause_ClauseId(
                        request.getSourceClauseId(), request.getTargetClauseId());
        
        if (existingMapping.isPresent()) {
            throw new RuntimeException("Mapping already exists between these clauses");
        }

        // Validate that clauses are from different standards
        if (sourceClause.getStandard() == targetClause.getStandard()) {
            throw new RuntimeException("Cannot map clauses from the same standard");
        }

        ComplianceMapping mapping = new ComplianceMapping();
        mapping.setSourceClause(sourceClause);
        mapping.setTargetClause(targetClause);
        mapping.setMappingType(request.getMappingType());
        mapping.setSimilarityScore(request.getSimilarityScore());
        mapping.setMappingNotes(request.getMappingNotes());
        mapping.setCreatedBy(createdBy);
        mapping.setCreatedAt(LocalDateTime.now());
        mapping.setUpdatedAt(LocalDateTime.now());

        ComplianceMapping savedMapping = complianceMappingRepository.save(mapping);
        return convertToResponse(savedMapping);
    }

    public ComplianceMappingResponse updateMapping(Long mappingId, ComplianceMappingRequest request) {
        ComplianceMapping mapping = complianceMappingRepository.findById(mappingId)
                .orElseThrow(() -> new RuntimeException("Mapping not found with id: " + mappingId));

        mapping.setMappingType(request.getMappingType());
        mapping.setSimilarityScore(request.getSimilarityScore());
        mapping.setMappingNotes(request.getMappingNotes());
        mapping.setUpdatedAt(LocalDateTime.now());

        ComplianceMapping updatedMapping = complianceMappingRepository.save(mapping);
        return convertToResponse(updatedMapping);
    }

    public ComplianceMappingResponse getMappingById(Long mappingId) {
        ComplianceMapping mapping = complianceMappingRepository.findById(mappingId)
                .orElseThrow(() -> new RuntimeException("Mapping not found with id: " + mappingId));
        return convertToResponse(mapping);
    }

    public List<ComplianceMappingResponse> getAllMappings() {
        return complianceMappingRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ComplianceMappingResponse> getMappingsByStandards(ISO sourceStandard, ISO targetStandard) {
        return complianceMappingRepository.findBySourceClause_StandardAndTargetClause_Standard(sourceStandard, targetStandard)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ComplianceMappingResponse> getMappingsByClauseId(Long clauseId) {
        return complianceMappingRepository.findByClauseId(clauseId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ComplianceMappingResponse> getMappingsByType(MappingType mappingType) {
        return complianceMappingRepository.findByMappingType(mappingType)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ComplianceMappingResponse> getVerifiedMappings() {
        return complianceMappingRepository.findByIsVerified(true)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ComplianceMappingResponse> getPendingVerificationMappings() {
        return complianceMappingRepository.findByIsVerified(false)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public ComplianceMappingResponse verifyMapping(Long mappingId, Long verifiedBy) {
        ComplianceMapping mapping = complianceMappingRepository.findById(mappingId)
                .orElseThrow(() -> new RuntimeException("Mapping not found with id: " + mappingId));

        mapping.setVerified(true);
        AuthUser verifier = new AuthUser();
        verifier.setUserId(verifiedBy.intValue());
        mapping.setVerifiedBy(verifier);
        mapping.setVerifiedAt(LocalDateTime.now());
        mapping.setUpdatedAt(LocalDateTime.now());

        ComplianceMapping verifiedMapping = complianceMappingRepository.save(mapping);
        return convertToResponse(verifiedMapping);
    }

    public void deleteMapping(Long mappingId) {
        if (!complianceMappingRepository.existsById(mappingId)) {
            throw new RuntimeException("Mapping not found with id: " + mappingId);
        }
        complianceMappingRepository.deleteById(mappingId);
    }

    public ComplianceMatrixResponse getComplianceMatrix() {
        List<ClauseLibraryResponse> iso9001Clauses = clauseLibraryService.getClausesByStandard(ISO.ISO_9001);
        List<ClauseLibraryResponse> iso27001Clauses = clauseLibraryService.getClausesByStandard(ISO.ISO_27001);
        
        List<ComplianceMappingResponse> allMappings = getAllMappings();
        
        // Create matrix
        Map<String, Map<String, MappingType>> matrix = new HashMap<>();
        for (ClauseLibraryResponse iso9001Clause : iso9001Clauses) {
            matrix.put(iso9001Clause.getClauseNumber(), new HashMap<>());
            for (ClauseLibraryResponse iso27001Clause : iso27001Clauses) {
                matrix.get(iso9001Clause.getClauseNumber()).put(iso27001Clause.getClauseNumber(), MappingType.NO_MAPPING);
            }
        }

        // Fill matrix with existing mappings
        for (ComplianceMappingResponse mapping : allMappings) {
            if (mapping.getSourceClause().getStandard() == ISO.ISO_9001 && 
                mapping.getTargetClause().getStandard() == ISO.ISO_27001) {
                matrix.get(mapping.getSourceClause().getClauseNumber())
                      .put(mapping.getTargetClause().getClauseNumber(), mapping.getMappingType());
            }
        }

        Map<String, List<ClauseLibraryResponse>> standardsClauses = new HashMap<>();
        standardsClauses.put("ISO_9001", iso9001Clauses);
        standardsClauses.put("ISO_27001", iso27001Clauses);

        int verifiedMappings = (int) allMappings.stream().filter(ComplianceMappingResponse::isVerified).count();
        int pendingVerification = allMappings.size() - verifiedMappings;

        ComplianceMatrixResponse response = new ComplianceMatrixResponse();
        response.setStandardsClauses(standardsClauses);
        response.setMappings(allMappings);
        response.setMatrix(matrix);
        response.setTotalMappings(allMappings.size());
        response.setVerifiedMappings(verifiedMappings);
        response.setPendingVerification(pendingVerification);

        return response;
    }

    public List<ComplianceMappingResponse> getMappingsBySimilarityScore(Double minScore) {
        return complianceMappingRepository.findByMinimumSimilarityScore(minScore)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ComplianceMappingResponse> getMappingsBySourceStandard(ISO standard) {
        return complianceMappingRepository.findBySourceClause_Standard(standard)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ComplianceMappingResponse> getMappingsByTargetStandard(ISO standard) {
        return complianceMappingRepository.findByTargetClause_Standard(standard)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private ComplianceMappingResponse convertToResponse(ComplianceMapping mapping) {
        ComplianceMappingResponse response = new ComplianceMappingResponse();
        response.setMappingId(mapping.getMappingId());
        response.setSourceClause(clauseLibraryService.getClauseById(mapping.getSourceClause().getClauseId()));
        response.setTargetClause(clauseLibraryService.getClauseById(mapping.getTargetClause().getClauseId()));
        response.setMappingType(mapping.getMappingType());
        response.setSimilarityScore(mapping.getSimilarityScore());
        response.setMappingNotes(mapping.getMappingNotes());
        response.setVerified(mapping.isVerified());
        response.setVerifiedBy(mapping.getVerifiedBy() != null ? mapping.getVerifiedBy().getUsername() : null);
        response.setVerifiedAt(mapping.getVerifiedAt());
        response.setCreatedAt(mapping.getCreatedAt());
        response.setUpdatedAt(mapping.getUpdatedAt());
        return response;
    }
}
