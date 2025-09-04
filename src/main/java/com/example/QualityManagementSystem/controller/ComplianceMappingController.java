package com.example.QualityManagementSystem.controller;

import com.example.QualityManagementSystem.dto.ComplianceMappingRequest;
import com.example.QualityManagementSystem.dto.ComplianceMappingResponse;
import com.example.QualityManagementSystem.dto.ComplianceMatrixResponse;
import com.example.QualityManagementSystem.model.ISO;
import com.example.QualityManagementSystem.model.MappingType;
import com.example.QualityManagementSystem.service.ComplianceMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compliance-mapping")
@CrossOrigin(origins = "*")
public class ComplianceMappingController {

    @Autowired
    private ComplianceMappingService complianceMappingService;

    // Create new compliance mapping (Compliance Officer, Admin)
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','ADMIN')")
    @PostMapping
    public ResponseEntity<ComplianceMappingResponse> createMapping(@RequestBody ComplianceMappingRequest request) {
        Long createdBy = getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(complianceMappingService.createMapping(request, createdBy));
    }

    // Update existing mapping (Compliance Officer, Admin)
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','ADMIN')")
    @PutMapping("/{mappingId}")
    public ResponseEntity<ComplianceMappingResponse> updateMapping(
            @PathVariable Long mappingId, 
            @RequestBody ComplianceMappingRequest request) {
        return ResponseEntity.ok(complianceMappingService.updateMapping(mappingId, request));
    }

    // Get mapping by ID (All authenticated users)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{mappingId}")
    public ResponseEntity<ComplianceMappingResponse> getMappingById(@PathVariable Long mappingId) {
        return ResponseEntity.ok(complianceMappingService.getMappingById(mappingId));
    }

    // Get all mappings (All authenticated users)
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<ComplianceMappingResponse>> getAllMappings() {
        return ResponseEntity.ok(complianceMappingService.getAllMappings());
    }

    // Get mappings between standards (All authenticated users)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/standards")
    public ResponseEntity<List<ComplianceMappingResponse>> getMappingsByStandards(
            @RequestParam ISO sourceStandard, 
            @RequestParam ISO targetStandard) {
        return ResponseEntity.ok(complianceMappingService.getMappingsByStandards(sourceStandard, targetStandard));
    }

    // Get mappings by clause ID (All authenticated users)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/clause/{clauseId}")
    public ResponseEntity<List<ComplianceMappingResponse>> getMappingsByClauseId(@PathVariable Long clauseId) {
        return ResponseEntity.ok(complianceMappingService.getMappingsByClauseId(clauseId));
    }

    // Get mappings by type (All authenticated users)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/type/{mappingType}")
    public ResponseEntity<List<ComplianceMappingResponse>> getMappingsByType(@PathVariable MappingType mappingType) {
        return ResponseEntity.ok(complianceMappingService.getMappingsByType(mappingType));
    }

    // Get verified mappings (All authenticated users)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/verified")
    public ResponseEntity<List<ComplianceMappingResponse>> getVerifiedMappings() {
        return ResponseEntity.ok(complianceMappingService.getVerifiedMappings());
    }

    // Get pending verification mappings (Compliance Officer, Admin)
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','ADMIN')")
    @GetMapping("/pending-verification")
    public ResponseEntity<List<ComplianceMappingResponse>> getPendingVerificationMappings() {
        return ResponseEntity.ok(complianceMappingService.getPendingVerificationMappings());
    }

    // Verify mapping (Compliance Officer, Admin)
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','ADMIN')")
    @PatchMapping("/{mappingId}/verify")
    public ResponseEntity<ComplianceMappingResponse> verifyMapping(@PathVariable Long mappingId) {
        Long verifiedBy = getCurrentUserId();
        return ResponseEntity.ok(complianceMappingService.verifyMapping(mappingId, verifiedBy));
    }

    // Delete mapping (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{mappingId}")
    public ResponseEntity<Void> deleteMapping(@PathVariable Long mappingId) {
        complianceMappingService.deleteMapping(mappingId);
        return ResponseEntity.noContent().build();
    }

    // Get compliance matrix (All authenticated users)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/matrix")
    public ResponseEntity<ComplianceMatrixResponse> getComplianceMatrix() {
        return ResponseEntity.ok(complianceMappingService.getComplianceMatrix());
    }

    // Get mappings by minimum similarity score (All authenticated users)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/similarity")
    public ResponseEntity<List<ComplianceMappingResponse>> getMappingsBySimilarityScore(
            @RequestParam Double minScore) {
        return ResponseEntity.ok(complianceMappingService.getMappingsBySimilarityScore(minScore));
    }

    // Get mappings by source standard (All authenticated users)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/source-standard/{standard}")
    public ResponseEntity<List<ComplianceMappingResponse>> getMappingsBySourceStandard(@PathVariable ISO standard) {
        return ResponseEntity.ok(complianceMappingService.getMappingsBySourceStandard(standard));
    }

    // Get mappings by target standard (All authenticated users)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/target-standard/{standard}")
    public ResponseEntity<List<ComplianceMappingResponse>> getMappingsByTargetStandard(@PathVariable ISO standard) {
        return ResponseEntity.ok(complianceMappingService.getMappingsByTargetStandard(standard));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // This is a simplified approach - in a real application, you'd get the user ID from the JWT token
        // For now, returning a default value
        return 1L; // Default admin user ID
    }
}
