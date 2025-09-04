package com.example.QualityManagementSystem.repository;

import com.example.QualityManagementSystem.model.ChecklistTemplateItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChecklistTemplateItemRepository extends JpaRepository<ChecklistTemplateItem, Long> {
    List<ChecklistTemplateItem> findByTemplate_TemplateIdOrderBySortOrderAsc(Long templateId);
    void deleteByTemplate_TemplateId(Long templateId);
    
    // Find template items with null clauses (for cleanup)
    List<ChecklistTemplateItem> findByTemplate_TemplateIdAndClauseIsNull(Long templateId);
}
