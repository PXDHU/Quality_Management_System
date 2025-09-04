package com.example.QualityManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private Long documentId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String description;
    private String uploadedBy;
    private LocalDateTime uploadedAt;

    // Always initialize mutable lists to avoid ConcurrentModificationException
    private List<Long> auditIds = new ArrayList<>();
    private List<String> auditTitles = new ArrayList<>();
    private List<Long> ncIds = new ArrayList<>();
    private List<String> ncTitles = new ArrayList<>();

    private String tags;
    private String clauseReference;
    private String department;
    private Boolean isEvidence;
    private LocalDateTime lastAccessed;
    private Integer accessCount;
}
