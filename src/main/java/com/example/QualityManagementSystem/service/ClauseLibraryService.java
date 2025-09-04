package com.example.QualityManagementSystem.service;

import com.example.QualityManagementSystem.dto.ClauseLibraryRequest;
import com.example.QualityManagementSystem.dto.ClauseLibraryResponse;
import com.example.QualityManagementSystem.model.Clause_library;
import com.example.QualityManagementSystem.model.ISO;
import com.example.QualityManagementSystem.model.RiskLevel;
import com.example.QualityManagementSystem.repository.ClauseLibraryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClauseLibraryService {

    @Autowired
    private ClauseLibraryRepository clauseLibraryRepository;

    public ClauseLibraryResponse createClause(ClauseLibraryRequest request) {
        // Check if clause already exists
        Optional<Clause_library> existingClause = clauseLibraryRepository
                .findByClauseNumberAndStandard(request.getClauseNumber(), request.getStandard());
        
        if (existingClause.isPresent()) {
            throw new RuntimeException("Clause with number " + request.getClauseNumber() + 
                    " already exists for standard " + request.getStandard());
        }

        Clause_library clause = new Clause_library();
        clause.setClauseNumber(request.getClauseNumber());
        clause.setClauseName(request.getClauseName());
        clause.setDescription(request.getDescription());
        clause.setStandard(request.getStandard());
        clause.setVersion(request.getVersion() != null ? request.getVersion() : "1.0");
        clause.setEffectiveDate(request.getEffectiveDate() != null ? request.getEffectiveDate() : LocalDateTime.now());
        clause.setCategory(request.getCategory());
        clause.setRiskLevel(request.getRiskLevel() != null ? request.getRiskLevel() : RiskLevel.MEDIUM);
        clause.setActive(true);
        clause.setCreatedAt(LocalDateTime.now());
        clause.setUpdatedAt(LocalDateTime.now());

        Clause_library savedClause = clauseLibraryRepository.save(clause);
        return convertToResponse(savedClause);
    }

    public ClauseLibraryResponse updateClause(Long clauseId, ClauseLibraryRequest request) {
        Clause_library clause = clauseLibraryRepository.findById(clauseId)
                .orElseThrow(() -> new RuntimeException("Clause not found with id: " + clauseId));

        clause.setClauseName(request.getClauseName());
        clause.setDescription(request.getDescription());
        clause.setVersion(request.getVersion());
        clause.setEffectiveDate(request.getEffectiveDate());
        clause.setCategory(request.getCategory());
        clause.setRiskLevel(request.getRiskLevel());
        clause.setUpdatedAt(LocalDateTime.now());

        Clause_library updatedClause = clauseLibraryRepository.save(clause);
        return convertToResponse(updatedClause);
    }

    public ClauseLibraryResponse getClauseById(Long clauseId) {
        Clause_library clause = clauseLibraryRepository.findById(clauseId)
                .orElseThrow(() -> new RuntimeException("Clause not found with id: " + clauseId));
        return convertToResponse(clause);
    }

    public List<ClauseLibraryResponse> getAllClauses() {
        return clauseLibraryRepository.findByIsActiveOrderByStandardAscClauseNumberAsc(true)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ClauseLibraryResponse> getClausesByStandard(ISO standard) {
        return clauseLibraryRepository.findByStandardAndIsActive(standard, true)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ClauseLibraryResponse> getClausesByStandardAndCategory(ISO standard, String category) {
        return clauseLibraryRepository.findByStandardAndCategory(standard, category)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ClauseLibraryResponse> getClausesByStandardAndRiskLevel(ISO standard, RiskLevel riskLevel) {
        return clauseLibraryRepository.findByStandardAndRiskLevel(standard, riskLevel)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ClauseLibraryResponse> searchClauses(String keyword) {
        return clauseLibraryRepository.searchClausesByKeyword(keyword)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ClauseLibraryResponse> searchClausesByStandardAndKeyword(ISO standard, String keyword) {
        return clauseLibraryRepository.searchClausesByStandardAndKeyword(standard, keyword)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public void deactivateClause(Long clauseId) {
        Clause_library clause = clauseLibraryRepository.findById(clauseId)
                .orElseThrow(() -> new RuntimeException("Clause not found with id: " + clauseId));
        clause.setActive(false);
        clause.setUpdatedAt(LocalDateTime.now());
        clauseLibraryRepository.save(clause);
    }

    public void activateClause(Long clauseId) {
        Clause_library clause = clauseLibraryRepository.findById(clauseId)
                .orElseThrow(() -> new RuntimeException("Clause not found with id: " + clauseId));
        clause.setActive(true);
        clause.setUpdatedAt(LocalDateTime.now());
        clauseLibraryRepository.save(clause);
    }

    public List<ClauseLibraryResponse> getActiveClausesByStandardAndDate(ISO standard, LocalDateTime date) {
        return clauseLibraryRepository.findActiveClausesByStandardAndDate(standard, date)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private ClauseLibraryResponse convertToResponse(Clause_library clause) {
        ClauseLibraryResponse response = new ClauseLibraryResponse();
        response.setClauseId(clause.getClauseId());
        response.setClauseNumber(clause.getClauseNumber());
        response.setClauseName(clause.getClauseName());
        response.setDescription(clause.getDescription());
        response.setStandard(clause.getStandard());
        response.setVersion(clause.getVersion());
        response.setEffectiveDate(clause.getEffectiveDate());
        response.setActive(clause.isActive());
        response.setCategory(clause.getCategory());
        response.setRiskLevel(clause.getRiskLevel());
        response.setCreatedAt(clause.getCreatedAt());
        response.setUpdatedAt(clause.getUpdatedAt());
        return response;
    }
}
