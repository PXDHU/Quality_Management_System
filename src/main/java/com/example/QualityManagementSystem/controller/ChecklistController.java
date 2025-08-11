package com.example.QualityManagementSystem.controller;

import com.example.QualityManagementSystem.dto.*;
import com.example.QualityManagementSystem.model.ISO;
import com.example.QualityManagementSystem.service.ChecklistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checklists")
public class ChecklistController {

    private final ChecklistService checklistService;

    public ChecklistController(ChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    @PreAuthorize("hasRole('AUDITOR')")
    @PostMapping
    public ResponseEntity<ChecklistResponse> createChecklist(@RequestBody ChecklistRequest request) {
        ChecklistResponse created = checklistService.createChecklist(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasRole('AUDITOR')")
    @PostMapping("/templates")
    public ResponseEntity<Long> createTemplate(@RequestBody ChecklistTemplate dto) {
        Long checklistId = checklistService.createChecklistTemplate(dto);
        return ResponseEntity.ok(checklistId);
    }

    @PreAuthorize("hasRole('AUDITOR')")
    @PostMapping("/audit/{auditId}/assign")
    public ResponseEntity<Void> assignChecklist(@PathVariable Long auditId, @RequestBody AssignChecklistToAudit dto) {
        checklistService.assignChecklistToAudit(auditId, dto);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN', 'REVIEWER')")
    @GetMapping("/audit/{auditId}")
    public ResponseEntity<ChecklistResponse> getChecklist(@PathVariable Long auditId) {
        ChecklistResponse dto = checklistService.getChecklistByAudit(auditId);
        return ResponseEntity.ok(dto);
    }

    @PreAuthorize("hasRole('AUDITOR')")
    @PatchMapping("/clauses/{instanceId}/evaluate")
    public ResponseEntity<Void> evaluateClause(@PathVariable Long instanceId, @RequestBody ClauseEvaluation dto) {
        checklistService.evaluateClause(instanceId, dto);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN', 'REVIEWER')")
    @GetMapping("/audit/{auditId}/progress")
    public ResponseEntity<AuditProgress> getAuditProgress(@PathVariable Long auditId) {
        return ResponseEntity.ok(checklistService.getAuditProgress(auditId));
    }

    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN', 'REVIEWER')")
    @GetMapping
    public ResponseEntity<List<ChecklistResponse>> getAllChecklists() {
        return ResponseEntity.ok(checklistService.getAllChecklists());
    }

    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN', 'REVIEWER')")
    @GetMapping("/{id}")
    public ResponseEntity<ChecklistResponse> getChecklistById(@PathVariable Long id) {
        return ResponseEntity.ok(checklistService.getChecklistById(id));
    }

    @PreAuthorize("hasRole('AUDITOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ChecklistResponse> updateChecklist(@PathVariable Long id, @RequestBody ChecklistRequest request) {
        return ResponseEntity.ok(checklistService.updateChecklist(id, request));
    }

    @PreAuthorize("hasRole('AUDITOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChecklist(@PathVariable Long id) {
        checklistService.deleteChecklist(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN', 'REVIEWER')")
    @GetMapping("/iso/{isoStandard}")
    public ResponseEntity<List<ChecklistResponse>> getByISO(@PathVariable ISO isoStandard) {
        return ResponseEntity.ok(checklistService.getByIsoStandard(isoStandard));
    }
}
