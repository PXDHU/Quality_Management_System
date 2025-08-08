package com.example.QualityManagementSystem.dto;

import com.example.QualityManagementSystem.model.ISO;
import com.example.QualityManagementSystem.model.Status;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChecklistResponse {
    private Long checklistId;
    private ISO isoStandard;
    private Long auditId;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ChecklistItemDTO> checklistItems;

    @Data
    public static class ChecklistItemDTO {
        private Long itemId;
        private Long clauseId;
        private String clauseNumber;
        private String clauseName;
        private String customText;
        private String conformityStatus;
    }
}
