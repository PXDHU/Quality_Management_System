package com.example.QualityManagementSystem.service;

import com.example.QualityManagementSystem.dto.*;
import com.example.QualityManagementSystem.model.*;
import com.example.QualityManagementSystem.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private final AuditRepository auditRepo;
    private final UserRepository userRepo;

    public AuditService(AuditRepository auditRepo, UserRepository userRepo) {
        this.auditRepo = auditRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public AuditResponse createAudit(AuditRequest request, Long creatorId) {
        Audit audit = new Audit();
        audit.setTitle(request.title);
        audit.setScope(request.scope);
        audit.setObjectives(request.objectives);
        audit.setStart_date(request.startDate);
        audit.setEnd_date(request.endDate);
        audit.setStatus(Status.PLANNED);
        audit.setCreatedBy(userRepo.findById(creatorId).orElseThrow());
        audit.setAuditors(new HashSet<>(userRepo.findAllById(request.auditorIds)));

        audit = auditRepo.save(audit);

        return mapToDTO(audit);
    }

    public List<AuditResponse> getAllAudits() {
        return auditRepo.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public AuditResponse getAuditById(Long id) {
        return mapToDTO(auditRepo.findById(id).orElseThrow());
    }

    @Transactional
    public AuditResponse updateAuditStatus(Long id, UpdateAuditStatus statusDTO) {
        Audit audit = auditRepo.findById(id).orElseThrow();
        audit.setStatus(statusDTO.status);
        return mapToDTO(auditRepo.save(audit));
    }

    private AuditResponse mapToDTO(Audit audit) {
        AuditResponse dto = new AuditResponse();
        dto.id = audit.getAuditId();
        dto.title = audit.getTitle();
        dto.scope = audit.getScope();
        dto.objectives = audit.getObjectives();
        dto.startDate = audit.getStart_date();
        dto.endDate = audit.getEnd_date();
        dto.status = audit.getStatus();
        dto.createdAt = audit.getCreated_at();
        dto.updatedAt = audit.getUpdated_at();
        dto.auditorNames = audit.getAuditors().stream().map(AuthUser::getFullName).collect(Collectors.toSet());
        return dto;
    }
}
