package com.example.QualityManagementSystem.repository;

import com.example.QualityManagementSystem.model.Checklist;
import com.example.QualityManagementSystem.model.ISO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChecklistRepository extends JpaRepository<Checklist, Long> {
    List<Checklist> findByIsoStandard(ISO isoStandard);
    List<Checklist> findByAudit_AuditId(Long auditId);
}
