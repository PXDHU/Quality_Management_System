package com.example.QualityManagementSystem.repository;

import com.example.QualityManagementSystem.model.Checklist;
import com.example.QualityManagementSystem.model.ISO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChecklistRepository extends JpaRepository<Checklist, Long> {
    List<Checklist> findByIsoStandard(ISO isoStandard);

    List<Checklist> findByAudit_AuditId(Long auditId);

    // ✅ Method to fetch checklists with their items + clauses + evidence docs
    // Uses JOIN FETCH to avoid lazy loading issues
    @Query("SELECT DISTINCT c FROM Checklist c " +
            "LEFT JOIN FETCH c.checklistItems i " +
            "LEFT JOIN FETCH i.clause cl " +
            "LEFT JOIN FETCH i.evidenceDocuments ed " +
            "WHERE c.audit.auditId = :auditId")
    List<Checklist> findByAuditIdWithItems(@Param("auditId") Long auditId);
    
    // Simpler method to test basic checklist loading
    @Query("SELECT c FROM Checklist c WHERE c.audit.auditId = :auditId")
    List<Checklist> findByAuditIdSimple(@Param("auditId") Long auditId);
    
    // Method to fetch checklists with items using a simpler approach
    // This avoids the MultipleBagFetchException by not fetching evidence documents
    @Query("SELECT DISTINCT c FROM Checklist c " +
            "LEFT JOIN FETCH c.checklistItems i " +
            "LEFT JOIN FETCH i.clause " +
            "WHERE c.audit.auditId = :auditId")
    List<Checklist> findByAuditIdWithItemsSimple(@Param("auditId") Long auditId);
    
    // Method to fetch checklists with items and evidence documents separately
    // This avoids the MultipleBagFetchException by fetching in stages
    @Query("SELECT DISTINCT c FROM Checklist c " +
            "LEFT JOIN FETCH c.checklistItems i " +
            "LEFT JOIN FETCH i.clause " +
            "WHERE c.audit.auditId = :auditId")
    List<Checklist> findByAuditIdWithItemsAndClauses(@Param("auditId") Long auditId);
}
