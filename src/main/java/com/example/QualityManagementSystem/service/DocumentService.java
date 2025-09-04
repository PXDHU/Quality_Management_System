package com.example.QualityManagementSystem.service;

import com.example.QualityManagementSystem.dto.DocumentRequest;
import com.example.QualityManagementSystem.dto.DocumentResponse;
import com.example.QualityManagementSystem.dto.DocumentSearchRequest;
import com.example.QualityManagementSystem.model.Audit;
import com.example.QualityManagementSystem.model.AuthUser;
import com.example.QualityManagementSystem.model.Document;
import com.example.QualityManagementSystem.model.NonConformity;
import com.example.QualityManagementSystem.repository.AuditRepository;
import com.example.QualityManagementSystem.repository.DocumentRepository;
import com.example.QualityManagementSystem.repository.NonConformityRepository;
import com.example.QualityManagementSystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepo;

    @Autowired
    private AuditRepository auditRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private NonConformityRepository ncRepo;

    public DocumentService(DocumentRepository documentRepo, AuditRepository auditRepo,
                           UserRepository userRepo, NonConformityRepository ncRepo) {
        this.documentRepo = documentRepo;
        this.auditRepo = auditRepo;
        this.userRepo = userRepo;
        this.ncRepo = ncRepo;
    }

    @Transactional
    public DocumentResponse uploadDocument(DocumentRequest request, Long userId) throws IOException {
        // Fetch the user who is uploading
        AuthUser user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create new Document
        Document document = new Document();
        document.setFileName(request.file.getOriginalFilename());
        document.setFileType(request.file.getContentType());
        document.setFileSize(request.file.getSize());
        document.setData(request.file.getBytes());
        document.setDescription(request.description);
        document.setUploadedBy(user);
        document.setTags(request.tags);
        document.setClauseReference(request.clauseReference);
        document.setDepartment(request.department);
        document.setIsEvidence(request.isEvidence);

        // Initialize collections if null
        if (document.getAudits() == null) document.setAudits(new ArrayList<>());
        if (document.getNonConformities() == null) document.setNonConformities(new ArrayList<>());

        // Save document first so it has an ID
        document = documentRepo.save(document);

        // Link audits safely
        if (request.auditIds != null && !request.auditIds.isEmpty()) {
            List<Audit> audits = auditRepo.findAllById(request.auditIds);
            for (Audit audit : audits) {
                if (audit.getDocuments() == null) audit.setDocuments(new ArrayList<>());
                audit.getDocuments().add(document); // Add to owning side
                document.getAudits().add(audit);    // maintain bidirectional
            }
            auditRepo.saveAll(audits);
        }

        // Link NCs safely
        if (request.ncIds != null && !request.ncIds.isEmpty()) {
            List<NonConformity> ncs = ncRepo.findAllById(request.ncIds);
            document.getNonConformities().addAll(ncs);
        }

        // Return DTO
        return mapToDTO(document);
    }


    // Get all documents
    @Transactional(readOnly = true)
    public List<DocumentResponse> getAllDocuments() {
        return documentRepo.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // --- GET DOCUMENT BY ID (with LOB and collections initialized) ---
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(Long id) {
        Document document = documentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        document.getAudits().size(); // initialize audits
        document.getNonConformities().size(); // initialize NCs
        document.getData(); // initialize LOB

        updateAccessTracking(document);
        return mapToDTO(document);
    }

    // --- GET DOCUMENT DATA ---
    @Transactional(readOnly = true)
    public byte[] getDocumentData(Long documentId) {
        Document document = documentRepo.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        document.getData(); // force LOB load
        updateAccessTracking(document);

        return document.getData();
    }

    // --- UPDATE ACCESS TRACKING ---
    @Transactional
    private void updateAccessTracking(Document document) {
        document.setLastAccessed(LocalDateTime.now());
        document.setAccessCount(document.getAccessCount() + 1);
        documentRepo.save(document);
    }

    // Update metadata
    @Transactional
    public DocumentResponse updateDocumentMetadata(Long documentId, DocumentRequest request) {
        Document document = documentRepo.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (request.description != null) document.setDescription(request.description);
        if (request.tags != null) document.setTags(request.tags);
        if (request.clauseReference != null) document.setClauseReference(request.clauseReference);
        if (request.department != null) document.setDepartment(request.department);
        if (request.isEvidence != null) document.setIsEvidence(request.isEvidence);

        document = documentRepo.save(document);
        return mapToDTO(document);
    }

    // Delete document
    @Transactional
    public void deleteDocument(Long documentId) {
        Document document = documentRepo.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        documentRepo.delete(document);
    }

    // Link document to audits
    @Transactional
    public DocumentResponse linkDocumentToAudits(Long documentId, List<Long> auditIds) {
        Document document = documentRepo.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        List<Audit> audits = auditRepo.findAllById(auditIds);
        document.getAudits().addAll(new HashSet<>(audits));
        document = documentRepo.save(document);
        return mapToDTO(document);
    }

    // --- LINK DOCUMENT TO NCs (evidence) ---
    @Transactional
    public DocumentResponse linkDocumentToNcs(Long documentId, List<Long> ncIds) {
        Document document = documentRepo.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        List<NonConformity> ncs = ncRepo.findAllById(ncIds);
        document.getNonConformities().addAll(new HashSet<>(ncs));
        document.setIsEvidence(true);

        document = documentRepo.save(document);
        return mapToDTO(document);
    }

    // Pagination and search
    public Page<DocumentResponse> searchDocuments(DocumentSearchRequest request) {
        Sort sort = Sort.by(request.sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC,
                request.sortBy);
        Pageable pageable = PageRequest.of(request.page, request.size, sort);

        Page<Document> documents = documentRepo.searchDocuments(
                request.fileName,
                request.fileType,
                request.department,
                request.clauseReference,
                request.isEvidence,
                request.uploadedBy,
                request.description,
                request.tags,
                pageable
        );

        return documents.map(this::mapToDTO);
    }

    // By audit with pagination
    public Page<DocumentResponse> getDocumentsByAuditWithPagination(Long auditId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("uploadedAt").descending());
        Page<Document> documents = documentRepo.findByAuditIdWithPagination(auditId, pageable);
        return documents.map(this::mapToDTO);
    }

    // By NC with pagination
    public Page<DocumentResponse> getDocumentsByNcWithPagination(Long ncId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("uploadedAt").descending());
        Page<Document> documents = documentRepo.findByNcIdWithPagination(ncId, pageable);
        return documents.map(this::mapToDTO);
    }

    // Most accessed documents
    public Page<DocumentResponse> getMostAccessedDocuments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Document> documents = documentRepo.findMostAccessedDocuments(pageable);
        return documents.map(this::mapToDTO);
    }

    // Recently accessed documents
    public Page<DocumentResponse> getRecentlyAccessedDocuments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Document> documents = documentRepo.findRecentlyAccessedDocuments(pageable);
        return documents.map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByAudit(Long auditId) {
        List<Document> documents = documentRepo.findByAuditIdWithEagerAudits(auditId);

        return documents.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get documents by user
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByUser(Long userId) {
        return documentRepo.findByUploadedBy(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get documents by NC
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByNc(Long ncId) {
        return documentRepo.findByNcId(ncId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get documents by clause
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByClause(String clauseReference) {
        return documentRepo.findByClauseReference(clauseReference)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get documents by department
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByDepartment(String department) {
        return documentRepo.findByDepartment(department)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Get evidence documents
    @Transactional(readOnly = true)
    public List<DocumentResponse> getEvidenceDocuments() {
        return documentRepo.findByIsEvidence(true)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Search by filename, description, tags
    @Transactional(readOnly = true)
    public List<DocumentResponse> searchDocumentsByFileName(String fileName) {
        return documentRepo.findByFileNameContaining(fileName)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> searchDocumentsByDescription(String description) {
        return documentRepo.findByDescriptionContaining(description)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> searchDocumentsByTags(String tag) {
        return documentRepo.findByTagsContaining(tag)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Map entity to DTO
    // --- MAP TO DTO ---
    private DocumentResponse mapToDTO(Document document) {
        DocumentResponse dto = new DocumentResponse();
        dto.setDocumentId(document.getDocumentId());
        dto.setFileName(document.getFileName());
        dto.setFileType(document.getFileType());
        dto.setFileSize(document.getFileSize());
        dto.setDescription(document.getDescription());
        dto.setUploadedBy(document.getUploadedBy().getFullName());
        dto.setUploadedAt(document.getUploadedAt());
        dto.setTags(document.getTags());
        dto.setClauseReference(document.getClauseReference());
        dto.setDepartment(document.getDepartment());
        dto.setIsEvidence(document.getIsEvidence());
        dto.setLastAccessed(document.getLastAccessed());
        dto.setAccessCount(document.getAccessCount());

        // Convert audits safely
        List<Audit> audits = Optional.ofNullable(document.getAudits()).orElse(new ArrayList<>());
        dto.setAuditIds(audits.stream().map(Audit::getAuditId).collect(Collectors.toList()));
        dto.setAuditTitles(audits.stream().map(Audit::getTitle).collect(Collectors.toList()));

        // Convert NCs safely
        List<NonConformity> ncs = Optional.ofNullable(document.getNonConformities()).orElse(new ArrayList<>());
        dto.setNcIds(ncs.stream().map(NonConformity::getNonConformityId).collect(Collectors.toList()));
        dto.setNcTitles(ncs.stream().map(NonConformity::getTitle).collect(Collectors.toList()));

        return dto;
    }

}
