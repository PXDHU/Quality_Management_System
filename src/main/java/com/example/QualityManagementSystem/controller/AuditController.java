package com.example.QualityManagementSystem.controller;

import com.example.QualityManagementSystem.dto.*;
import com.example.QualityManagementSystem.service.AuditService;
import com.example.QualityManagementSystem.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.example.QualityManagementSystem.repository.AuditRepository;
import com.example.QualityManagementSystem.model.Audit;
import com.example.QualityManagementSystem.model.Checklist;
import com.example.QualityManagementSystem.model.Checklist_item;

import java.util.Set;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/audits")
@CrossOrigin(origins = "*")
public class AuditController {

    private final AuditService auditService;
    private final UserService userService;
    private final AuditRepository auditRepo;

    public AuditController(AuditService auditService, UserService userService, AuditRepository auditRepo) {
        this.auditService = auditService;
        this.userService = userService;
        this.auditRepo = auditRepo;
    }

    // 1. POST /api/audits - Create Audit Plan (AUDITOR, ADMIN)
    @PreAuthorize("hasAnyRole('AUDITOR','ADMIN')")
    @PostMapping
    public ResponseEntity<AuditResponse> createAudit(@Valid @RequestBody AuditRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long creatorId = Long.valueOf(userService.getUserByUsername(auth.getName()).getUserId());
            return ResponseEntity.ok(auditService.createAudit(request, creatorId));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 2. GET /api/audits - Get all audits with optional filters (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @GetMapping
    public ResponseEntity<?> getAllAudits(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String auditType,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Boolean isOverdue) {
        
        try {
            System.out.println("DEBUG: getAllAudits called with params:");
            System.out.println("  status: " + status);
            System.out.println("  auditType: " + auditType);
            System.out.println("  department: " + department);
            System.out.println("  location: " + location);
            System.out.println("  searchTerm: " + searchTerm);
            System.out.println("  isOverdue: " + isOverdue);
            
            AuditFilterRequest filter = new AuditFilterRequest();
            if (status != null && !status.trim().isEmpty()) {
                try {
                    filter.setStatus(com.example.QualityManagementSystem.model.Status.valueOf(status.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Invalid status: " + status + ". Valid values: PLANNED, PENDING, IN_PROGRESS, COMPLETED, CLOSED");
                    return ResponseEntity.badRequest().body(error);
                }
            }
            filter.setAuditType(auditType);
            filter.setDepartment(department);
            filter.setLocation(location);
            filter.setSearchTerm(searchTerm);
            filter.setIsOverdue(isOverdue);
            
            System.out.println("DEBUG: Filter object created: " + filter);
            
            List<AuditResponse> result = auditService.getAuditsWithFilters(filter);
            System.out.println("DEBUG: Service returned " + result.size() + " audits");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("ERROR in getAllAudits: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch audits: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Test endpoint for debugging
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Audit controller is working");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    // Public test endpoint for debugging (no auth required)
    @GetMapping("/public-test")
    public ResponseEntity<Map<String, String>> publicTestEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Audit controller public endpoint is working");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        response.put("javaVersion", System.getProperty("java.version"));
        response.put("springVersion", org.springframework.core.SpringVersion.getVersion());
        return ResponseEntity.ok(response);
    }

    // Health check endpoint (no auth required)
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        try {
            // Test database connectivity
            long auditCount = auditRepo.count();
            response.put("database", "UP");
            response.put("auditCount", auditCount);
        } catch (Exception e) {
            response.put("database", "DOWN");
            response.put("databaseError", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    // 3. GET /api/audits/{id} - Get specific audit (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @GetMapping("/{id}")
    public ResponseEntity<AuditResponse> getAuditById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(auditService.getAuditById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 4. PUT /api/audits/{id} - Update audit (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @PutMapping("/{id}")
    public ResponseEntity<AuditResponse> updateAudit(@PathVariable Long id, @RequestBody AuditRequest request) {
        try {
            return ResponseEntity.ok(auditService.updateAudit(id, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 5. PATCH /api/audits/{id}/status - Update audit status (AUDITOR)
    @PreAuthorize("hasRole('AUDITOR')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<AuditResponse> updateAuditStatus(
            @PathVariable Long id,
            @RequestBody UpdateAuditStatus statusDTO) {
        try {
            return ResponseEntity.ok(auditService.updateAuditStatus(id, statusDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 6. PATCH /api/audits/{id}/phase - Update audit phase (AUDITOR, ADMIN)
    @PreAuthorize("hasAnyRole('AUDITOR', 'ADMIN')")
    @PatchMapping("/{id}/phase")
    public ResponseEntity<AuditResponse> updateAuditPhase(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String phase = request.get("phase");
            if (phase == null || phase.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(auditService.updateAuditPhase(id, phase));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 7. GET /api/audits/calendar - Get audit calendar view (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @GetMapping("/calendar")
    public ResponseEntity<List<AuditCalendarResponse>> getAuditCalendar() {
        try {
            return ResponseEntity.ok(auditService.getAuditCalendar());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 8. GET /api/audits/progress - Get audit progress tracking (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @GetMapping("/progress")
    public ResponseEntity<List<AuditProgress>> getAuditProgress() {
        try {
            return ResponseEntity.ok(auditService.getAuditProgress());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 9. GET /api/audits/status/{status} - Get audits by status (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AuditResponse>> getAuditsByStatus(@PathVariable String status) {
        try {
            com.example.QualityManagementSystem.model.Status statusEnum = 
                com.example.QualityManagementSystem.model.Status.valueOf(status.toUpperCase());
            return ResponseEntity.ok(auditService.getAuditsByStatus(statusEnum));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 10. GET /api/audits/overdue - Get overdue audits (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @GetMapping("/overdue")
    public ResponseEntity<List<AuditResponse>> getOverdueAudits() {
        try {
            return ResponseEntity.ok(auditService.getOverdueAudits());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 11. GET /api/audits/upcoming - Get upcoming audits (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @GetMapping("/upcoming")
    public ResponseEntity<List<AuditResponse>> getUpcomingAudits() {
        try {
            return ResponseEntity.ok(auditService.getUpcomingAudits());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 12. DELETE /api/audits/{id} - Delete audit (ADMIN only)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteAudit(@PathVariable Long id) {
        try {
            auditService.deleteAudit(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Audit deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 13. POST /api/audits/create-checklists - Create checklists for existing audits (ADMIN only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create-checklists")
    public ResponseEntity<?> createChecklistsForExistingAudits() {
        try {
            List<Audit> auditsWithoutChecklists = auditService.getAuditsWithoutChecklists();
            int createdCount = 0;
            
            for (Audit audit : auditsWithoutChecklists) {
                try {
                    auditService.createDefaultChecklistForAudit(audit);
                    createdCount++;
                } catch (Exception e) {
                    System.err.println("Failed to create checklist for audit " + audit.getAuditId() + ": " + e.getMessage());
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Created checklists for " + createdCount + " audits");
            response.put("totalAudits", auditsWithoutChecklists.size());
            response.put("successfulCreations", createdCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Debug endpoint to check audit data structure
    @GetMapping("/debug/{id}")
    public ResponseEntity<?> debugAudit(@PathVariable Long id) {
        try {
            Map<String, Object> debugInfo = new HashMap<>();
            
            // Get audit with basic info
            Audit audit = auditRepo.findById(id).orElse(null);
            if (audit == null) {
                debugInfo.put("error", "Audit not found");
                return ResponseEntity.badRequest().body(debugInfo);
            }
            
            debugInfo.put("auditId", audit.getAuditId());
            debugInfo.put("title", audit.getTitle());
            debugInfo.put("status", audit.getStatus());
            
            // Check checklists
            Set<Checklist> checklists = audit.getChecklists();
            debugInfo.put("checklistsCount", checklists != null ? checklists.size() : 0);
            
            if (checklists != null && !checklists.isEmpty()) {
                List<Map<String, Object>> checklistDetails = new ArrayList<>();
                for (Checklist checklist : checklists) {
                    Map<String, Object> checklistInfo = new HashMap<>();
                    checklistInfo.put("checklistId", checklist.getChecklistId());
                    checklistInfo.put("isoStandard", checklist.getIsoStandard());
                    checklistInfo.put("status", checklist.getStatus());
                    
                    Set<Checklist_item> items = checklist.getChecklistItems();
                    checklistInfo.put("itemsCount", items != null ? items.size() : 0);
                    
                    if (items != null && !items.isEmpty()) {
                        List<Map<String, Object>> itemDetails = new ArrayList<>();
                        for (Checklist_item item : items) {
                            Map<String, Object> itemInfo = new HashMap<>();
                            itemInfo.put("itemId", item.getItemId());
                            itemInfo.put("clauseId", item.getClause() != null ? item.getClause().getClauseId() : null);
                            itemInfo.put("clauseName", item.getClause() != null ? item.getClause().getClauseName() : null);
                            itemInfo.put("conformityStatus", item.getConformityStatus());
                            itemDetails.add(itemInfo);
                        }
                        checklistInfo.put("items", itemDetails);
                    }
                    
                    checklistDetails.add(checklistInfo);
                }
                debugInfo.put("checklistDetails", checklistDetails);
            }
            
            // Try to get with checklists using the special method
            try {
                Audit auditWithChecklists = auditRepo.findByIdWithChecklistsAndItems(id).orElse(null);
                if (auditWithChecklists != null) {
                    Set<Checklist> detailedChecklists = auditWithChecklists.getChecklists();
                    debugInfo.put("detailedChecklistsCount", detailedChecklists != null ? detailedChecklists.size() : 0);
                    
                    if (detailedChecklists != null && !detailedChecklists.isEmpty()) {
                        int totalItems = 0;
                        for (Checklist checklist : detailedChecklists) {
                            if (checklist.getChecklistItems() != null) {
                                totalItems += checklist.getChecklistItems().size();
                            }
                        }
                        debugInfo.put("totalItemsInDetailedChecklists", totalItems);
                    }
                }
            } catch (Exception e) {
                debugInfo.put("detailedChecklistsError", e.getMessage());
            }
            
            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Service-based debug endpoint
    @GetMapping("/debug-service/{id}")
    public ResponseEntity<?> debugAuditService(@PathVariable Long id) {
        try {
            Map<String, Object> debugInfo = auditService.debugAuditData(id);
            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
