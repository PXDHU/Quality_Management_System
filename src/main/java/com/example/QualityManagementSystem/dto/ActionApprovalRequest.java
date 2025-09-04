package com.example.QualityManagementSystem.dto;

import lombok.Data;

@Data
public class ActionApprovalRequest {
    private Long reviewerUserId;
    private boolean approved;
    private String comments;
}


