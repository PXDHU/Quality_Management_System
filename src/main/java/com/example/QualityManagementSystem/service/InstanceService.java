package com.example.QualityManagementSystem.service;

import com.example.QualityManagementSystem.dto.InstanceCreateRequest;
import com.example.QualityManagementSystem.dto.InstanceResponse;
import com.example.QualityManagementSystem.model.*;
import com.example.QualityManagementSystem.repository.AuditRepository;
import com.example.QualityManagementSystem.repository.ChecklistItemRepository;
import com.example.QualityManagementSystem.repository.InstanceRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class InstanceService {

    private final InstanceRepository instanceRepo;
    private final AuditRepository auditRepo;
    private final ChecklistItemRepository itemRepo;

    public InstanceService(InstanceRepository instanceRepo,
                           AuditRepository auditRepo,
                           ChecklistItemRepository itemRepo) {
        this.instanceRepo = instanceRepo;
        this.auditRepo = auditRepo;
        this.itemRepo = itemRepo;
    }

    @Transactional
    public InstanceResponse createInstance(InstanceCreateRequest req) {
        Audit audit = auditRepo.findById(req.getAuditId())
                .orElseThrow(() -> new RuntimeException("Audit not found"));

        Checklist_item item = itemRepo.findById(req.getChecklistItemId())
                .orElseThrow(() -> new RuntimeException("Checklist item not found"));

        Instance inst = new Instance();
        inst.setAudit(audit);
        inst.setChecklistItem(item);

        // Optional fields / sensible defaults
        inst.setConformityStatus(
                req.getConformityStatus() != null ? req.getConformityStatus() : ConformityStatus.COMPLIANT
        );
        inst.setSeverity(req.getSeverity()); // may be null
        inst.setComments(req.getComments()); // may be null

        LocalDateTime now = LocalDateTime.now();
        inst.setCreatedAt(now);
        inst.setUpdatedAt(now);

        inst = instanceRepo.save(inst);
        return toResponse(inst);
    }

    private InstanceResponse toResponse(Instance inst) {
        InstanceResponse dto = new InstanceResponse();
        dto.setInstanceId(inst.getInstanceId());
        dto.setAuditId(inst.getAudit() != null ? inst.getAudit().getAuditId() : null);
        dto.setChecklistItemId(inst.getChecklistItem() != null ? inst.getChecklistItem().getItemId() : null);
        dto.setClauseId(
                (inst.getChecklistItem() != null && inst.getChecklistItem().getClause() != null)
                        ? inst.getChecklistItem().getClause().getClauseId() : null
        );
        dto.setConformityStatus(inst.getConformityStatus());
        dto.setSeverity(inst.getSeverity());
        dto.setComments(inst.getComments());
        dto.setCreatedAt(inst.getCreatedAt());
        dto.setUpdatedAt(inst.getUpdatedAt());
        return dto;
    }
}
