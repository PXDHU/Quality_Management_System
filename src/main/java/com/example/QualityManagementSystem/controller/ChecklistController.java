package com.example.QualityManagementSystem.controller;

import com.example.QualityManagementSystem.dto.*;
import com.example.QualityManagementSystem.service.ChecklistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checklists")
public class ChecklistController {

    private final ChecklistService checklistService;

    public ChecklistController(ChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    // Create Checklist Template (ISO 9001/27001) — AUDITOR only
    @PreAuthorize("hasRole('AUDITOR')")
    @PostMapping("/templates")
    public ResponseEntity<Long> createTemplate(@RequestBody ChecklistTemplate dto) {
        Long checklistId = checklistService.createChecklistTemplate(dto);
        return ResponseEntity.ok(checklistId);
    }

    // Assign Checklist Template to Audit — AUDITOR only
    @PreAuthorize("hasRole('AUDITOR')")
    @PostMapping("/audit/{auditId}/assign")
    public ResponseEntity<Void> assignChecklist(
            @PathVariable Long auditId,
            @RequestBody AssignChecklistToAudit dto
    ) {
        checklistService.assignChecklistToAudit(auditId, dto);
        return ResponseEntity.ok().build();
    }

    // Get Checklist by Audit ID — AUDITOR, ADMIN, REVIEWER
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN', 'REVIEWER')")
    @GetMapping("/audit/{auditId}")
    public ResponseEntity<ChecklistResponse> getChecklist(@PathVariable Long auditId) {
        ChecklistResponse dto = checklistService.getChecklistByAudit(auditId);
        return ResponseEntity.ok(dto);
    }

    // Evaluate Clause — AUDITOR only
    @PreAuthorize("hasRole('AUDITOR')")
    @PatchMapping("/clauses/{instanceId}/evaluate")
    public ResponseEntity<Void> evaluateClause(
            @PathVariable Long instanceId,
            @RequestBody ClauseEvaluation dto
    ) {
        checklistService.evaluateClause(instanceId, dto);
        return ResponseEntity.ok().build();
    }
    // Audit Progress — AUDITOR, ADMIN, REVIEWER
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN', 'REVIEWER')")
    @GetMapping("/audit/{auditId}/progress")
    public ResponseEntity<AuditProgress> getAuditProgress(@PathVariable Long auditId) {
        return ResponseEntity.ok(checklistService.getAuditProgress(auditId));
    }
}
