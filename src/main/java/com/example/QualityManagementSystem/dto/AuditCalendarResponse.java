package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditCalendarResponse {
    public Long id;
    public String title;
    public String scope;
    public LocalDate startDate;
    public LocalDate endDate;
    public Status status;
    public String auditType;
    public String department;
    public String location;
    public List<String> auditorNames;
    public String createdByName;
    
    // Calendar-specific fields
    public String backgroundColor; // Color based on status
    public String borderColor; // Border color for calendar events
    public Boolean allDay; // Whether it's an all-day event
    public String url; // URL to view audit details
}
