package com.example.QualityManagementSystem.service;

import com.example.QualityManagementSystem.dto.DocumentRequest;
import com.example.QualityManagementSystem.dto.DocumentResponse;
import com.example.QualityManagementSystem.model.Audit;
import com.example.QualityManagementSystem.model.AuthUser;
import com.example.QualityManagementSystem.model.Document;
import com.example.QualityManagementSystem.repository.AuditRepository;
import com.example.QualityManagementSystem.repository.DocumentRepository;
import com.example.QualityManagementSystem.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentRepository docRepo;
    private final AuditRepository auditRepo;
    private final UserRepository userRepo;

    public DocumentService(DocumentRepository docRepo, AuditRepository auditRepo, UserRepository userRepo) {
        this.docRepo = docRepo;
        this.auditRepo = auditRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public DocumentResponse uploadDocument(DocumentRequest request, MultipartFile file, Long uploaderId) throws IOException {
        AuthUser uploader = userRepo.findById(uploaderId).orElseThrow();

        Document doc = new Document();
        doc.setFile_name(file.getOriginalFilename());
        doc.setFile_type(file.getContentType());
        doc.setDescription(request.getDescription());
        doc.setData(file.getBytes());
        doc.setUploadedBy(uploader);

        if (request.getAuditIds() != null && !request.getAuditIds().isEmpty()) {
            doc.setAudits(new HashSet<>(auditRepo.findAllById(request.getAuditIds())));
        }

        Document saved = docRepo.save(doc);
        return mapToDTO(saved);
    }

    public List<DocumentResponse> getAllDocuments() {
        return docRepo.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public DocumentResponse getDocumentById(Long id) {
        return mapToDTO(docRepo.findById(id).orElseThrow());
    }

    private DocumentResponse mapToDTO(Document doc) {
        DocumentResponse dto = new DocumentResponse();
        dto.setId(doc.getDocument_id());
        dto.setFileName(doc.getFile_name());
        dto.setFileType(doc.getFile_type());
        dto.setDescription(doc.getDescription());
        dto.setUploadedAt(doc.getUploaded_at());
        dto.setUploadedBy(doc.getUploadedBy().getFullName());
        dto.setAuditTitles(doc.getAudits().stream().map(Audit::getTitle).collect(Collectors.toSet()));
        return dto;
    }
}
