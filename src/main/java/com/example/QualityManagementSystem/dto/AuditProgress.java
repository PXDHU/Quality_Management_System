package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditProgress {
    public Long auditId;
    public String title;
    public Status status;
    public LocalDate startDate;
    public LocalDate endDate;
    
    // Progress metrics
    public Integer totalChecklists;
    public Integer completedChecklists;
    public Integer pendingChecklists;
    public Double progressPercentage;
    
    // Clause-level progress metrics
    public Integer totalClauses;
    public Integer evaluatedClauses;
    public Double completionPercentage;
    
    // Timeline tracking
    public LocalDateTime lastActivity;
    public String lastActivityBy;
    public String currentPhase; // PLANNING, EXECUTION, REPORTING, FOLLOW_UP
    
    // Status indicators
    public Boolean isOverdue;
    public Boolean isOnTrack;
    public String estimatedCompletion;
}
