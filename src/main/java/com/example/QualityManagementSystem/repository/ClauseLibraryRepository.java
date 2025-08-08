package com.example.QualityManagementSystem.repository;

import com.example.QualityManagementSystem.model.Clause_library;
import com.example.QualityManagementSystem.model.ISO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClauseLibraryRepository extends JpaRepository<Clause_library, Long> {
    List<Clause_library> findByStandard(ISO standard);
}
