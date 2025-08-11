package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.ISO;
import com.example.QualityManagementSystem.model.Status;
import lombok.Data;

@Data
public class ChecklistRequest {
    private ISO isoStandard;
    private Long auditId;
    private Status status;
}

