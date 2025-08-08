package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.Status;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public class AuditResponse {
    public Long id;
    public String title;
    public String scope;
    public String objectives;
    public LocalDate startDate;
    public LocalDate endDate;
    public Status status;
    public Set<String> auditorNames;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
