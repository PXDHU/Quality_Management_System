package com.example.QualityManagementSystem.dto;

import lombok.Data;

import java.util.List;

@Data
public class RCARequest {
    private List<Step> steps; // 3–5 steps

    @Data
    public static class Step {
        private int stepNumber; // 1..5
        private String whyText;
    }
}
