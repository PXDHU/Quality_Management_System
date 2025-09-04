package com.example.QualityManagementSystem.controller;

import com.example.QualityManagementSystem.dto.DocumentRequest;
import com.example.QualityManagementSystem.dto.DocumentResponse;
import com.example.QualityManagementSystem.dto.DocumentSearchRequest;
import com.example.QualityManagementSystem.model.Audit;
import com.example.QualityManagementSystem.model.NonConformity;
import com.example.QualityManagementSystem.repository.AuditRepository;
import com.example.QualityManagementSystem.repository.NonConformityRepository;
import com.example.QualityManagementSystem.service.DocumentService;
import com.example.QualityManagementSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final UserService userService;

    @Autowired
    NonConformityRepository nonConformityRepository;

    @Autowired
    AuditRepository auditRepository;

    public DocumentController(DocumentService documentService, UserService userService) {
        this.documentService = documentService;
        this.userService = userService;
    }

    // Simple health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Document Controller is working!");
    }

    // Test endpoint without authentication for debugging
    @PostMapping("/test-upload")
    public ResponseEntity<String> testUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description
    ) {
        try {
            System.out.println("=== Test Upload Debug ===");
            System.out.println("File name: " + (file != null ? file.getOriginalFilename() : "NULL"));
            System.out.println("File size: " + (file != null ? file.getSize() : "NULL"));
            System.out.println("Description: " + description);
            
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is null or empty");
            }
            
            return ResponseEntity.ok("Test upload successful! File: " + file.getOriginalFilename());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "auditIds", required = false) String auditIdsString,
            @RequestParam(value = "ncIds", required = false) String ncIdsString,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "clauseReference", required = false) String clauseReference,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "isEvidence", required = false, defaultValue = "false") Boolean isEvidence
    ) {
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }

            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Long userId = Long.valueOf(userService.getUserByUsername(auth.getName()).getUserId());

            // Prepare DocumentRequest DTO
            DocumentRequest request = new DocumentRequest();
            request.file = file;
            request.description = description;
            request.tags = tags;
            request.clauseReference = clauseReference;
            request.department = department;
            request.isEvidence = isEvidence;

            // Parse auditIds safely
            List<Long> auditIds = new ArrayList<>();
            if (auditIdsString != null && !auditIdsString.trim().isEmpty()) {
                for (String id : auditIdsString.split(",")) {
                    auditIds.add(Long.parseLong(id.trim()));
                }
            }
            request.auditIds = auditIds;

            // Parse ncIds safely
            List<Long> ncIds = new ArrayList<>();
            if (ncIdsString != null && !ncIdsString.trim().isEmpty()) {
                for (String id : ncIdsString.split(",")) {
                    ncIds.add(Long.parseLong(id.trim()));
                }
            }
            request.ncIds = ncIds;

            // Upload document via service
            DocumentResponse response = documentService.uploadDocument(request, userId);

            // --- Safe linking of audits ---
            if (!auditIds.isEmpty()) {
                List<Audit> audits = auditRepository.findAllById(auditIds);

                List<Long> auditIdList = new ArrayList<>();
                List<String> auditTitleList = new ArrayList<>();
                for (Audit a : audits) {
                    auditIdList.add(a.getAuditId());
                    auditTitleList.add(a.getTitle());
                }

                response.setAuditIds(auditIdList);
                response.setAuditTitles(auditTitleList);
            }

            // --- Safe linking of NCs ---
            if (!ncIds.isEmpty()) {
                List<NonConformity> ncs = nonConformityRepository.findAllById(ncIds);

                List<Long> ncIdList = new ArrayList<>();
                List<String> ncTitleList = new ArrayList<>();
                for (NonConformity nc : ncs) {
                    ncIdList.add(nc.getNonConformityId());
                    ncTitleList.add(nc.getTitle());
                }

                response.setNcIds(ncIdList);
                response.setNcTitles(ncTitleList);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }




    // Any authenticated user can view all documents
        @PreAuthorize("isAuthenticated()")
        @GetMapping
        public ResponseEntity<List<DocumentResponse>> getAllDocuments() {
            return ResponseEntity.ok(documentService.getAllDocuments());
        }

    // Any authenticated user can view specific document
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocumentById(@PathVariable Long id) {
        try {
            DocumentResponse response = documentService.getDocumentById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Any authenticated user can download document
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        try {
            DocumentResponse document = documentService.getDocumentById(id);
            byte[] data = documentService.getDocumentData(id);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(document.getFileType()));
            headers.setContentDispositionFormData("attachment", document.getFileName());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(data);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Any authenticated user can view documents by audit
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/audit/{auditId}")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByAudit(@PathVariable Long auditId) {
        if (auditId == null || auditId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        List<DocumentResponse> docs = documentService.getDocumentsByAudit(auditId);
        return ResponseEntity.ok(docs);
    }

    // Any authenticated user can view documents by audit with pagination
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/audit/{auditId}/page")
    public ResponseEntity<Page<DocumentResponse>> getDocumentsByAuditWithPagination(
            @PathVariable Long auditId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(documentService.getDocumentsByAuditWithPagination(auditId, page, size));
    }

    // Any authenticated user can view documents by NC
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/nc/{ncId}")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByNc(@PathVariable Long ncId) {
        return ResponseEntity.ok(documentService.getDocumentsByNc(ncId));
    }

    // Any authenticated user can view documents by NC with pagination
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/nc/{ncId}/page")
    public ResponseEntity<Page<DocumentResponse>> getDocumentsByNcWithPagination(
            @PathVariable Long ncId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(documentService.getDocumentsByNcWithPagination(ncId, page, size));
    }

    // Any authenticated user can view evidence documents
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/evidence")
    public ResponseEntity<List<DocumentResponse>> getEvidenceDocuments() {
        return ResponseEntity.ok(documentService.getEvidenceDocuments());
    }

    // Any authenticated user can view documents by clause
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/clause/{clauseReference}")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByClause(@PathVariable String clauseReference) {
        return ResponseEntity.ok(documentService.getDocumentsByClause(clauseReference));
    }

    // Any authenticated user can view documents by department
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/department/{department}")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(documentService.getDocumentsByDepartment(department));
    }

    // Any authenticated user can search documents by file name
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search/filename")
    public ResponseEntity<List<DocumentResponse>> searchDocumentsByFileName(@RequestParam String fileName) {
        return ResponseEntity.ok(documentService.searchDocumentsByFileName(fileName));
    }

    // Any authenticated user can search documents by description
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search/description")
    public ResponseEntity<List<DocumentResponse>> searchDocumentsByDescription(@RequestParam String description) {
        return ResponseEntity.ok(documentService.searchDocumentsByDescription(description));
    }

    // Any authenticated user can search documents by tags
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search/tags")
    public ResponseEntity<List<DocumentResponse>> searchDocumentsByTags(@RequestParam String tag) {
        return ResponseEntity.ok(documentService.searchDocumentsByTags(tag));
    }

    // Any authenticated user can perform advanced search
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/search")
    public ResponseEntity<Page<DocumentResponse>> searchDocuments(@RequestBody DocumentSearchRequest request) {
        return ResponseEntity.ok(documentService.searchDocuments(request));
    }

    // Any authenticated user can view most accessed documents
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/most-accessed")
    public ResponseEntity<Page<DocumentResponse>> getMostAccessedDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(documentService.getMostAccessedDocuments(page, size));
    }

    // Any authenticated user can view recently accessed documents
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/recently-accessed")
    public ResponseEntity<Page<DocumentResponse>> getRecentlyAccessedDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(documentService.getRecentlyAccessedDocuments(page, size));
    }

    // Any authenticated user can view their own documents
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(documentService.getDocumentsByUser(userId));
    }

    // Any authenticated user can link document to additional audits
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/link-audits")
    public ResponseEntity<DocumentResponse> linkDocumentToAudits(
            @PathVariable Long id,
            @RequestBody List<Long> auditIds
    ) {
        try {
            DocumentResponse response = documentService.linkDocumentToAudits(id, auditIds);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Any authenticated user can link document to NCs
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/link-ncs")
    public ResponseEntity<DocumentResponse> linkDocumentToNcs(
            @PathVariable Long id,
            @RequestBody List<Long> ncIds
    ) {
        try {
            DocumentResponse response = documentService.linkDocumentToNcs(id, ncIds);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Any authenticated user can update document metadata
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/metadata")
    public ResponseEntity<DocumentResponse> updateDocumentMetadata(
            @PathVariable Long id,
            @RequestBody DocumentRequest request
    ) {
        try {
            DocumentResponse response = documentService.updateDocumentMetadata(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Only ADMIN can delete documents
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
