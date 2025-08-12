package com.example.QualityManagementSystem.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class DocumentResponse {
    private Long id;
    private String fileName;
    private String fileType;
    private String description;
    private LocalDateTime uploadedAt;
    private String uploadedBy;
    private Set<String> auditTitles; // Titles of linked audits
}
