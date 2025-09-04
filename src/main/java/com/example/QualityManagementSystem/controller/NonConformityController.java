package com.example.QualityManagementSystem.controller;

import com.example.QualityManagementSystem.dto.*;
import com.example.QualityManagementSystem.model.Status;
import com.example.QualityManagementSystem.model.Severity;
import com.example.QualityManagementSystem.service.NonConformityService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nc")
@CrossOrigin(origins = "*")
public class NonConformityController {

    private final NonConformityService service;

    public NonConformityController(NonConformityService service) {
        this.service = service;
    }

    // NC Identification (Auditor)
    @PreAuthorize("hasAnyRole('AUDITOR','ADMIN')")
    @PostMapping
    public ResponseEntity<NCResponse> createNC(@RequestBody CreateNCRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createNC(req));
    }

    // Get NC by id
    @PreAuthorize("hasAnyRole('AUDITOR','ADMIN','REVIEWER')")
    @GetMapping("/{ncId}")
    public ResponseEntity<NCResponse> getOne(@PathVariable Long ncId) {
        return ResponseEntity.ok(service.getOne(ncId));
    }

    // List by audit
    @PreAuthorize("hasAnyRole('AUDITOR','ADMIN','REVIEWER')")
    @GetMapping("/audit/{auditId}")
    public ResponseEntity<List<NCResponse>> listByAudit(@PathVariable Long auditId) {
        return ResponseEntity.ok(service.listByAudit(auditId));
    }

    // NC Dashboard for assignee (Auditee): filter by status (optional)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/assignee/{userId}")
    public ResponseEntity<List<NCResponse>> listAssigned(
            @PathVariable Long userId,
            @RequestParam(value = "status", required = false) Status status
    ) {
        return ResponseEntity.ok(service.listAssigned(userId, status));
    }

    // Assign Corrective Action (Auditor)
    @PreAuthorize("hasAnyRole('AUDITOR','ADMIN')")
    @PostMapping("/{ncId}/actions")
    public ResponseEntity<NCResponse> addAction(@PathVariable Long ncId, @RequestBody CorrectiveActionRequest req) {
        return ResponseEntity.ok(service.addCorrectiveAction(ncId, req));
    }

    // Update Corrective Action Status (Responsible or Auditor/Admin)
    @PreAuthorize("hasAnyRole('AUDITOR','ADMIN')")
    @PatchMapping("/actions/{actionId}/status")
    public ResponseEntity<NCResponse> updateActionStatus(@PathVariable Long actionId,
                                                         @RequestBody UpdateActionStatusRequest req) {
        return ResponseEntity.ok(service.updateActionStatus(actionId, req));
    }

    // Approve or Reject Corrective Action completion (Reviewer/Admin)
    @PreAuthorize("hasAnyRole('REVIEWER','ADMIN')")
    @PostMapping("/actions/{actionId}/approval")
    public ResponseEntity<NCResponse> approveOrRejectAction(@PathVariable Long actionId,
                                                            @RequestBody ActionApprovalRequest body) {
        return ResponseEntity.ok(service.approveOrRejectAction(actionId, body.getReviewerUserId(), body.isApproved(), body.getComments()));
    }

    // Submit RCA (3–5 Why) for major NCs (Auditor or Admin)
    @PreAuthorize("hasAnyRole('AUDITOR','ADMIN')")
    @PostMapping("/{ncId}/rca")
    public ResponseEntity<NCResponse> submitRCA(@PathVariable Long ncId, @RequestBody RCARequest rca) {
        return ResponseEntity.ok(service.submitRCA(ncId, rca));
    }

    // NC Closure (Reviewer validates corrective actions and evidence)
    @PreAuthorize("hasAnyRole('REVIEWER','ADMIN')")
    @PostMapping("/{ncId}/close")
    public ResponseEntity<NCResponse> closeNC(@PathVariable Long ncId, @RequestBody CloseNCRequest req) {
        return ResponseEntity.ok(service.closeNC(ncId, req));
    }

    // Get all NCs with filtering
    @PreAuthorize("hasAnyRole('AUDITOR','ADMIN','REVIEWER')")
    @GetMapping
    public ResponseEntity<List<NCResponse>> getAllNCs(
            @RequestParam(value = "status", required = false) Status status,
            @RequestParam(value = "severity", required = false) Severity severity,
            @RequestParam(value = "auditId", required = false) Long auditId
    ) {
        return ResponseEntity.ok(service.getAllNCs(status, severity, auditId));
    }

    // Get NCs by severity
    @PreAuthorize("hasAnyRole('AUDITOR','ADMIN','REVIEWER')")
    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<NCResponse>> getNCsBySeverity(@PathVariable Severity severity) {
        return ResponseEntity.ok(service.getNCsBySeverity(severity));
    }

    // Get overdue NCs
    @PreAuthorize("hasAnyRole('AUDITOR','ADMIN','REVIEWER')")
    @GetMapping("/overdue")
    public ResponseEntity<List<NCResponse>> getOverdueNCs() {
        return ResponseEntity.ok(service.getOverdueNCs());
    }

    // Update NC status
    @PreAuthorize("hasAnyRole('AUDITOR','ADMIN')")
    @PatchMapping("/{ncId}/status")
    public ResponseEntity<NCResponse> updateNCStatus(
            @PathVariable Long ncId,
            @RequestBody UpdateNCStatusRequest req
    ) {
        return ResponseEntity.ok(service.updateNCStatus(ncId, req.getStatus()));
    }
}
