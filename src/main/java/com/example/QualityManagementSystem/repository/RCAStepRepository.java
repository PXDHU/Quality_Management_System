package com.example.QualityManagementSystem.repository;

import com.example.QualityManagementSystem.model.RCAStep;
import com.example.QualityManagementSystem.model.NonConformity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RCAStepRepository extends JpaRepository<RCAStep, Long> {
    void deleteByNc(NonConformity nc);
}
