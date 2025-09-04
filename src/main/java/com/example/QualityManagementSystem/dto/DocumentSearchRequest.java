package com.example.QualityManagementSystem.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DocumentSearchRequest {
    public String fileName;
    public String fileType;
    public String tags;
    public String clauseReference;
    public String department;
    public String uploadedBy;
    public LocalDate uploadedFrom;
    public LocalDate uploadedTo;
    public Long auditId;
    public Long ncId;
    public Boolean isEvidence;
    public String description; // Search in description
    public Integer page = 0;
    public Integer size = 20;
    public String sortBy = "uploadedAt";
    public String sortDirection = "DESC";
}
