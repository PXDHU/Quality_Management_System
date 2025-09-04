package com.example.QualityManagementSystem.service;

import com.example.QualityManagementSystem.dto.*;
import com.example.QualityManagementSystem.model.*;
import com.example.QualityManagementSystem.model.ChecklistTemplate;
import com.example.QualityManagementSystem.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Objects;

@Service
public class ChecklistService {

    @Autowired
    ChecklistRepository checklistRepo;

    @Autowired
    ChecklistItemRepository itemRepo;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ClauseLibraryRepository clauseRepo;

    @Autowired
    InstanceRepository instanceRepo;

    @Autowired
    AuditRepository auditRepo;

    @Autowired
    ChecklistTemplateRepository templateRepo;

    @Autowired
    ChecklistTemplateItemRepository templateItemRepo;

    @Autowired
    DocumentRepository documentRepo;

    @Autowired
    ChecklistItemRepository checklistItemRepository;

    @Autowired
    NonConformityRepository nonConformityRepository;

    @Autowired
    UserService userService;

    // ==================== CHECKLIST TEMPLATE MANAGEMENT ====================

    @Transactional
    public ChecklistTemplateResponse createChecklistTemplate(ChecklistTemplateRequest request) {
        ChecklistTemplate template = new ChecklistTemplate();
        template.setTemplateName(request.getTemplateName());
        template.setDescription(request.getDescription());
        template.setIsoStandard(request.getIsoStandard());
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setActive(true);

        template = templateRepo.save(template);

        // Add clauses to template
        if (request.getClauses() != null) {
            for (int i = 0; i < request.getClauses().size(); i++) {
                ChecklistTemplateRequest.ChecklistClauseRequest clauseRequest = request.getClauses().get(i);
                
                Clause_library clause = clauseRepo.findById(clauseRequest.getClauseId())
                    .orElseThrow(() -> new RuntimeException("Clause not found: " + clauseRequest.getClauseId()));

                ChecklistTemplateItem templateItem = new ChecklistTemplateItem();
                templateItem.setTemplate(template);
                templateItem.setClause(clause);
                templateItem.setCustomText(clauseRequest.getCustomText());
                templateItem.setCustomDescription(clauseRequest.getCustomDescription());
                templateItem.setSortOrder(i + 1);
                templateItem.setCreatedAt(LocalDateTime.now());
                templateItem.setUpdatedAt(LocalDateTime.now());

                templateItemRepo.save(templateItem);
            }
        }

        return mapToTemplateResponse(template);
    }

    public List<ChecklistTemplateResponse> getAllTemplates() {
        return templateRepo.findByIsActiveTrue().stream()
            .map(this::mapToTemplateResponse)
            .collect(Collectors.toList());
    }

    public List<ChecklistTemplateResponse> getTemplatesByIsoStandard(ISO isoStandard) {
        return templateRepo.findByIsoStandardAndIsActiveTrue(isoStandard).stream()
            .map(this::mapToTemplateResponse)
            .collect(Collectors.toList());
    }

    public ChecklistTemplateResponse getTemplateById(Long templateId) {
        ChecklistTemplate template = templateRepo.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template not found"));
        return mapToTemplateResponse(template);
    }

    @Transactional
    public ChecklistTemplateResponse updateTemplate(Long templateId, ChecklistTemplateRequest request) {
        ChecklistTemplate template = templateRepo.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template not found"));

        template.setTemplateName(request.getTemplateName());
        template.setDescription(request.getDescription());
        template.setIsoStandard(request.getIsoStandard());
        template.setUpdatedAt(LocalDateTime.now());

        // Remove existing items
        templateItemRepo.deleteByTemplate_TemplateId(templateId);

        // Add new items
        if (request.getClauses() != null) {
            for (int i = 0; i < request.getClauses().size(); i++) {
                ChecklistTemplateRequest.ChecklistClauseRequest clauseRequest = request.getClauses().get(i);
                
                Clause_library clause = clauseRepo.findById(clauseRequest.getClauseId())
                    .orElseThrow(() -> new RuntimeException("Clause not found: " + clauseRequest.getClauseId()));

                ChecklistTemplateItem templateItem = new ChecklistTemplateItem();
                templateItem.setTemplate(template);
                templateItem.setClause(clause);
                templateItem.setCustomText(clauseRequest.getCustomText());
                templateItem.setCustomDescription(clauseRequest.getCustomDescription());
                templateItem.setSortOrder(i + 1);
                templateItem.setCreatedAt(LocalDateTime.now());
                templateItem.setUpdatedAt(LocalDateTime.now());

                templateItemRepo.save(templateItem);
            }
        }

        return mapToTemplateResponse(templateRepo.save(template));
    }

    @Transactional
    public void deleteTemplate(Long templateId) {
        ChecklistTemplate template = templateRepo.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template not found"));
        template.setActive(false);
        template.setUpdatedAt(LocalDateTime.now());
        templateRepo.save(template);
    }

    // ==================== CHECKLIST CREATION FROM TEMPLATE ====================

