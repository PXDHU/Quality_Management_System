package com.example.QualityManagementSystem.service;

import com.example.QualityManagementSystem.dto.*;
import com.example.QualityManagementSystem.model.*;
import com.example.QualityManagementSystem.model.ChecklistTemplate;
import com.example.QualityManagementSystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuditService {

    @Autowired
    @Lazy
    AuditRepository auditRepo;

    @Autowired
    @Lazy
    UserRepository userRepo;

    @Autowired
    @Lazy
    ChecklistRepository checklistRepo;

    @Autowired
    @Lazy
    ChecklistTemplateRepository checklistTemplateRepo;

    @Autowired
    @Lazy
    ClauseLibraryRepository clauseLibraryRepo;

    @Autowired
    @Lazy
    ChecklistTemplateItemRepository checklistTemplateItemRepo;

    @Autowired
    @Lazy
    ChecklistItemRepository checklistItemRepo;

    @Transactional
    public AuditResponse createAudit(AuditRequest request, Long creatorId) {
        // Validate dates
        if (request.endDate.isBefore(request.startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        Audit audit = new Audit();
        audit.setTitle(request.title);
        audit.setScope(request.scope);
        audit.setObjectives(request.objectives);
        audit.setStartDate(request.startDate);
        audit.setEndDate(request.endDate);
        audit.setStatus(Status.PLANNED);
        audit.setAuditType(request.auditType);
        audit.setDepartment(request.department);
        audit.setLocation(request.location);
        audit.setNotes(request.notes);
        audit.setCurrentPhase("PLANNING");
        audit.setLastActivity(LocalDateTime.now());
        audit.setCreatedBy(
                userRepo.findById(creatorId)
                        .orElseThrow(() -> new RuntimeException("Creator user not found"))
        );

        List<Long> auditorIds = request.auditorIds != null ? request.auditorIds : Collections.emptyList();
        if (auditorIds.isEmpty()) {
            throw new IllegalArgumentException("At least one auditor must be assigned");
        }
        
        List<AuthUser> allById = userRepo.findAllById(auditorIds);

        audit.setAuditors(new HashSet<>(allById));

        audit = auditRepo.save(audit);
        
        return mapToDTO(audit);
    }
    
    /**
     * Creates a checklist from template for an audit
     */
    private void createChecklistFromTemplate(Long auditId, Long templateId, List<Long> selectedClauseIds) {
        try {
            // Get the audit
            Audit audit = auditRepo.findById(auditId)
                .orElseThrow(() -> new RuntimeException("Audit not found"));

            // Get the template
            ChecklistTemplate template = checklistTemplateRepo.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));

            // Create new checklist with minimal entity relationships
            Checklist checklist = new Checklist();
            checklist.setIsoStandard(template.getIsoStandard());
            checklist.setStatus(Status.PLANNED);
            checklist.setCreatedAt(LocalDateTime.now());
            checklist.setUpdatedAt(LocalDateTime.now());
            
            // Set audit directly without using helper methods
            checklist.setAudit(audit);
            
            // Initialize empty items set
            checklist.setChecklistItems(new HashSet<>());

            // Save the checklist first
            checklist = checklistRepo.save(checklist);

            // Get template items
            List<ChecklistTemplateItem> templateItems = checklistTemplateItemRepo.findByTemplate_TemplateIdOrderBySortOrderAsc(templateId);
            
            // Filter by selected clauses if provided
            if (selectedClauseIds != null && !selectedClauseIds.isEmpty()) {
                templateItems = templateItems.stream()
                    .filter(item -> selectedClauseIds.contains(item.getClause().getClauseId()))
                    .collect(Collectors.toList());
            }

            // Create checklist items individually and save them
            for (ChecklistTemplateItem templateItem : templateItems) {
                Checklist_item item = new Checklist_item();
                item.setChecklist(checklist);
                item.setClause(templateItem.getClause());
                item.setCustomText(templateItem.getCustomText());
                item.setConformityStatus(ConformityStatus.PENDING_EVALUATION); // Default status
                item.setCreatedAt(LocalDateTime.now());
                item.setUpdatedAt(LocalDateTime.now());

                // Save the item directly
                checklistItemRepo.save(item);
            }

            System.out.println("Created checklist " + checklist.getChecklistId() + " with " + templateItems.size() + " items");

        } catch (Exception e) {
            System.err.println("Failed to create checklist from template: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Creates a default ISO 9001 template with basic clauses
     */
    private void createDefaultISOTemplate() {
        try {
            ChecklistTemplate template = new ChecklistTemplate();
            template.setTemplateName("Default ISO 9001 Template");
            template.setDescription("Default template with basic ISO 9001 clauses");
            template.setIsoStandard(ISO.ISO_9001);
            template.setCreatedAt(LocalDateTime.now());
            template.setUpdatedAt(LocalDateTime.now());
            template.setActive(true);
            
            template = checklistTemplateRepo.save(template);
            
            // Add some basic clauses
            String[] clauseNumbers = {"4.1", "4.2", "4.3", "4.4", "5.1", "5.2", "5.3", "6.1", "6.2", "7.1", "7.2", "7.3", "7.4", "7.5", "8.1", "8.2", "8.3", "8.4", "8.5", "9.1", "9.2", "9.3", "10.1", "10.2", "10.3"};
            String[] clauseNames = {
                "Understanding the organization and its context",
                "Understanding the needs and expectations of interested parties",
                "Determining the scope of the quality management system",
                "Quality management system and its processes",
                "Leadership and commitment",
                "Quality policy",
                "Organizational roles, responsibilities and authorities",
                "Actions to address risks and opportunities",
                "Quality objectives and planning to achieve them",
                "Resources",
                "Competence",
                "Awareness",
                "Communication",
                "Documented information",
                "Operational planning and control",
                "Requirements for products and services",
                "Design and development of products and services",
                "Control of externally provided processes, products and services",
                "Production and service provision",
                "Control of nonconforming outputs",
                "Monitoring, measurement, analysis and evaluation",
                "Internal audit",
                "Management review",
                "Improvement",
                "Nonconformity and corrective action",
                "Continual improvement"
            };
            
            for (int i = 0; i < clauseNumbers.length; i++) {
                final int index = i; // Create a final copy for the lambda
                final String clauseNumber = clauseNumbers[i];
                final String clauseName = clauseNames[i];
                
                // Check if clause exists, if not create it
                Clause_library clause = clauseLibraryRepo.findByClauseNumber(clauseNumber)
                    .orElseGet(() -> {
                        Clause_library newClause = new Clause_library();
                        newClause.setClauseNumber(clauseNumber);
                        newClause.setClauseName(clauseName);
                        newClause.setDescription("ISO 9001:2015 " + clauseNumber + " - " + clauseName);
                        newClause.setStandard(ISO.ISO_9001);
                        newClause.setCreatedAt(LocalDateTime.now());
                        newClause.setUpdatedAt(LocalDateTime.now());
                        return clauseLibraryRepo.save(newClause);
                    });
                
                ChecklistTemplateItem templateItem = new ChecklistTemplateItem();
                templateItem.setTemplate(template);
                templateItem.setClause(clause);
                templateItem.setCustomText("Evaluate compliance with " + clauseNumber + " - " + clauseName);
                templateItem.setSortOrder(index + 1);
                templateItem.setCreatedAt(LocalDateTime.now());
                templateItem.setUpdatedAt(LocalDateTime.now());
                
                checklistTemplateItemRepo.save(templateItem);
            }
            
            System.out.println("Created default ISO 9001 template with " + clauseNumbers.length + " clauses");
            
        } catch (Exception e) {
            System.err.println("Failed to create default ISO template: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get audits that don't have any checklists
     */
    @Transactional(readOnly = true)
    public List<Audit> getAuditsWithoutChecklists() {
        List<Audit> allAudits = auditRepo.findAll();
        return allAudits.stream()
            .filter(audit -> audit.getChecklists() == null || audit.getChecklists().isEmpty())
            .collect(Collectors.toList());
    }

    /**
     * Creates a default checklist for an existing audit
     */
    @Transactional
    public void createDefaultChecklistForAudit(Audit audit) {
        try {
            // Check if there are any active templates available
            List<ChecklistTemplate> availableTemplates = checklistTemplateRepo.findByIsActiveTrue();
            
            if (availableTemplates.isEmpty()) {
                // Create a default ISO 9001 template if none exists
                createDefaultISOTemplate();
                availableTemplates = checklistTemplateRepo.findByIsActiveTrue();
            }
            
            // Use the first available template to create a checklist
            if (!availableTemplates.isEmpty()) {
                ChecklistTemplate defaultTemplate = availableTemplates.get(0);
                createChecklistFromTemplate(audit.getAuditId(), defaultTemplate.getTemplateId(), null);
                System.out.println("Created checklist from template " + defaultTemplate.getTemplateName() + " for audit " + audit.getAuditId());
            }
        } catch (Exception e) {
            // Log the error but don't fail the audit creation
            System.err.println("Failed to create default checklist for audit " + audit.getAuditId() + ": " + e.getMessage());
        }
    }
    
    /**
     * Adds clauses to the template
     */
    private void addClausesToTemplate(ChecklistTemplate template) {
        try {
            // Get the clauses we just created
            List<Clause_library> clauses = clauseLibraryRepo.findByStandard(ISO.ISO_9001);
            
            for (int i = 0; i < clauses.size(); i++) {
                Clause_library clause = clauses.get(i);
                
                ChecklistTemplateItem templateItem = new ChecklistTemplateItem();
                templateItem.setTemplate(template);
                templateItem.setClause(clause);
                templateItem.setCustomText(clause.getDescription());
                templateItem.setCustomDescription(clause.getClauseName());
                templateItem.setSortOrder(i + 1);
                templateItem.setCreatedAt(LocalDateTime.now());
                templateItem.setUpdatedAt(LocalDateTime.now());
                
                checklistTemplateItemRepo.save(templateItem);
            }
            
            System.out.println("Added " + clauses.size() + " clauses to template " + template.getTemplateName());
            
        } catch (Exception e) {
            System.err.println("Failed to add clauses to template: " + e.getMessage());
        }
    }
    
    /**
     * Creates default ISO 9001 clauses if they don't exist
     */
    private void createDefaultISO9001Clauses() {
        try {
            // Check if clauses already exist
            List<Clause_library> existingClauses = clauseLibraryRepo.findByStandard(ISO.ISO_9001);
            if (!existingClauses.isEmpty()) {
                System.out.println("ISO 9001 clauses already exist, skipping creation");
                return;
            }
            
            // Create basic ISO 9001 clauses
            String[][] clauseData = {
                {"4.1", "Understanding the organization and its context", "Context of the organization"},
                {"4.2", "Understanding the needs and expectations of interested parties", "Interested parties"},
                {"4.3", "Determining the scope of the quality management system", "Scope of the QMS"},
                {"4.4", "Quality management system and its processes", "QMS and processes"},
                {"5.1", "Leadership and commitment", "Leadership commitment"},
                {"5.2", "Quality policy", "Quality policy"},
                {"5.3", "Organizational roles, responsibilities and authorities", "Roles and responsibilities"},
                {"6.1", "Actions to address risks and opportunities", "Risk management"},
                {"6.2", "Quality objectives and planning to achieve them", "Quality objectives"},
                {"7.1", "Resources", "Resources"},
                {"7.2", "Competence", "Competence"},
                {"7.3", "Awareness", "Awareness"},
                {"7.4", "Communication", "Communication"},
                {"7.5", "Documented information", "Documentation"},
                {"8.1", "Operational planning and control", "Operational planning"},
                {"8.2", "Requirements for products and services", "Product requirements"},
                {"8.3", "Design and development of products and services", "Design and development"},
                {"8.4", "Control of externally provided processes, products and services", "External providers"},
                {"8.5", "Production and service provision", "Production and service"},
                {"8.6", "Release of products and services", "Release of products"},
                {"8.7", "Control of nonconforming outputs", "Nonconforming outputs"},
                {"9.1", "Monitoring, measurement, analysis and evaluation", "Monitoring and measurement"},
                {"9.2", "Internal audit", "Internal audit"},
                {"9.3", "Management review", "Management review"},
                {"10.1", "General", "Improvement"},
                {"10.2", "Nonconformity and corrective action", "Corrective action"},
                {"10.3", "Continual improvement", "Continual improvement"}
            };
            
            for (String[] clauseInfo : clauseData) {
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
                
                clauseLibraryRepo.save(clause);
            }
            
            System.out.println("Created " + clauseData.length + " default ISO 9001 clauses");
            
        } catch (Exception e) {
            System.err.println("Failed to create default clauses: " + e.getMessage());
        }
    }

    /**
     * Creates checklists for existing audits that don't have them
     */
    @Transactional
    public void createChecklistsForExistingAudits() {
        try {
            List<Audit> auditsWithoutChecklists = auditRepo.findAll().stream()
                .filter(audit -> audit.getChecklists() == null || audit.getChecklists().isEmpty())
                .collect(Collectors.toList());
            
            System.out.println("Found " + auditsWithoutChecklists.size() + " audits without checklists");
            
            for (Audit audit : auditsWithoutChecklists) {
                try {
                    createDefaultChecklistForAudit(audit);
                    System.out.println("Created checklist for existing audit: " + audit.getTitle() + " (ID: " + audit.getAuditId() + ")");
                } catch (Exception e) {
                    System.err.println("Failed to create checklist for audit " + audit.getAuditId() + ": " + e.getMessage());
                }
            }
            
            System.out.println("Completed creating checklists for existing audits");
            
        } catch (Exception e) {
            System.err.println("Failed to create checklists for existing audits: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<AuditResponse> getAllAudits() {
        // For list views, we don't need full checklist data, just the count
        List<Audit> audits = auditRepo.findAll();
        return audits.stream().map(a -> {
            try {
                return mapToDTO(a);
            } catch (Exception ex) {
                System.err.println("Failed to map audit id=" + a.getAuditId() + ": " + ex.getMessage());
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditResponse> getAuditsWithFilters(AuditFilterRequest filter) {
        try {
            System.out.println("DEBUG: getAuditsWithFilters called with filter: " + filter);
            
            // For filtered list views, we don't need full checklist data
            List<Audit> audits = auditRepo.findAll();
            System.out.println("DEBUG: Found " + audits.size() + " audits in database");
            
            List<AuditResponse> result = audits.stream()
                    .filter(audit -> {
                        try {
                            boolean statusOk = filterByStatus(audit, filter.getStatus());
                            boolean typeOk = filterByAuditType(audit, filter.getAuditType());
                            boolean deptOk = filterByDepartment(audit, filter.getDepartment());
                            boolean locOk = filterByLocation(audit, filter.getLocation());
                            boolean dateOk = filterByDateRange(audit, filter.getStartDateFrom(), filter.getStartDateTo(), 
                                                          filter.getEndDateFrom(), filter.getEndDateTo());
                            boolean searchOk = filterBySearchTerm(audit, filter.getSearchTerm());
                            boolean overdueOk = filterByOverdue(audit, filter.getIsOverdue());
                            
                            boolean allOk = statusOk && typeOk && deptOk && locOk && dateOk && searchOk && overdueOk;
                            
                            if (!allOk) {
                                System.out.println("DEBUG: Audit " + audit.getAuditId() + " filtered out:");
                                System.out.println("  statusOk: " + statusOk + " (audit status: " + audit.getStatus() + ", filter status: " + filter.getStatus() + ")");
                                System.out.println("  typeOk: " + typeOk + " (audit type: " + audit.getAuditType() + ", filter type: " + filter.getAuditType() + ")");
                                System.out.println("  deptOk: " + deptOk + " (audit dept: " + audit.getDepartment() + ", filter dept: " + filter.getDepartment() + ")");
                                System.out.println("  locOk: " + locOk + " (audit loc: " + audit.getLocation() + ", filter loc: " + filter.getLocation() + ")");
                                System.out.println("  dateOk: " + dateOk);
                                System.out.println("  searchOk: " + searchOk);
                                System.out.println("  overdueOk: " + overdueOk);
                            }
                            
                            return allOk;
                        } catch (Exception e) {
                            System.err.println("ERROR filtering audit " + audit.getAuditId() + ": " + e.getMessage());
                            e.printStackTrace();
                            return false;
                        }
                    })
                    .map(audit -> {
                        try {
                            return mapToDTO(audit);
                        } catch (Exception e) {
                            System.err.println("ERROR mapping audit " + audit.getAuditId() + " to DTO: " + e.getMessage());
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            System.out.println("DEBUG: Returning " + result.size() + " filtered audits");
            return result;
            
        } catch (Exception e) {
            System.err.println("ERROR in getAuditsWithFilters: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<AuditCalendarResponse> getAuditCalendar() {
        // For calendar views, we don't need full checklist data
        List<Audit> audits = auditRepo.findAll();
        return audits.stream()
                .map(this::mapToCalendarDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditProgress> getAuditProgress() {
        // For progress views, we don't need full checklist data
        List<Audit> audits = auditRepo.findAll();
        return audits.stream()
                .map(this::mapToProgressDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AuditResponse getAuditById(Long id) {
        Audit audit = auditRepo.findByIdWithChecklistsAndItems(id)
                .orElseThrow(() -> new RuntimeException("Audit not found"));

        System.out.println("DEBUG: Fetched audit " + id +
                " with " + (audit.getChecklists() != null ? audit.getChecklists().size() : 0) +
                " checklists");

        return mapToDTO(audit);
    }


    /**
     * Test method to debug audit data loading
     */
    @Transactional
    public Map<String, Object> debugAuditData(Long id) {
        Map<String, Object> debugInfo = new HashMap<>();
        
        try {
            // Get basic audit info
            Audit audit = auditRepo.findById(id).orElse(null);
            if (audit == null) {
                debugInfo.put("error", "Audit not found");
                return debugInfo;
            }
            
            debugInfo.put("auditId", audit.getAuditId());
            debugInfo.put("title", audit.getTitle());
            debugInfo.put("status", audit.getStatus());
            
            // Try to get checklists
            try {
                List<Checklist> checklists = checklistRepo.findByAuditIdWithItemsSimple(id);
                debugInfo.put("checklistsCount", checklists.size());
                
                List<Map<String, Object>> checklistDetails = new ArrayList<>();
                for (Checklist checklist : checklists) {
                    Map<String, Object> checklistInfo = new HashMap<>();
                    checklistInfo.put("checklistId", checklist.getChecklistId());
                    checklistInfo.put("isoStandard", checklist.getIsoStandard());
                    checklistInfo.put("status", checklist.getStatus());
                    
                    if (checklist.getChecklistItems() != null) {
                        checklistInfo.put("itemsCount", checklist.getChecklistItems().size());
                    } else {
                        checklistInfo.put("itemsCount", 0);
                    }
                    
                    checklistDetails.add(checklistInfo);
                }
                debugInfo.put("checklistDetails", checklistDetails);
                
            } catch (Exception e) {
                debugInfo.put("checklistsError", e.getMessage());
                e.printStackTrace();
            }
            
        } catch (Exception e) {
            debugInfo.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return debugInfo;
    }

    @Transactional
    public AuditResponse updateAuditStatus(Long id, UpdateAuditStatus statusDTO) {
        Audit audit = auditRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Audit not found"));

        if (statusDTO.status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        audit.setStatus(statusDTO.status);
        audit.setUpdatedAt(LocalDateTime.now());
        audit.setLastActivity(LocalDateTime.now());

        // Update current phase based on status
        switch (statusDTO.status) {
            case PLANNED:
                audit.setCurrentPhase("PLANNING");
                break;
            case IN_PROGRESS:
                audit.setCurrentPhase("EXECUTION");
                break;
            case COMPLETED:
                audit.setCurrentPhase("REPORTING");
                break;
            default:
                audit.setCurrentPhase("PLANNING");
        }

        return mapToDTO(audit);
    }

    @Transactional
    public AuditResponse updateAudit(Long id, AuditRequest req) {
        Audit audit = auditRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Audit not found"));

        if (req.title != null) audit.setTitle(req.title);
        if (req.scope != null) audit.setScope(req.scope);
        if (req.objectives != null) audit.setObjectives(req.objectives);
        if (req.startDate != null) audit.setStartDate(req.startDate);
        if (req.endDate != null) audit.setEndDate(req.endDate);
        if (req.auditType != null) audit.setAuditType(req.auditType);
        if (req.department != null) audit.setDepartment(req.department);
        if (req.location != null) audit.setLocation(req.location);
        if (req.notes != null) audit.setNotes(req.notes);
        
        if (req.auditorIds != null && !req.auditorIds.isEmpty()) {
            audit.setAuditors(new HashSet<>(userRepo.findAllById(req.auditorIds)));
        }

        audit.setUpdatedAt(LocalDateTime.now());
        audit.setLastActivity(LocalDateTime.now());
        return mapToDTO(audit);
    }

    @Transactional
    public AuditResponse updateAuditPhase(Long id, String phase) {
        Audit audit = auditRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Audit not found"));

        audit.setCurrentPhase(phase);
        audit.setUpdatedAt(LocalDateTime.now());
        audit.setLastActivity(LocalDateTime.now());

        return mapToDTO(audit);
    }

    @Transactional(readOnly = true)
    public List<AuditResponse> getAuditsByStatus(Status status) {
        // For status-based views, we don't need full checklist data
        List<Audit> audits = auditRepo.findByStatus(status);
        return audits.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditResponse> getOverdueAudits() {
        // For overdue views, we don't need full checklist data
        List<Audit> audits = auditRepo.findAll();
        LocalDate today = LocalDate.now();
        
        return audits.stream()
                .filter(audit -> audit.getEndDate().isBefore(today) && 
                               audit.getStatus() != Status.COMPLETED)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditResponse> getUpcomingAudits() {
        // For upcoming views, we don't need full checklist data
        List<Audit> audits = auditRepo.findAll();
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        
        return audits.stream()
                .filter(audit -> audit.getStartDate().isAfter(today) &&
audit.getStartDate().isBefore(nextWeek))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAudit(Long id) {
        Audit audit = auditRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Audit not found"));
        
        // Check if audit can be deleted (only PLANNED audits can be deleted)
        if (audit.getStatus() != Status.PLANNED) {
            throw new RuntimeException("Only planned audits can be deleted. Current status: " + audit.getStatus());
        }
        
        // Delete associated checklists first
        if (audit.getChecklists() != null) {
            for (Checklist checklist : audit.getChecklists()) {
                // Delete checklist items first
                if (checklist.getChecklistItems() != null) {
                    for (Checklist_item item : checklist.getChecklistItems()) {
                        checklistItemRepo.delete(item);
                    }
                }
                checklistRepo.delete(checklist);
            }
        }
        
        // Delete the audit
        auditRepo.delete(audit);
    }

    // Helper methods for filtering
    private boolean filterByStatus(Audit audit, Status status) {
        return status == null || audit.getStatus() == status;
    }

    private boolean filterByAuditType(Audit audit, String auditType) {
        return auditType == null || auditType.isEmpty() || 
               (audit.getAuditType() != null && audit.getAuditType().equalsIgnoreCase(auditType));
    }

    private boolean filterByDepartment(Audit audit, String department) {
        return department == null || department.isEmpty() || 
               (audit.getDepartment() != null && audit.getDepartment().equalsIgnoreCase(department));
    }

    private boolean filterByLocation(Audit audit, String location) {
        return location == null || location.isEmpty() || 
               (audit.getLocation() != null && audit.getLocation().equalsIgnoreCase(location));
    }

    private boolean filterByDateRange(Audit audit, LocalDate startFrom, LocalDate startTo, 
                                    LocalDate endFrom, LocalDate endTo) {
        boolean startDateOk = (startFrom == null || !audit.getStartDate().isBefore(startFrom)) &&
(startTo == null || !audit.getStartDate().isAfter(startTo));
boolean endDateOk = (endFrom == null || !audit.getEndDate().isBefore(endFrom)) &&
(endTo == null || !audit.getEndDate().isAfter(endTo));
        return startDateOk && endDateOk;
    }

    private boolean filterBySearchTerm(Audit audit, String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) return true;
        String term = searchTerm.toLowerCase();
        return (audit.getTitle() != null && audit.getTitle().toLowerCase().contains(term)) ||
               (audit.getScope() != null && audit.getScope().toLowerCase().contains(term)) ||
               (audit.getObjectives() != null && audit.getObjectives().toLowerCase().contains(term));
    }

    private boolean filterByOverdue(Audit audit, Boolean isOverdue) {
        if (isOverdue == null) return true;
        LocalDate today = LocalDate.now();
        boolean isActuallyOverdue = audit.getEndDate().isBefore(today) && 
                                   audit.getStatus() != Status.COMPLETED;
        return isOverdue == isActuallyOverdue;
    }

    /**
     * Safely maps entity to DTO by copying collections into new lists/sets.
     */
    private AuditResponse mapToDTO(Audit audit) {
        try {
            System.out.println("DEBUG: Mapping audit " + audit.getAuditId() + " to DTO");

            AuditResponse dto = new AuditResponse();
            dto.setId(audit.getAuditId());
            dto.setTitle(audit.getTitle());
            dto.setScope(audit.getScope());
            dto.setObjectives(audit.getObjectives());
            dto.setStartDate(audit.getStartDate());
            dto.setEndDate(audit.getEndDate());
            dto.setStatus(audit.getStatus());
            dto.setCreatedAt(audit.getCreatedAt());
            dto.setUpdatedAt(audit.getUpdatedAt());
            dto.setAuditType(audit.getAuditType());
            dto.setDepartment(audit.getDepartment());
            dto.setLocation(audit.getLocation());
            dto.setNotes(audit.getNotes());

            // Creator info
            if (audit.getCreatedBy() != null) {
                dto.setCreatedBy(audit.getCreatedBy().getUsername());
                dto.setCreatedByName(audit.getCreatedBy().getFullName());
            }

            // ✅ Defensive copy for auditors
            try {
                Set<AuthUser> auditAuditors = audit.getAuditors() != null
                        ? new HashSet<>(audit.getAuditors())   // copy
                        : Collections.emptySet();

                List<String> auditorNames = new ArrayList<>();
                List<Long> auditorIds = new ArrayList<>();

                for (AuthUser user : auditAuditors) {
                    if (user.getFullName() != null) {
                        auditorNames.add(user.getFullName());
                    }
                    auditorIds.add(user.getUserId().longValue());
                }

                dto.setAuditorNames(auditorNames);
                dto.setAuditorIds(auditorIds);

            } catch (Exception ex) {
                System.err.println("ERROR mapping auditors for audit " + audit.getAuditId() + ": " + ex.getMessage());
                dto.setAuditorNames(Collections.emptyList());
                dto.setAuditorIds(Collections.emptyList());
            }

            // ✅ Checklist mapping with defensive copy
            try {
                System.out.println("DEBUG: Starting checklist mapping for audit " + audit.getAuditId());
                Set<Checklist> auditChecklists = audit.getChecklists() != null
                        ? new HashSet<>(audit.getChecklists())  // copy out of Hibernate proxy
                        : Collections.emptySet();

                System.out.println("DEBUG: Audit has " + auditChecklists.size() + " checklists");

                dto.setTotalChecklists(auditChecklists.size());
                dto.setCompletedChecklists((int) auditChecklists.stream()
                        .filter(c -> c.getStatus() == Status.COMPLETED)
                        .count());

                dto.setProgressPercentage(dto.getTotalChecklists() > 0 ?
                        (double) dto.getCompletedChecklists() / dto.getTotalChecklists() * 100 : 0.0);

                if (!auditChecklists.isEmpty()) {
                    System.out.println("DEBUG: Mapping " + auditChecklists.size() + " checklists to DTOs");

                    List<AuditResponse.ChecklistDTO> checklistDTOs = auditChecklists.stream()
                            .map(this::mapToChecklistDTO)
                            .collect(Collectors.toList());

                    dto.setChecklists(checklistDTOs);
                    System.out.println("DEBUG: Successfully mapped " + checklistDTOs.size() + " checklist DTOs");
                } else {
                    dto.setChecklists(new ArrayList<>());
                    System.out.println("DEBUG: Set empty checklists list");
                }
            } catch (Exception ex) {
                System.err.println("ERROR mapping checklists for audit " + audit.getAuditId() + ": " + ex.getMessage());
                ex.printStackTrace();
                dto.setTotalChecklists(0);
                dto.setCompletedChecklists(0);
                dto.setProgressPercentage(0.0);
                dto.setChecklists(Collections.emptyList());
            }

            System.out.println("DEBUG: Successfully mapped audit " + audit.getAuditId() + " to DTO");
            return dto;

        } catch (Exception e) {
            System.err.println("ERROR in mapToDTO for audit " + audit.getAuditId() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * Maps a Checklist entity to ChecklistDTO for inclusion in audit response
     */
    private AuditResponse.ChecklistDTO mapToChecklistDTO(Checklist checklist) {
        try {
            System.out.println("DEBUG: Mapping checklist " + checklist.getChecklistId() + " to DTO");

            AuditResponse.ChecklistDTO dto = new AuditResponse.ChecklistDTO();
            dto.setChecklistId(checklist.getChecklistId());
            dto.setIsoStandard(checklist.getIsoStandard());
            dto.setStatus(checklist.getStatus());
            dto.setCreatedAt(checklist.getCreatedAt());
            dto.setUpdatedAt(checklist.getUpdatedAt());

            // ✅ Defensive copy before streaming
            if (checklist.getChecklistItems() != null && !checklist.getChecklistItems().isEmpty()) {
                List<Checklist_item> itemsCopy = new ArrayList<>(checklist.getChecklistItems());

                System.out.println("DEBUG: Checklist " + checklist.getChecklistId() + " has " + itemsCopy.size() + " items");

                List<AuditResponse.ChecklistItemDTO> itemDTOs = itemsCopy.stream()
                        .map(this::mapToChecklistItemDTO)
                        .collect(Collectors.toList());

                dto.setClauses(itemDTOs);
                System.out.println("DEBUG: Successfully mapped " + itemDTOs.size() + " items for checklist " + checklist.getChecklistId());
            } else {
                dto.setClauses(new ArrayList<>());
                System.out.println("DEBUG: Checklist " + checklist.getChecklistId() + " has no items, set empty clauses list");
            }

            return dto;

        } catch (Exception e) {
            System.err.println("ERROR mapping checklist " + checklist.getChecklistId() + ": " + e.getMessage());
            e.printStackTrace();

            AuditResponse.ChecklistDTO errorDTO = new AuditResponse.ChecklistDTO();
            errorDTO.setChecklistId(checklist.getChecklistId());
            errorDTO.setIsoStandard(checklist.getIsoStandard());
            errorDTO.setStatus(checklist.getStatus());
            errorDTO.setClauses(new ArrayList<>());
            return errorDTO;
        }
    }


    /**
     * Maps a Checklist_item entity to ChecklistItemDTO
     */
    private AuditResponse.ChecklistItemDTO mapToChecklistItemDTO(Checklist_item item) {
        try {
            System.out.println("DEBUG: Mapping checklist item " + item.getItemId() + " to DTO");
            
            AuditResponse.ChecklistItemDTO dto = new AuditResponse.ChecklistItemDTO();
            dto.setItemId(item.getItemId());
            
            // Safely handle clause data
            if (item.getClause() != null) {
                System.out.println("DEBUG: Item " + item.getItemId() + " has clause " + item.getClause().getClauseId());
                dto.setClauseId(item.getClause().getClauseId());
                dto.setClauseNumber(String.valueOf(item.getClause().getClauseNumber()));
                dto.setClauseName(item.getClause().getClauseName());
            } else {
                System.out.println("WARNING: Item " + item.getItemId() + " has null clause");
                dto.setClauseId(0L);
                dto.setClauseNumber("N/A");
                dto.setClauseName("Clause not found");
            }
            
            dto.setCustomText(item.getCustomText());
            dto.setConformityStatus(item.getConformityStatus() != null ? item.getConformityStatus().name() : null);
            dto.setComments(item.getComments());
            dto.setEvidenceNotes(item.getEvidenceNotes());
            dto.setEvaluatedBy(item.getEvaluatedBy());
            dto.setEvaluatedAt(item.getEvaluatedAt());
            dto.setEvaluated(item.getConformityStatus() != null);
            
            System.out.println("DEBUG: Successfully mapped item " + item.getItemId());
            return dto;
        } catch (Exception e) {
            System.err.println("ERROR mapping checklist item " + item.getItemId() + ": " + e.getMessage());
            e.printStackTrace();
            
            // Return a minimal DTO on error
            AuditResponse.ChecklistItemDTO errorDTO = new AuditResponse.ChecklistItemDTO();
            errorDTO.setItemId(item.getItemId());
            errorDTO.setClauseId(0L);
            errorDTO.setClauseNumber("ERROR");
            errorDTO.setClauseName("Error loading item");
            errorDTO.setEvaluated(false);
            return errorDTO;
        }
    }

    private AuditCalendarResponse mapToCalendarDTO(Audit audit) {
        AuditCalendarResponse dto = new AuditCalendarResponse();
        dto.setId(audit.getAuditId());
        dto.setTitle(audit.getTitle());
        dto.setScope(audit.getScope());
        dto.setStartDate(audit.getStartDate());
        dto.setEndDate(audit.getEndDate());
        dto.setStatus(audit.getStatus());
        dto.setAuditType(audit.getAuditType());
        dto.setDepartment(audit.getDepartment());
        dto.setLocation(audit.getLocation());
        dto.setAllDay(true);
        dto.setUrl("/audits/" + audit.getAuditId());

        // Set auditor names
        if (audit.getAuditors() != null) {
            dto.setAuditorNames(audit.getAuditors().stream()
                    .map(AuthUser::getFullName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }

        // Set creator name
        if (audit.getCreatedBy() != null) {
            dto.setCreatedByName(audit.getCreatedBy().getFullName());
        }

        // Set colors based on status
        switch (audit.getStatus()) {
            case PLANNED:
                dto.setBackgroundColor("#3498db"); // Blue
                dto.setBorderColor("#2980b9");
                break;
            case IN_PROGRESS:
                dto.setBackgroundColor("#f39c12"); // Orange
                dto.setBorderColor("#e67e22");
                break;
            case COMPLETED:
                dto.setBackgroundColor("#27ae60"); // Green
                dto.setBorderColor("#229954");
                break;
            default:
                dto.setBackgroundColor("#95a5a6"); // Gray
                dto.setBorderColor("#7f8c8d");
        }

        return dto;
    }

    private AuditProgress mapToProgressDTO(Audit audit) {
        AuditProgress dto = new AuditProgress();
        dto.setAuditId(audit.getAuditId());
        dto.setTitle(audit.getTitle());
        dto.setStatus(audit.getStatus());
        dto.setStartDate(audit.getStartDate());
        dto.setEndDate(audit.getEndDate());
        dto.setCurrentPhase(audit.getCurrentPhase());
        dto.setLastActivity(audit.getLastActivity());

        // Calculate progress metrics
        Set<Checklist> auditChecklists = audit.getChecklists() != null ? audit.getChecklists() : Collections.emptySet();
        dto.setTotalChecklists(auditChecklists.size());
        dto.setCompletedChecklists((int) auditChecklists.stream()
                .filter(checklist -> checklist.getStatus() == Status.COMPLETED)
                .count());
        dto.setPendingChecklists(dto.getTotalChecklists() - dto.getCompletedChecklists());
        dto.setProgressPercentage(dto.getTotalChecklists() > 0 ? 
                (double) dto.getCompletedChecklists() / dto.getTotalChecklists() * 100 : 0.0);

        // Set status indicators
        LocalDate today = LocalDate.now();
        dto.setIsOverdue(audit.getEndDate().isBefore(today) && audit.getStatus() != Status.COMPLETED);
        dto.setIsOnTrack(!dto.getIsOverdue() && dto.getProgressPercentage() >= 50.0);

        // Set last activity by
        if (audit.getCreatedBy() != null) {
            dto.setLastActivityBy(audit.getCreatedBy().getFullName());
        }

        return dto;
    }
}
