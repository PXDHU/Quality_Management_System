package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditFilterRequest {
    public Status status;
    public String auditType;
    public String department;
    public String location;
    public Long auditorId;
    public LocalDate startDateFrom;
    public LocalDate startDateTo;
    public LocalDate endDateFrom;
    public LocalDate endDateTo;
    public String searchTerm; // Search in title, scope, objectives
    public Boolean isOverdue;
    public Boolean isOnTrack;
    public String sortBy; // title, startDate, endDate, status, createdBy
    public String sortOrder; // ASC, DESC
    public Integer page;
    public Integer size;
}
