package com.example.QualityManagementSystem.controller;

import com.example.QualityManagementSystem.dto.*;
import com.example.QualityManagementSystem.model.*;
import com.example.QualityManagementSystem.model.ChecklistTemplate;
import com.example.QualityManagementSystem.repository.ChecklistTemplateItemRepository;
import com.example.QualityManagementSystem.repository.ChecklistTemplateRepository;
import com.example.QualityManagementSystem.repository.ClauseLibraryRepository;
import com.example.QualityManagementSystem.repository.ChecklistRepository;
import com.example.QualityManagementSystem.repository.AuditRepository;
import com.example.QualityManagementSystem.service.ChecklistService;
import com.example.QualityManagementSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Arrays;

@RestController
@RequestMapping("/api/checklists")
@CrossOrigin(origins = "*")
public class ChecklistController {

    private final ChecklistService checklistService;
    private final UserService userService;
    private final ChecklistTemplateRepository templateRepo;
    private final ChecklistTemplateItemRepository templateItemRepo;
    private final ClauseLibraryRepository clauseRepo;
    private final ChecklistRepository checklistRepo;
    private final AuditRepository auditRepo;

    @Autowired
    public ChecklistController(ChecklistService checklistService, UserService userService, ChecklistTemplateRepository templateRepo, ChecklistTemplateItemRepository templateItemRepo, ClauseLibraryRepository clauseRepo, ChecklistRepository checklistRepo, AuditRepository auditRepo) {
        this.checklistService = checklistService;
        this.userService = userService;
        this.templateRepo = templateRepo;
        this.templateItemRepo = templateItemRepo;
        this.clauseRepo = clauseRepo;
        this.checklistRepo = checklistRepo;
        this.auditRepo = auditRepo;
    }

    // ==================== CHECKLIST TEMPLATE MANAGEMENT ====================

