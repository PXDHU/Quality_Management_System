package com.example.QualityManagementSystem.repository;

import com.example.QualityManagementSystem.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}
