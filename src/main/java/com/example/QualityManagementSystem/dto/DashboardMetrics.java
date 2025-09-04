package com.example.QualityManagementSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardMetrics {
    public Long totalAudits;
    public Long auditsPlanned;
    public Long auditsInProgress;
    public Long auditsCompleted;
    public Long auditsClosed;

    public Long totalNCs;
    public Long ncsPending;
    public Long ncsInProgress;
    public Long ncsCompleted;
    public Long ncsClosed;

    public Map<String, Long> auditsByDepartment;
    public Map<String, Long> ncsByDepartment;
    public Map<String, Long> ncsBySeverity;

    public Double complianceScoreOverall; // 0..100
    public Map<String, Double> complianceByDepartment; // department -> 0..100

    public List<TrendPoint> ncMonthlyTrend;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TrendPoint {
        public String period; // e.g. 2025-01
        public Long count;
    }
}


