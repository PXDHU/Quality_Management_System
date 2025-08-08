package com.example.QualityManagementSystem.dto;

import lombok.Data;

@Data
public class AuditProgress {
    private int totalClauses;
    private int evaluatedClauses;
    private double completionPercentage;
}
