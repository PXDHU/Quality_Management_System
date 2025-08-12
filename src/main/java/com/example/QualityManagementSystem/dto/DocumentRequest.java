package com.example.QualityManagementSystem.dto;

import lombok.Data;

import java.util.Set;

@Data
public class DocumentRequest {
    private String description;
    private Set<Long> auditIds; // Link document to multiple audits
}

