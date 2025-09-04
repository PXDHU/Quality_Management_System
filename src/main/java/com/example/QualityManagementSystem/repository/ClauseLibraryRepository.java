package com.example.QualityManagementSystem.repository;

import com.example.QualityManagementSystem.model.Clause_library;
import com.example.QualityManagementSystem.model.ISO;
import com.example.QualityManagementSystem.model.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClauseLibraryRepository extends JpaRepository<Clause_library, Long> {
    List<Clause_library> findByStandard(ISO standard);
    
    List<Clause_library> findByStandardAndIsActive(ISO standard, boolean isActive);
    
    List<Clause_library> findByStandardAndCategory(ISO standard, String category);
    
    List<Clause_library> findByStandardAndRiskLevel(ISO standard, RiskLevel riskLevel);
    
    List<Clause_library> findByStandardAndVersion(ISO standard, String version);
    
    Optional<Clause_library> findByClauseNumberAndStandard(String clauseNumber, ISO standard);
    
    @Query("SELECT c FROM Clause_library c WHERE c.standard = :standard AND c.effectiveDate <= :date AND c.isActive = true")
    List<Clause_library> findActiveClausesByStandardAndDate(@Param("standard") ISO standard, @Param("date") LocalDateTime date);
    
    @Query("SELECT c FROM Clause_library c WHERE c.clauseName LIKE %:keyword% OR c.description LIKE %:keyword%")
    List<Clause_library> searchClausesByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT c FROM Clause_library c WHERE c.standard = :standard AND (c.clauseName LIKE %:keyword% OR c.description LIKE %:keyword%)")
    List<Clause_library> searchClausesByStandardAndKeyword(@Param("standard") ISO standard, @Param("keyword") String keyword);
    
    List<Clause_library> findByIsActiveOrderByStandardAscClauseNumberAsc(boolean isActive);
    
    Optional<Clause_library> findByClauseNumber(String clauseNumber);
}
