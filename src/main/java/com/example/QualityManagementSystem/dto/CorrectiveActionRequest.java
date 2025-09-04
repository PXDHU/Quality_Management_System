package com.example.QualityManagementSystem.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CorrectiveActionRequest {
    private String description;   // required
    private Long responsibleId;   // required
    private LocalDate dueDate;    // optional
}
