package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.Severity;
import com.example.QualityManagementSystem.model.Status;
import com.example.QualityManagementSystem.model.Severity;
import com.example.QualityManagementSystem.model.Status;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class NCResponse {
    private Long nonConformityId;
    private Long auditId;
    private Long instanceId;
    private Long clauseId;

    private String title;
    private String description;
    private Severity severity;
    private Status status;

    private Long createdById;
    private Long assignedToId;

    private List<String> evidenceIds;

    private List<ActionDTO> actions;
    private List<RCAStepDTO> rcaSteps;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    public static class ActionDTO {
        private Long actionId;
        private String description;
        private Long responsibleId;
        private LocalDate dueDate;
        private String status; // PENDING/IN_PROGRESS/COMPLETED
    }

    @Data
    public static class RCAStepDTO {
        private Long rcaStepId;
        private int stepNumber;
        private String whyText;
    }
}
