package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.Status;
import lombok.Data;

@Data
public class UpdateActionStatusRequest {
    private Status status; // PENDING/IN_PROGRESS/COMPLETED
}
