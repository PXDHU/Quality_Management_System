package com.example.QualityManagementSystem.controller;

import com.example.QualityManagementSystem.dto.AuditRequest;
import com.example.QualityManagementSystem.dto.AuditResponse;
import com.example.QualityManagementSystem.dto.UpdateAuditStatus;
import com.example.QualityManagementSystem.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audits")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    // Only AUDITOR can create audit plans
    @PreAuthorize("hasRole('AUDITOR')")
    @PostMapping
    public ResponseEntity<AuditResponse> createAudit(
            @RequestBody AuditRequest request,
            @RequestHeader("X-User-Id") Long userId // Simulated login
    ) {
        return ResponseEntity.ok(auditService.createAudit(request, userId));
    }

    // Both ADMIN and AUDITOR can view all audits
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @GetMapping
    public ResponseEntity<List<AuditResponse>> getAll() {
        return ResponseEntity.ok(auditService.getAllAudits());
    }

    // Both ADMIN and AUDITOR can view specific audit
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @GetMapping("/{id}")
    public ResponseEntity<AuditResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(auditService.getAuditById(id));
    }

    // Only AUDITOR can update audit status
    @PreAuthorize("hasRole('AUDITOR')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<AuditResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateAuditStatus statusDTO
    ) {
        return ResponseEntity.ok(auditService.updateAuditStatus(id, statusDTO));
    }
}
