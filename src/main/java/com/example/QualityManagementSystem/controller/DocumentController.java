package com.example.QualityManagementSystem.controller;

import com.example.QualityManagementSystem.dto.DocumentRequest;
import com.example.QualityManagementSystem.dto.DocumentResponse;
import com.example.QualityManagementSystem.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService docService;

    public DocumentController(DocumentService docService) {
        this.docService = docService;
    }

    @PreAuthorize("hasAnyRole('AUDITOR', 'AUDITEE', 'ADMIN', 'REVIEWER')")
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestPart("metadata") DocumentRequest request,
            @RequestPart("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long userId
    ) throws IOException {
        return ResponseEntity.ok(docService.uploadDocument(request, file, userId));
    }

    @PreAuthorize("hasAnyRole('AUDITOR', 'AUDITEE', 'ADMIN', 'REVIEWER')")
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAll() {
        return ResponseEntity.ok(docService.getAllDocuments());
    }

    @PreAuthorize("hasAnyRole('AUDITOR', 'AUDITEE', 'ADMIN', 'REVIEWER')")
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(docService.getDocumentById(id));
    }
}
