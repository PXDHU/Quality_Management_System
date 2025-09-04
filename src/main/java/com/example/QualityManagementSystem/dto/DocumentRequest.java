package com.example.QualityManagementSystem.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class DocumentRequest {
    public MultipartFile file;
    public String description;
    public List<Long> auditIds;
    public List<Long> ncIds; // Non-conformity IDs for evidence linking
    public String tags; // Comma-separated tags
    public String clauseReference; // ISO clause reference
    public String department; // Department related to document
    public Boolean isEvidence = false; // Whether this is evidence for NCs
}
