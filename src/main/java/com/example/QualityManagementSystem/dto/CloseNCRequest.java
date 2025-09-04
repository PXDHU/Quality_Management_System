package com.example.QualityManagementSystem.dto;

import lombok.Data;

import java.util.List;

@Data
public class CloseNCRequest {
    // Reviewer approval is implied by calling endpoint as reviewer
    private List<String> finalEvidenceIds; // attach evidence IDs for closure
    private String reviewerComment;        // optional
}
