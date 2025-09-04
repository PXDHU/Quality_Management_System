package com.example.QualityManagementSystem.repository;

import com.example.QualityManagementSystem.model.ComplianceMapping;
import com.example.QualityManagementSystem.model.MappingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplianceMappingRepository extends JpaRepository<ComplianceMapping, Long> {
    
    List<ComplianceMapping> findBySourceClause_StandardAndTargetClause_Standard(
            com.example.QualityManagementSystem.model.ISO sourceStandard, 
            com.example.QualityManagementSystem.model.ISO targetStandard);
    
    List<ComplianceMapping> findBySourceClause_ClauseId(Long sourceClauseId);
    
    List<ComplianceMapping> findByTargetClause_ClauseId(Long targetClauseId);
    
    List<ComplianceMapping> findByMappingType(MappingType mappingType);
    
    List<ComplianceMapping> findByIsVerified(boolean isVerified);
    
    List<ComplianceMapping> findBySourceClause_Standard(com.example.QualityManagementSystem.model.ISO standard);
    
    List<ComplianceMapping> findByTargetClause_Standard(com.example.QualityManagementSystem.model.ISO standard);
    
    @Query("SELECT cm FROM ComplianceMapping cm WHERE " +
           "cm.sourceClause.clauseId = :clauseId OR cm.targetClause.clauseId = :clauseId")
    List<ComplianceMapping> findByClauseId(@Param("clauseId") Long clauseId);
    
    @Query("SELECT cm FROM ComplianceMapping cm WHERE " +
           "cm.sourceClause.standard = :sourceStandard AND cm.targetClause.standard = :targetStandard " +
           "AND cm.isVerified = true")
    List<ComplianceMapping> findVerifiedMappingsByStandards(
            @Param("sourceStandard") com.example.QualityManagementSystem.model.ISO sourceStandard,
            @Param("targetStandard") com.example.QualityManagementSystem.model.ISO targetStandard);
    
    @Query("SELECT cm FROM ComplianceMapping cm WHERE " +
           "cm.similarityScore >= :minScore")
    List<ComplianceMapping> findByMinimumSimilarityScore(@Param("minScore") Double minScore);
    
    Optional<ComplianceMapping> findBySourceClause_ClauseIdAndTargetClause_ClauseId(
            Long sourceClauseId, Long targetClauseId);
}
