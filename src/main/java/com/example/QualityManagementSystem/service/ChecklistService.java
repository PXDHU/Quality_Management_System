package com.example.QualityManagementSystem.service;

import com.example.QualityManagementSystem.dto.*;
import com.example.QualityManagementSystem.model.*;
import com.example.QualityManagementSystem.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChecklistService {

    private final ChecklistRepository checklistRepo;
    private final ChecklistItemRepository itemRepo;
    private final ClauseLibraryRepository clauseRepo;
    private final InstanceRepository instanceRepo;
    private final AuditRepository auditRepo;

    public ChecklistService(
            ChecklistRepository checklistRepo,
            ChecklistItemRepository itemRepo,
            ClauseLibraryRepository clauseRepo,
            InstanceRepository instanceRepo,
            AuditRepository auditRepo
    ) {
        this.checklistRepo = checklistRepo;
        this.itemRepo = itemRepo;
        this.clauseRepo = clauseRepo;
        this.instanceRepo = instanceRepo;
        this.auditRepo = auditRepo;
    }

    public ChecklistResponse createChecklist(ChecklistRequest request) {
        Checklist checklist = new Checklist();
        checklist.setIsoStandard(request.getIsoStandard());

        Audit audit = auditRepo.findById(request.getAuditId())
                .orElseThrow(() -> new RuntimeException("Audit not found"));
        checklist.setAudit(audit);

        checklist.setStatus(request.getStatus());
        checklist.setCreated_at(LocalDateTime.now());
        checklist.setUpdated_at(LocalDateTime.now());

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
        checklist.setUpdated_at(LocalDateTime.now());

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

    private ChecklistResponse mapToResponse(Checklist checklist) {
        ChecklistResponse dto = new ChecklistResponse();
        dto.setChecklistId(checklist.getChecklist_id());
        dto.setIsoStandard(checklist.getIsoStandard());
        dto.setAuditId(checklist.getAudit().getAuditId());
        dto.setStatus(checklist.getStatus());
        dto.setCreatedAt(checklist.getCreated_at());
        dto.setUpdatedAt(checklist.getUpdated_at());

        List<ChecklistResponse.ChecklistItemDTO> items = checklist.getChecklistItems().stream().map(item -> {
            ChecklistResponse.ChecklistItemDTO d = new ChecklistResponse.ChecklistItemDTO();
            d.setItemId(item.getItem_id());
            d.setClauseId(item.getClause().getClause_id());
            d.setClauseName(item.getClause().getClause_name());
            d.setClauseNumber(String.valueOf(item.getClause().getClause_number()));
            d.setCustomText(item.getCustom_text());
            d.setConformityStatus(item.getConformity_status().name());
            return d;
        }).collect(Collectors.toList());

        dto.setChecklistItems(items);
        return dto;
    }

    @Transactional
    public Long createChecklistTemplate(ChecklistTemplate dto) {
        Checklist checklist = new Checklist();
        checklist.setIsoStandard(dto.getIsoStandard());
        checklist.setStatus(Status.PLANNED);
        checklist.setChecklistItems(new HashSet<>());

        checklist = checklistRepo.save(checklist);

        for (ChecklistTemplate.ChecklistClauseDTO clauseDTO : dto.getClauses()) {
            Clause_library clause = clauseRepo.findById(clauseDTO.getClauseId()).orElseThrow();

            Checklist_item item = new Checklist_item();
            item.setChecklist(checklist);
            item.setClause(clause);
            item.setCustom_text(clauseDTO.getCustomText());
            item.setConformity_status(ConformityStatus.CONFORMITY);

            itemRepo.save(item);
        }

        return checklist.getChecklist_id();
    }

    @Transactional
    public void assignChecklistToAudit(Long auditId, AssignChecklistToAudit request) {
        Checklist template = checklistRepo.findById(request.getTemplateId()).orElseThrow();
        Audit audit = auditRepo.findById(auditId).orElseThrow();

        Checklist assigned = new Checklist();
        assigned.setIsoStandard(template.getIsoStandard());
        assigned.setAudit(audit);
        assigned.setStatus(Status.PLANNED);

        checklistRepo.save(assigned);

        List<Checklist_item> templateItems = itemRepo.findByChecklist(template);
        for (Checklist_item item : templateItems) {
            Checklist_item newItem = new Checklist_item();
            newItem.setChecklist(assigned);
            newItem.setClause(item.getClause());
            newItem.setCustom_text(item.getCustom_text());
            newItem.setConformity_status(item.getConformity_status());

            itemRepo.save(newItem);
        }
    }

    public ChecklistResponse getChecklistByAudit(Long auditId) {
        List<Checklist> list = checklistRepo.findByAudit_AuditId(auditId);
        if (list.isEmpty()) throw new RuntimeException("No checklist found for audit");

        return mapToResponse(list.get(0));
    }

    @Transactional
    public void evaluateClause(Long instanceId, ClauseEvaluation evaluation) {
        Instance instance = instanceRepo.findById(instanceId).orElseThrow();
        instance.setConformity_status(evaluation.getConformityStatus());
        instance.setSeverity(evaluation.getSeverity());
        instance.setComments(evaluation.getComments());
        instance.setUpdated_at(LocalDateTime.now());
        instanceRepo.save(instance);
    }

    public AuditProgress getAuditProgress(Long auditId) {
        int total = instanceRepo.countByAuditId(auditId);
        int completed = instanceRepo.countEvaluatedByAuditId(auditId);

        AuditProgress dto = new AuditProgress();
        dto.setTotalClauses(total);
        dto.setEvaluatedClauses(completed);
        dto.setCompletionPercentage(total > 0 ? (completed * 100.0 / total) : 0);
        return dto;
    }
}
