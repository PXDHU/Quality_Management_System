package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAuditStatus {

    public Status status;

}