    @Transactional
    public ChecklistResponse createChecklistFromTemplate(Long auditId, Long templateId, List<Long> selectedClauseIds) {
        try {
            System.out.println("=== DEBUG: createChecklistFromTemplate ===");
            System.out.println("auditId: " + auditId);
            System.out.println("templateId: " + templateId);
            System.out.println("selectedClauseIds: " + selectedClauseIds);

            // Validate inputs
            if (auditId == null) {
                throw new IllegalArgumentException("Audit ID cannot be null");
            }
            if (templateId == null) {
                throw new IllegalArgumentException("Template ID cannot be null");
            }

            Audit audit = auditRepo.findById(auditId)
                    .orElseThrow(() -> new RuntimeException("Audit not found with ID: " + auditId));
            ChecklistTemplate template = templateRepo.findById(templateId)
                    .orElseThrow(() -> new RuntimeException("Template not found with ID: " + templateId));

            if (!template.isActive()) {
                throw new RuntimeException("Template is not active: " + template.getTemplateName());
            }

            // Create new checklist
            Checklist checklist = new Checklist();
            checklist.setIsoStandard(template.getIsoStandard());
            checklist.setStatus(Status.PLANNED);
            checklist.setCreatedAt(LocalDateTime.now());
            checklist.setUpdatedAt(LocalDateTime.now());
            
            // Initialize the checklist items set
            checklist.setChecklistItems(new HashSet<>());
            
            // Use the helper method to properly manage bidirectional relationship
            audit.addChecklist(checklist);

            // Load template items
            List<ChecklistTemplateItem> templateItems =
                    templateItemRepo.findByTemplate_TemplateIdOrderBySortOrderAsc(templateId);

            if (templateItems.isEmpty()) {
                throw new RuntimeException("Template has no clauses: " + template.getTemplateName());
            }

            // Filter if selectedClauseIds is provided
            if (selectedClauseIds != null && !selectedClauseIds.isEmpty()) {
                templateItems = templateItems.stream()
                        .filter(item -> selectedClauseIds.contains(item.getClause().getClauseId()))
                        .collect(Collectors.toList());
                
                if (templateItems.isEmpty()) {
                    throw new RuntimeException("No valid clauses found for the selected clause IDs");
                }
            }

            System.out.println("DEBUG: Creating " + templateItems.size() + " checklist items");

            // Create and add checklist items
            for (ChecklistTemplateItem templateItem : templateItems) {
                try {
                    Clause_library clause = clauseRepo.findById(templateItem.getClause().getClauseId())
                            .orElseThrow(() -> new RuntimeException("Clause not found: " + templateItem.getClause().getClauseId()));

                    Checklist_item item = new Checklist_item();
                    item.setClause(clause);
                    item.setCustomText(templateItem.getCustomText());
                    item.setConformityStatus(ConformityStatus.PENDING_EVALUATION);
                    item.setCreatedAt(LocalDateTime.now());
                    item.setUpdatedAt(LocalDateTime.now());

                    // Use the helper method to properly manage bidirectional relationship
                    checklist.addChecklistItem(item);
                    
                    System.out.println("DEBUG: Added item for clause " + clause.getClauseId() + " to checklist");
                } catch (Exception e) {
                    System.err.println("ERROR creating checklist item for clause " + templateItem.getClause().getClauseId() + ": " + e.getMessage());
                    throw new RuntimeException("Failed to create checklist item: " + e.getMessage(), e);
                }
            }

            // Save the checklist - this should cascade to save all items
            System.out.println("DEBUG: Saving checklist with " + checklist.getChecklistItems().size() + " items");
            checklist = checklistRepo.save(checklist);
            
            // Verify that items were saved
            if (checklist.getChecklistItems() == null || checklist.getChecklistItems().isEmpty()) {
                System.err.println("WARNING: Checklist items were not saved properly");
                // Try to reload the checklist to see if items exist
                Checklist reloadedChecklist = checklistRepo.findById(checklist.getChecklistId())
                    .orElseThrow(() -> new RuntimeException("Failed to reload checklist after save"));
                System.out.println("DEBUG: Reloaded checklist has " + 
                    (reloadedChecklist.getChecklistItems() != null ? reloadedChecklist.getChecklistItems().size() : 0) + " items");
            }

            System.out.println("=== END DEBUG: Created checklist " + checklist.getChecklistId()
                    + " with " + (checklist.getChecklistItems() != null ? checklist.getChecklistItems().size() : 0) + " items ===");

            // Use the simple mapping method to avoid recursion
            return mapToSimpleResponse(checklist);
            
        } catch (Exception e) {
            System.err.println("ERROR in createChecklistFromTemplate: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Safe method to create checklist from template without complex entity relationships
     */
    @Transactional
    public ChecklistResponse createChecklistFromTemplateSafe(Long auditId, Long templateId, List<Long> selectedClauseIds) {
        try {
            System.out.println("=== DEBUG: createChecklistFromTemplateSafe ===");
            System.out.println("auditId: " + auditId);
            System.out.println("templateId: " + templateId);
            System.out.println("selectedClauseIds: " + selectedClauseIds);

            if (auditId == null) {
                throw new IllegalArgumentException("Audit ID cannot be null");
            }
            if (templateId == null) {
                throw new IllegalArgumentException("Template ID cannot be null");
            }

            // Check existing checklist
            List<Checklist> existingChecklists = checklistRepo.findByAudit_AuditId(auditId);
            for (Checklist existing : existingChecklists) {
                if (existing.getIsoStandard() != null &&
                        existing.getIsoStandard().equals(templateRepo.findById(templateId).orElse(null).getIsoStandard())) {
                    throw new RuntimeException("Checklist already exists for audit " + auditId +
                            " with ISO standard " + existing.getIsoStandard());
                }
            }

            // Fetch audit + template
            Audit audit = auditRepo.findById(auditId)
                    .orElseThrow(() -> new RuntimeException("Audit not found with ID: " + auditId));
            ChecklistTemplate template = templateRepo.findById(templateId)
                    .orElseThrow(() -> new RuntimeException("Template not found with ID: " + templateId));

            if (!template.isActive()) {
                throw new RuntimeException("Template is not active: " + template.getTemplateName());
            }

            // Fetch template items
            List<ChecklistTemplateItem> templateItems =
                    templateItemRepo.findByTemplate_TemplateIdOrderBySortOrderAsc(templateId);

            if (templateItems.isEmpty()) {
                throw new RuntimeException("Template has no clauses: " + template.getTemplateName());
            }

            // Cleanup invalid template items
            cleanupInvalidTemplateItems(templateId);

            // Reload valid items
            templateItems = templateItemRepo.findByTemplate_TemplateIdOrderBySortOrderAsc(templateId);

            // Filter by selectedClauseIds if provided
            if (selectedClauseIds != null && !selectedClauseIds.isEmpty()) {
                templateItems = templateItems.stream()
                        .filter(item -> item.getClause() != null &&
                                selectedClauseIds.contains(item.getClause().getClauseId()))
                        .collect(Collectors.toList());

                if (templateItems.isEmpty()) {
                    throw new RuntimeException("No valid clauses found for the selected clause IDs");
                }
            }

            List<ChecklistTemplateItem> validTemplateItems = templateItems.stream()
                    .filter(item -> item.getClause() != null)
                    .collect(Collectors.toList());

            if (validTemplateItems.isEmpty()) {
                throw new RuntimeException("Template has no valid clauses (all clauses are null): " +
                        template.getTemplateName());
            }

            // Create checklist
            Checklist checklist = new Checklist();
            checklist.setIsoStandard(template.getIsoStandard());
            checklist.setStatus(Status.PLANNED);
            checklist.setCreatedAt(LocalDateTime.now());
            checklist.setUpdatedAt(LocalDateTime.now());
            checklist.setAudit(audit);
            checklist.setChecklistItems(new HashSet<>());

            // Save checklist first to generate ID
            checklist = checklistRepo.save(checklist);

            // Build checklist items
            List<Checklist_item> itemsToSave = new ArrayList<>();
            for (ChecklistTemplateItem templateItem : validTemplateItems) {
                Clause_library clause = clauseRepo.findById(templateItem.getClause().getClauseId())
                        .orElseThrow(() -> new RuntimeException("Clause not found: " +
                                templateItem.getClause().getClauseId()));

                Checklist_item item = new Checklist_item();
                item.setChecklist(checklist);
                item.setClause(clause);
                item.setCustomText(templateItem.getCustomText());
                item.setConformityStatus(ConformityStatus.PENDING_EVALUATION);
                item.setCreatedAt(LocalDateTime.now());
                item.setUpdatedAt(LocalDateTime.now());

                itemsToSave.add(item);
            }

            // Save items explicitly
            List<Checklist_item> savedItems = checklistItemRepository.saveAll(itemsToSave);

            // Attach items back to checklist
            checklist.setChecklistItems(new HashSet<>(savedItems));

            System.out.println("DEBUG: Saved checklist " + checklist.getChecklistId() +
                    " with " + savedItems.size() + " items");

            // Map to DTO
            ChecklistResponse response = new ChecklistResponse();
            response.setChecklistId(checklist.getChecklistId());
            response.setIsoStandard(checklist.getIsoStandard());
            response.setAuditId(auditId);
            response.setStatus(checklist.getStatus());
            response.setCreatedAt(checklist.getCreatedAt());
            response.setUpdatedAt(checklist.getUpdatedAt());

            List<ChecklistResponse.ChecklistItemDTO> itemDTOs = savedItems.stream().map(item -> {
                ChecklistResponse.ChecklistItemDTO dto = new ChecklistResponse.ChecklistItemDTO();
                dto.setItemId(item.getItemId());
                dto.setClauseId(item.getClause().getClauseId());
                dto.setClauseNumber(item.getClause().getClauseNumber());
                dto.setClauseName(item.getClause().getClauseName());
                dto.setCustomText(item.getCustomText());
                dto.setConformityStatus(item.getConformityStatus().name());
                dto.setComments(item.getComments());
                dto.setEvidenceNotes(item.getEvidenceNotes());
                dto.setEvaluatedBy(item.getEvaluatedBy());
                dto.setEvaluatedAt(item.getEvaluatedAt());
                dto.setEvidenceDocuments(new ArrayList<>());
                dto.setEvaluated(item.getEvaluatedAt() != null);
                return dto;
            }).collect(Collectors.toList());

            response.setChecklistItems(itemDTOs);
            response.setAuditProgress(null);

            System.out.println("=== END DEBUG: Created checklist " + checklist.getChecklistId() + " ===");

            return response;

        } catch (Exception e) {
            System.err.println("ERROR in createChecklistFromTemplateSafe: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }




    // ==================== CHECKLIST MANAGEMENT ====================

    public ChecklistResponse createChecklist(ChecklistRequest request) {
        Checklist checklist = new Checklist();
        checklist.setIsoStandard(request.getIsoStandard());

        Audit audit = auditRepo.findById(request.getAuditId())
                .orElseThrow(() -> new RuntimeException("Audit not found"));
        checklist.setAudit(audit);

        checklist.setStatus(request.getStatus());
        checklist.setCreatedAt(LocalDateTime.now());
        checklist.setUpdatedAt(LocalDateTime.now());

        checklist = checklistRepo.save(checklist);
        return mapToResponse(checklist);
    }

    public List<ChecklistResponse> getAllChecklists() {
        return checklistRepo.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ChecklistResponse getChecklistById(Long id) {
        Checklist checklist = checklistRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Checklist not found"));
        return mapToResponse(checklist);
    }

    public ChecklistResponse updateChecklist(Long id, ChecklistRequest request) {
        Checklist checklist = checklistRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Checklist not found"));

        checklist.setIsoStandard(request.getIsoStandard());
        checklist.setStatus(request.getStatus());
        checklist.setUpdatedAt(LocalDateTime.now());

        if (!Objects.equals(checklist.getAudit().getAuditId(), request.getAuditId())) {
            Audit audit = auditRepo.findById(request.getAuditId())
                    .orElseThrow(() -> new RuntimeException("Audit not found"));
            checklist.setAudit(audit);
        }

        return mapToResponse(checklistRepo.save(checklist));
    }

    public void deleteChecklist(Long id) {
        Checklist checklist = checklistRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Checklist not found"));
        checklistRepo.delete(checklist);
    }

    public List<ChecklistResponse> getByIsoStandard(ISO iso) {
        return checklistRepo.findByIsoStandard(iso).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ChecklistResponse getChecklistByAudit(Long auditId) {
        List<Checklist> list = checklistRepo.findByAudit_AuditId(auditId);
        if (list.isEmpty()) throw new RuntimeException("No checklist found for audit");

        return mapToResponse(list.get(0));
    }

    // ==================== CHECKLIST ITEM EVALUATION ====================

    @Transactional
    public void evaluateChecklistItem(ChecklistItemEvaluationRequest request) {
        Checklist_item item = itemRepo.findById(request.getItemId())
            .orElseThrow(() -> new RuntimeException("Checklist item not found"));

        item.setConformityStatus(request.getConformityStatus());
        item.setComments(request.getComments());
        item.setEvidenceNotes(request.getEvidenceNotes());
        item.setEvaluatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        // Add evidence documents if provided
        if (request.getEvidenceDocumentIds() != null && !request.getEvidenceDocumentIds().isEmpty()) {
            List<Document> evidenceDocs = documentRepo.findAllById(request.getEvidenceDocumentIds());
            item.setEvidenceDocuments(new ArrayList<>(evidenceDocs));
        }


        itemRepo.save(item);

        // Update checklist status if all items are evaluated
        updateChecklistStatusIfComplete(item.getChecklist().getChecklistId());
    }

    @Transactional
    public void evaluateMultipleItems(List<ChecklistItemEvaluationRequest> requests) {
        for (ChecklistItemEvaluationRequest request : requests) {
            evaluateChecklistItem(request);
        }
    }

    @Transactional
    public void executeAuditClause(AuditExecutionRequest request) {
        try {
            System.out.println("DEBUG: executeAuditClause called for item: " + request.getChecklistItemId());

            // Fetch checklist item with checklist and audit
            Checklist_item item = itemRepo.findByIdWithChecklistAndAudit(request.getChecklistItemId())
                    .orElseThrow(() -> new RuntimeException("Checklist item not found"));

            // Update fields
            item.setConformityStatus(request.getConformityStatus());
            item.setComments(request.getComments());
            item.setEvidenceNotes(request.getEvidenceNotes());
            item.setEvaluatedBy(request.getEvaluatedBy());
            item.setEvaluatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());

            // Handle evidence documents safely
            if (request.getEvidenceDocumentIds() != null && !request.getEvidenceDocumentIds().isEmpty()) {
                List<Document> evidenceDocs = documentRepo.findAllById(request.getEvidenceDocumentIds());
                item.setEvidenceDocuments(new ArrayList<>(evidenceDocs));
            }


            itemRepo.saveAndFlush(item); // flush immediately to avoid lazy init issues

            // Update audit last activity
            Audit audit = item.getChecklist().getAudit();
            audit.setLastActivity(LocalDateTime.now());
            auditRepo.saveAndFlush(audit);

            // Check audit completion
            checkAndUpdateAuditCompletion(audit.getAuditId());

            // Create NonConformity if NON_COMPLIANT
            if (request.getConformityStatus() == ConformityStatus.NON_COMPLIANT) {
                NonConformity nc = new NonConformity();
                nc.setAudit(audit);
                nc.setClause(item.getClause());
                nc.setInstance(null); // optional
                nc.setTitle("Non-compliance: " + item.getClause().getDescription());
                nc.setDescription(request.getComments() != null ? request.getComments() : "Marked non-compliant");
                nc.setSeverity(request.getSeverity() != null ? request.getSeverity() : Severity.HIGH);
                nc.setStatus(Status.PENDING);

                // Assign NC
                AuthUser assignee = audit.getCreatedBy(); // adjust as needed
                nc.setAssignedTo(assignee);

                AuthUser creator = userRepository.findByUsername(request.getEvaluatedBy()).orElse(null);
                nc.setCreatedBy(creator);

                nc.setCreatedAt(LocalDateTime.now());
                nc.setUpdatedAt(LocalDateTime.now());

                nonConformityRepository.save(nc);
                System.out.println("DEBUG: NonConformity created for clause " + item.getClause().getDescription());
            }

            System.out.println("DEBUG: Successfully executed audit clause");

        } catch (Exception e) {
            System.err.println("ERROR in executeAuditClause: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }





    public List<AuditExecutionResponse> getAuditsInProgress() {
        try {
            System.out.println("DEBUG: getAuditsInProgress called");
            
            // Use a completely separate approach to avoid any concurrent modification issues
            List<AuditExecutionResponse> result = new ArrayList<>();
            
            // Get all audits that should be considered "in progress"
            List<Audit> allAudits = auditRepo.findAll();
            System.out.println("DEBUG: Found " + allAudits.size() + " total audits");
            
            for (Audit audit : allAudits) {
                try {
                    // Check if audit should be included
                    boolean shouldInclude = false;
                    boolean needsStatusUpdate = false;
                    
                    if (audit.getStatus() == Status.PLANNED || 
                        audit.getStatus() == Status.IN_PROGRESS || 
                        audit.getStatus() == Status.PENDING) {
                        shouldInclude = true;
                        
                        // Check if audit needs status update
                        if (audit.getStatus() == Status.PLANNED && isAuditWithinScheduledDates(audit)) {
                            needsStatusUpdate = true;
                        }
                    }
                    
                    if (shouldInclude) {
                        // Create a basic response without calling getAuditExecution recursively
                        AuditExecutionResponse basicResponse = createBasicAuditExecutionResponse(audit);
                        result.add(basicResponse);
                        
                        // Update status if needed (after creating the response)
                        if (needsStatusUpdate) {
                            try {
                                audit.setStatus(Status.IN_PROGRESS);
                                audit.setCurrentPhase("IN_PROGRESS");
                                audit.setLastActivity(LocalDateTime.now());
                                auditRepo.save(audit);
                                System.out.println("DEBUG: Updated audit " + audit.getAuditId() + " status to IN_PROGRESS");
                            } catch (Exception e) {
                                System.err.println("ERROR updating audit " + audit.getAuditId() + " status: " + e.getMessage());
                            }
                        }
                    }
                    
                } catch (Exception e) {
                    System.err.println("ERROR processing audit " + audit.getAuditId() + ": " + e.getMessage());
                    // Continue with other audits instead of failing completely
                }
            }
                
            System.out.println("DEBUG: Found " + result.size() + " audits in progress");
            return result;
            
        } catch (Exception e) {
            System.err.println("ERROR in getAuditsInProgress: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Create a basic audit execution response without full checklist data
     * to avoid recursive calls and potential concurrent modification issues
     */
    private AuditExecutionResponse createBasicAuditExecutionResponse(Audit audit) {
        try {
            AuditExecutionResponse response = new AuditExecutionResponse();
            response.setAuditId(audit.getAuditId());
            response.setAuditTitle(audit.getTitle());
            response.setAuditStatus(audit.getStatus());
            response.setCurrentPhase(audit.getCurrentPhase());
            response.setLastActivity(audit.getLastActivity());
            response.setStartDate(audit.getStartDate());
            response.setEndDate(audit.getEndDate());
            
            // Create a basic progress without full checklist data
            AuditExecutionResponse.AuditProgressDTO progress = new AuditExecutionResponse.AuditProgressDTO();
            progress.setTotalClauses(0); // Will be calculated separately if needed
            progress.setEvaluatedClauses(0);
            progress.setCompletionPercentage(0.0);
            progress.setCompliantClauses(0);
            progress.setNonCompliantClauses(0);
            progress.setPartiallyCompliantClauses(0);
            progress.setNotApplicableClauses(0);
            progress.setAuditComplete(false);
            
            response.setProgress(progress);
            response.setChecklists(new ArrayList<>()); // Empty list to avoid recursive loading
            
            return response;
            
        } catch (Exception e) {
            System.err.println("ERROR creating basic response for audit " + audit.getAuditId() + ": " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Check if an audit is currently within its scheduled date range
     */
    private boolean isAuditWithinScheduledDates(Audit audit) {
        if (audit.getStartDate() == null || audit.getEndDate() == null) {
            return false;
        }
        
        LocalDate today = LocalDate.now();
        LocalDate startDate = audit.getStartDate();
        LocalDate endDate = audit.getEndDate();
        
        // Audit is within scheduled dates if today is between start and end dates (inclusive)
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    // ==================== AUDIT PROGRESS TRACKING ====================

    public AuditProgress getAuditProgress(Long auditId) {
        int total = (int) itemRepo.countByAuditId(auditId);
        int completed = (int) itemRepo.countEvaluatedByAuditId(auditId);

        AuditProgress dto = new AuditProgress();
        dto.setTotalClauses(total);
        dto.setEvaluatedClauses(completed);
        dto.setCompletionPercentage(total > 0 ? (completed * 100.0 / total) : 0);
        return dto;
    }

    public List<Checklist_item> getUnevaluatedItems(Long auditId) {
        return itemRepo.findUnevaluatedByAuditId(auditId);
    }

    public List<Checklist_item> getItemsByStatus(Long auditId, ConformityStatus status) {
        return itemRepo.findByAuditIdAndStatus(auditId, status);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Creates checklists for existing audits that don't have them
     */
    @Transactional
    public void createChecklistsForExistingAudits() {
        try {
            System.out.println("DEBUG: Creating checklists for existing audits");
            
            // Get all audits
            List<Audit> allAudits = auditRepo.findAll();
            System.out.println("DEBUG: Found " + allAudits.size() + " total audits");
            
            for (Audit audit : allAudits) {
                try {
                    // Check if audit already has checklists
                    List<Checklist> existingChecklists = checklistRepo.findByAudit_AuditId(audit.getAuditId());
                    
                    if (existingChecklists.isEmpty()) {
                        System.out.println("DEBUG: Audit " + audit.getAuditId() + " has no checklists, creating default");
                        
                        // Get available templates for the audit
                        List<ChecklistTemplate> availableTemplates = templateRepo.findByIsActiveTrue();
                        
                        if (!availableTemplates.isEmpty()) {
                            // Use the first available template
                            ChecklistTemplate defaultTemplate = availableTemplates.get(0);
                            createChecklistFromTemplate(audit.getAuditId(), defaultTemplate.getTemplateId(), null);
                            System.out.println("DEBUG: Created checklist for audit " + audit.getAuditId() + " using template " + defaultTemplate.getTemplateName());
                        } else {
                            System.out.println("WARNING: No active templates available for audit " + audit.getAuditId());
                        }
                    } else {
                        System.out.println("DEBUG: Audit " + audit.getAuditId() + " already has " + existingChecklists.size() + " checklists");
                    }
                    
                } catch (Exception e) {
                    System.err.println("ERROR creating checklist for audit " + audit.getAuditId() + ": " + e.getMessage());
                    // Continue with other audits
                }
            }
            
            System.out.println("DEBUG: Finished creating checklists for existing audits");
            
        } catch (Exception e) {
            System.err.println("ERROR in createChecklistsForExistingAudits: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void updateChecklistStatusIfComplete(Long checklistId) {
        Checklist checklist = checklistRepo.findById(checklistId)
            .orElseThrow(() -> new RuntimeException("Checklist not found"));

        List<Checklist_item> items = itemRepo.findByChecklist_ChecklistId(checklistId);
        boolean allEvaluated = items.stream()
            .allMatch(item -> item.getConformityStatus() != null);

        if (allEvaluated) {
            checklist.setStatus(Status.COMPLETED);
            checklist.setUpdatedAt(LocalDateTime.now());
            checklistRepo.save(checklist);
        }
    }

    private ChecklistResponse mapToResponse(Checklist checklist) {
        ChecklistResponse dto = new ChecklistResponse();
        dto.setChecklistId(checklist.getChecklistId());
        dto.setIsoStandard(checklist.getIsoStandard());
        dto.setAuditId(checklist.getAudit() != null ? checklist.getAudit().getAuditId() : null);
        dto.setStatus(checklist.getStatus());
        dto.setCreatedAt(checklist.getCreatedAt());
        dto.setUpdatedAt(checklist.getUpdatedAt());

        List<ChecklistResponse.ChecklistItemDTO> items = checklist.getChecklistItems().stream().map(item -> {
            ChecklistResponse.ChecklistItemDTO itemDto = new ChecklistResponse.ChecklistItemDTO();
            itemDto.setItemId(item.getItemId());
            itemDto.setClauseId(item.getClause().getClauseId());
            itemDto.setClauseName(item.getClause().getClauseName());
            itemDto.setClauseNumber(String.valueOf(item.getClause().getClauseNumber()));
            itemDto.setCustomText(item.getCustomText());
            itemDto.setConformityStatus(item.getConformityStatus() != null ? item.getConformityStatus().name() : null);
            itemDto.setComments(item.getComments());
            itemDto.setEvidenceNotes(item.getEvidenceNotes());
            itemDto.setEvaluatedBy(item.getEvaluatedBy());
            itemDto.setEvaluatedAt(item.getEvaluatedAt());
            itemDto.setEvaluated(item.getConformityStatus() != null);
            
            // Map evidence documents
            if (item.getEvidenceDocuments() != null) {
                itemDto.setEvidenceDocuments(item.getEvidenceDocuments().stream()
                    .map(doc -> {
                        DocumentResponse docResponse = new DocumentResponse();
                        docResponse.setDocumentId(doc.getDocumentId());
                        docResponse.setFileName(doc.getFileName());
                        docResponse.setFileType(doc.getFileType());
                        docResponse.setUploadedAt(doc.getUploadedAt());
                        return docResponse;
                    })
                    .collect(Collectors.toList()));
            }

            return itemDto;
        }).collect(Collectors.toList());

        dto.setChecklistItems(items);
        
        // Add audit progress only if not in a recursive call context
        // This prevents StackOverflowError when mapping checklist responses
        try {
            if (checklist.getAudit() != null) {
                dto.setAuditProgress(getAuditProgress(checklist.getAudit().getAuditId()));
            }
        } catch (StackOverflowError e) {
            System.err.println("WARNING: StackOverflowError prevented in mapToResponse for checklist " + checklist.getChecklistId());
            // Don't set audit progress to prevent infinite recursion
        }

        return dto;
    }

    private ChecklistResponse mapToSimpleResponse(Checklist checklist) {
        try {
            ChecklistResponse dto = new ChecklistResponse();
            dto.setChecklistId(checklist.getChecklistId());
            dto.setIsoStandard(checklist.getIsoStandard());
            dto.setAuditId(checklist.getAudit() != null ? checklist.getAudit().getAuditId() : null);
            dto.setStatus(checklist.getStatus());
            dto.setCreatedAt(checklist.getCreatedAt());
            dto.setUpdatedAt(checklist.getUpdatedAt());

            // Safely get checklist items without triggering lazy loading issues
            List<ChecklistResponse.ChecklistItemDTO> items = new ArrayList<>();
            try {
                if (checklist.getChecklistItems() != null) {
                    for (Checklist_item item : checklist.getChecklistItems()) {
                        try {
                            ChecklistResponse.ChecklistItemDTO itemDto = new ChecklistResponse.ChecklistItemDTO();
                            itemDto.setItemId(item.getItemId());
                            
                            // Safely get clause information
                            if (item.getClause() != null) {
                                itemDto.setClauseId(item.getClause().getClauseId());
                                itemDto.setClauseName(item.getClause().getClauseName());
                                itemDto.setClauseNumber(String.valueOf(item.getClause().getClauseNumber()));
                            } else {
                                itemDto.setClauseId(0L);
                                itemDto.setClauseName("N/A");
                                itemDto.setClauseNumber("N/A");
                            }
                            
                            itemDto.setCustomText(item.getCustomText());
                            itemDto.setConformityStatus(item.getConformityStatus() != null ? item.getConformityStatus().name() : null);
                            itemDto.setComments(item.getComments());
                            itemDto.setEvidenceNotes(item.getEvidenceNotes());
                            itemDto.setEvaluatedBy(item.getEvaluatedBy());
                            itemDto.setEvaluatedAt(item.getEvaluatedAt());
                            itemDto.setEvaluated(item.getConformityStatus() != null);
                            
                            // Safely map evidence documents
                            if (item.getEvidenceDocuments() != null && !item.getEvidenceDocuments().isEmpty()) {
                                List<DocumentResponse> docResponses = new ArrayList<>();
                                for (Document doc : item.getEvidenceDocuments()) {
                                    try {
                                        DocumentResponse docResponse = new DocumentResponse();
                                        docResponse.setDocumentId(doc.getDocumentId());
                                        docResponse.setFileName(doc.getFileName());
                                        docResponse.setFileType(doc.getFileType());
                                        docResponse.setUploadedAt(doc.getUploadedAt());
                                        docResponses.add(docResponse);
                                    } catch (Exception e) {
                                        System.err.println("ERROR mapping evidence document: " + e.getMessage());
                                        // Continue with other documents
                                    }
                                }
                                itemDto.setEvidenceDocuments(docResponses);
                            } else {
                                itemDto.setEvidenceDocuments(new ArrayList<>());
                            }

                            items.add(itemDto);
                        } catch (Exception e) {
                            System.err.println("ERROR mapping checklist item " + item.getItemId() + ": " + e.getMessage());
                            // Create a minimal DTO instead of skipping the item
                            try {
                                ChecklistResponse.ChecklistItemDTO fallbackDto = new ChecklistResponse.ChecklistItemDTO();
                                fallbackDto.setItemId(item.getItemId());
                                fallbackDto.setClauseId(0L);
                                fallbackDto.setClauseName("Error loading clause");
                                fallbackDto.setClauseNumber("ERROR");
                                fallbackDto.setCustomText("Error loading item");
                                fallbackDto.setConformityStatus("PENDING_EVALUATION");
                                fallbackDto.setEvaluated(false);
                                fallbackDto.setEvidenceDocuments(new ArrayList<>());
                                items.add(fallbackDto);
                            } catch (Exception fallbackError) {
                                System.err.println("ERROR creating fallback DTO for item " + item.getItemId() + ": " + fallbackError.getMessage());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("ERROR accessing checklist items: " + e.getMessage());
                // Continue with empty items list
            }

            dto.setChecklistItems(items);
            
            // DO NOT include audit progress to prevent recursion
            // dto.setAuditProgress(getAuditProgress(checklist.getAudit().getAuditId()));

            return dto;
        } catch (Exception e) {
            System.err.println("ERROR in mapToSimpleResponse: " + e.getMessage());
            e.printStackTrace();
            
            // Return a minimal response to prevent complete failure
            ChecklistResponse fallbackResponse = new ChecklistResponse();
            fallbackResponse.setChecklistId(checklist.getChecklistId());
            fallbackResponse.setIsoStandard(checklist.getIsoStandard());
            fallbackResponse.setAuditId(checklist.getAudit() != null ? checklist.getAudit().getAuditId() : null);
            fallbackResponse.setStatus(checklist.getStatus());
            fallbackResponse.setChecklistItems(new ArrayList<>());
            return fallbackResponse;
        }
    }

    private ChecklistTemplateResponse mapToTemplateResponse(ChecklistTemplate template) {
        ChecklistTemplateResponse dto = new ChecklistTemplateResponse();
        dto.setTemplateId(template.getTemplateId());
        dto.setTemplateName(template.getTemplateName());
        dto.setDescription(template.getDescription());
        dto.setIsoStandard(template.getIsoStandard());
        dto.setCreatedAt(template.getCreatedAt());
        dto.setUpdatedAt(template.getUpdatedAt());

        List<ChecklistTemplateItem> templateItems = templateItemRepo.findByTemplate_TemplateIdOrderBySortOrderAsc(template.getTemplateId());
        
        List<ChecklistTemplateResponse.ChecklistClauseResponse> clauses = templateItems.stream()
            .map(item -> {
                ChecklistTemplateResponse.ChecklistClauseResponse clauseDto = new ChecklistTemplateResponse.ChecklistClauseResponse();
                clauseDto.setClauseId(item.getClause().getClauseId());
                clauseDto.setClauseNumber(item.getClause().getClauseNumber());
                clauseDto.setClauseName(item.getClause().getClauseName());
                clauseDto.setCustomText(item.getCustomText());
                clauseDto.setCustomDescription(item.getCustomDescription());
                return clauseDto;
            })
            .collect(Collectors.toList());

        dto.setClauses(clauses);
        return dto;
    }

    // Expose for controller auxiliary endpoints
    public ChecklistResponse mapToResponsePublic(Checklist checklist) { 
        return mapToResponse(checklist); 
    }

    // ==================== AUDIT EXECUTION ====================

    public AuditExecutionResponse getAuditExecution(Long auditId) {
        try {
            System.out.println("DEBUG: getAuditExecution called for audit ID: " + auditId);
            
            Audit audit = auditRepo.findById(auditId)
                .orElseThrow(() -> new RuntimeException("Audit not found with ID: " + auditId));

            System.out.println("DEBUG: Found audit: " + audit.getTitle());

            AuditExecutionResponse response = new AuditExecutionResponse();
            response.setAuditId(audit.getAuditId());
            response.setAuditTitle(audit.getTitle());
            response.setAuditStatus(audit.getStatus());
            response.setCurrentPhase(audit.getCurrentPhase());
            response.setLastActivity(audit.getLastActivity());
            response.setStartDate(audit.getStartDate());
            response.setEndDate(audit.getEndDate());

            // Get checklists for this audit with proper eager fetching
            try {
                System.out.println("DEBUG: Attempting to fetch checklists for audit " + auditId);
                
                // Try the simple method first to see if basic data exists
                List<Checklist> simpleChecklists = checklistRepo.findByAuditIdSimple(auditId);
                System.out.println("DEBUG: Simple query found " + simpleChecklists.size() + " checklists");
                
                // Try the complex method
                List<Checklist> auditChecklists = checklistRepo.findByAuditIdWithItemsAndClauses(auditId);
                System.out.println("DEBUG: Complex query found " + auditChecklists.size() + " checklists");
                
                // Use whichever method returned more results, or the complex one if they're equal
                List<Checklist> finalChecklists = auditChecklists.size() >= simpleChecklists.size() ? auditChecklists : simpleChecklists;
                System.out.println("DEBUG: Using " + finalChecklists.size() + " checklists from " + 
                    (finalChecklists == auditChecklists ? "complex" : "simple") + " query");
                
                // Debug each checklist
                for (Checklist checklist : finalChecklists) {
                    System.out.println("DEBUG: Checklist " + checklist.getChecklistId() + 
                        " has " + (checklist.getChecklistItems() != null ? checklist.getChecklistItems().size() : "NULL") + " items");
                }
                
                // Create a defensive copy to avoid any concurrent modification issues
                List<Checklist> safeChecklists = new ArrayList<>(finalChecklists);
                
                List<AuditExecutionResponse.ChecklistExecutionDTO> checklistDTOs = safeChecklists.stream()
                    .map(this::mapToExecutionChecklistDTO)
                    .collect(Collectors.toList());

                System.out.println("DEBUG: Mapped " + checklistDTOs.size() + " checklist DTOs");
                response.setChecklists(checklistDTOs);
            } catch (Exception e) {
                System.err.println("ERROR processing checklists for audit " + auditId + ": " + e.getMessage());
                e.printStackTrace();
                
                // Fallback: try the simple method
                try {
                    System.out.println("DEBUG: Trying fallback with simple query...");
                    List<Checklist> fallbackChecklists = checklistRepo.findByAuditIdSimple(auditId);
                    System.out.println("DEBUG: Fallback found " + fallbackChecklists.size() + " checklists");
                    
                    List<AuditExecutionResponse.ChecklistExecutionDTO> fallbackDTOs = fallbackChecklists.stream()
                        .map(this::mapToExecutionChecklistDTO)
                        .collect(Collectors.toList());
                    
                    response.setChecklists(fallbackDTOs);
                    System.out.println("DEBUG: Fallback successful, set " + fallbackDTOs.size() + " checklists");
                } catch (Exception fallbackError) {
                    System.err.println("ERROR in fallback approach: " + fallbackError.getMessage());
                    fallbackError.printStackTrace();
                    response.setChecklists(new ArrayList<>()); // Set empty list on error
                }
            }

            // Calculate progress with error handling
            try {
                AuditExecutionResponse.AuditProgressDTO progress = calculateAuditProgress(auditId);
                response.setProgress(progress);
                System.out.println("DEBUG: Progress calculated successfully");
            } catch (Exception e) {
                System.err.println("ERROR calculating progress for audit " + auditId + ": " + e.getMessage());
                // Create a default progress object
                AuditExecutionResponse.AuditProgressDTO defaultProgress = new AuditExecutionResponse.AuditProgressDTO();
                defaultProgress.setTotalClauses(0);
                defaultProgress.setEvaluatedClauses(0);
                defaultProgress.setCompletionPercentage(0.0);
                defaultProgress.setCompliantClauses(0);
                defaultProgress.setNonCompliantClauses(0);
                defaultProgress.setPartiallyCompliantClauses(0);
                defaultProgress.setNotApplicableClauses(0);
                defaultProgress.setAuditComplete(false);
                response.setProgress(defaultProgress);
            }

            System.out.println("DEBUG: Final response has " + response.getChecklists().size() + " checklists");
            System.out.println("DEBUG: Final response has " + response.getProgress().getTotalClauses() + " total clauses");

            return response;
            
        } catch (Exception e) {
            System.err.println("ERROR in getAuditExecution for audit " + auditId + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private AuditExecutionResponse.ChecklistExecutionDTO mapToExecutionChecklistDTO(Checklist checklist) {
        try {
            System.out.println("DEBUG: Mapping checklist " + checklist.getChecklistId() + " to execution DTO");
            
            AuditExecutionResponse.ChecklistExecutionDTO dto = new AuditExecutionResponse.ChecklistExecutionDTO();
            dto.setChecklistId(checklist.getChecklistId());
            dto.setIsoStandard(checklist.getIsoStandard().name());
            dto.setStatus(checklist.getStatus());
            dto.setCreatedAt(checklist.getCreatedAt());
            dto.setUpdatedAt(checklist.getUpdatedAt());

            // Map checklist items -> ClauseExecutionDTO with defensive copying
            Set<Checklist_item> checklistItems = checklist.getChecklistItems();
            if (checklistItems != null && !checklistItems.isEmpty()) {
                System.out.println("DEBUG: Checklist " + checklist.getChecklistId() + " has " + checklistItems.size() + " items");
                
                List<AuditExecutionResponse.ClauseExecutionDTO> clauseDTOs = new ArrayList<>();
                
                for (Checklist_item item : checklistItems) {
                    try {
                        System.out.println("DEBUG: Processing item " + item.getItemId() + " with clause " + 
                            (item.getClause() != null ? item.getClause().getClauseId() : "NULL"));
                        
                        AuditExecutionResponse.ClauseExecutionDTO cdto = new AuditExecutionResponse.ClauseExecutionDTO();
                        cdto.setItemId(item.getItemId());
                        
                        // Safely get clause information
                        if (item.getClause() != null) {
                            cdto.setClauseId(item.getClause().getClauseId());
                            cdto.setClauseNumber(String.valueOf(item.getClause().getClauseNumber()));
                            cdto.setClauseName(item.getClause().getClauseName());
                        } else {
                            System.err.println("WARNING: Item " + item.getItemId() + " has null clause");
                            cdto.setClauseId(0L);
                            cdto.setClauseNumber("N/A");
                            cdto.setClauseName("N/A");
                        }
                        
                        cdto.setCustomText(item.getCustomText());
                        cdto.setConformityStatus(item.getConformityStatus());
                        cdto.setComments(item.getComments());
                        cdto.setEvidenceNotes(item.getEvidenceNotes());
                        cdto.setEvaluatedBy(item.getEvaluatedBy());
                        cdto.setEvaluatedAt(item.getEvaluatedAt());
                        cdto.setCreatedAt(item.getCreatedAt());
                        cdto.setUpdatedAt(item.getUpdatedAt());
                        cdto.setEvaluated(item.getConformityStatus() != null);

                        // Evidence documents mapping with defensive copying
                        if (item.getEvidenceDocuments() != null && !item.getEvidenceDocuments().isEmpty()) {
                            List<AuditExecutionResponse.EvidenceDocumentDTO> evidenceDTOs = new ArrayList<>();
                            for (Document doc : item.getEvidenceDocuments()) {
                                try {
                                    AuditExecutionResponse.EvidenceDocumentDTO evidenceDTO = mapToEvidenceDocumentDTO(doc);
                                    evidenceDTOs.add(evidenceDTO);
                                } catch (Exception e) {
                                    System.err.println("ERROR mapping evidence document: " + e.getMessage());
                                    // Continue with other documents
                                }
                            }
                            cdto.setEvidenceDocuments(evidenceDTOs);
                        } else {
                            cdto.setEvidenceDocuments(new ArrayList<>());
                        }

                        clauseDTOs.add(cdto);
                        System.out.println("DEBUG: Successfully mapped item " + item.getItemId());
                        
                    } catch (Exception e) {
                        System.err.println("ERROR mapping checklist item " + item.getItemId() + ": " + e.getMessage());
                        e.printStackTrace();
                        // Create a minimal DTO instead of skipping the item
                        try {
                            AuditExecutionResponse.ClauseExecutionDTO fallbackDTO = new AuditExecutionResponse.ClauseExecutionDTO();
                            fallbackDTO.setItemId(item.getItemId());
                            fallbackDTO.setClauseId(item.getClause() != null ? item.getClause().getClauseId() : 0L);
                            fallbackDTO.setClauseNumber(item.getClause() != null ? String.valueOf(item.getClause().getClauseNumber()) : "ERROR");
                            fallbackDTO.setClauseName(item.getClause() != null ? item.getClause().getClauseName() : "Error loading clause");
                            fallbackDTO.setCustomText("Error loading item");
                            fallbackDTO.setConformityStatus(ConformityStatus.PENDING_EVALUATION);
                            fallbackDTO.setEvaluated(false);
                            fallbackDTO.setEvidenceDocuments(new ArrayList<>());
                            clauseDTOs.add(fallbackDTO);
                        } catch (Exception fallbackError) {
                            System.err.println("ERROR creating fallback DTO for item " + item.getItemId() + ": " + fallbackError.getMessage());
                        }
                    }
                }

                dto.setClauses(clauseDTOs);
                System.out.println("DEBUG: Successfully mapped checklist " + checklist.getChecklistId() + " with " + clauseDTOs.size() + " clauses");
            } else {
                dto.setClauses(new ArrayList<>());
                System.out.println("DEBUG: Checklist " + checklist.getChecklistId() + " has no items (null or empty)");
            }

            return dto;
            
        } catch (Exception e) {
            System.err.println("ERROR in mapToExecutionChecklistDTO for checklist " + checklist.getChecklistId() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


    private AuditExecutionResponse.ClauseExecutionDTO mapToExecutionClauseDTO(Checklist_item item) {
        try {
            System.out.println("DEBUG: Mapping clause item " + item.getItemId() + " with clause " + item.getClause().getClauseId());
            
            AuditExecutionResponse.ClauseExecutionDTO dto = new AuditExecutionResponse.ClauseExecutionDTO();
            dto.setItemId(item.getItemId());
            dto.setClauseId(item.getClause().getClauseId());
            dto.setClauseNumber(String.valueOf(item.getClause().getClauseNumber()));
            dto.setClauseName(item.getClause().getClauseName());
            dto.setCustomText(item.getCustomText());
            dto.setConformityStatus(item.getConformityStatus());
            dto.setComments(item.getComments());
            dto.setEvidenceNotes(item.getEvidenceNotes());
            dto.setEvaluatedBy(item.getEvaluatedBy());
            dto.setEvaluatedAt(item.getEvaluatedAt());
            dto.setEvaluated(item.getConformityStatus() != null);

            // Set evidence documents if provided with defensive copying
            if (item.getEvidenceDocuments() != null) {
                List<AuditExecutionResponse.EvidenceDocumentDTO> evidenceDTOs = item.getEvidenceDocuments().stream()
                    .map(this::mapToEvidenceDocumentDTO)
                    .collect(Collectors.toList());
                dto.setEvidenceDocuments(evidenceDTOs);
            }

            return dto;
            
        } catch (Exception e) {
            System.err.println("ERROR in mapToExecutionClauseDTO for item " + item.getItemId() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private AuditExecutionResponse.EvidenceDocumentDTO mapToEvidenceDocumentDTO(Document document) {
        AuditExecutionResponse.EvidenceDocumentDTO dto = new AuditExecutionResponse.EvidenceDocumentDTO();
        dto.setDocumentId(document.getDocumentId());
        dto.setFileName(document.getFileName());
        dto.setFileType(document.getFileType());
        dto.setDescription(document.getDescription());
        dto.setFileSize(document.getFileSize());
        dto.setUploadedAt(document.getUploadedAt());
        dto.setUploadedBy(document.getUploadedBy() != null ? document.getUploadedBy().getUsername() : null);
        dto.setTags(document.getTags());
        dto.setClauseReference(document.getClauseReference());
        dto.setDepartment(document.getDepartment());
        dto.setIsEvidence(document.getIsEvidence());
        return dto;
    }

    private AuditExecutionResponse.AuditProgressDTO calculateAuditProgress(Long auditId) {
        try {
            System.out.println("DEBUG: Calculating audit progress for audit ID: " + auditId);
            
            AuditExecutionResponse.AuditProgressDTO progress = new AuditExecutionResponse.AuditProgressDTO();
            
            int total = (int) itemRepo.countByAuditId(auditId);
            int evaluated = (int) itemRepo.countEvaluatedByAuditId(auditId);
            
            System.out.println("DEBUG: Total clauses: " + total + ", Evaluated clauses: " + evaluated);
            
            progress.setTotalClauses(total);
            progress.setEvaluatedClauses(evaluated);
            progress.setCompletionPercentage(total > 0 ? (evaluated * 100.0 / total) : 0);

            // Count by status
            progress.setCompliantClauses((int) itemRepo.countByAuditIdAndConformityStatus(auditId, ConformityStatus.COMPLIANT));
            progress.setNonCompliantClauses((int) itemRepo.countByAuditIdAndConformityStatus(auditId, ConformityStatus.NON_COMPLIANT));
            progress.setPartiallyCompliantClauses((int) itemRepo.countByAuditIdAndConformityStatus(auditId, ConformityStatus.PARTIALLY_COMPLIANT));
            progress.setNotApplicableClauses((int) itemRepo.countByAuditIdAndConformityStatus(auditId, ConformityStatus.NOT_APPLICABLE));

            // Check if audit is complete (all clauses evaluated and all are compliant)
            progress.setAuditComplete(evaluated == total && progress.getNonCompliantClauses() == 0 && progress.getPartiallyCompliantClauses() == 0);

            System.out.println("DEBUG: Progress calculated successfully");
            return progress;
            
        } catch (Exception e) {
            System.err.println("ERROR in calculateAuditProgress for audit " + auditId + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void checkAndUpdateAuditCompletion(Long auditId) {
        try {
            AuditExecutionResponse.AuditProgressDTO progress = calculateAuditProgress(auditId);
            
            if (progress.isAuditComplete()) {
                Audit audit = auditRepo.findById(auditId)
                    .orElseThrow(() -> new RuntimeException("Audit not found"));
                
                audit.setStatus(Status.COMPLETED);
                audit.setCurrentPhase("COMPLETED");
                audit.setLastActivity(LocalDateTime.now());
                auditRepo.save(audit);
                
                System.out.println("DEBUG: Audit " + auditId + " marked as completed");
            }
        } catch (Exception e) {
            System.err.println("ERROR in checkAndUpdateAuditCompletion for audit " + auditId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Simple method to get basic audit information without complex processing
     * to help debug concurrent modification issues
     */
    public Map<String, Object> getBasicAuditInfo(Long auditId) {
        try {
            System.out.println("DEBUG: getBasicAuditInfo called for audit ID: " + auditId);
            
            Map<String, Object> result = new HashMap<>();
            
            // Get basic audit info
            Audit audit = auditRepo.findById(auditId)
                .orElseThrow(() -> new RuntimeException("Audit not found with ID: " + auditId));
            
            result.put("auditId", audit.getAuditId());
            result.put("title", audit.getTitle());
            result.put("status", audit.getStatus());
            result.put("currentPhase", audit.getCurrentPhase());
            result.put("startDate", audit.getStartDate());
            result.put("endDate", audit.getEndDate());
            
            // Get basic checklist count
            try {
                List<Checklist> checklists = checklistRepo.findByAuditIdWithItems(auditId);
                result.put("checklistCount", checklists.size());
                
                // Get basic item count
                int totalItems = 0;
                for (Checklist checklist : checklists) {
                    if (checklist.getChecklistItems() != null) {
                        totalItems += checklist.getChecklistItems().size();
                    }
                }
                result.put("totalItems", totalItems);
                
            } catch (Exception e) {
                System.err.println("ERROR getting checklist info: " + e.getMessage());
                result.put("checklistCount", 0);
                result.put("totalItems", 0);
            }
            
            System.out.println("DEBUG: Basic audit info retrieved successfully");
            return result;
            
        } catch (Exception e) {
            System.err.println("ERROR in getBasicAuditInfo for audit " + auditId + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Test method to compare different checklist loading approaches
     */
    public Map<String, Object> testChecklistLoading(Long auditId) {
        try {
            System.out.println("DEBUG: testChecklistLoading called for audit ID: " + auditId);
            
            Map<String, Object> result = new HashMap<>();
            
            // Test 1: Complex query with joins
            try {
                List<Checklist> complexChecklists = checklistRepo.findByAuditIdWithItems(auditId);
                result.put("complexQueryCount", complexChecklists.size());
                result.put("complexQueryItems", complexChecklists.stream()
                    .mapToInt(c -> c.getChecklistItems() != null ? c.getChecklistItems().size() : 0)
                    .sum());
                
                // Debug complex query results
                List<Map<String, Object>> complexDetails = new ArrayList<>();
                for (Checklist c : complexChecklists) {
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("checklistId", c.getChecklistId());
                    detail.put("itemCount", c.getChecklistItems() != null ? c.getChecklistItems().size() : 0);
                    detail.put("hasItems", c.getChecklistItems() != null && !c.getChecklistItems().isEmpty());
                    complexDetails.add(detail);
                }
                result.put("complexQueryDetails", complexDetails);
                
            } catch (Exception e) {
                result.put("complexQueryError", e.getMessage());
            }
            
            // Test 2: Simple query without joins
            try {
                List<Checklist> simpleChecklists = checklistRepo.findByAuditIdSimple(auditId);
                result.put("simpleQueryCount", simpleChecklists.size());
                
                // Manually load items for simple query
                int totalItems = 0;
                List<Map<String, Object>> simpleDetails = new ArrayList<>();
                for (Checklist c : simpleChecklists) {
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("checklistId", c.getChecklistId());
                    
                    // Force load items
                    c.getChecklistItems().size(); // This should trigger lazy loading
                    int itemCount = c.getChecklistItems().size();
                    detail.put("itemCount", itemCount);
                    detail.put("hasItems", itemCount > 0);
                    totalItems += itemCount;
                    
                    simpleDetails.add(detail);
                }
                result.put("simpleQueryItems", totalItems);
                result.put("simpleQueryDetails", simpleDetails);
                
            } catch (Exception e) {
                result.put("simpleQueryError", e.getMessage());
            }
            
            System.out.println("DEBUG: Checklist loading test completed");
            return result;
            
        } catch (Exception e) {
            System.err.println("ERROR in testChecklistLoading for audit " + auditId + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Cleans up invalid template items (those with null clauses) from a template.
     * This is necessary because the template might have been modified externally
     * or there might be orphaned items in the database.
     */
    @Transactional
    public void cleanupInvalidTemplateItems(Long templateId) {
        System.out.println("DEBUG: Cleaning up invalid template items for template ID: " + templateId);
        List<ChecklistTemplateItem> invalidItems = templateItemRepo.findByTemplate_TemplateIdAndClauseIsNull(templateId);
        System.out.println("DEBUG: Found " + invalidItems.size() + " invalid template items to delete.");
        templateItemRepo.deleteAll(invalidItems);
        System.out.println("DEBUG: Deleted " + invalidItems.size() + " invalid template items.");
    }
}