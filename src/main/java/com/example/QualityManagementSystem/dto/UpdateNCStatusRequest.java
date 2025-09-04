package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.Status;
import lombok.Data;

@Data
public class UpdateNCStatusRequest {
    private Status status;
}
