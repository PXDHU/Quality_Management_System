package com.example.QualityManagementSystem.repository;

import com.example.QualityManagementSystem.model.ChecklistTemplate;
import com.example.QualityManagementSystem.model.ISO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChecklistTemplateRepository extends JpaRepository<ChecklistTemplate, Long> {
    List<ChecklistTemplate> findByIsoStandardAndIsActiveTrue(ISO isoStandard);
    List<ChecklistTemplate> findByIsActiveTrue();
    List<ChecklistTemplate> findByTemplateNameContainingIgnoreCaseAndIsActiveTrue(String templateName);
}
