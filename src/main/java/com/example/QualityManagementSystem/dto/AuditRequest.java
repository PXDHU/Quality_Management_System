package com.example.QualityManagementSystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public class AuditRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    public String title;

    @NotBlank(message = "Scope is required")
    @Size(min = 10, max = 1000, message = "Scope must be between 10 and 1000 characters")
    public String scope;

    @Size(max = 1000, message = "Objectives must not exceed 1000 characters")
    public String objectives;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    public LocalDate startDate;

    @NotNull(message = "End date is required")
    public LocalDate endDate;

    @Size(min = 1, message = "At least one auditor must be assigned")
    public List<Long> auditorIds;

    // Additional fields for enhanced audit planning
    public String auditType; // INTERNAL, EXTERNAL, SUPPLIER, etc.
    public String department; // Department being audited
    public String location; // Location of the audit
    public String notes; // Additional notes for the audit
}
