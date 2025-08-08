package com.example.QualityManagementSystem.repository;

import com.example.QualityManagementSystem.model.Audit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<Audit, Long> {
}
