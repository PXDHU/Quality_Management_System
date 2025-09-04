package com.example.QualityManagementSystem.controller;

import com.example.QualityManagementSystem.model.AuditReport;
import com.example.QualityManagementSystem.service.AuditReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/audit-reports")
@CrossOrigin(origins = "*")
public class AuditReportController {

    private final AuditReportService auditReportService;

    public AuditReportController(AuditReportService auditReportService) {
        this.auditReportService = auditReportService;
    }

    // Submit audit report for approval
    @PreAuthorize("hasAnyRole('AUDITOR','ADMIN')")
    @PostMapping
    public ResponseEntity<AuditReport> submit(@RequestBody Map<String, Object> body) {
        Long auditId = ((Number) body.get("auditId")).longValue();
        Long submittedBy = ((Number) body.get("submittedByUserId")).longValue();
        String content = (String) body.getOrDefault("content", "");
        Long reviewerId = body.get("reviewerUserId") != null ? ((Number) body.get("reviewerUserId")).longValue() : null;
        return ResponseEntity.ok(auditReportService.submitReport(auditId, submittedBy, content, reviewerId));
    }

    // Approve report
    @PreAuthorize("hasAnyRole('REVIEWER','ADMIN')")
    @PostMapping("/{id}/approve")
    public ResponseEntity<AuditReport> approve(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long reviewerId = ((Number) body.get("reviewerUserId")).longValue();
        String comments = (String) body.getOrDefault("comments", "");
        return ResponseEntity.ok(auditReportService.approveReport(id, reviewerId, comments));
    }

    // Reject report
    @PreAuthorize("hasAnyRole('REVIEWER','ADMIN')")
    @PostMapping("/{id}/reject")
    public ResponseEntity<AuditReport> reject(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long reviewerId = ((Number) body.get("reviewerUserId")).longValue();
        String comments = (String) body.getOrDefault("comments", "");
        return ResponseEntity.ok(auditReportService.rejectReport(id, reviewerId, comments));
    }
}


