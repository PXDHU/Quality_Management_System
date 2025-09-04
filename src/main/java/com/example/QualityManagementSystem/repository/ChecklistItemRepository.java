package com.example.QualityManagementSystem.repository;

import com.example.QualityManagementSystem.model.Checklist_item;
import com.example.QualityManagementSystem.model.Checklist;
import com.example.QualityManagementSystem.model.ConformityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChecklistItemRepository extends JpaRepository<Checklist_item, Long> {
    List<Checklist_item> findByChecklist(Checklist checklist);
    
    List<Checklist_item> findByChecklist_ChecklistId(Long checklistId);

    @Query("SELECT ci FROM Checklist_item ci JOIN FETCH ci.checklist c JOIN FETCH c.audit WHERE ci.itemId = :itemId")
    Optional<Checklist_item> findByIdWithChecklistAndAudit(@Param("itemId") Long itemId);
    
    List<Checklist_item> findByChecklist_ChecklistIdAndConformityStatus(Long checklistId, ConformityStatus status);
    
    @Query("SELECT COUNT(ci) FROM Checklist_item ci WHERE ci.checklist.audit.auditId = :auditId")
    long countByAuditId(@Param("auditId") Long auditId);
    
    @Query("SELECT COUNT(ci) FROM Checklist_item ci WHERE ci.checklist.audit.auditId = :auditId AND ci.conformityStatus IS NOT NULL")
    long countEvaluatedByAuditId(@Param("auditId") Long auditId);
    
    @Query("SELECT ci FROM Checklist_item ci WHERE ci.checklist.audit.auditId = :auditId AND ci.conformityStatus IS NULL")
    List<Checklist_item> findUnevaluatedByAuditId(@Param("auditId") Long auditId);
    
    @Query("SELECT ci FROM Checklist_item ci WHERE ci.checklist.audit.auditId = :auditId AND ci.conformityStatus = :status")
    List<Checklist_item> findByAuditIdAndStatus(@Param("auditId") Long auditId, @Param("status") ConformityStatus status);
    
    @Query("SELECT COUNT(ci) FROM Checklist_item ci WHERE ci.checklist.audit.auditId = :auditId AND ci.conformityStatus = :status")
    long countByAuditIdAndConformityStatus(@Param("auditId") Long auditId, @Param("status") ConformityStatus status);
}
