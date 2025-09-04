package com.example.QualityManagementSystem.service;

import com.example.QualityManagementSystem.dto.*;
import com.example.QualityManagementSystem.model.*;
import com.example.QualityManagementSystem.repository.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NonConformityService {

    private final NonConformityRepository ncRepo;
    private final CorrectiveActionRepository actionRepo;
    private final RCAStepRepository rcaRepo;
    private final AuditRepository auditRepo;
    private final InstanceRepository instanceRepo;
    private final ClauseLibraryRepository clauseRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    public NonConformityService(NonConformityRepository ncRepo,
                                CorrectiveActionRepository actionRepo,
                                RCAStepRepository rcaRepo,
                                AuditRepository auditRepo,
                                InstanceRepository instanceRepo,
                                ClauseLibraryRepository clauseRepo,
                                UserRepository userRepo,
                                NotificationService notificationService) {
        this.ncRepo = ncRepo;
        this.actionRepo = actionRepo;
        this.rcaRepo = rcaRepo;
        this.auditRepo = auditRepo;
        this.instanceRepo = instanceRepo;
        this.clauseRepo = clauseRepo;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
    }

    @Transactional
    public NCResponse createNC(CreateNCRequest req) {
        // Validate required fields
        if (req.getAuditId() == null) {
            throw new RuntimeException("Audit ID is required");
        }
        if (req.getTitle() == null || req.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Title is required");
        }
        if (req.getDescription() == null || req.getDescription().trim().isEmpty()) {
            throw new RuntimeException("Description is required");
        }
        if (req.getSeverity() == null) {
            throw new RuntimeException("Severity is required");
        }
        if (req.getAssignedToId() == null) {
            throw new RuntimeException("Assignee is required");
        }

        Audit audit = auditRepo.findById(req.getAuditId()).orElseThrow(() -> new RuntimeException("Audit not found"));
        Instance instance = req.getInstanceId() == null ? null :
                instanceRepo.findById(req.getInstanceId()).orElseThrow(() -> new RuntimeException("Instance not found"));
        Clause_library clause = req.getClauseId() == null ? null :
                clauseRepo.findById(req.getClauseId()).orElseThrow(() -> new RuntimeException("Clause not found"));
        AuthUser createdBy = req.getCreatedById() == null ? null :
                userRepo.findById(req.getCreatedById()).orElseThrow(() -> new RuntimeException("Creator not found"));
        AuthUser assignedTo = userRepo.findById(req.getAssignedToId()).orElseThrow(() -> new RuntimeException("Assignee not found"));

        NonConformity nc = new NonConformity();
        nc.setAudit(audit);
        nc.setInstance(instance);
        nc.setClause(clause);
        nc.setTitle(req.getTitle().trim());
        nc.setDescription(req.getDescription().trim());
        nc.setSeverity(req.getSeverity());
        nc.setStatus(Status.PENDING);
        nc.setCreatedBy(createdBy);
        nc.setAssignedTo(assignedTo);
        nc.setCreatedAt(LocalDateTime.now());
        nc.setUpdatedAt(LocalDateTime.now());

        return toResponse(ncRepo.save(nc));
    }

    @Transactional(readOnly = true)
    public NCResponse getOne(Long ncId) {
        return toResponse(ncRepo.findByIdWithCollections(ncId));
    }

    @Transactional(readOnly = true)
    public List<NCResponse> listByAudit(Long auditId) {
        return ncRepo.findByAuditIdWithCollections(auditId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NCResponse> listAssigned(Long assigneeId, Status status) {
        AuthUser user = userRepo.findById(assigneeId).orElseThrow(() -> new RuntimeException("User not found"));
        List<NonConformity> list = (status == null) ? 
            ncRepo.findByAssignedToWithCollections(user) : 
            ncRepo.findByAssignedToAndStatusWithCollections(user, status);
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public NCResponse addCorrectiveAction(Long ncId, CorrectiveActionRequest req) {
        // Validate required fields
        if (req.getDescription() == null || req.getDescription().trim().isEmpty()) {
            throw new RuntimeException("Action description is required");
        }
        if (req.getResponsibleId() == null) {
            throw new RuntimeException("Responsible person is required");
        }

        NonConformity nc = ncRepo.findById(ncId).orElseThrow(() -> new RuntimeException("NC not found"));
        AuthUser responsible = userRepo.findById(req.getResponsibleId()).orElseThrow(() -> new RuntimeException("Responsible person not found"));

        CorrectiveAction action = new CorrectiveAction();
        action.setNc(nc);
        action.setDescription(req.getDescription().trim());
        action.setResponsible(responsible);
        action.setDueDate(req.getDueDate());
        action.setStatus(Status.PENDING);
        action.setCreatedAt(LocalDateTime.now());
        action.setUpdatedAt(LocalDateTime.now());
        actionRepo.save(action);

        // when a corrective action is added, mark NC IN_PROGRESS
        if (nc.getStatus() == Status.PENDING) {
            nc.setStatus(Status.IN_PROGRESS);
            nc.setUpdatedAt(LocalDateTime.now());
            ncRepo.save(nc);
        }

        // Notify responsible of assignment
        try {
            notificationService.notifyTaskAssigned(responsible, action, nc);
        } catch (Exception ignored) {}

        return getOne(ncId);
    }

    @Transactional
    public NCResponse updateActionStatus(Long actionId, UpdateActionStatusRequest req) {
        CorrectiveAction action = actionRepo.findById(actionId).orElseThrow(() -> new RuntimeException("Action not found"));
        action.setStatus(req.getStatus());
        action.setUpdatedAt(LocalDateTime.now());
        actionRepo.save(action);
        return getOne(action.getNc().getNonConformityId());
    }

    @Transactional
    public NCResponse approveOrRejectAction(Long actionId, Long reviewerUserId, boolean approved, String comments) {
        CorrectiveAction action = actionRepo.findById(actionId).orElseThrow(() -> new RuntimeException("Action not found"));
        AuthUser reviewer = userRepo.findById(reviewerUserId).orElseThrow(() -> new RuntimeException("Reviewer not found"));
        action.setApprovalReviewer(reviewer);
        action.setApprovalComments(comments);
        action.setApprovalAt(LocalDateTime.now());
        action.setStatus(approved ? Status.COMPLETED : Status.IN_PROGRESS);
        action.setUpdatedAt(LocalDateTime.now());
        actionRepo.save(action);
        return getOne(action.getNc().getNonConformityId());
    }

    @Transactional
    public NCResponse submitRCA(Long ncId, RCARequest rca) {
        // Validate RCA request
        if (rca.getSteps() == null || rca.getSteps().isEmpty()) {
            throw new RuntimeException("RCA steps are required");
        }
        if (rca.getSteps().size() < 3 || rca.getSteps().size() > 5) {
            throw new RuntimeException("RCA must have 3-5 steps");
        }

        NonConformity nc = ncRepo.findById(ncId).orElseThrow(() -> new RuntimeException("NC not found"));

        // Validate that steps are sequential and have content
        for (int i = 0; i < rca.getSteps().size(); i++) {
            RCARequest.Step step = rca.getSteps().get(i);
            if (step.getStepNumber() != i + 1) {
                throw new RuntimeException("RCA steps must be sequential starting from 1");
            }
            if (step.getWhyText() == null || step.getWhyText().trim().isEmpty()) {
                throw new RuntimeException("Why text is required for step " + (i + 1));
            }
        }

        // clear previous steps
        rcaRepo.deleteByNc(nc);
        nc.getRcaSteps().clear();

        for (RCARequest.Step s : rca.getSteps()) {
            RCAStep step = new RCAStep();
            step.setNc(nc);
            step.setStepNumber(s.getStepNumber());
            step.setWhyText(s.getWhyText().trim());
            rcaRepo.save(step);
        }
        nc.setUpdatedAt(LocalDateTime.now());
        ncRepo.save(nc);
        return getOne(ncId);
    }

    @Transactional
    public NCResponse closeNC(Long ncId, CloseNCRequest req) {
        NonConformity nc = ncRepo.findById(ncId).orElseThrow(() -> new RuntimeException("NC not found"));

        // Check if NC is already closed
        if (nc.getStatus() == Status.COMPLETED || nc.getStatus() == Status.CLOSED) {
            throw new RuntimeException("NC is already closed");
        }

        // require at least 3 RCA steps if severity == HIGH (as per requirement 3–5 steps for major)
        if (nc.getSeverity() == Severity.HIGH && (nc.getRcaSteps() == null || nc.getRcaSteps().size() < 3)) {
            throw new RuntimeException("RCA with at least 3 steps is required for HIGH severity NCs before closing.");
        }

        // Check if all corrective actions are completed
        boolean allActionsCompleted = nc.getActions().stream()
                .allMatch(action -> action.getStatus() == Status.COMPLETED);
        if (!allActionsCompleted) {
            throw new RuntimeException("All corrective actions must be completed before closing the NC");
        }

        if (req.getFinalEvidenceIds() != null && !req.getFinalEvidenceIds().isEmpty()) {
            nc.getEvidenceIds().addAll(req.getFinalEvidenceIds());
        }
        
        nc.setStatus(Status.COMPLETED);
        nc.setUpdatedAt(LocalDateTime.now());
        return toResponse(ncRepo.save(nc));
    }

    private NCResponse toResponse(NonConformity nc) {
        NCResponse dto = new NCResponse();
        dto.setNonConformityId(nc.getNonConformityId());
        dto.setAuditId(nc.getAudit() != null ? nc.getAudit().getAuditId() : null);
        dto.setInstanceId(nc.getInstance() != null ? nc.getInstance().getInstanceId() : null);
        dto.setClauseId(nc.getClause() != null ? nc.getClause().getClauseId() : null);
        dto.setTitle(nc.getTitle());
        dto.setDescription(nc.getDescription());
        dto.setSeverity(nc.getSeverity());
        dto.setStatus(nc.getStatus());
        dto.setCreatedById(Long.valueOf(nc.getCreatedBy() != null ? nc.getCreatedBy().getUserId() : null));
        dto.setAssignedToId(Long.valueOf(nc.getAssignedTo() != null ? nc.getAssignedTo().getUserId() : null));
        dto.setEvidenceIds(nc.getEvidenceIds());
        dto.setCreatedAt(nc.getCreatedAt());
        dto.setUpdatedAt(nc.getUpdatedAt());

        // Safely handle actions collection to avoid lazy loading issues
        if (nc.getActions() != null) {
            dto.setActions(nc.getActions().stream().map(a -> {
                NCResponse.ActionDTO ad = new NCResponse.ActionDTO();
                ad.setActionId(a.getActionId());
                ad.setDescription(a.getDescription());
                ad.setResponsibleId(Long.valueOf(a.getResponsible() != null ? a.getResponsible().getUserId() : null));
                ad.setDueDate(a.getDueDate());
                ad.setStatus(a.getStatus().name());
                return ad;
            }).collect(Collectors.toList()));
        } else {
            dto.setActions(new ArrayList<>());
        }

        // Safely handle RCA steps collection to avoid lazy loading issues
        if (nc.getRcaSteps() != null) {
            dto.setRcaSteps(nc.getRcaSteps().stream().map(s -> {
                NCResponse.RCAStepDTO sd = new NCResponse.RCAStepDTO();
                sd.setRcaStepId(s.getRcaStepId());
                sd.setStepNumber(s.getStepNumber());
                sd.setWhyText(s.getWhyText());
                return sd;
            }).collect(Collectors.toList()));
        } else {
            dto.setRcaSteps(new ArrayList<>());
        }

        return dto;
    }

    @Transactional(readOnly = true)
    public List<NCResponse> getAllNCs(Status status, Severity severity, Long auditId) {
        List<NonConformity> ncs;
        
        if (status != null && severity != null && auditId != null) {
            ncs = ncRepo.findByStatusAndSeverityAndAuditIdOnlyWithCollections(status, severity, auditId);
        } else if (status != null && severity != null) {
            ncs = ncRepo.findByStatusAndSeverityOnlyWithCollections(status, severity);
        } else if (status != null && auditId != null) {
            ncs = ncRepo.findByStatusAndAuditIdOnlyWithCollections(status, auditId);
        } else if (severity != null && auditId != null) {
            ncs = ncRepo.findBySeverityAndAuditIdOnlyWithCollections(severity, auditId);
        } else if (status != null) {
            ncs = ncRepo.findByStatusWithCollections(status);
        } else if (severity != null) {
            ncs = ncRepo.findBySeverityWithCollections(severity);
        } else if (auditId != null) {
            ncs = ncRepo.findByAuditIdOnlyWithCollections(auditId);
        } else {
            // Use the method with collections eagerly loaded to avoid lazy loading issues
            ncs = ncRepo.findAllWithCollections();
        }
        
        return ncs.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NCResponse> getNCsBySeverity(Severity severity) {
        return ncRepo.findBySeverityWithCollections(severity).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NCResponse> getOverdueNCs() {
        LocalDate today = LocalDate.now();
        // Use the method with collections eagerly loaded to avoid lazy loading issues
        List<NonConformity> allNCs = ncRepo.findAllWithCollections();
        return allNCs.stream()
                .filter(nc -> {
                    // Collections are already loaded, no lazy loading issues
                    if (nc.getActions() != null) {
                        return nc.getActions().stream()
                                .anyMatch(action -> action.getDueDate() != null && 
                                        action.getDueDate().isBefore(today) && 
                                        action.getStatus() != Status.COMPLETED);
                    }
                    return false;
                })
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public NCResponse updateNCStatus(Long ncId, Status status) {
        NonConformity nc = ncRepo.findById(ncId).orElseThrow(() -> new RuntimeException("NC not found"));
        
        // Validate status transition
        if (nc.getStatus() == Status.COMPLETED && status != Status.COMPLETED) {
            throw new RuntimeException("Cannot change status of completed NC");
        }
        
        nc.setStatus(status);
        nc.setUpdatedAt(LocalDateTime.now());
        return toResponse(ncRepo.save(nc));
    }
}
