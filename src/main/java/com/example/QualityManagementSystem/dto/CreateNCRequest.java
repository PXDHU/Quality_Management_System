package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.Severity;
import lombok.Data;

@Data
public class CreateNCRequest {
    private Long auditId;        // required
    private Long instanceId;     // optional
    private Long clauseId;       // optional
    private String title;        // required
    private String description;  // required
    private Severity severity; // LOW/MEDIUM/HIGH
    private Long assignedToId;   // user to assign NC to (auditee)
    private Long createdById;    // who raised the NC (auditor)
}
