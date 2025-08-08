package com.example.QualityManagementSystem.dto;

import java.time.LocalDate;
import java.util.Set;

public class AuditRequest {
    public String title;
    public String scope;
    public String objectives;
    public LocalDate startDate;
    public LocalDate endDate;
    public Set<Long> auditorIds;
}
