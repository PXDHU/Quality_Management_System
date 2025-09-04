package com.example.QualityManagementSystem.dto;

import lombok.Data;
import java.util.List;

@Data
public class AssignChecklistToAudit {
    private Long templateId;
    private List<Long> clauseIds;
}
