package com.example.QualityManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportFilterRequest {
    public Long auditorId; // filter audits/NCs by assigned auditor
    public String department; // filter by audit.department
    public LocalDate startDateFrom; // audit.start_date from
    public LocalDate startDateTo;   // audit.start_date to
    public Long clauseId; // filter NCs by clauseId and audits that have such NCs
}