    // 1. POST /api/checklists/templates - Create checklist template (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @PostMapping("/templates")
    public ResponseEntity<ChecklistTemplateResponse> createTemplate(@Valid @RequestBody ChecklistTemplateRequest request) {
        try {
            return ResponseEntity.ok(checklistService.createChecklistTemplate(request));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 2. GET /api/checklists/templates - Get all templates (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @GetMapping("/templates")
    public ResponseEntity<List<ChecklistTemplateResponse>> getAllTemplates() {
        try {
            return ResponseEntity.ok(checklistService.getAllTemplates());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 3. GET /api/checklists/templates/{id} - Get template by ID (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @GetMapping("/templates/{id}")
    public ResponseEntity<ChecklistTemplateResponse> getTemplateById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(checklistService.getTemplateById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 4. GET /api/checklists/templates/iso/{isoStandard} - Get templates by ISO standard (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @GetMapping("/templates/iso/{isoStandard}")
    public ResponseEntity<List<ChecklistTemplateResponse>> getTemplatesByIsoStandard(@PathVariable String isoStandard) {
        try {
            ISO iso = ISO.valueOf(isoStandard.toUpperCase());
            return ResponseEntity.ok(checklistService.getTemplatesByIsoStandard(iso));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 5. PUT /api/checklists/templates/{id} - Update template (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @PutMapping("/templates/{id}")
    public ResponseEntity<ChecklistTemplateResponse> updateTemplate(@PathVariable Long id, @Valid @RequestBody ChecklistTemplateRequest request) {
        try {
            return ResponseEntity.ok(checklistService.updateTemplate(id, request));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 6. DELETE /api/checklists/templates/{id} - Delete template (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        try {
            checklistService.deleteTemplate(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== CHECKLIST CREATION FROM TEMPLATE ====================

    // 7. POST /api/checklists/audits/{auditId}/from-template - Create checklist from template (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @PostMapping("/audits/{auditId}/from-template")
    public ResponseEntity<?> createChecklistFromTemplate(
            @PathVariable Long auditId,
            @RequestParam Long templateId,
            @RequestParam(required = false) String selectedClauseIds) {
        try {
            System.out.println("DEBUG: createChecklistFromTemplate endpoint called");
            System.out.println("auditId: " + auditId);
            System.out.println("templateId: " + templateId);
            System.out.println("selectedClauseIds: " + selectedClauseIds);
            
            // Validate inputs
            if (auditId == null || auditId <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid audit ID: " + auditId);
                return ResponseEntity.badRequest().body(error);
            }
            
            if (templateId == null || templateId <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid template ID: " + templateId);
                return ResponseEntity.badRequest().body(error);
            }

            List<Long> clauseIds = null;
            if (selectedClauseIds != null && !selectedClauseIds.trim().isEmpty()) {
                try {
                    clauseIds = Arrays.stream(selectedClauseIds.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(Long::valueOf)
                            .collect(Collectors.toList());
                    
                    // Validate clause IDs
                    if (clauseIds.isEmpty()) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "No valid clause IDs provided");
                        return ResponseEntity.badRequest().body(error);
                    }
                } catch (NumberFormatException e) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Invalid clause ID format: " + selectedClauseIds);
                    return ResponseEntity.badRequest().body(error);
                }
            }

            System.out.println("DEBUG: Parsed clause IDs: " + clauseIds);

            // Use the safe method instead of the complex one
            ChecklistResponse resp = checklistService.createChecklistFromTemplateSafe(auditId, templateId, clauseIds);
            System.out.println("DEBUG: Checklist created successfully with ID: " + resp.getChecklistId());
            
            return ResponseEntity.ok(resp);

        } catch (StackOverflowError e) {
            System.err.println("ERROR: StackOverflowError in createChecklistFromTemplate");
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: Stack overflow prevented");
            error.put("details", "The operation was too complex and has been prevented for system stability");
            return ResponseEntity.status(500).body(error);
        } catch (Exception e) {
            System.err.println("ERROR in createChecklistFromTemplate: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage() != null ? e.getMessage() : "Unknown error occurred");
            error.put("exceptionType", e.getClass().getSimpleName());
            error.put("stackTrace", e.getStackTrace().length > 0 ? e.getStackTrace()[0].toString() : "No stack trace");
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Test endpoint to create checklist without complex mapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @PostMapping("/audits/{auditId}/from-template-simple")
    public ResponseEntity<?> createChecklistFromTemplateSimple(
            @PathVariable Long auditId,
            @RequestParam Long templateId,
            @RequestParam(required = false) String selectedClauseIds) {
        try {
            System.out.println("DEBUG: createChecklistFromTemplateSimple endpoint called");
            System.out.println("auditId: " + auditId);
            System.out.println("templateId: " + templateId);
            System.out.println("selectedClauseIds: " + selectedClauseIds);
            
            // Validate inputs
            if (auditId == null || auditId <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid audit ID: " + auditId);
                return ResponseEntity.badRequest().body(error);
            }
            
            if (templateId == null || templateId <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid template ID: " + templateId);
                return ResponseEntity.badRequest().body(error);
            }

            List<Long> clauseIds = null;
            if (selectedClauseIds != null && !selectedClauseIds.trim().isEmpty()) {
                try {
                    clauseIds = Arrays.stream(selectedClauseIds.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(Long::valueOf)
                            .collect(Collectors.toList());
                    
                    // Validate clause IDs
                    if (clauseIds.isEmpty()) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "No valid clause IDs provided");
                        return ResponseEntity.badRequest().body(error);
                    }
                } catch (NumberFormatException e) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Invalid clause ID format: " + selectedClauseIds);
                    return ResponseEntity.badRequest().body(error);
                }
            }

            System.out.println("DEBUG: Parsed clause IDs: " + clauseIds);

            // Create a simple response without complex mapping
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Checklist creation initiated");
            response.put("auditId", auditId);
            response.put("templateId", templateId);
            response.put("selectedClauseIds", clauseIds);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            // Try to create the checklist
            try {
                ChecklistResponse checklistResp = checklistService.createChecklistFromTemplate(auditId, templateId, clauseIds);
                response.put("success", true);
                response.put("checklistId", checklistResp.getChecklistId());
                response.put("message", "Checklist created successfully");
            } catch (Exception e) {
                response.put("success", false);
                response.put("error", e.getMessage());
                response.put("message", "Checklist creation failed");
            }
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("ERROR in createChecklistFromTemplateSimple: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage() != null ? e.getMessage() : "Unknown error occurred");
            return ResponseEntity.badRequest().body(error);
        }
    }


    // ==================== CHECKLIST MANAGEMENT ====================

    // 8. POST /api/checklists - Create checklist (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @PostMapping
    public ResponseEntity<ChecklistResponse> createChecklist(@Valid @RequestBody ChecklistRequest request) {
        try {
            return ResponseEntity.ok(checklistService.createChecklist(request));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 9. GET /api/checklists - Get all checklists (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @GetMapping
    public ResponseEntity<List<ChecklistResponse>> getAllChecklists() {
        try {
            return ResponseEntity.ok(checklistService.getAllChecklists());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 10. GET /api/checklists/{id} - Get checklist by ID (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ChecklistResponse> getChecklistById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(checklistService.getChecklistById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 11. GET /api/checklists/audits/{auditId} - Get checklist by audit ID (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @GetMapping("/audits/{auditId}")
    public ResponseEntity<ChecklistResponse> getChecklistByAudit(@PathVariable Long auditId) {
        try {
            return ResponseEntity.ok(checklistService.getChecklistByAudit(auditId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 12. PUT /api/checklists/{id} - Update checklist (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ChecklistResponse> updateChecklist(@PathVariable Long id, @Valid @RequestBody ChecklistRequest request) {
        try {
            return ResponseEntity.ok(checklistService.updateChecklist(id, request));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 13. DELETE /api/checklists/{id} - Delete checklist (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChecklist(@PathVariable Long id) {
        try {
            checklistService.deleteChecklist(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 14. GET /api/checklists/iso/{isoStandard} - Get checklists by ISO standard (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @GetMapping("/iso/{isoStandard}")
    public ResponseEntity<List<ChecklistResponse>> getChecklistsByIsoStandard(@PathVariable String isoStandard) {
        try {
            ISO iso = ISO.valueOf(isoStandard.toUpperCase());
            return ResponseEntity.ok(checklistService.getByIsoStandard(iso));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== CHECKLIST ITEM EVALUATION ====================

    // 15. POST /api/checklists/items/evaluate - Evaluate single checklist item (AUDITOR)
    @PreAuthorize("hasRole('AUDITOR')")
    @PostMapping("/items/evaluate")
    public ResponseEntity<Void> evaluateChecklistItem(@Valid @RequestBody ChecklistItemEvaluationRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String evaluator = auth.getName();
            
            // Set the evaluator in the request
            request.setEvaluatedBy(evaluator);
            
            checklistService.evaluateChecklistItem(request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 16. POST /api/checklists/items/evaluate-batch - Evaluate multiple checklist items (AUDITOR)
    @PreAuthorize("hasRole('AUDITOR')")
    @PostMapping("/items/evaluate-batch")
    public ResponseEntity<Void> evaluateMultipleItems(@Valid @RequestBody List<ChecklistItemEvaluationRequest> requests) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String evaluator = auth.getName();
            
            // Set the evaluator in all requests
            for (ChecklistItemEvaluationRequest request : requests) {
                request.setEvaluatedBy(evaluator);
            }
            
            checklistService.evaluateMultipleItems(requests);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== AUDIT PROGRESS TRACKING ====================

    // 17. GET /api/checklists/audits/{auditId}/progress - Get audit progress (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @GetMapping("/audits/{auditId}/progress")
    public ResponseEntity<AuditProgress> getAuditProgress(@PathVariable Long auditId) {
        try {
            return ResponseEntity.ok(checklistService.getAuditProgress(auditId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 18. GET /api/checklists/audits/{auditId}/items/unevaluated - Get unevaluated items (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @GetMapping("/audits/{auditId}/items/unevaluated")
    public ResponseEntity<List<Checklist_item>> getUnevaluatedItems(@PathVariable Long auditId) {
        try {
            return ResponseEntity.ok(checklistService.getUnevaluatedItems(auditId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 19. GET /api/checklists/audits/{auditId}/items/status/{status} - Get items by status (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @GetMapping("/audits/{auditId}/items/status/{status}")
    public ResponseEntity<List<Checklist_item>> getItemsByStatus(
            @PathVariable Long auditId,
            @PathVariable String status) {
        try {
            ConformityStatus conformityStatus = ConformityStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(checklistService.getItemsByStatus(auditId, conformityStatus));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== AUDIT EXECUTION ====================

    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @GetMapping("/audits/{auditId}/execution")
    public ResponseEntity<?> getAuditExecution(@PathVariable Long auditId) {
        try {
            return ResponseEntity.ok(checklistService.getAuditExecution(auditId));
        } catch (Exception e) {
            e.printStackTrace(); // ✅ log full stack trace in console
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage() != null ? e.getMessage() : e.toString()); // ✅ ensure not null
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 21. POST /api/checklists/audits/{auditId}/execute - Execute audit clause evaluation (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @PostMapping("/audits/{auditId}/execute")
    public ResponseEntity<Void> executeAuditClause(@PathVariable Long auditId, @Valid @RequestBody AuditExecutionRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String evaluator = auth.getName();
            
            // Set the evaluator and audit ID in the request
            request.setEvaluatedBy(evaluator);

            
            checklistService.executeAuditClause(request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 22. GET /api/checklists/audits/in-progress - Get all audits in progress (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @GetMapping("/audits/in-progress")
    public ResponseEntity<List<AuditExecutionResponse>> getAuditsInProgress() {
        try {
            return ResponseEntity.ok(checklistService.getAuditsInProgress());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 23. GET /api/checklists/debug/templates - Debug endpoint to check templates (ADMIN only)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/debug/templates")
    public ResponseEntity<Map<String, Object>> debugTemplates() {
        try {
            Map<String, Object> debugInfo = new HashMap<>();
            
            // Get all templates
            List<ChecklistTemplate> allTemplates = templateRepo.findAll();
            debugInfo.put("totalTemplates", allTemplates.size());
            
            List<Map<String, Object>> templateDetails = new ArrayList<>();
            for (ChecklistTemplate template : allTemplates) {
                Map<String, Object> templateInfo = new HashMap<>();
                templateInfo.put("templateId", template.getTemplateId());
                templateInfo.put("templateName", template.getTemplateName());
                templateInfo.put("isoStandard", template.getIsoStandard());
                templateInfo.put("isActive", template.isActive());
                
                // Get template items count
                List<ChecklistTemplateItem> items = templateItemRepo.findByTemplate_TemplateIdOrderBySortOrderAsc(template.getTemplateId());
                templateInfo.put("clauseCount", items.size());
                
                templateDetails.add(templateInfo);
            }
            debugInfo.put("templates", templateDetails);
            
            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Public test endpoint for debugging checklist execution (no auth required)
    @GetMapping("/public-test-execution/{auditId}")
    public ResponseEntity<Map<String, Object>> publicTestExecution(@PathVariable Long auditId) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Testing checklist execution for audit: " + auditId);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            // Test basic audit lookup
            try {
                Audit audit = auditRepo.findById(auditId).orElse(null);
                if (audit != null) {
                    response.put("auditFound", true);
                    response.put("auditTitle", audit.getTitle());
                    response.put("auditStatus", audit.getStatus());
                } else {
                    response.put("auditFound", false);
                }
            } catch (Exception e) {
                response.put("auditLookupError", e.getMessage());
            }
            
            // Test checklist lookup with different methods
            try {
                // Method 1: Simple query
                List<Checklist> simpleChecklists = checklistRepo.findByAuditIdSimple(auditId);
                response.put("simpleQueryCount", simpleChecklists.size());
                
                // Method 2: Complex query
                List<Checklist> complexChecklists = checklistRepo.findByAuditIdWithItemsAndClauses(auditId);
                response.put("complexQueryCount", complexChecklists.size());
                
                // Method 3: Basic findByAudit_AuditId
                List<Checklist> basicChecklists = checklistRepo.findByAudit_AuditId(auditId);
                response.put("basicQueryCount", basicChecklists.size());
                
                // Show details of first checklist if any found
                if (!simpleChecklists.isEmpty()) {
                    Checklist firstChecklist = simpleChecklists.get(0);
                    Map<String, Object> checklistInfo = new HashMap<>();
                    checklistInfo.put("checklistId", firstChecklist.getChecklistId());
                    checklistInfo.put("isoStandard", firstChecklist.getIsoStandard());
                    checklistInfo.put("status", firstChecklist.getStatus());
                    checklistInfo.put("auditId", firstChecklist.getAudit() != null ? firstChecklist.getAudit().getAuditId() : "NULL");
                    checklistInfo.put("itemsCount", firstChecklist.getChecklistItems() != null ? firstChecklist.getChecklistItems().size() : "NULL");
                    response.put("firstChecklist", checklistInfo);
                }
                
                // Debug: Show all checklists in the system and their audit IDs
                try {
                    List<Checklist> allChecklists = checklistRepo.findAll();
                    List<Map<String, Object>> allChecklistInfo = new ArrayList<>();
                    
                    for (Checklist checklist : allChecklists) {
                        Map<String, Object> info = new HashMap<>();
                        info.put("checklistId", checklist.getChecklistId());
                        info.put("auditId", checklist.getAudit() != null ? checklist.getAudit().getAuditId() : "NULL");
                        info.put("isoStandard", checklist.getIsoStandard());
                        allChecklistInfo.add(info);
                    }
                    
                    response.put("allChecklistsInSystem", allChecklistInfo);
                    response.put("totalChecklistsInSystem", allChecklists.size());
                    
                } catch (Exception e) {
                    response.put("allChecklistsError", e.getMessage());
                }
                
            } catch (Exception e) {
                response.put("checklistLookupError", e.getMessage());
                e.printStackTrace();
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Simple endpoint to test basic audit info without complex processing
    @GetMapping("/basic-audit-info/{auditId}")
    public ResponseEntity<Map<String, Object>> getBasicAuditInfo(@PathVariable Long auditId) {
        try {
            Map<String, Object> result = checklistService.getBasicAuditInfo(auditId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // Test endpoint to compare different checklist loading approaches
    @GetMapping("/test-checklist-loading/{auditId}")
    public ResponseEntity<Map<String, Object>> testChecklistLoading(@PathVariable Long auditId) {
        try {
            Map<String, Object> result = checklistService.testChecklistLoading(auditId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 24. POST /api/checklists/create-test-template - Create a test template (ADMIN only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create-test-template")
    public ResponseEntity<Map<String, Object>> createTestTemplate() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Check if templates already exist
            List<ChecklistTemplate> existingTemplates = templateRepo.findAll();
            if (!existingTemplates.isEmpty()) {
                result.put("message", "Templates already exist");
                result.put("existingTemplates", existingTemplates.size());
                return ResponseEntity.ok(result);
            }
            
            // Create a test ISO 9001 template
            ChecklistTemplate template = new ChecklistTemplate();
            template.setTemplateName("Test ISO 9001 Template");
            template.setDescription("Test template for ISO 9001:2015");
            template.setIsoStandard(ISO.ISO_9001);
            template.setActive(true);
            template.setCreatedAt(LocalDateTime.now());
            template.setUpdatedAt(LocalDateTime.now());
            
            template = templateRepo.save(template);
            result.put("message", "Test template created successfully");
            result.put("templateId", template.getTemplateId());
            result.put("templateName", template.getTemplateName());
            
            // Create some basic clauses
            createTestClauses(template);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    private void createTestClauses(ChecklistTemplate template) {
        try {
            // Create some basic ISO 9001 clauses
            String[][] clauseData = {
                {"4.1", "Understanding the organization and its context", "Context of the organization"},
                {"4.2", "Understanding the needs and expectations of interested parties", "Interested parties"},
                {"4.3", "Determining the scope of the quality management system", "Scope of the QMS"},
                {"4.4", "Quality management system and its processes", "QMS and processes"},
                {"5.1", "Leadership and commitment", "Leadership commitment"}
            };
            
            for (int i = 0; i < clauseData.length; i++) {
                String[] clauseInfo = clauseData[i];
                
                // Create clause library entry
                Clause_library clause = new Clause_library();
                clause.setClauseNumber(clauseInfo[0]);
                clause.setClauseName(clauseInfo[1]);
                clause.setDescription(clauseInfo[2]);
                clause.setStandard(ISO.ISO_9001);
                clause.setCategory("General");
                clause.setRiskLevel(RiskLevel.MEDIUM);
                clause.setVersion("2015");
                clause.setEffectiveDate(LocalDateTime.now());
                clause.setActive(true);
                clause.setCreatedAt(LocalDateTime.now());
                clause.setUpdatedAt(LocalDateTime.now());
                
                // Save clause
                clause = clauseRepo.save(clause);
                
                // Create template item
                ChecklistTemplateItem templateItem = new ChecklistTemplateItem();
                templateItem.setTemplate(template);
                templateItem.setClause(clause);
                templateItem.setCustomText(clauseInfo[2]);
                templateItem.setCustomDescription(clauseInfo[1]);
                templateItem.setSortOrder(i + 1);
                templateItem.setCreatedAt(LocalDateTime.now());
                templateItem.setUpdatedAt(LocalDateTime.now());
                
                templateItemRepo.save(templateItem);
            }
            
        } catch (Exception e) {
            System.err.println("Failed to create test clauses: " + e.getMessage());
        }
    }

    // 25. GET /api/checklists/debug/templates/{templateId}/clauses - Debug endpoint to check template clauses (ADMIN only)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/debug/templates/{templateId}/clauses")
    public ResponseEntity<Map<String, Object>> debugTemplateClauses(@PathVariable Long templateId) {
        try {
            Map<String, Object> debugInfo = new HashMap<>();
            
            // Get template
            ChecklistTemplate template = templateRepo.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found with ID: " + templateId));
            
            debugInfo.put("templateId", template.getTemplateId());
            debugInfo.put("templateName", template.getTemplateName());
            debugInfo.put("isoStandard", template.getIsoStandard());
            debugInfo.put("isActive", template.isActive());
            
            // Get template items with clause details
            List<ChecklistTemplateItem> items = templateItemRepo.findByTemplate_TemplateIdOrderBySortOrderAsc(templateId);
            List<Map<String, Object>> clauseDetails = new ArrayList<>();
            
            for (ChecklistTemplateItem item : items) {
                Map<String, Object> clauseInfo = new HashMap<>();
                clauseInfo.put("itemId", item.getTemplateItemId());
                clauseInfo.put("clauseId", item.getClause().getClauseId());
                clauseInfo.put("clauseNumber", item.getClause().getClauseNumber());
                clauseInfo.put("clauseName", item.getClause().getClauseName());
                clauseInfo.put("customText", item.getCustomText());
                clauseInfo.put("sortOrder", item.getSortOrder());
                clauseDetails.add(clauseInfo);
            }
            
            debugInfo.put("totalClauses", items.size());
            debugInfo.put("clauses", clauseDetails);
            
            return ResponseEntity.ok(debugInfo);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 26. POST /api/checklists/debug/test-creation - Debug endpoint to test checklist creation step by step (ADMIN only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/debug/test-creation")
    public ResponseEntity<Map<String, Object>> debugChecklistCreation(
            @RequestParam Long auditId,
            @RequestParam Long templateId,
            @RequestParam(required = false) List<Long> selectedClauseIds) {
        try {
            Map<String, Object> debugInfo = new HashMap<>();
            
            // Step 1: Check if audit exists
            try {
                // This would normally call auditService, but for debugging we'll just log
                debugInfo.put("auditId", auditId);
                debugInfo.put("auditExists", "Will check in service");
            } catch (Exception e) {
                debugInfo.put("auditError", e.getMessage());
                return ResponseEntity.badRequest().body(debugInfo);
            }
            
            // Step 2: Check if template exists
            try {
                ChecklistTemplate template = templateRepo.findById(templateId)
                    .orElseThrow(() -> new RuntimeException("Template not found with ID: " + templateId));
                debugInfo.put("templateId", template.getTemplateId());
                debugInfo.put("templateName", template.getTemplateName());
                debugInfo.put("templateExists", true);
            } catch (Exception e) {
                debugInfo.put("templateError", e.getMessage());
                return ResponseEntity.badRequest().body(debugInfo);
            }
            
            // Step 3: Check if clauses exist
            try {
                List<ChecklistTemplateItem> items = templateItemRepo.findByTemplate_TemplateIdOrderBySortOrderAsc(templateId);
                List<Long> availableClauseIds = items.stream()
                    .map(item -> item.getClause().getClauseId())
                    .collect(Collectors.toList());
                
                debugInfo.put("availableClauseIds", availableClauseIds);
                debugInfo.put("totalAvailableClauses", items.size());
                
                if (selectedClauseIds != null && !selectedClauseIds.isEmpty()) {
                    List<Long> invalidClauseIds = selectedClauseIds.stream()
                        .filter(id -> !availableClauseIds.contains(id))
                        .collect(Collectors.toList());
                    
                    debugInfo.put("selectedClauseIds", selectedClauseIds);
                    debugInfo.put("invalidClauseIds", invalidClauseIds);
                    debugInfo.put("validClauseIds", selectedClauseIds.stream()
                        .filter(id -> availableClauseIds.contains(id))
                        .collect(Collectors.toList()));
                }
                
            } catch (Exception e) {
                debugInfo.put("clauseError", e.getMessage());
                return ResponseEntity.badRequest().body(debugInfo);
            }
            
            debugInfo.put("message", "All validation checks passed. Ready to create checklist.");
            return ResponseEntity.ok(debugInfo);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 27. POST /api/checklists/create-for-existing-audits - Create checklists for existing audits (ADMIN only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create-for-existing-audits")
    public ResponseEntity<Map<String, Object>> createChecklistsForExistingAudits() {
        try {
            System.out.println("DEBUG: createChecklistsForExistingAudits endpoint called");
            
            checklistService.createChecklistsForExistingAudits();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Checklists created successfully for existing audits");
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            response.put("success", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("ERROR in createChecklistsForExistingAudits: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            error.put("success", false);
            
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 28. GET /api/checklists/test-creation - Test checklist creation (ADMIN only)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/test-creation")
    public ResponseEntity<Map<String, Object>> testChecklistCreation(
            @RequestParam Long auditId,
            @RequestParam Long templateId) {
        try {
            System.out.println("DEBUG: testChecklistCreation endpoint called");
            System.out.println("auditId: " + auditId);
            System.out.println("templateId: " + templateId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Testing checklist creation");
            response.put("auditId", auditId);
            response.put("templateId", templateId);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            // Test the creation process
            try {
                ChecklistResponse checklistResp = checklistService.createChecklistFromTemplate(auditId, templateId, null);
                response.put("success", true);
                response.put("checklistId", checklistResp.getChecklistId());
                response.put("itemCount", checklistResp.getChecklistItems() != null ? checklistResp.getChecklistItems().size() : 0);
                response.put("message", "Checklist created successfully");
                
                // Verify the checklist was saved
                Checklist savedChecklist = checklistRepo.findById(checklistResp.getChecklistId())
                    .orElseThrow(() -> new RuntimeException("Checklist not found after creation"));
                response.put("verified", true);
                response.put("verifiedItemCount", savedChecklist.getChecklistItems() != null ? savedChecklist.getChecklistItems().size() : 0);
                
            } catch (Exception e) {
                response.put("success", false);
                response.put("error", e.getMessage());
                response.put("message", "Checklist creation failed");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("ERROR in testChecklistCreation: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            error.put("success", false);
            
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 29. POST /api/checklists/audits/{auditId}/from-template-safe - Create checklist from template safely (ADMIN, AUDITOR)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    @PostMapping("/audits/{auditId}/from-template-safe")
    public ResponseEntity<?> createChecklistFromTemplateSafe(
            @PathVariable Long auditId,
            @RequestParam Long templateId,
            @RequestParam(required = false) String selectedClauseIds) {
        try {
            System.out.println("DEBUG: createChecklistFromTemplateSafe endpoint called");
            System.out.println("auditId: " + auditId);
            System.out.println("templateId: " + templateId);
            System.out.println("selectedClauseIds: " + selectedClauseIds);
            
            // Validate inputs
            if (auditId == null || auditId <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid audit ID: " + auditId);
                return ResponseEntity.badRequest().body(error);
            }
            
            if (templateId == null || templateId <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid template ID: " + templateId);
                return ResponseEntity.badRequest().body(error);
            }

            List<Long> clauseIds = null;
            if (selectedClauseIds != null && !selectedClauseIds.trim().isEmpty()) {
                try {
                    clauseIds = Arrays.stream(selectedClauseIds.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(Long::valueOf)
                            .collect(Collectors.toList());
                    
                    // Validate clause IDs
                    if (clauseIds.isEmpty()) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "No valid clause IDs provided");
                        return ResponseEntity.badRequest().body(error);
                    }
                } catch (NumberFormatException e) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Invalid clause ID format: " + selectedClauseIds);
                    return ResponseEntity.badRequest().body(error);
                }
            }

            System.out.println("DEBUG: Parsed clause IDs: " + clauseIds);

            // Use the safe method
            ChecklistResponse resp = checklistService.createChecklistFromTemplateSafe(auditId, templateId, clauseIds);
            System.out.println("DEBUG: Checklist created safely with ID: " + resp.getChecklistId());
            
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            System.err.println("ERROR in createChecklistFromTemplateSafe: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage() != null ? e.getMessage() : "Unknown error occurred");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // 30. GET /api/checklists/test - Simple test endpoint (no auth required)
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Checklist controller is working");
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            response.put("status", "OK");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 31. GET /api/checklists/debug/template-integrity/{templateId} - Debug template integrity (ADMIN only)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/debug/template-integrity/{templateId}")
    public ResponseEntity<Map<String, Object>> debugTemplateIntegrity(@PathVariable Long templateId) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("templateId", templateId);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            // Get template
            ChecklistTemplate template = templateRepo.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found with ID: " + templateId));
            
            response.put("templateName", template.getTemplateName());
            response.put("isoStandard", template.getIsoStandard());
            response.put("isActive", template.isActive());
            
            // Get all template items
            List<ChecklistTemplateItem> allItems = templateItemRepo.findByTemplate_TemplateIdOrderBySortOrderAsc(templateId);
            response.put("totalTemplateItems", allItems.size());
            
            // Check for items with null clauses
            List<ChecklistTemplateItem> invalidItems = templateItemRepo.findByTemplate_TemplateIdAndClauseIsNull(templateId);
            response.put("invalidItemsCount", invalidItems.size());
            
            List<Map<String, Object>> invalidItemDetails = new ArrayList<>();
            for (ChecklistTemplateItem item : invalidItems) {
                Map<String, Object> itemInfo = new HashMap<>();
                itemInfo.put("templateItemId", item.getTemplateItemId());
                itemInfo.put("customText", item.getCustomText());
                itemInfo.put("sortOrder", item.getSortOrder());
                invalidItemDetails.add(itemInfo);
            }
            response.put("invalidItems", invalidItemDetails);
            
            // Check for valid items
            List<ChecklistTemplateItem> validItems = allItems.stream()
                .filter(item -> item.getClause() != null)
                .collect(Collectors.toList());
            response.put("validItemsCount", validItems.size());
            
            List<Map<String, Object>> validItemDetails = new ArrayList<>();
            for (ChecklistTemplateItem item : validItems) {
                Map<String, Object> itemInfo = new HashMap<>();
                itemInfo.put("templateItemId", item.getTemplateItemId());
                itemInfo.put("clauseId", item.getClause().getClauseId());
                itemInfo.put("clauseNumber", item.getClause().getClauseNumber());
                itemInfo.put("clauseName", item.getClause().getClauseName());
                itemInfo.put("customText", item.getCustomText());
                itemInfo.put("sortOrder", item.getSortOrder());
                validItemDetails.add(itemInfo);
            }
            response.put("validItems", validItemDetails);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 32. POST /api/checklists/debug/cleanup-template/{templateId} - Clean up invalid template items (ADMIN only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/debug/cleanup-template/{templateId}")
    public ResponseEntity<Map<String, Object>> cleanupTemplate(@PathVariable Long templateId) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("templateId", templateId);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            // Get count before cleanup
            List<ChecklistTemplateItem> invalidItemsBefore = templateItemRepo.findByTemplate_TemplateIdAndClauseIsNull(templateId);
            response.put("invalidItemsBefore", invalidItemsBefore.size());
            
            // Perform cleanup
            checklistService.cleanupInvalidTemplateItems(templateId);
            
            // Get count after cleanup
            List<ChecklistTemplateItem> invalidItemsAfter = templateItemRepo.findByTemplate_TemplateIdAndClauseIsNull(templateId);
            response.put("invalidItemsAfter", invalidItemsAfter.size());
            response.put("itemsCleanedUp", invalidItemsBefore.size() - invalidItemsAfter.size());
            
            response.put("message", "Template cleanup completed successfully");
            response.put("success", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            error.put("success", false);
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 33. POST /api/checklists/debug/create-basic-template - Create a basic ISO 9001 template (ADMIN only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/debug/create-basic-template")
    public ResponseEntity<Map<String, Object>> createBasicTemplate() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            // Check if templates already exist
            List<ChecklistTemplate> existingTemplates = templateRepo.findAll();
            if (!existingTemplates.isEmpty()) {
                response.put("message", "Templates already exist");
                response.put("existingTemplates", existingTemplates.size());
                List<Map<String, Object>> templateList = new ArrayList<>();
                for (ChecklistTemplate t : existingTemplates) {
                    Map<String, Object> templateInfo = new HashMap<>();
                    templateInfo.put("templateId", t.getTemplateId());
                    templateInfo.put("templateName", t.getTemplateName());
                    templateInfo.put("isoStandard", t.getIsoStandard().name());
                    templateList.add(templateInfo);
                }
                response.put("templates", templateList);
                return ResponseEntity.ok(response);
            }
            
            // Create a basic ISO 9001 template
            ChecklistTemplate template = new ChecklistTemplate();
            template.setTemplateName("Basic ISO 9001:2015 Template");
            template.setDescription("Basic template for ISO 9001:2015 quality management system");
            template.setIsoStandard(ISO.ISO_9001);
            template.setActive(true);
            template.setCreatedAt(LocalDateTime.now());
            template.setUpdatedAt(LocalDateTime.now());
            
            template = templateRepo.save(template);
            response.put("message", "Basic template created successfully");
            response.put("templateId", template.getTemplateId());
            response.put("templateName", template.getTemplateName());
            
            // Create some basic clauses
            createBasicClauses(template);
            
            response.put("success", true);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("timestamp", java.time.LocalDateTime.now().toString());
            error.put("success", false);
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    private void createBasicClauses(ChecklistTemplate template) {
        try {
            // Create some basic ISO 9001 clauses
            String[][] clauseData = {
                {"4.1", "Understanding the organization and its context", "Context of the organization"},
                {"4.2", "Understanding the needs and expectations of interested parties", "Interested parties"},
                {"4.3", "Determining the scope of the quality management system", "Scope of the QMS"},
                {"4.4", "Quality management system and its processes", "QMS and processes"},
                {"5.1", "Leadership and commitment", "Leadership commitment"},
                {"5.2", "Quality policy", "Quality policy"},
                {"5.3", "Organizational roles, responsibilities and authorities", "Organizational roles"},
                {"6.1", "Actions to address risks and opportunities", "Risk management"},
                {"6.2", "Quality objectives and planning to achieve them", "Quality objectives"},
                {"7.1", "Resources", "Resources"},
                {"7.2", "Competence", "Competence"},
                {"7.3", "Awareness", "Awareness"},
                {"7.4", "Communication", "Communication"},
                {"7.5", "Documented information", "Documented information"},
                {"8.1", "Operational planning and control", "Operational planning"},
                {"8.2", "Requirements for products and services", "Product requirements"},
                {"8.3", "Design and development of products and services", "Design and development"},
                {"8.4", "Control of externally provided processes, products and services", "External providers"},
                {"8.5", "Production and service provision", "Production and service provision"},
                {"8.6", "Release of products and services", "Release of products"},
                {"8.7", "Control of nonconforming outputs", "Nonconforming outputs"},
                {"9.1", "Monitoring, measurement, analysis and evaluation", "Monitoring and measurement"},
                {"9.2", "Internal audit", "Internal audit"},
                {"9.3", "Management review", "Management review"},
                {"10.1", "Improvement", "Improvement"},
                {"10.2", "Nonconformity and corrective action", "Nonconformity and corrective action"}
            };
            
            for (int i = 0; i < clauseData.length; i++) {
                String[] clauseInfo = clauseData[i];
                
                // Create clause library entry
                Clause_library clause = new Clause_library();
                clause.setClauseNumber(clauseInfo[0]);
                clause.setClauseName(clauseInfo[1]);
                clause.setDescription(clauseInfo[2]);
                clause.setStandard(ISO.ISO_9001);
                clause.setCategory("General");
                clause.setRiskLevel(RiskLevel.MEDIUM);
                clause.setVersion("2015");
                clause.setEffectiveDate(LocalDateTime.now());
                clause.setActive(true);
                clause.setCreatedAt(LocalDateTime.now());
                clause.setUpdatedAt(LocalDateTime.now());
                
                // Save clause
                clause = clauseRepo.save(clause);
                
                // Create template item
                ChecklistTemplateItem templateItem = new ChecklistTemplateItem();
                templateItem.setTemplate(template);
                templateItem.setClause(clause);
                templateItem.setCustomText(clauseInfo[2]);
                templateItem.setCustomDescription(clauseInfo[1]);
                templateItem.setSortOrder(i + 1);
                templateItem.setCreatedAt(LocalDateTime.now());
                templateItem.setUpdatedAt(LocalDateTime.now());
                
                templateItemRepo.save(templateItem);
            }
            
            System.out.println("Created " + clauseData.length + " basic clauses for template " + template.getTemplateId());
            
        } catch (Exception e) {
            System.err.println("Failed to create basic clauses: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
