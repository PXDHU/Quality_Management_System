package com.example.QualityManagementSystem.controller;

import com.example.QualityManagementSystem.dto.ClauseLibraryRequest;
import com.example.QualityManagementSystem.dto.ClauseLibraryResponse;
import com.example.QualityManagementSystem.model.ISO;
import com.example.QualityManagementSystem.model.RiskLevel;
import com.example.QualityManagementSystem.service.ClauseLibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/clauses")
@CrossOrigin(origins = "*")
public class ClauseLibraryController {

    @Autowired
    private ClauseLibraryService clauseLibraryService;

    // Create new clause (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ClauseLibraryResponse> createClause(@RequestBody ClauseLibraryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clauseLibraryService.createClause(request));
    }

    // Update existing clause (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{clauseId}")
    public ResponseEntity<ClauseLibraryResponse> updateClause(
            @PathVariable Long clauseId, 
            @RequestBody ClauseLibraryRequest request) {
        return ResponseEntity.ok(clauseLibraryService.updateClause(clauseId, request));
    }

    // Get clause by ID (All authenticated users)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{clauseId}")
    public ResponseEntity<ClauseLibraryResponse> getClauseById(@PathVariable Long clauseId) {
        return ResponseEntity.ok(clauseLibraryService.getClauseById(clauseId));
    }

    // Get all clauses (All authenticated users)
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<ClauseLibraryResponse>> getAllClauses() {
        return ResponseEntity.ok(clauseLibraryService.getAllClauses());
    }

    // Get clauses by standard (All authenticated users)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/standard/{standard}")
    public ResponseEntity<List<ClauseLibraryResponse>> getClausesByStandard(@PathVariable ISO standard) {
        return ResponseEntity.ok(clauseLibraryService.getClausesByStandard(standard));
    }

    // Get clauses by standard and category (All authenticated users)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/standard/{standard}/category/{category}")
    public ResponseEntity<List<ClauseLibraryResponse>> getClausesByStandardAndCategory(
            @PathVariable ISO standard, 
            @PathVariable String category) {
        return ResponseEntity.ok(clauseLibraryService.getClausesByStandardAndCategory(standard, category));
    }

    // Get clauses by standard and risk level (All authenticated users)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/standard/{standard}/risk/{riskLevel}")
    public ResponseEntity<List<ClauseLibraryResponse>> getClausesByStandardAndRiskLevel(
            @PathVariable ISO standard, 
            @PathVariable RiskLevel riskLevel) {
        return ResponseEntity.ok(clauseLibraryService.getClausesByStandardAndRiskLevel(standard, riskLevel));
    }

    // Search clauses by keyword (All authenticated users)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search")
    public ResponseEntity<List<ClauseLibraryResponse>> searchClauses(
            @RequestParam String keyword) {
        return ResponseEntity.ok(clauseLibraryService.searchClauses(keyword));
    }

    // Search clauses by standard and keyword (All authenticated users)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search/{standard}")
    public ResponseEntity<List<ClauseLibraryResponse>> searchClausesByStandardAndKeyword(
            @PathVariable ISO standard, 
            @RequestParam String keyword) {
        return ResponseEntity.ok(clauseLibraryService.searchClausesByStandardAndKeyword(standard, keyword));
    }

    // Get active clauses by standard and date (All authenticated users)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/standard/{standard}/active")
    public ResponseEntity<List<ClauseLibraryResponse>> getActiveClausesByStandardAndDate(
            @PathVariable ISO standard, 
            @RequestParam LocalDateTime date) {
        return ResponseEntity.ok(clauseLibraryService.getActiveClausesByStandardAndDate(standard, date));
    }

    // Deactivate clause (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{clauseId}/deactivate")
    public ResponseEntity<Void> deactivateClause(@PathVariable Long clauseId) {
        clauseLibraryService.deactivateClause(clauseId);
        return ResponseEntity.noContent().build();
    }

    // Activate clause (Admin only)
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{clauseId}/activate")
    public ResponseEntity<Void> activateClause(@PathVariable Long clauseId) {
        clauseLibraryService.activateClause(clauseId);
        return ResponseEntity.noContent().build();
    }
}
